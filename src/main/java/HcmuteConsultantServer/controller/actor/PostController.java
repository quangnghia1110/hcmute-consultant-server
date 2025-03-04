package HcmuteConsultantServer.controller.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.constant.enums.NotificationContent;
import HcmuteConsultantServer.constant.enums.NotificationType;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.exception.Exceptions.ErrorException;
import HcmuteConsultantServer.model.payload.dto.actor.PostDTO;
import HcmuteConsultantServer.model.payload.request.CreatePostRequest;
import HcmuteConsultantServer.model.payload.request.UpdatePostRequest;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.service.interfaces.actor.IPostService;
import HcmuteConsultantServer.service.interfaces.common.IExcelService;
import HcmuteConsultantServer.service.interfaces.common.INotificationService;
import HcmuteConsultantServer.service.interfaces.common.IPdfService;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${base.url}")
public class PostController {

    @Autowired
    private IPostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private IExcelService excelService;

    @Autowired
    private IPdfService pdfService;

    @PreAuthorize(SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @PostMapping("/post")
    public ResponseEntity<DataResponse<PostDTO>> createPost(@ModelAttribute CreatePostRequest postRequest, Principal principal) {
        String email = principal.getName();
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UserInformationEntity user = userOpt.get();

        PostDTO postDTO = postService.createPost(postRequest, user.getId()).getData();

        List<UserInformationEntity> admins = userRepository.findAllByRole(SecurityConstants.Role.ADMIN);

        for (UserInformationEntity admin : admins) {
            notificationService.sendUserNotification(
                    user.getId(),
                    admin.getId(),
                    NotificationContent.NEW_POST_CREATED.formatMessage(user.getLastName() + " " + user.getFirstName()),
                    NotificationType.ADMIN
            );
        }

        return ResponseEntity.ok(DataResponse.<PostDTO>builder()
                .status("success")
                .message("Tạo bài viết thành công.")
                .data(postDTO)
                .build());
    }

    @GetMapping("/post/list")
    public ResponseEntity<DataResponse<Page<PostDTO>>> getPosts(
            @RequestParam boolean isApproved,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Principal principal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<PostDTO> posts;
        String message = "Lấy danh sách các bài đăng thành công";

        if (principal == null) {
            posts = postService.getAllPostsWithFilters(true, Optional.ofNullable(startDate), Optional.ofNullable(endDate), pageable);
            return ResponseEntity.ok(DataResponse.<Page<PostDTO>>builder().status("success").message(message).data(posts).build());
        }
        String email = principal.getName();
        UserInformationEntity user = userRepository.findUserInfoByEmail(email)
                .orElseThrow(() -> new ErrorException("Không tìm thấy người dùng"));


        String userRole = user.getAccount().getRole().getName();

        if (userRole.equals(SecurityConstants.Role.USER)) {
            posts = postService.getAllPostsWithFilters(true, Optional.ofNullable(startDate), Optional.ofNullable(endDate), pageable);
        } else if (userRole.equals(SecurityConstants.Role.ADMIN)) {
            posts = postService.getAllPostsWithFilters(isApproved, Optional.ofNullable(startDate), Optional.ofNullable(endDate), pageable);
        } else if (userRole.equals(SecurityConstants.Role.TRUONGBANTUVAN) || userRole.equals(SecurityConstants.Role.TUVANVIEN)) {
            posts = postService.getPostByRole(isApproved, Optional.ofNullable(startDate), Optional.ofNullable(endDate), pageable, principal);
        } else {
            throw new ErrorException("Bạn không có quyền truy cập vào danh sách bài viết.");
        }

        return ResponseEntity.ok(DataResponse.<Page<PostDTO>>builder().status("success").message(message).data(posts).build());
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @PutMapping("/post/update")
    public ResponseEntity<DataResponse<PostDTO>> updatePost(@RequestParam Integer id, @ModelAttribute UpdatePostRequest postRequest, Principal principal) {
        String email = principal.getName();
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UserInformationEntity user = userOpt.get();
        DataResponse<PostDTO> response = postService.updatePost(id, postRequest, user.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @DeleteMapping("/post/delete")
    public ResponseEntity<DataResponse<String>> deletePost(@RequestParam Integer id, Principal principal) {
        String email = principal.getName();
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UserInformationEntity user = userOpt.get();
        postService.deletePost(id, user.getId());

        return new ResponseEntity<>(DataResponse.<String>builder().status("success").message("Bài viết đã được xóa thành công").build(), HttpStatus.OK);
    }

    @PreAuthorize(SecurityConstants.PreAuthorize.USER + " or " + SecurityConstants.PreAuthorize.TUVANVIEN + " or " + SecurityConstants.PreAuthorize.TRUONGBANTUVAN + " or " + SecurityConstants.PreAuthorize.ADMIN)
    @GetMapping("/post/detail")
    public ResponseEntity<DataResponse<PostDTO>> getPostDetail(@RequestParam Integer id, Principal principal) {
        String email = principal.getName();
        Optional<UserInformationEntity> userOpt = userRepository.findUserInfoByEmail(email);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Không tìm thấy người dùng");
        }

        UserInformationEntity user = userOpt.get();
        DataResponse<PostDTO> postDetail = postService.getPostById(id, user.getId());
        return ResponseEntity.ok(postDetail);
    }
}
