import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import exec from 'k6/execution';


//   k6 run load-all.js                          
//   k6 run -e PEAK_RPS=3000 load-all.js        
//   k6 run -e SPIKE=1 load-all.js               
//   k6 run -e BASE_URL=http://prod/api/v1 -e PEAK_RPS=10000 load-all.js  

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const SEED_PREFIX = __ENV.SEED_PREFIX || 'seed';
const SEED_COUNT = parseInt(__ENV.SEED_COUNT || '5000', 10);
const FIXED_COURSE_ID = __ENV.COURSE_ID || '';
const PEAK_RPS = parseInt(__ENV.PEAK_RPS || '1500', 10);
const SPIKE = __ENV.SPIKE === '1';

const BROWSE_RPS = Math.round(PEAK_RPS * 0.90);
const LOGIN_RPS = Math.round(PEAK_RPS * 0.08);
const ENROLL_RPS = Math.max(1, Math.round(PEAK_RPS * 0.02));
const M = SPIKE ? 2 : 1;

function rateStages(peak) {
  const stages = [
    { target: Math.round(peak * 0.1), duration: '1m' },
    { target: Math.round(peak * 0.4), duration: '2m' },  // ramp
    { target: peak, duration: '2m' },                    // peak
    { target: Math.round(peak * 0.7), duration: '2m' },  // soak
    { target: 0, duration: '30s' },                      // ramp-down
  ];
  if (SPIKE) stages.splice(3, 0, { target: peak * M, duration: '1m' });
  return stages;
}

export const options = {
  scenarios: {
    browse: {
      executor: 'ramping-arrival-rate',
      exec: 'browse',
      timeUnit: '1s',
      startRate: 10,
      stages: rateStages(BROWSE_RPS * M),
      preAllocatedVUs: Math.min(400, Math.round(BROWSE_RPS * 0.9)),
      maxVUs: BROWSE_RPS * 2,
    },
    loginStorm: {
      executor: 'ramping-arrival-rate',
      exec: 'loginStorm',
      timeUnit: '1s',
      startRate: 2,
      stages: rateStages(LOGIN_RPS * M),
      preAllocatedVUs: Math.min(150, Math.round(LOGIN_RPS * 1.2) + 20),
      maxVUs: LOGIN_RPS * 4 + 50,
    },
    enrollJourney: {
      executor: 'ramping-arrival-rate',
      exec: 'enrollJourney',
      timeUnit: '1s',
      startRate: 1,
      stages: rateStages(ENROLL_RPS * M),
      preAllocatedVUs: Math.min(80, ENROLL_RPS * 2 + 20),
      maxVUs: ENROLL_RPS * 6 + 50,
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1200', 'p(99)<2500'],
    http_req_failed: ['rate<0.05'],
    'http_req_duration{scenario:browse}': ['p(95)<600'],
    'http_req_duration{scenario:loginStorm}': ['p(95)<1500'],
    'http_req_duration{scenario:enrollJourney}': ['p(95)<2000'],
  },
  gracefulStop: '30s',
};

const enrollErrors = new Counter('enroll_errors');
const loginErrors = new Counter('login_errors');
const browseTrend = new Trend('browse_duration');

function extractData(body) {
  try {
    const parsed = JSON.parse(body);
    return parsed.data !== undefined ? parsed.data : parsed;
  } catch (e) {
    return null;
  }
}

function extractToken(body) {
  // Backend envelope: ApiResponse { success, data: { accessToken, ... } } — يقبل كل الأشكال
  try {
    const p = JSON.parse(body);
    const d = p.data !== undefined ? p.data : p;
    if (d && d.accessToken) return d.accessToken;
    if (d && d.token) return d.token;
    if (d && d.data && (d.data.accessToken || d.data.token)) return d.data.accessToken || d.data.token;
    if (p.accessToken) return p.accessToken;
    if (p.token) return p.token;
  } catch (e) { }
  return '';
}

let debugLogged = 0;
function debugOnce(res, label) {
  if (debugLogged < 3 && res.status !== 200 && res.status !== 201) {
    debugLogged++;
    console.log(`[DEBUG:${label}] status=${res.status} body=${String(res.body).slice(0, 300)}`);
  }
}

let cachedCourseId = FIXED_COURSE_ID || null;
function resolveCourseId(data) {
  if (data && data.courseId) {
    cachedCourseId = data.courseId;
    return cachedCourseId;
  }
  if (cachedCourseId) return cachedCourseId;
  const res = http.get(`${BASE_URL}/courses?size=5`, { tags: { endpoint: 'list-courses' } });
  try {
    const d = extractData(res.body);
    if (Array.isArray(d) && d.length > 0) cachedCourseId = d[0].id;
  } catch (e) { }
  return cachedCourseId;
}

export function setup() {
  if (FIXED_COURSE_ID) return { courseId: FIXED_COURSE_ID };
  const res = http.get(`${BASE_URL}/courses?size=5`);
  try {
    const data = extractData(res.body);
    if (Array.isArray(data) && data.length > 0) return { courseId: data[0].id };
  } catch (e) { }
  return { courseId: null };
}

function randomThink(min, max) {
  sleep(Math.random() * (max - min) + min);
}

function doRegister(email, password) {
  // POST /auth/register — الـ DTO: {email, password, firstName, lastName} → 201 + User 
  const res = http.post(
    `${BASE_URL}/auth/register`,
    JSON.stringify({ email, password, firstName: 'Seed', lastName: email.slice(0, 20) }),
    { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'register' } }
  );
  check(res, { 'register 201 or 409': (r) => [201, 409].includes(r.status) });
  if (res.status !== 201 && res.status !== 409) debugOnce(res, 'register');
  return res;
}

