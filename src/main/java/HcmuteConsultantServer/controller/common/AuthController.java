package HcmuteConsultantServer.controller.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import HcmuteConsultantServer.model.payload.dto.actor.UserInformationDTO;
import HcmuteConsultantServer.model.payload.request.*;
import HcmuteConsultantServer.model.payload.response.DataResponse;
import HcmuteConsultantServer.repository.admin.AccountRepository;
import HcmuteConsultantServer.service.implement.common.StatusOnlineServiceImpl;
import HcmuteConsultantServer.service.implement.common.UserServiceImpl;

import javax.validation.Valid;

@RestController
@RequestMapping("${base.url}")
public class AuthController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private StatusOnlineServiceImpl commonStatusOnlineServiceImpl;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping(value = "/auth/refresh")
    public ResponseEntity<DataResponse<DataResponse.LoginData>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }

    @PostMapping(value = "/auth/register")
    public ResponseEntity<DataResponse<UserInformationDTO>> registerUser(@Valid @RequestBody RegisterRequest userRegisterRequest) {
        return ResponseEntity.ok(userService.register(userRegisterRequest));
    }

    @PostMapping(value = "/auth/confirm-registration")
    public ResponseEntity<DataResponse<Object>> confirmRegistration(@Valid @RequestBody ConfirmRegistrationRequest confirmRegistrationRequest) {
        return ResponseEntity.ok(userService.confirmRegistration(confirmRegistrationRequest));
    }

    @PostMapping(value = "/auth/login")
    public ResponseEntity<DataResponse<DataResponse.LoginData>> login(@Valid @RequestBody LoginRequest loginRequest) {
        DataResponse<DataResponse.LoginData> loginResponse = userService.login(loginRequest);

        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping(value = "/auth/forgot-password")
    public ResponseEntity<DataResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(userService.forgotPassword(forgotPasswordRequest));
    }

    @PostMapping(value = "/auth/verify-code")
    public ResponseEntity<DataResponse<Object>> checkVerifyCode(@Valid @RequestBody VerifyCodeCheckRequest verifyCodeCheckRequest) {
        return ResponseEntity.ok(userService.checkVerifyCode(verifyCodeCheckRequest));
    }

    @PostMapping(value = "/auth/reset-password")
    public ResponseEntity<DataResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return ResponseEntity.ok(userService.resetPassword(resetPasswordRequest));
    }

    @PostMapping(value = "/auth/resend-register-verification-code")
    public ResponseEntity<DataResponse<Object>> resendRegisterVerificationCode(@Valid @RequestBody ResendVerificationRequest resendVerificationRequest) {
        return ResponseEntity.ok(userService.resendVerificationCodeForRegister(resendVerificationRequest));
    }

    @PostMapping(value = "/auth/resend-forgot-password-verification-code")
    public ResponseEntity<DataResponse<Object>> resendForgotPasswordVerificationCode(@Valid @RequestBody ResendVerificationRequest resendVerificationRequest) {
        return ResponseEntity.ok(userService.resendVerificationCodeForForgotPassword(resendVerificationRequest));
    }


    @PostMapping(value = "/auth/change-email")
    public ResponseEntity<DataResponse<Object>> changeEmail(@Valid @RequestBody ChangeEmailRequest changeEmailRequest) {
        return ResponseEntity.ok(userService.changeEmail(changeEmailRequest));
    }
}
