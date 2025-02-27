package HcmuteConsultantServer.repository.actor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import HcmuteConsultantServer.model.entity.ConsultationScheduleEntity;
import HcmuteConsultantServer.model.entity.ConsultationScheduleRegistrationEntity;
import HcmuteConsultantServer.model.entity.DepartmentEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationScheduleRepository extends PagingAndSortingRepository<ConsultationScheduleEntity, Integer>, JpaSpecificationExecutor<ConsultationScheduleEntity> {

    List<ConsultationScheduleEntity> findByUserId(Integer userId);

    Page<ConsultationScheduleEntity> findByUserAndDepartmentIdAndTitleContaining(UserInformationEntity user,
                                                                                 Integer departmentId, String title, Pageable pageable);

    Page<ConsultationScheduleEntity> findByUserAndDepartmentId(UserInformationEntity user, Integer departmentId,
                                                               Pageable pageable);

    Page<ConsultationScheduleEntity> findByUserAndTitleContaining(UserInformationEntity user, String title,
                                                                  Pageable pageable);

    Page<ConsultationScheduleEntity> findByUser(UserInformationEntity user, Pageable pageable);

    @Query("SELECT c FROM ConsultationScheduleEntity c WHERE c.id = :scheduleId")
    Optional<ConsultationScheduleEntity> findConsulationScheduleById(@Param("scheduleId") Integer scheduleId);

    @Query("SELECT c FROM ConsultationScheduleRegistrationEntity c WHERE c.consultationSchedule.id = :scheduleId")
    Optional<ConsultationScheduleRegistrationEntity> findConsultationScheduleByScheduleId(@Param("scheduleId") Integer scheduleId);

    boolean existsByIdAndCreatedBy(Integer id, Integer createdBy);

    @Query("SELECT c FROM ConsultationScheduleEntity c WHERE (c.id = :scheduleId AND c.consultant.account.department.id = :departmentId)")
    Optional<ConsultationScheduleEntity> findByIdAndDepartmentId(@Param("scheduleId") Integer scheduleId, @Param("departmentId") Integer departmentId);

    @Query("SELECT c FROM ConsultationScheduleEntity c WHERE (c.id = :scheduleId AND c.createdBy = :createdById)")
    Optional<ConsultationScheduleEntity> findByIdAndCreatedBy(@Param("scheduleId") Integer scheduleId, @Param("createdById") Integer createdById);

    @Query("SELECT s FROM ConsultationScheduleEntity s WHERE s.id = :id AND (s.department.id = :departmentId OR s.createdBy.id = :userId)")
    Optional<ConsultationScheduleEntity> findByIdAndDepartmentOrCreatedBy(@Param("id") Integer id, @Param("departmentId") Integer departmentId, @Param("userId") Integer userId);

    @Query("SELECT c FROM ConsultationScheduleEntity c WHERE c.id = :scheduleId " +
            "AND ((c.type = false AND c.statusConfirmed = true) " +
            "     OR (c.createdBy = :createdById))")
    Optional<ConsultationScheduleEntity> findByScheduleIdAndConditions(@Param("scheduleId") Integer scheduleId,
                                                                       @Param("createdById") Integer createdById);

    @Query("SELECT c FROM ConsultationScheduleEntity c WHERE c.id = :scheduleId " +
            "AND ((c.consultant.account.department.id = :departmentId) " +
            "     OR (c.type = false AND c.statusConfirmed = true))")
    Optional<ConsultationScheduleEntity> findByIdAndDepartmentIds(@Param("scheduleId") Integer scheduleId, @Param("departmentId") Integer departmentId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ConsultationScheduleRegistrationEntity r " +
            "WHERE r.user = :user AND r.consultationSchedule = :schedule")
    boolean existsByUserAndConsultationSchedule(@Param("user") UserInformationEntity user,
                                                @Param("schedule") ConsultationScheduleEntity schedule);

    Optional<ConsultationScheduleEntity> findByDepartmentAndStatusConfirmedFalseAndCreatedBy(
            DepartmentEntity department, Integer userId);

    Optional<ConsultationScheduleEntity> findByDepartmentAndStatusConfirmedTrueAndConsultationDateAfterAndCreatedBy(
            DepartmentEntity department, LocalDate currentDate, Integer userId);


    @Query("SELECT s FROM ConsultationScheduleEntity s WHERE s.consultationDate > :date AND s.statusConfirmed = true")
    List<ConsultationScheduleEntity> findByConsultationDateAfterAndStatusConfirmedTrue(@Param("date") LocalDate date);
}
