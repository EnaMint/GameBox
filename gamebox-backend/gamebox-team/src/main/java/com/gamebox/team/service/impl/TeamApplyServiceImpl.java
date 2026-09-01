package com.gamebox.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.team.assembler.TeamAssembler;
import com.gamebox.team.dto.ApplyAuditDTO;
import com.gamebox.team.dto.ApplyDTO;
import com.gamebox.team.entity.TeamApplication;
import com.gamebox.team.entity.TeamPost;
import com.gamebox.team.mapper.TeamApplicationMapper;
import com.gamebox.team.mapper.TeamPostMapper;
import com.gamebox.team.service.TeamApplyService;
import com.gamebox.team.vo.ApplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamApplyServiceImpl implements TeamApplyService {

    private static final int APPLY_PENDING = 0;
    private static final int APPLY_APPROVED = 1;
    private static final int APPLY_REJECTED = 2;
    private static final int APPLY_WITHDRAWN = 3;

    private final TeamApplicationMapper teamApplicationMapper;
    private final TeamPostMapper teamPostMapper;
    private final TeamAssembler teamAssembler;

    @Override
    public void apply(Long postId, ApplyDTO dto) {
        Long userId = requireLogin();
        TeamPost post = teamPostMapper.selectById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw BizException.of("该组队已结束或已满员");
        }
        if (post.getUserId().equals(userId)) {
            throw BizException.of("不能申请自己发布的组队");
        }
        String message = dto == null || dto.getMessage() == null ? "" : dto.getMessage();
        TeamApplication existing = selectByPostAndUser(postId, userId);
        if (existing == null) {
            TeamApplication application = new TeamApplication();
            application.setPostId(postId);
            application.setUserId(userId);
            application.setMessage(message);
            application.setStatus(APPLY_PENDING);
            teamApplicationMapper.insert(application);
            return;
        }
        switch (existing.getStatus()) {
            case APPLY_PENDING -> throw BizException.of("已提交过申请，请等待审核");
            case APPLY_APPROVED -> throw BizException.of("你已在队伍中");
            default -> {
                TeamApplication update = new TeamApplication();
                update.setId(existing.getId());
                update.setStatus(APPLY_PENDING);
                update.setMessage(message);
                teamApplicationMapper.updateById(update);
            }
        }
    }

    @Override
    public void withdraw(Long postId) {
        Long userId = requireLogin();
        TeamApplication application = selectByPostAndUser(postId, userId);
        if (application == null) {
            throw BizException.of("你还未申请该组队");
        }
        if (application.getStatus() == null || application.getStatus() != APPLY_PENDING) {
            throw BizException.of("仅待审核的申请可撤回");
        }
        TeamApplication update = new TeamApplication();
        update.setId(application.getId());
        update.setStatus(APPLY_WITHDRAWN);
        teamApplicationMapper.updateById(update);
    }

    @Override
    public List<ApplyVO> applicationsOfPost(Long postId) {
        Long userId = requireLogin();
        TeamPost post = teamPostMapper.selectById(postId);
        if (post == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        List<TeamApplication> applications = teamApplicationMapper.selectList(
                new LambdaQueryWrapper<TeamApplication>()
                        .eq(TeamApplication::getPostId, postId)
                        .orderByAsc(TeamApplication::getCreatedAt));
        if (applications.isEmpty()) {
            return List.of();
        }
        Set<Long> applicantIds = applications.stream()
                .map(TeamApplication::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserBriefVO> userMap = teamAssembler.userMap(applicantIds);
        return applications.stream()
                .map(a -> ApplyVO.builder()
                        .id(a.getId())
                        .message(a.getMessage())
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .user(userMap.get(a.getUserId()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long applicationId, ApplyAuditDTO dto) {
        Long userId = requireLogin();
        TeamApplication application = teamApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        TeamPost post = teamPostMapper.selectById(application.getPostId());
        if (post == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        if ("reject".equals(dto.getAction())) {
            int rows = teamApplicationMapper.update(null, new LambdaUpdateWrapper<TeamApplication>()
                    .set(TeamApplication::getStatus, APPLY_REJECTED)
                    .eq(TeamApplication::getId, applicationId)
                    .eq(TeamApplication::getStatus, APPLY_PENDING));
            if (rows == 0) {
                throw BizException.of("该申请已处理");
            }
            return;
        }
        if ("approve".equals(dto.getAction())) {
            int rows = teamApplicationMapper.update(null, new LambdaUpdateWrapper<TeamApplication>()
                    .set(TeamApplication::getStatus, APPLY_APPROVED)
                    .eq(TeamApplication::getId, applicationId)
                    .eq(TeamApplication::getStatus, APPLY_PENDING));
            if (rows == 0) {
                throw BizException.of("该申请已处理");
            }
            int updated = teamPostMapper.update(null, new LambdaUpdateWrapper<TeamPost>()
                    .setSql("member_count = member_count + 1")
                    .eq(TeamPost::getId, post.getId())
                    .eq(TeamPost::getStatus, 1)
                    .apply("member_count < member_limit"));
            if (updated == 0) {
                throw BizException.of("队伍已满或已关闭");
            }
            TeamPost latest = teamPostMapper.selectById(post.getId());
            if (latest != null && latest.getMemberCount() >= latest.getMemberLimit()) {
                teamPostMapper.update(null, new LambdaUpdateWrapper<TeamPost>()
                        .set(TeamPost::getStatus, 2)
                        .eq(TeamPost::getId, latest.getId())
                        .eq(TeamPost::getStatus, 1));
            }
            return;
        }
        throw BizException.of("无效的操作");
    }

    @Override
    public List<ApplyVO> myApplications() {
        Long userId = requireLogin();
        List<TeamApplication> applications = teamApplicationMapper.selectList(
                new LambdaQueryWrapper<TeamApplication>()
                        .eq(TeamApplication::getUserId, userId)
                        .orderByDesc(TeamApplication::getCreatedAt));
        if (applications.isEmpty()) {
            return List.of();
        }
        Set<Long> postIds = applications.stream()
                .map(TeamApplication::getPostId)
                .collect(Collectors.toSet());
        Map<Long, TeamPost> postMap = teamPostMapper.selectBatchIds(postIds).stream()
                .collect(Collectors.toMap(TeamPost::getId, Function.identity()));
        Set<Long> gameIds = postMap.values().stream()
                .map(TeamPost::getGameId)
                .collect(Collectors.toSet());
        Map<Long, GameBriefVO> gameMap = teamAssembler.gameMap(gameIds);
        return applications.stream()
                .map(a -> {
                    TeamPost post = postMap.get(a.getPostId());
                    return ApplyVO.builder()
                            .id(a.getId())
                            .message(a.getMessage())
                            .status(a.getStatus())
                            .createdAt(a.getCreatedAt())
                            .postId(a.getPostId())
                            .postTitle(post == null ? null : post.getTitle())
                            .postStatus(post == null ? null : post.getStatus())
                            .game(post == null ? null : gameMap.get(post.getGameId()))
                            .build();
                })
                .toList();
    }

    private TeamApplication selectByPostAndUser(Long postId, Long userId) {
        return teamApplicationMapper.selectOne(new LambdaQueryWrapper<TeamApplication>()
                .eq(TeamApplication::getPostId, postId)
                .eq(TeamApplication::getUserId, userId));
    }

    private Long requireLogin() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
