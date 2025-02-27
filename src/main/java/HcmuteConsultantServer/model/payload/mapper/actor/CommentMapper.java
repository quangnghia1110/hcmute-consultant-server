package HcmuteConsultantServer.model.payload.mapper.actor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import HcmuteConsultantServer.model.entity.CommentEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.model.payload.dto.actor.CommentDTO;
import HcmuteConsultantServer.model.payload.dto.actor.UserCommentDTO;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    // Ánh xạ từ CommentEntity sang CommentDTO
    @Mapping(source = "userComment", target = "user", qualifiedByName = "mapUserToUserDTO")
    @Mapping(source = "idComment", target = "id")
    @Mapping(source = "parentComment.idComment", target = "parentCommentId")
    @Mapping(source = "comment", target = "text")
    @Mapping(source = "createDate", target = "create_date")
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "childComments", target = "childComments", qualifiedByName = "mapChildComments")
    CommentDTO mapToDTO(CommentEntity comment);

    @Named("mapUserToUserDTO")
    default UserCommentDTO mapUserToUserDTO(UserInformationEntity user) {
        if (user == null) return null;

        UserCommentDTO userDTO = new UserCommentDTO();
        userDTO.setId(user.getId());
        userDTO.setLastName(user.getLastName());
        userDTO.setFirstName(" " + user.getFirstName());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        return userDTO;
    }

    @Named("mapChildComments")
    default List<CommentDTO> mapChildComments(List<CommentEntity> childComments) {
        return childComments != null ? childComments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList()) : List.of();
    }

}
