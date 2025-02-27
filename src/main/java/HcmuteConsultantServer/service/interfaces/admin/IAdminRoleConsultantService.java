package HcmuteConsultantServer.service.interfaces.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import HcmuteConsultantServer.model.payload.dto.manage.ManageRoleConsultantDTO;
import HcmuteConsultantServer.model.payload.request.RoleConsultantRequest;

import java.util.Optional;

public interface IAdminRoleConsultantService {

    public ManageRoleConsultantDTO createRoleConsultant(RoleConsultantRequest roleConsultantRequest);

    ManageRoleConsultantDTO updateRoleConsultant(Integer id, RoleConsultantRequest roleConsultantRequest);

    void deleteRoleConsultantById(Integer id);

    ManageRoleConsultantDTO getRoleConsultantById(Integer id);

    Page<ManageRoleConsultantDTO> getRoleConsultantByAdmin(String name, Optional<Integer> roleId, Pageable pageable);

    boolean existsById(Integer id);
}

