package HcmuteConsultantServer.model.payload.dto.actor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultantDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String avatarUrl;
    private DepartmentDTO department;
}
