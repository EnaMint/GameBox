package com.gamebox.strategy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.strategy.assembler.StrategyAssembler;
import com.gamebox.strategy.dto.CommentCreateDTO;
import com.gamebox.strategy.entity.Strategy;
import com.gamebox.strategy.entity.StrategyComment;
import com.gamebox.strategy.mapper.StrategyCommentMapper;
import com.gamebox.strategy.mapper.StrategyMapper;
import com.gamebox.strategy.service.CommentService;
import com.gamebox.strategy.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final StrategyCommentMapper strategyCommentMapper;
    private final StrategyMapper strategyMapper;
    private final StrategyAssembler strategyAssembler;

    @Override
    public Page<CommentVO> pageComments(Long strategyId, Integer page, Integer size) {
        checkActiveStrategy(strategyId);
        Page<StrategyComment> result = strategyCommentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<StrategyComment>()
                        .eq(StrategyComment::getStrategyId, strategyId)
                        .orderByDesc(StrategyComment::getCreatedAt));
        Map<Long, UserBriefVO> userMap = strategyAssembler.userMap(
                result.getRecords().stream().map(StrategyComment::getUserId).collect(Collectors.toSet()));
        Page<CommentVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(comment -> toVO(comment, userMap))
                .toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(Long strategyId, CommentCreateDTO dto) {
        Long userId = requireLogin();
        checkActiveStrategy(strategyId);
        StrategyComment comment = new StrategyComment();
        comment.setStrategyId(strategyId);
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        strategyCommentMapper.insert(comment);
        strategyMapper.update(null, new LambdaUpdateWrapper<Strategy>()
                .eq(Strategy::getId, strategyId)
                .setSql("comment_count = comment_count + 1"));
        Map<Long, UserBriefVO> userMap = strategyAssembler.userMap(Set.of(userId));
        return toVO(comment, userMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = requireLogin();
        StrategyComment comment = strategyCommentMapper.selectById(id);
        if (comment == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评论不存在");
        }
        boolean commentOwner = comment.getUserId().equals(userId);
        boolean strategyAuthor = false;
        Strategy strategy = strategyMapper.selectById(comment.getStrategyId());
        if (strategy != null && strategy.getUserId().equals(userId)) {
            strategyAuthor = true;
        }
        if (!commentOwner && !strategyAuthor) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        strategyCommentMapper.deleteById(id);
        strategyMapper.update(null, new LambdaUpdateWrapper<Strategy>()
                .eq(Strategy::getId, comment.getStrategyId())
                .setSql("comment_count = GREATEST(comment_count - 1, 0)"));
    }

    private CommentVO toVO(StrategyComment comment, Map<Long, UserBriefVO> userMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUser(userMap.getOrDefault(comment.getUserId(),
                UserBriefVO.builder().id(comment.getUserId()).nickname("未知用户").build()));
        return vo;
    }

    private void checkActiveStrategy(Long strategyId) {
        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null || strategy.getStatus() == null || strategy.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "攻略不存在");
        }
    }

    private Long requireLogin() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
