package HcmuteConsultantServer.model.payload.dto.actor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import HcmuteConsultantServer.constant.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Integer id;
    private Integer conversationId;
    private UserInformationDTO sender;
    private List<UserInformationDTO> receiver;
    private String message;
    private String imageUrl;
    private String fileUrl;
    private String typeUrl;
    private LocalDateTime date;
    private MessageStatus messageStatus;

    private Boolean recalledBySender;
    private Boolean recalledForEveryone;
    private Boolean edited;
    private LocalDateTime editedDate;

    public void setReceivers(List<UserInformationDTO> receivers) {
        this.receiver = receivers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInformationDTO {
        private Integer id;
        private String avatarUrl;
        private String name;
    }

}
