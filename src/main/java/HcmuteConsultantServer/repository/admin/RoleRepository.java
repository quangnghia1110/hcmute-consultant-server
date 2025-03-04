package HcmuteConsultantServer.repository.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import HcmuteConsultantServer.model.entity.RoleEntity;

import java.util.Optional;

@Repository
public interface RoleRepository extends PagingAndSortingRepository<RoleEntity, Integer>, JpaSpecificationExecutor<RoleEntity>, JpaRepository<RoleEntity, Integer> {

    RoleEntity findByName(String name);

    @Query("SELECT r FROM RoleEntity r WHERE r.name = :name")
    Optional<RoleEntity> findByNames(String name);

    @Query("SELECT r FROM RoleEntity r WHERE r.name = :name")
    Optional<RoleEntity> findByRoleName(@Param("name") String name);

    Page<RoleEntity> findAllByNameContaining(String name, Pageable pageable);

}
