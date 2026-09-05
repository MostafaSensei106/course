package com.mostafasensei.course.modules.course.handler.usecase

import com.mostafasensei.course.core.error.Failures
import com.mostafasensei.course.core.utils.result.Executor
import com.mostafasensei.course.core.utils.result.Result
import com.mostafasensei.course.core.utils.result.fold
import com.mostafasensei.course.core.utils.usecase.UseCaseBase
import com.mostafasensei.course.modules.course.data.repository.*
import com.mostafasensei.course.modules.course.domain.entity.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// ================= Category =================
data class CreateCategoryParams(val name: String, val description: String?)
data class UpdateCategoryParams(val id: UUID, val name: String?, val description: String?)

@Service
class CreateCategoryUseCase(private val repo: CategoryRepository) : UseCaseBase<Category, CreateCategoryParams> {
    override suspend fun invoke(params: CreateCategoryParams): Result<Category, Failures> {
        val base = slugify(params.name)
        var slug = base
        var i = 1
        while (repo.existsBySlug(slug).fold(onSuccess = { it }, onFailure = { false })) slug = "$base-$i".also { i++ }
        val now = Instant.now()
        return repo.save(Category(UUID.randomUUID(), params.name, slug, params.description, now, now))
    }
}

@Service
class UpdateCategoryUseCase(private val repo: CategoryRepository) : UseCaseBase<Category, UpdateCategoryParams> {
    override suspend fun invoke(params: UpdateCategoryParams): Result<Category, Failures> =
        repo.findById(params.id).fold(
            onSuccess = { c ->
                val newName = params.name ?: c.name
                val newSlug = if (params.name != null && params.name != c.name) slugify(params.name) else c.slug
                repo.save(c.copy(name = newName, slug = newSlug, description = params.description ?: c.description, updatedAt = Instant.now()))
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class GetCategoryUseCase(private val repo: CategoryRepository) : UseCaseBase<Category, UUID> {
    override suspend fun invoke(params: UUID): Result<Category, Failures> = repo.findById(params)
}

@Service
class GetCategoryBySlugUseCase(private val repo: CategoryRepository) : UseCaseBase<Category, String> {
    override suspend fun invoke(params: String): Result<Category, Failures> = repo.findBySlug(params)
}

@Service
class ListCategoriesUseCase(private val repo: CategoryRepository) : UseCaseBase<List<Category>, Unit> {
    override suspend fun invoke(params: Unit): Result<List<Category>, Failures> = repo.findAll()
}

@Service
class DeleteCategoryUseCase(private val repo: CategoryRepository) : UseCaseBase<Unit, UUID> {
    override suspend fun invoke(params: UUID): Result<Unit, Failures> =
        repo.findById(params).fold(
            onSuccess = { repo.deleteById(params) },
            onFailure = { Result.failure(it) }
        )
}

// ================= Course =================
data class CreateCourseParams(
    val title: String, val description: String?, val price: BigDecimal,
    val thumbnailUrl: String?, val categoryId: UUID?, val instructorId: UUID
)
data class UpdateCourseParams(
    val id: UUID, val title: String?, val description: String?, val price: BigDecimal?,
    val thumbnailUrl: String?, val categoryId: UUID?, val status: CourseStatus?
)

@Service
class CreateCourseUseCase(private val repo: CourseRepository) : UseCaseBase<Course, CreateCourseParams> {
    override suspend fun invoke(params: CreateCourseParams): Result<Course, Failures> {
        val base = slugify(params.title)
        var slug = base
        var i = 1
        while (repo.existsBySlug(slug).fold(onSuccess = { it }, onFailure = { false })) slug = "$base-$i".also { i++ }
        val now = Instant.now()
        return repo.save(
            Course(UUID.randomUUID(), params.title, slug, params.description, params.price,
                params.thumbnailUrl, params.categoryId, params.instructorId, CourseStatus.DRAFT, now, now)
        )
    }
}

@Service
class UpdateCourseUseCase(private val repo: CourseRepository) : UseCaseBase<Course, UpdateCourseParams> {
    override suspend fun invoke(params: UpdateCourseParams): Result<Course, Failures> =
        repo.findById(params.id).fold(
            onSuccess = { c ->
                val title = params.title ?: c.title
                val slug = if (params.title != null && params.title != c.title) slugify(params.title) + "-" + c.id.toString().take(8) else c.slug
                repo.save(c.copy(title = title, slug = slug, description = params.description ?: c.description,
                    price = params.price ?: c.price, thumbnailUrl = params.thumbnailUrl ?: c.thumbnailUrl,
                    categoryId = params.categoryId ?: c.categoryId, status = params.status ?: c.status, updatedAt = Instant.now()))
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class PublishCourseUseCase(private val repo: CourseRepository) : UseCaseBase<Course, UUID> {
    override suspend fun invoke(params: UUID): Result<Course, Failures> =
        repo.findById(params).fold(
            onSuccess = { repo.save(it.copy(status = CourseStatus.PUBLISHED, updatedAt = Instant.now())) },
            onFailure = { Result.failure(it) }
        )
}

@Service
class GetCourseUseCase(private val repo: CourseRepository) : UseCaseBase<Course, UUID> {
    override suspend fun invoke(params: UUID): Result<Course, Failures> = repo.findById(params)
}

@Service
class GetCourseBySlugUseCase(private val repo: CourseRepository) : UseCaseBase<Course, String> {
    override suspend fun invoke(params: String): Result<Course, Failures> = repo.findBySlug(params)
}

data class ListCoursesParams(val page: Int = 0, val size: Int = 20, val categoryId: UUID? = null, val search: String? = null)

@Service
class ListPublishedCoursesUseCase(private val repo: CourseRepository) : UseCaseBase<List<Course>, ListCoursesParams> {
    override suspend fun invoke(params: ListCoursesParams): Result<List<Course>, Failures> =
        repo.findPublished(params.page, params.size, params.categoryId, params.search)
}

data class ListAdminCoursesParams(val page: Int = 0, val size: Int = 20, val status: CourseStatus? = null)

@Service
class ListAdminCoursesUseCase(private val repo: CourseRepository) : UseCaseBase<List<Course>, ListAdminCoursesParams> {
    override suspend fun invoke(params: ListAdminCoursesParams): Result<List<Course>, Failures> =
        repo.findAllAdmin(params.page, params.size, params.status)
}

@Service
class DeleteCourseUseCase(
    private val repo: CourseRepository,
    private val sections: SectionRepository,
    private val lessons: LessonRepository
) : UseCaseBase<Unit, UUID> {
    override suspend fun invoke(params: UUID): Result<Unit, Failures> =
        repo.findById(params).fold(
            onSuccess = {
                // cascade delete sections + lessons manually (no FK cascade)
                val secs = sections.findByCourse(params).fold(onSuccess = { it }, onFailure = { emptyList() })
                secs.forEach { s ->
                    lessons.findBySection(s.id).fold(onSuccess = { ls -> ls.forEach { l -> lessons.deleteById(l.id) } }, onFailure = {})
                    sections.deleteById(s.id)
                }
                repo.deleteById(params)
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class GetCourseDetailsUseCase(
    private val courses: CourseRepository,
    private val sections: SectionRepository,
    private val lessons: LessonRepository
) : UseCaseBase<CourseDetails, UUID> {
    override suspend fun invoke(params: UUID): Result<CourseDetails, Failures> =
        courses.findById(params).fold(
            onSuccess = { course ->
                sections.findByCourse(params).fold(
                    onSuccess = { secs ->
                        val ids = secs.map { it.id }
                        val allLessons = lessons.findBySections(ids).fold(onSuccess = { it }, onFailure = { emptyList() })
                        val grouped = secs.map { s -> SectionWithLessons(s, allLessons.filter { it.sectionId == s.id }.sortedBy { l -> l.orderIndex }) }
                        Result.success(CourseDetails(course, grouped))
                    },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { Result.failure(it) }
        )
}

// ================= Section =================
data class CreateSectionParams(val courseId: UUID, val title: String, val orderIndex: Int)
data class UpdateSectionParams(val id: UUID, val title: String?, val orderIndex: Int?)

@Service
class CreateSectionUseCase(private val courses: CourseRepository, private val repo: SectionRepository) : UseCaseBase<CourseSection, CreateSectionParams> {
    override suspend fun invoke(params: CreateSectionParams): Result<CourseSection, Failures> =
        courses.findById(params.courseId).fold(
            onSuccess = {
                val now = Instant.now()
                repo.save(CourseSection(UUID.randomUUID(), params.courseId, params.title, params.orderIndex, now, now))
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class UpdateSectionUseCase(private val repo: SectionRepository) : UseCaseBase<CourseSection, UpdateSectionParams> {
    override suspend fun invoke(params: UpdateSectionParams): Result<CourseSection, Failures> =
        repo.findById(params.id).fold(
            onSuccess = { repo.save(it.copy(title = params.title ?: it.title, orderIndex = params.orderIndex ?: it.orderIndex, updatedAt = Instant.now())) },
            onFailure = { Result.failure(it) }
        )
}

@Service
class DeleteSectionUseCase(private val repo: SectionRepository, private val lessons: LessonRepository) : UseCaseBase<Unit, UUID> {
    override suspend fun invoke(params: UUID): Result<Unit, Failures> =
        repo.findById(params).fold(
            onSuccess = {
                lessons.findBySection(params).fold(onSuccess = { ls -> ls.forEach { lessons.deleteById(it.id) } }, onFailure = {})
                repo.deleteById(params)
            },
            onFailure = { Result.failure(it) }
        )
}

// ================= Lesson =================
data class CreateLessonParams(
    val sectionId: UUID, val title: String, val contentType: LessonContentType,
    val contentUrl: String?, val textContent: String?, val durationSeconds: Int?,
    val orderIndex: Int, val isPreview: Boolean
)
data class UpdateLessonParams(
    val id: UUID, val title: String?, val contentType: LessonContentType?,
    val contentUrl: String?, val textContent: String?, val durationSeconds: Int?,
    val orderIndex: Int?, val isPreview: Boolean?
)

@Service
class CreateLessonUseCase(private val sections: SectionRepository, private val repo: LessonRepository) : UseCaseBase<Lesson, CreateLessonParams> {
    override suspend fun invoke(params: CreateLessonParams): Result<Lesson, Failures> =
        sections.findById(params.sectionId).fold(
            onSuccess = {
                val now = Instant.now()
                repo.save(Lesson(UUID.randomUUID(), params.sectionId, params.title, params.contentType,
                    params.contentUrl, params.textContent, params.durationSeconds, params.orderIndex, params.isPreview, now, now))
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class UpdateLessonUseCase(private val repo: LessonRepository) : UseCaseBase<Lesson, UpdateLessonParams> {
    override suspend fun invoke(params: UpdateLessonParams): Result<Lesson, Failures> =
        repo.findById(params.id).fold(
            onSuccess = {
                repo.save(it.copy(title = params.title ?: it.title, contentType = params.contentType ?: it.contentType,
                    contentUrl = params.contentUrl ?: it.contentUrl, textContent = params.textContent ?: it.textContent,
                    durationSeconds = params.durationSeconds ?: it.durationSeconds, orderIndex = params.orderIndex ?: it.orderIndex,
                    isPreview = params.isPreview ?: it.isPreview, updatedAt = Instant.now()))
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class DeleteLessonUseCase(private val repo: LessonRepository) : UseCaseBase<Unit, UUID> {
    override suspend fun invoke(params: UUID): Result<Unit, Failures> =
        repo.findById(params).fold(onSuccess = { repo.deleteById(params) }, onFailure = { Result.failure(it) })
}

// ================= Enrollment =================
@Service
class EnrollUseCase(
    private val courses: CourseRepository,
    private val repo: EnrollmentRepository
) : UseCaseBase<Enrollment, Pair<UUID, UUID>> {
    // params = studentId to courseId
    override suspend fun invoke(params: Pair<UUID, UUID>): Result<Enrollment, Failures> {
        val (studentId, courseId) = params
        return courses.findById(courseId).fold(
            onSuccess = { course ->
                if (course.status != CourseStatus.PUBLISHED)
                    return Result.failure(Failures.LocalStorageFailure.InvalidDataFailure("Course is not published", "COURSE_NOT_PUBLISHED"))
                val existing = repo.findByStudentAndCourse(studentId, courseId).fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                if (existing != null && existing.status == EnrollmentStatus.ACTIVE)
                    return Result.failure(Failures.LocalStorageFailure.DuplicateEntryFailure("Already enrolled", "ALREADY_ENROLLED"))
                val now = Instant.now()
                repo.save(Enrollment(UUID.randomUUID(), studentId, courseId, EnrollmentStatus.ACTIVE, 0, now, now))
            },
            onFailure = { Result.failure(it) }
        )
    }
}

@Service
class MyEnrollmentsUseCase(private val repo: EnrollmentRepository) : UseCaseBase<List<Enrollment>, UUID> {
    override suspend fun invoke(params: UUID): Result<List<Enrollment>, Failures> = repo.findByStudent(params)
}

@Service
class UpdateProgressUseCase(private val repo: EnrollmentRepository) : UseCaseBase<Enrollment, Pair<UUID, Int>> {
    override suspend fun invoke(params: Pair<UUID, Int>): Result<Enrollment, Failures> =
        repo.findById(params.first).fold(
            onSuccess = { e ->
                val status = if (params.second >= 100) EnrollmentStatus.COMPLETED else e.status
                repo.save(e.copy(progressPercent = params.second.coerceIn(0, 100), status = status, updatedAt = Instant.now()))
            },
            onFailure = { Result.failure(it) }
        )
}

@Service
class CancelEnrollmentUseCase(private val repo: EnrollmentRepository) : UseCaseBase<Enrollment, UUID> {
    override suspend fun invoke(params: UUID): Result<Enrollment, Failures> =
        repo.findById(params).fold(
            onSuccess = { repo.save(it.copy(status = EnrollmentStatus.CANCELLED, updatedAt = Instant.now())) },
            onFailure = { Result.failure(it) }
        )
}