function doLogin(email, password) {
  // POST /auth/login — Request {email, password} → 200 + { success, data: { accessToken, ... } }
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'login' } }
  );
  const ok = check(res, { 'login 200': (r) => r.status === 200 });
  if (!ok) {
    loginErrors.add(1);
    debugOnce(res, 'login');
  }
  return { res, token: extractToken(res.body) };
}

function ensureLogin(email, password) {
  let attempt = doLogin(email, password);
  if (attempt.res.status === 200 && attempt.token) return attempt.token;
  if ([400, 401, 404].includes(attempt.res.status)) {
    const reg = doRegister(email, password);
    if ([201, 409].includes(reg.status)) {
      sleep(0.2);
      attempt = doLogin(email, password);
      if (attempt.res.status === 200 && attempt.token) return attempt.token;
      sleep(0.5);
      attempt = doLogin(email, password);
      return attempt.token;
    }
  }
  return attempt.token;
}

export function browse() {
  const responses = http.batch([
    ['GET', `${BASE_URL}/courses?size=20`, null, { tags: { endpoint: 'list-courses' } }],
    ['GET', `${BASE_URL}/categories`, null, { tags: { endpoint: 'list-categories' } }],
  ]);
  const ok = check(responses[0], { 'courses 200': (r) => r.status === 200 })
    && check(responses[1], { 'categories 200': (r) => r.status === 200 });
  browseTrend.add(responses[0].timings.duration);
  if (!ok) enrollErrors.add(0);
  randomThink(0.3, 1.0);
}

export function loginStorm() {
  const idx = (exec.vu.idInTest % SEED_COUNT) + 1;
  const token = ensureLogin(`${SEED_PREFIX}_${idx}@example.com`, 'Password123!');
  check({ token }, { 'token extracted': (o) => o.token !== '' });
  if (token) {
    const me = http.get(`${BASE_URL}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
      tags: { endpoint: 'me' },
    });
    check(me, { 'me 200': (r) => r.status === 200 });
  }
  randomThink(0.3, 1.0);
}

export function enrollJourney(data) {
  const courseId = resolveCourseId(data);
  const uniqueId = `${exec.vu.idInTest}-${exec.vu.iterationInInstance}-${Date.now()}`;

  let token = '';
  let email = '';
  if (Math.random() < 0.9) {
    const idx = (exec.vu.idInTest % SEED_COUNT) + 1;
    email = `${SEED_PREFIX}_${idx}@example.com`;
    token = ensureLogin(email, 'Password123!');
  } else {
    email = `violent_${uniqueId}@example.com`;
    const reg = http.post(
      `${BASE_URL}/auth/register`,
      JSON.stringify({ email, password: 'Password123!', firstName: 'Violent', lastName: uniqueId.slice(0, 20) }),
      { headers: { 'Content-Type': 'application/json' }, tags: { endpoint: 'register' } }
    );
    check(reg, { 'register 201': (r) => r.status === 201 });
    token = ensureLogin(email, 'Password123!');
  }

  if (!token) {
    randomThink(0.3, 1.0);
    return;
  }
  const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` };

  if (courseId) {
    const enroll = http.post(
      `${BASE_URL}/enrollments`,
      JSON.stringify({ courseId }),
      { headers, tags: { endpoint: 'enroll' } }
    );
    const ok = check(enroll, {
      'enroll responded': (r) => [201, 400, 404, 409].includes(r.status),
    });
    if (!ok) enrollErrors.add(1);
    if (enroll.status === 500) enrollErrors.add(1);
  }

  const mine = http.get(`${BASE_URL}/enrollments/me`, { headers, tags: { endpoint: 'my-enrollments' } });
  check(mine, { 'my-enrollments 200': (r) => r.status === 200 });

  randomThink(0.3, 1.0);
}

export default function (data) {
  const r = Math.random();
  if (r < 0.9) browse(data);
  else if (r < 0.98) loginStorm(data);
  else enrollJourney(data);
}
