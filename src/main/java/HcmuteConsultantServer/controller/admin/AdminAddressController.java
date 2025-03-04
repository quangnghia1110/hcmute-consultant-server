package HcmuteConsultantServer.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.model.payload.dto.manage.ManageAddressDTO;
import HcmuteConsultantServer.model.payload.request.AddressRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.service.interfaces.admin.IAdminAdressService;
import HcmuteConsultantServer.service.interfaces.common.IExcelService;
import HcmuteConsultantServer.service.interfaces.common.IPdfService;

@RestController
@RequestMapping("${base.url}")
public class AdminAddressController {

    @Autowired
    private IAdminAdressService addressService;

    @Autowired
    private IExcelService excelService;

    @Autowired
    private IPdfService pdfService;

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/admin/address/list")
    public ResponseEntity<DataResponse<Page<ManageAddressDTO>>> getAddresses(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String line,
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String districtCode,
            @RequestParam(required = false) String wardCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "line") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<ManageAddressDTO> addresses = addressService.getAddressByAdmin(id, line, provinceCode, districtCode, wardCode, pageable);

        return ResponseEntity.ok(
                DataResponse.<Page<ManageAddressDTO>>builder()
                        .status("success")
                        .message("Lấy danh sách địa chỉ thành công")
                        .data(addresses)
                        .build()
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @PostMapping("/admin/address/create")
    public ResponseEntity<DataResponse<ManageAddressDTO>> createAddress(@RequestBody AddressRequest addressRequest) {
        if (addressRequest == null || addressRequest.getProvinceCode() == null || addressRequest.getDistrictCode() == null || addressRequest.getWardCode() == null) {
            return ResponseEntity.status(400).body(
                    DataResponse.<ManageAddressDTO>builder()
                            .status("error")
                            .message("Dữ liệu địa chỉ không hợp lệ")
                            .build()
            );
        }

        ManageAddressDTO savedAddress = addressService.createAddress(addressRequest);

        return ResponseEntity.ok(
                DataResponse.<ManageAddressDTO>builder()
                        .status("success")
                        .message("Tạo địa chỉ thành công")
                        .data(savedAddress)
                        .build()
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @PutMapping("/admin/address/update")
    public ResponseEntity<DataResponse<ManageAddressDTO>> updateAddress(@RequestParam Integer id, @RequestBody AddressRequest addressRequest) {
        if (addressRequest == null || id == null) {
            return ResponseEntity.status(400).body(
                    DataResponse.<ManageAddressDTO>builder()
                            .status("error")
                            .message("Dữ liệu địa chỉ hoặc mã ID không hợp lệ")
                            .build()
            );
        }

        ManageAddressDTO updatedAddress = addressService.updateAddress(id, addressRequest);

        return ResponseEntity.ok(
                DataResponse.<ManageAddressDTO>builder()
                        .status("success")
                        .message("Cập nhật địa chỉ thành công")
                        .data(updatedAddress)
                        .build()
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @DeleteMapping("/admin/address/delete")
    public ResponseEntity<DataResponse<Void>> deleteAddress(@RequestParam Integer id) {
            addressService.deleteAddressById(id);
            return ResponseEntity.ok(
                    DataResponse.<Void>builder()
                            .status("success")
                            .message("Xóa địa chỉ thành công")
                            .build()
            );

    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/admin/address/detail")
    public ResponseEntity<DataResponse<ManageAddressDTO>> getAddressById(@RequestParam Integer id) {
            ManageAddressDTO addressDTO = addressService.getAddressById(id);
            return ResponseEntity.ok(
                    DataResponse.<ManageAddressDTO>builder()
                            .status("success")
                            .message("Lấy thông tin địa chỉ thành công")
                            .data(addressDTO)
                            .build()
            );
    }
}
