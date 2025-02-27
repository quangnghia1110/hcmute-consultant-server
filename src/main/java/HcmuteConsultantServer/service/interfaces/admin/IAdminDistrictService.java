package HcmuteConsultantServer.service.interfaces.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import HcmuteConsultantServer.model.payload.dto.manage.ManageDistrictDTO;
import HcmuteConsultantServer.model.payload.request.DistrictRequest;

public interface IAdminDistrictService {

    ManageDistrictDTO createDistrict(String code, String provinceCode, DistrictRequest districtRequest);

    ManageDistrictDTO updateDistrict(String code, String provinceCode, DistrictRequest districtRequest);

    void deleteDistrictByCode(String code);

    ManageDistrictDTO getDistrictByCode(String code);

    Page<ManageDistrictDTO> getDistrictByAdmin(String code, String name, String nameEn, String fullName, String fullNameEn, String codeName, String provinceCode, Pageable pageable);

    boolean existsByCode(String code);

}
