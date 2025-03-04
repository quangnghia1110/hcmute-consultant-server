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
import HcmuteConsultantServer.model.payload.dto.manage.ManageWardDTO;
import HcmuteConsultantServer.model.payload.request.WardRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.service.interfaces.admin.IAdminWardService;
import HcmuteConsultantServer.service.interfaces.common.IExcelService;
import HcmuteConsultantServer.service.interfaces.common.IPdfService;

@RestController
@RequestMapping("${base.url}")
public class AdminWardController {

    @Autowired
    private IAdminWardService wardService;

    @Autowired
    private IExcelService excelService;

    @Autowired
    private IPdfService pdfService;

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/admin/ward/list")
    public ResponseEntity<DataResponse<Page<ManageWardDTO>>> getWards(
            @RequestParam(required = false) String districtCode,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nameEn,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String fullNameEn,
            @RequestParam(required = false) String codeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "code") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<ManageWardDTO> wards = wardService.getWardByAdmin(code, name, nameEn, fullName, fullNameEn, codeName, districtCode, pageable);

        return ResponseEntity.ok(
                DataResponse.<Page<ManageWardDTO>>builder()
                        .status("success")
                        .message("Lấy danh sách Phường/Xã thành công")
                        .data(wards)
                        .build()
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @PostMapping("/admin/ward/create")
    public ResponseEntity<DataResponse<ManageWardDTO>> createWard(
            @RequestParam String districtCode,
            @RequestParam String code,
            @RequestBody WardRequest wardRequest) {
        if (wardRequest == null || districtCode == null || code == null) {
            return ResponseEntity.status(400).body(
                    DataResponse.<ManageWardDTO>builder()
                            .status("error")
                            .message("Dữ liệu Phường/Xã hoặc mã quận/huyện không hợp lệ")
                            .build()
            );
        }

        if (wardService.existsByCode(code)) {
            return ResponseEntity.status(400).body(
                    DataResponse.<ManageWardDTO>builder()
                            .status("error")
                            .message("Mã Phường/Xã đã tồn tại")
                            .build()
            );
        }

        ManageWardDTO savedWard = wardService.createWard(code, districtCode, wardRequest);

        return ResponseEntity.ok(
                DataResponse.<ManageWardDTO>builder()
                        .status("success")
                        .message("Tạo Phường/Xã thành công")
                        .data(savedWard)
                        .build()
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @PutMapping("/admin/ward/update")
    public ResponseEntity<DataResponse<ManageWardDTO>> updateWard(
            @RequestParam String code,
            @RequestParam String districtCode,
            @RequestBody WardRequest wardRequest) {
        if (wardRequest == null || districtCode == null || code == null) {
            return ResponseEntity.status(400).body(
                    DataResponse.<ManageWardDTO>builder()
                            .status("error")
                            .message("Dữ liệu Phường/Xã hoặc mã quận/huyện không hợp lệ")
                            .build()
            );
        }

        ManageWardDTO updatedWard = wardService.updateWard(code, districtCode, wardRequest);

        return ResponseEntity.ok(
                DataResponse.<ManageWardDTO>builder()
                        .status("success")
                        .message("Cập nhật Phường/Xã thành công")
                        .data(updatedWard)
                        .build()
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @DeleteMapping("/admin/ward/delete")
    public ResponseEntity<DataResponse<Void>> deleteWard(@RequestParam String code) {
            wardService.deleteWardByCode(code);
            return ResponseEntity.ok(
                    DataResponse.<Void>builder()
                            .status("success")
                            .message("Xóa Phường/Xã thành công")
                            .build()
            );

    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/admin/ward/detail")
    public ResponseEntity<DataResponse<ManageWardDTO>> getWardByCode(@RequestParam String code) {
            ManageWardDTO manageWardDTO = wardService.getWardByCode(code);
            return ResponseEntity.ok(
                    DataResponse.<ManageWardDTO>builder()
                            .status("success")
                            .data(manageWardDTO)
                            .build()
            );

    }
}
