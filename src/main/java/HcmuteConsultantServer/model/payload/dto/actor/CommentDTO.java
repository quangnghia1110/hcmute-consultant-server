package HcmuteConsultantServer.model.payload.dto.actor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Integer id;
    private Integer parentCommentId;
    private String text;
    private UserCommentDTO user;
    private LocalDate create_date;
    private Integer postId;
    private List<CommentDTO> childComments;
}
