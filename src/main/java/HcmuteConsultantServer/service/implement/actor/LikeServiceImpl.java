package HcmuteConsultantServer.service.implement.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import HcmuteConsultantServer.constant.enums.LikeType;
import HcmuteConsultantServer.model.entity.LikeKeyEntity;
import HcmuteConsultantServer.model.entity.LikeRecordEntity;
import HcmuteConsultantServer.model.entity.UserInformationEntity;
import HcmuteConsultantServer.repository.actor.LikeRecordRepository;
import HcmuteConsultantServer.service.interfaces.actor.ILikeService;
import HcmuteConsultantServer.service.interfaces.common.IUserService;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class LikeServiceImpl implements ILikeService {

    private final LikeRecordRepository likeRecordRepository;
    private final IUserService userService;

    @Autowired
    public LikeServiceImpl(LikeRecordRepository likeRecordRepository, @Lazy IUserService userService) {
        this.likeRecordRepository = likeRecordRepository;
        this.userService = userService;
    }

    @Override
    public List<LikeRecordEntity> getLikeRecordByPostId(Integer postId) {
        return likeRecordRepository.getLikeRecordsByPostId(postId);
    }

    @Override
    public List<LikeRecordEntity> getLikeRecordByCommentId(Integer commentId) {
        return likeRecordRepository.getLikeRecordsByCommentId(commentId);
    }

    @Override
    public void likePost(Integer postId, Integer userId) {
        LikeRecordEntity likeRecord = new LikeRecordEntity(new LikeKeyEntity(postId, userId, LikeType.POST.toString()));
        likeRecordRepository.save(likeRecord);
    }

    @Override
    public void unlikePost(Integer postId, Integer userId) {
        LikeRecordEntity likeRecord = new LikeRecordEntity(new LikeKeyEntity(postId, userId, LikeType.POST.toString()));
        likeRecordRepository.delete(likeRecord);
    }

    @Override
    public void likeComment(Integer commentId, Integer userId) {
        LikeRecordEntity likeRecord = new LikeRecordEntity(new LikeKeyEntity(commentId, userId, LikeType.COMMENT.toString()));
        likeRecordRepository.save(likeRecord);
    }

    @Override
    public void unlikeComment(Integer commentId, Integer userId) {
        LikeRecordEntity likeRecord = new LikeRecordEntity(new LikeKeyEntity(commentId, userId, LikeType.COMMENT.toString()));
        likeRecordRepository.delete(likeRecord);
    }

    @Override
    public void likeQuestion(Integer questionId, Integer userId) {
        LikeRecordEntity likeRecord = new LikeRecordEntity(new LikeKeyEntity(questionId, userId, LikeType.QUESTION.toString()));
        likeRecordRepository.save(likeRecord);
    }

    @Override
    public void unlikeQuestion(Integer questionId, Integer userId) {
        LikeRecordEntity likeRecord = new LikeRecordEntity(new LikeKeyEntity(questionId, userId, LikeType.QUESTION.toString()));
        likeRecordRepository.delete(likeRecord);
    }

    @Override
    public Integer getUserIdByEmail(String email) {
        return userService.getUserIdByEmail(email);
    }

    @Override
    public Integer countLikesByPostId(Integer postId) {
        return likeRecordRepository.countByLikeKeyTargetIdAndLikeKeyType(postId, "post");
    }

    @Override
    public Integer countLikesByCommentId(Integer commentId) {
        return likeRecordRepository.countByLikeKeyTargetIdAndLikeKeyType(commentId, "comment");
    }

    @Override
    public Integer countLikesByQuestionId(Integer questionId) {
        return likeRecordRepository.countByLikeKeyTargetIdAndLikeKeyType(questionId, LikeType.QUESTION.toString());
    }

    @Override
    public boolean existsByUserAndPost(UserInformationEntity user, Integer postId) {
        return likeRecordRepository.existsByLikeKeyUserIdAndLikeKeyTargetIdAndLikeKeyType(user.getId(), postId, "post");
    }

    @Override
    public boolean existsByUserAndComment(UserInformationEntity user, Integer commentId) {
        return likeRecordRepository.existsByLikeKeyUserIdAndLikeKeyTargetIdAndLikeKeyType(user.getId(), commentId, "comment");
    }

    @Override
    public boolean existsByUserAndQuestion(UserInformationEntity user, Integer questionId) {
        return likeRecordRepository.existsByLikeKeyUserIdAndLikeKeyTargetIdAndLikeKeyType(user.getId(), questionId, "question");
    }


}
