package HcmuteConsultantServer.model.payload.request;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String emailRequest;
}
