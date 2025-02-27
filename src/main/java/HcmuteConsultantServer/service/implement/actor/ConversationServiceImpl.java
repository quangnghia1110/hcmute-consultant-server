package HcmuteConsultantServer.service.implement.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import HcmuteConsultantServer.constant.FilePaths;
import HcmuteConsultantServer.constant.SecurityConstants;
import HcmuteConsultantServer.model.entity.*;
import HcmuteConsultantServer.model.exception.CustomFieldErrorException;
import HcmuteConsultantServer.model.exception.Exceptions.ErrorException;
import HcmuteConsultantServer.model.exception.FieldErrorDetail;
import HcmuteConsultantServer.model.payload.dto.actor.ConversationDTO;
import HcmuteConsultantServer.model.payload.dto.actor.EmailDTO;
import HcmuteConsultantServer.model.payload.dto.actor.MemberDTO;
import HcmuteConsultantServer.model.payload.mapper.actor.ConversationMapper;
import HcmuteConsultantServer.model.payload.request.CreateConversationRequest;
import HcmuteConsultantServer.model.payload.request.CreateConversationUserRequest;
import HcmuteConsultantServer.repository.actor.ConversationDeleteRepository;
import HcmuteConsultantServer.repository.actor.ConversationRepository;
import HcmuteConsultantServer.repository.actor.ConversationUserRepository;
import HcmuteConsultantServer.repository.actor.MessageRepository;
import HcmuteConsultantServer.repository.admin.AccountRepository;
import HcmuteConsultantServer.repository.admin.DepartmentRepository;
import HcmuteConsultantServer.repository.admin.UserRepository;
import HcmuteConsultantServer.service.interfaces.actor.IConversationService;
import HcmuteConsultantServer.specification.actor.ConversationSpecification;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements IConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ConversationUserRepository conversationUserRepository;

    @Autowired
    private ConversationDeleteRepository conversationDeleteRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public ConversationDTO createConversation(CreateConversationUserRequest request, UserInformationEntity user) {
        List<FieldErrorDetail> errors = new ArrayList<>();

        Optional<UserInformationEntity> consultantOpt = userRepository.findById(request.getConsultantId());
        if (!consultantOpt.isPresent()) {
            errors.add(new FieldErrorDetail("consultant", "Tư vấn viên không tồn tại"));
            throw new CustomFieldErrorException(errors);
        }

        Optional<DepartmentEntity> departmentOpt = departmentRepository.findById(request.getDepartmentId());
        if (!departmentOpt.isPresent()) {
            errors.add(new FieldErrorDetail("department", "Phòng ban không tồn tại"));
            throw new CustomFieldErrorException(errors);
        }

        UserInformationEntity consultant = consultantOpt.get();
        DepartmentEntity department = departmentOpt.get();

        if (!consultant.getAccount().getDepartment().getId().equals(department.getId())) {
            errors.add(new FieldErrorDetail("consultant", "Tư vấn viên không thuộc phòng ban đã chọn"));
            throw new CustomFieldErrorException(errors);
        }

        boolean conversationExists = conversationRepository.existsByUserAndConsultantAndDepartment(user, consultant, department);

        if (conversationExists) {
            throw new ErrorException("Cuộc trò chuyện giữa bạn và tư vấn viên này trong phòng ban đã tồn tại");
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setCreatedAt(LocalDate.now());
        conversation.setUser(user);
        conversation.setConsultant(consultant);
        conversation.setIsGroup(false);
        conversation.setStatusActive(true);
        conversation.setDepartment(department);

        ConversationEntity savedConversation = conversationRepository.save(conversation);

        ConversationUserEntity conversationUserForCreator = new ConversationUserEntity();
        ConversationUserKeyEntity creatorKey = new ConversationUserKeyEntity(savedConversation.getId(), user.getId());
        conversationUserForCreator.setId(creatorKey);
        conversationUserForCreator.setConversation(savedConversation);
        conversationUserForCreator.setUser(user);
        conversationUserRepository.save(conversationUserForCreator);

        ConversationUserEntity conversationUserForConsultant = new ConversationUserEntity();
        ConversationUserKeyEntity consultantKey = new ConversationUserKeyEntity(savedConversation.getId(), consultant.getId());
        conversationUserForConsultant.setId(consultantKey);
        conversationUserForConsultant.setConversation(savedConversation);
        conversationUserForConsultant.setUser(consultant);
        conversationUserRepository.save(conversationUserForConsultant);

        return conversationMapper.mapToDTO(savedConversation, conversationUserRepository);
    }

    @Override
    public ConversationDTO createConversationByConsultant(CreateConversationRequest request,
                                                          UserInformationEntity user) {
        List<FieldErrorDetail> errors = new ArrayList<>();

        boolean hasConsultantRole = userRepository.existsByUserIdAndRoleName(user.getId(), SecurityConstants.Role.TUVANVIEN);
        if (!hasConsultantRole) {
            errors.add(new FieldErrorDetail("role", "Người dùng không có vai trò tư vấn viên"));
        }

        if (!errors.isEmpty()) {
            throw new CustomFieldErrorException(errors);
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setCreatedAt(LocalDate.now());
        conversation.setConsultant(user);
        conversation.setUser(user);
        conversation.setName(request.getName());
        conversation.setIsGroup(true);
        conversation.setStatusActive(true);
        conversation.setAvatarUrl(FilePaths.AVATAR_CONVERSATION);
        conversation.setDepartment(user.getAccount().getDepartment());

        ConversationEntity savedConversation = conversationRepository.save(conversation);

        ConversationUserKeyEntity conversationUserKey = new ConversationUserKeyEntity(savedConversation.getId(), user.getId());
        ConversationUserEntity conversationUser = new ConversationUserEntity();
        conversationUser.setId(conversationUserKey);
        conversationUser.setConversation(savedConversation);
        conversationUser.setUser(user);

        conversationUserRepository.save(conversationUser);

        return conversationMapper.mapToDTO(savedConversation, conversationUserRepository);
    }

    @Override
    public Page<ConversationDTO> getConversationByRole(Integer userId, String role, Integer depId, String name, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<ConversationEntity> spec;

        if (SecurityConstants.Role.USER.equals(role)) {
            spec = Specification.where(ConversationSpecification.isOwner(userId))
                    .or(ConversationSpecification.isMember(userId));
        } else if (SecurityConstants.Role.TUVANVIEN.equals(role)) {
            spec = Specification.where(ConversationSpecification.isOwner(userId))
                    .or(ConversationSpecification.isMember(userId));
        } else if (SecurityConstants.Role.ADMIN.equals(role)) {
            spec = Specification.where(null);
        } else if (SecurityConstants.Role.TRUONGBANTUVAN.equals(role)) {
            spec = Specification.where(ConversationSpecification.hasDepartment(depId));
        } else {
            throw new ErrorException("Vai trò không được hỗ trợ");
        }

        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(ConversationSpecification.hasName(name));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(ConversationSpecification.hasExactDateRange(startDate, endDate));
        } else if (startDate != null) {
            spec = spec.and(ConversationSpecification.hasExactStartDate(startDate));
        } else if (endDate != null) {
            spec = spec.and(ConversationSpecification.hasDateBefore(endDate));
        }

        Page<ConversationEntity> conversations = conversationRepository.findAll(spec, pageable);

        return conversations.map(conversation -> conversationMapper.mapToDTO(conversation, conversationUserRepository));
    }

    @Override
    @Transactional
    public void updateConversationName(Integer conversationId, String newName) {
        Optional<ConversationEntity> conversationOpt = conversationRepository.findById(conversationId);

        if (!conversationOpt.isPresent()) {
            throw new ErrorException("Cuộc trò chuyện không tồn tại");
        }

        ConversationEntity conversation = conversationOpt.get();
        conversation.setName(newName);
        conversationRepository.save(conversation);
    }

    @Transactional
    @Override
    public boolean recordDeletion(Integer conversationId, Integer userId) {
        Optional<ConversationEntity> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new ErrorException("Cuộc trò chuyện không tồn tại");
        }
        ConversationEntity conversation = conversationOpt.get();

        Optional<UserInformationEntity> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new ErrorException("Người dùng không tồn tại");
        }
        UserInformationEntity user = userOpt.get();

        Optional<ConversationDeleteEntity> deleteOpt = conversationDeleteRepository.findByConversationIdAndUserId(conversationId, userId);
        if (deleteOpt.isPresent()) {
            return false;
        }

        ConversationDeleteKeyEntity key = new ConversationDeleteKeyEntity(conversation.getId(), user.getId());

        ConversationDeleteEntity conversationDelete = new ConversationDeleteEntity();
        conversationDelete.setId(key);
        conversationDelete.setConversation(conversation);
        conversationDelete.setUser(user);

        conversationDeleteRepository.save(conversationDelete);

        return true;
    }

    @Transactional
    @Override
    public void deleteConversation(Integer conversationId) {
        long totalUsersInConversation = conversationUserRepository.countUsersInConversation(conversationId);
        long totalUsersDeleted = conversationDeleteRepository.countUsersDeleted(conversationId);

        Optional<ConversationEntity> conversationOpt = conversationRepository.findById(conversationId);

        if (!conversationOpt.isPresent()) {
            throw new ErrorException("Cuộc trò chuyện không tồn tại");
        }

        ConversationEntity conversation = conversationOpt.get();

        if (totalUsersInConversation == totalUsersDeleted) {
            conversationDeleteRepository.deleteMembersByConversation(conversation);
            conversationUserRepository.deleteMembersByConversation(conversation);
            messageRepository.deleteMessagesByConversationId(conversationId);
            conversationRepository.deleteConversation(conversation);
        }
    }

    @Transactional
    @Override
    public void deleteMembersFromConversation(ConversationEntity conversation, Integer userId) {
        conversationDeleteRepository.deleteMembersByConversationAndUserId(conversation, userId);
    }


    @Override
    public ConversationDTO getDetailConversationByRole(Integer conversationId) {
        Optional<ConversationEntity> conversationOpt = conversationRepository.findById(conversationId);

        if (!conversationOpt.isPresent()) {
            throw new ErrorException("Nhóm không tồn tại");
        }

        ConversationEntity conversation = conversationOpt.get();

        return conversationMapper.mapToDTO(conversation, conversationUserRepository);
    }

    @Override
    @Transactional
    public ConversationDTO approveMembersByEmail(Integer groupId, List<String> emailsToApprove) {
        ConversationEntity group = conversationRepository.findById(groupId)
                .orElseThrow(() -> new ErrorException("Cuộc hội thoại  không tồn tại"));

        for (String emailToApprove : emailsToApprove) {
            UserInformationEntity user = userRepository.findUserInfoByEmail(emailToApprove)
                    .orElseThrow(() -> new ErrorException("Người dùng với email này không tồn tại"));

            boolean isMember = conversationUserRepository.existsByConversationAndUser(group, user);

            if (!isMember) {
                ConversationUserKeyEntity key = new ConversationUserKeyEntity(group.getId(), user.getId());

                ConversationUserEntity conversationUser = new ConversationUserEntity();
                conversationUser.setId(key);
                conversationUser.setConversation(group);
                conversationUser.setUser(user);

                conversationUserRepository.save(conversationUser);
            }
        }

        conversationRepository.save(group);

        return conversationMapper.mapToDTO(group, conversationUserRepository);
    }

    @Override
    @Transactional
    public void removeMemberFromConversation(Integer conversationId, Integer userId) {
        Optional<ConversationEntity> conversationOpt = conversationRepository.findById(conversationId);
        Optional<UserInformationEntity> userOpt = userRepository.findById(userId);

        if (!conversationOpt.isPresent() || !userOpt.isPresent()) {
            throw new ErrorException("Cuộc trò chuyện hoặc người dùng không tồn tại");
        }

        ConversationEntity conversation = conversationOpt.get();
        UserInformationEntity user = userOpt.get();

        boolean isMember = conversationUserRepository.existsByConversationAndUser(conversation, user);

        if (isMember) {
            conversationUserRepository.deleteByConversationAndUser(conversation, user);
        } else {
            throw new ErrorException("Người dùng không phải là thành viên của cuộc trò chuyện này");
        }
    }

    @Override
    public List<MemberDTO> findNonConsultantMembers(Integer conversationId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer currentUserId = accountRepository.findByEmail(userDetails.getUsername())
                .map(AccountEntity::getId)
                .orElseThrow(() -> new ErrorException("Người dùng không được tìm thấy"));


        List<ConversationUserEntity> members = conversationUserRepository.findByConversationId(conversationId);

        return members.stream()
                .filter(member -> !userRepository.existsByUserIdAndRoleName(member.getUser().getId(), SecurityConstants.Role.TUVANVIEN))
                .map(member -> new MemberDTO(
                        member.getUser().getId(),
                        member.getUser().getLastName() + " " + member.getUser().getFirstName(),
                        member.getUser().getAvatarUrl(),
                        member.getUser().getId().equals(currentUserId)
                ))
                .sorted((m1, m2) -> Boolean.compare(m2.isSender(), m1.isSender()))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmailDTO> findAllUsers() {
        // Lấy danh sách người dùng có vai trò trong danh sách các vai trò
        List<String> roles = Arrays.asList(SecurityConstants.Role.USER, SecurityConstants.Role.TUVANVIEN, SecurityConstants.Role.TRUONGBANTUVAN, SecurityConstants.Role.ADMIN);
        List<UserInformationEntity> users = userRepository.findAllByRoleIn(roles);

        return users.stream()
                .map(user -> new EmailDTO(
                        user.getId(),
                        user.getAccount().getEmail(),
                        user.getLastName() + " " + user.getFirstName(),
                        user.getAvatarUrl()
                ))
                .collect(Collectors.toList());
    }

}
