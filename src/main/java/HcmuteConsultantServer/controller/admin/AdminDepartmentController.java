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
import HcmuteConsultantServer.model.payload.dto.manage.ManageAccountDTO;
import HcmuteConsultantServer.model.payload.dto.manage.ManageDepartmentDTO;
import HcmuteConsultantServer.model.payload.request.DepartmentRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.service.interfaces.admin.IAdminDepartmentService;
import HcmuteConsultantServer.service.interfaces.common.IExcelService;
import HcmuteConsultantServer.service.interfaces.common.IPdfService;

@RestController
@RequestMapping("${base.url}")
public class AdminDepartmentController {

    @Autowired
    private IAdminDepartmentService departmentService;

    @Autowired
    private IExcelService excelService;

    @Autowired
    private IPdfService pdfService;

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/admin/department/list")
    public ResponseEntity<DataResponse<Page<ManageDepartmentDTO>>> getDepartments(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<ManageDepartmentDTO> departments = departmentService.getDepartmentByAdmin(name, pageable);

        return ResponseEntity.ok(
                new DataResponse<>("success", "Lấy danh sách phòng ban thành công", departments)
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @PostMapping("/admin/department/create")
    public ResponseEntity<DataResponse<ManageDepartmentDTO>> createDepartment(@RequestBody DepartmentRequest departmentRequest) {
        if (departmentRequest == null || departmentRequest.getName().trim().isEmpty()) {
            return ResponseEntity.status(400).body(
                    new DataResponse<>("error", "Dữ liệu phòng ban không hợp lệ")
            );
        }

        ManageDepartmentDTO savedDepartment = departmentService.createDepartment(departmentRequest);

        return ResponseEntity.ok(
                new DataResponse<>("success", "Tạo phòng ban thành công", savedDepartment)
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @PutMapping("/admin/department/update")
    public ResponseEntity<DataResponse<ManageDepartmentDTO>> updateDepartment(@RequestParam Integer id, @RequestBody DepartmentRequest departmentRequest) {
        if (departmentRequest == null || departmentRequest.getName().trim().isEmpty()) {
            return ResponseEntity.status(400).body(
                    new DataResponse<>("error", "Dữ liệu phòng ban không hợp lệ")
            );
        }

        ManageDepartmentDTO updatedDepartment = departmentService.updateDepartment(id, departmentRequest);

        return ResponseEntity.ok(
                new DataResponse<>("success", "Cập nhật phòng ban thành công", updatedDepartment)
        );
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @DeleteMapping("/admin/department/delete")
    public ResponseEntity<DataResponse<Void>> deleteDepartment(@RequestParam Integer id) {
            departmentService.deleteDepartmentById(id);
            return ResponseEntity.ok(
                    new DataResponse<>("success", "Xóa phòng ban thành công")
            );

    }

    @PreAuthorize(SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/admin/department/detail")
    public ResponseEntity<DataResponse<ManageDepartmentDTO>> getDepartmentById(@RequestParam Integer id) {

            ManageDepartmentDTO manageDepartmentDTO = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(
                DataResponse.<ManageDepartmentDTO>builder()
                        .status("success")
                        .data(manageDepartmentDTO)
                        .build()
        );
    }
}

