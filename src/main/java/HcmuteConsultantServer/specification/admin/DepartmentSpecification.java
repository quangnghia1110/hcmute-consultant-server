package HcmuteConsultantServer.specification.admin;


import org.springframework.data.jpa.domain.Specification;
import HcmuteConsultantServer.model.entity.DepartmentEntity;

public class DepartmentSpecification {

    public static Specification<DepartmentEntity> hasExactYear(Integer year) {
        return (root, query, criteriaBuilder) -> {
            if (year == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.function("YEAR", Integer.class, root.get("createdAt")), year);
        };
    }

    public static Specification<DepartmentEntity> hasDepartment(Integer departmentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("department").get("id"), departmentId);
    }
}

