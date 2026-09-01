package com.gamebox.team.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.team.assembler.TeamAssembler;
import com.gamebox.team.dto.StatusUpdateDTO;
import com.gamebox.team.dto.TeamCreateDTO;
import com.gamebox.team.entity.TeamApplication;
import com.gamebox.team.entity.TeamPost;
import com.gamebox.team.mapper.TeamApplicationMapper;
import com.gamebox.team.mapper.TeamPostMapper;
import com.gamebox.team.service.TeamPostService;
import com.gamebox.team.vo.TeamCardVO;
import com.gamebox.team.vo.TeamDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamPostServiceImpl implements TeamPostService {

    private final TeamPostMapper teamPostMapper;
    private final TeamApplicationMapper teamApplicationMapper;
    private final TeamAssembler teamAssembler;

    @Override
    public Page<TeamCardVO> list(Long gameId, Integer status, Integer page, Integer size) {
        LambdaQueryWrapper<TeamPost> wrapper = new LambdaQueryWrapper<TeamPost>()
                .eq(gameId != null, TeamPost::getGameId, gameId)
                .eq(TeamPost::getStatus, status == null ? 1 : status)
                .orderByDesc(TeamPost::getCreatedAt);
        Page<TeamPost> result = teamPostMapper.selectPage(new Page<>(page, size), wrapper);
        return toCardPage(result);
    }

    @Override
    public TeamDetailVO detail(Long id) {
        TeamPost post = teamPostMapper.selectById(id);
        if (post == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        TeamCardVO card = toCards(List.of(post)).get(0);
        TeamDetailVO vo = new TeamDetailVO();
        BeanUtils.copyProperties(card, vo);
        vo.setContent(post.getContent());
        vo.setMyApplication(myApplicationStatus(post.getId()));
        return vo;
    }

    @Override
    public Long create(TeamCreateDTO dto) {
        Long userId = requireLogin();
        TeamPost post = new TeamPost();
        post.setUserId(userId);
        post.setGameId(dto.getGameId());
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent() == null ? "" : dto.getContent());
        post.setMemberLimit(dto.getMemberLimit() == null ? 4 : dto.getMemberLimit());
        post.setMemberCount(1);
        post.setNeedVoice(dto.getNeedVoice() == null ? 0 : dto.getNeedVoice());
        post.setPlayTime(dto.getPlayTime() == null ? "" : dto.getPlayTime());
        post.setStatus(1);
        teamPostMapper.insert(post);
        return post.getId();
    }

    @Override
    public void updateStatus(Long id, StatusUpdateDTO dto) {
        Long userId = requireLogin();
        TeamPost post = teamPostMapper.selectById(id);
        if (post == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        Integer status = dto.getStatus();
        if (status != 2 && status != 3) {
            throw BizException.of("状态仅允许修改为已满员或已关闭");
        }
        TeamPost update = new TeamPost();
        update.setId(post.getId());
        update.setStatus(status);
        teamPostMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = requireLogin();
        TeamPost post = teamPostMapper.selectById(id);
        if (post == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        teamPostMapper.deleteById(id);
        teamApplicationMapper.delete(
                new LambdaQueryWrapper<TeamApplication>().eq(TeamApplication::getPostId, id));
    }

    @Override
    public Page<TeamCardVO> my(Integer page, Integer size) {
        Long userId = requireLogin();
        LambdaQueryWrapper<TeamPost> wrapper = new LambdaQueryWrapper<TeamPost>()
                .eq(TeamPost::getUserId, userId)
                .orderByDesc(TeamPost::getCreatedAt);
        Page<TeamPost> result = teamPostMapper.selectPage(new Page<>(page, size), wrapper);
        return toCardPage(result);
    }

    private Integer myApplicationStatus(Long postId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return null;
        }
        TeamApplication application = teamApplicationMapper.selectOne(
                new LambdaQueryWrapper<TeamApplication>()
                        .eq(TeamApplication::getPostId, postId)
                        .eq(TeamApplication::getUserId, userId));
        return application == null ? null : application.getStatus();
    }

    private Page<TeamCardVO> toCardPage(Page<TeamPost> result) {
        Page<TeamCardVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toCards(result.getRecords()));
        return voPage;
    }

    private List<TeamCardVO> toCards(List<TeamPost> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }
        Set<Long> gameIds = posts.stream().map(TeamPost::getGameId).collect(Collectors.toSet());
        Set<Long> userIds = posts.stream().map(TeamPost::getUserId).collect(Collectors.toSet());
        Map<Long, GameBriefVO> gameMap = teamAssembler.gameMap(gameIds);
        Map<Long, UserBriefVO> userMap = teamAssembler.userMap(userIds);
        return posts.stream()
                .map(post -> toCard(post, gameMap, userMap))
                .toList();
    }

    private TeamCardVO toCard(TeamPost post, Map<Long, GameBriefVO> gameMap, Map<Long, UserBriefVO> userMap) {
        UserBriefVO leader = userMap.get(post.getUserId());
        if (leader == null) {
            leader = UserBriefVO.builder().id(post.getUserId()).nickname("未知用户").build();
        }
        return TeamCardVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .memberLimit(post.getMemberLimit())
                .memberCount(post.getMemberCount())
                .needVoice(post.getNeedVoice())
                .playTime(post.getPlayTime())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .game(gameMap.get(post.getGameId()))
                .leader(leader)
                .build();
    }

    private Long requireLogin() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
