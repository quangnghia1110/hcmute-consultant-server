package HcmuteConsultantServer.repository.actor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import HcmuteConsultantServer.model.entity.CommonQuestionEntity;

import java.util.Optional;

public interface CommonQuestionRepository extends PagingAndSortingRepository<CommonQuestionEntity, Integer>, JpaSpecificationExecutor<CommonQuestionEntity> {
    Page<CommonQuestionEntity> findByDepartmentIdAndTitle(Integer departmentId, String title, Pageable pageable);

    Page<CommonQuestionEntity> findByDepartmentId(Integer departmentId, Pageable pageable);

    @Query("SELECT c FROM CommonQuestionEntity c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<CommonQuestionEntity> findByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT c FROM CommonQuestionEntity c WHERE c.id = :id AND c.department.id = :departmentId")
    Optional<CommonQuestionEntity> findByIdAndDepartmentId(@Param("id") Integer id, @Param("departmentId") Integer departmentId);

    @Query("SELECT q FROM CommonQuestionEntity q WHERE q.id = :questionId AND q.createdBy.account.email = :email")
    Optional<CommonQuestionEntity> findByIdAndUserAccountEmail(@Param("questionId") Integer questionId, @Param("email") String email);

}
