
-- Published catalog listing: WHERE status=? ORDER BY created_at DESC
CREATE INDEX IF NOT EXISTS idx_courses_status_created ON courses(status, created_at DESC);
-- Filtered listing: WHERE status=? AND category_id=?
CREATE INDEX IF NOT EXISTS idx_courses_status_category ON courses(status, category_id);
-- Instructor listing
CREATE INDEX IF NOT EXISTS idx_courses_instructor ON courses(instructor_id);

-- Course details path: sections -> lessons
CREATE INDEX IF NOT EXISTS idx_sections_course ON course_sections(course_id);
CREATE INDEX IF NOT EXISTS idx_lessons_section ON lessons(section_id);

-- Enrollment hot paths: my-enrollments + duplicate check + course roster
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_course ON enrollments(course_id);
