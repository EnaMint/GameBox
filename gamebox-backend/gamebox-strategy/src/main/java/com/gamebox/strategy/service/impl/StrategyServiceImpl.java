package com.gamebox.strategy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.strategy.assembler.StrategyAssembler;
import com.gamebox.strategy.dto.StrategyCreateDTO;
import com.gamebox.strategy.dto.StrategyUpdateDTO;
import com.gamebox.strategy.entity.Strategy;
import com.gamebox.strategy.entity.StrategyLike;
import com.gamebox.strategy.mapper.StrategyLikeMapper;
import com.gamebox.strategy.mapper.StrategyMapper;
import com.gamebox.strategy.service.StrategyService;
import com.gamebox.strategy.vo.LikeResultVO;
import com.gamebox.strategy.vo.StrategyCardVO;
import com.gamebox.strategy.vo.StrategyDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategyServiceImpl implements StrategyService {

    private final StrategyMapper strategyMapper;
    private final StrategyLikeMapper strategyLikeMapper;
    private final StrategyAssembler strategyAssembler;

    @Override
    public Page<StrategyCardVO> pageList(String category, Long gameId, String keyword, String sort,
                                         Integer page, Integer size) {
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<Strategy>()
                .eq(Strategy::getStatus, 1);
        if (category != null && !category.isBlank()) {
            wrapper.eq(Strategy::getCategory, category);
        }
        if (gameId != null && gameId > 0) {
            wrapper.eq(Strategy::getGameId, gameId);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Strategy::getTitle, kw).or().like(Strategy::getSummary, kw));
        }
        if ("hot".equalsIgnoreCase(sort)) {
            wrapper.last("ORDER BY (like_count*5+view_count) DESC");
        } else {
            wrapper.orderByDesc(Strategy::getCreatedAt);
        }
        Page<Strategy> result = strategyMapper.selectPage(new Page<>(page, size), wrapper);
        return toCardPage(result);
    }

    @Override
    public StrategyDetailVO detail(Long id) {
        Strategy strategy = getActiveStrategy(id);
        strategyMapper.incrementViewCount(id);
        StrategyCardVO card = toCards(List.of(strategy)).get(0);
        StrategyDetailVO vo = new StrategyDetailVO();
        BeanUtils.copyProperties(card, vo);
        vo.setGameId(strategy.getGameId());
        vo.setContent(strategy.getContent());
        vo.setViewCount(strategy.getViewCount() + 1);
        return vo;
    }

    @Override
    public Long create(StrategyCreateDTO dto) {
        Long userId = requireLogin();
        Strategy strategy = new Strategy();
        strategy.setUserId(userId);
        strategy.setTitle(dto.getTitle());
        strategy.setCategory(dto.getCategory());
        strategy.setGameId(dto.getGameId() == null ? 0L : dto.getGameId());
        strategy.setCover(dto.getCover() == null ? "" : dto.getCover());
        strategy.setSummary(dto.getSummary() == null ? "" : dto.getSummary());
        strategy.setContent(dto.getContent());
        strategyMapper.insert(strategy);
        return strategy.getId();
    }

    @Override
    public void update(Long id, StrategyUpdateDTO dto) {
        Long userId = requireLogin();
        Strategy strategy = getActiveStrategy(id);
        if (!strategy.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        Strategy update = new Strategy();
        update.setId(id);
        update.setTitle(dto.getTitle());
        update.setCategory(dto.getCategory());
        update.setGameId(dto.getGameId() == null ? 0L : dto.getGameId());
        update.setCover(dto.getCover() == null ? "" : dto.getCover());
        update.setSummary(dto.getSummary() == null ? "" : dto.getSummary());
        update.setContent(dto.getContent());
        strategyMapper.updateById(update);
    }

    @Override
    public void delete(Long id) {
        Long userId = requireLogin();
        Strategy strategy = getActiveStrategy(id);
        if (!strategy.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        strategyMapper.update(null, new LambdaUpdateWrapper<Strategy>()
                .eq(Strategy::getId, id)
                .set(Strategy::getStatus, 0));
    }

    @Override
    public Page<StrategyCardVO> myList(Integer page, Integer size) {
        Long userId = requireLogin();
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<Strategy>()
                .eq(Strategy::getUserId, userId)
                .eq(Strategy::getStatus, 1)
                .orderByDesc(Strategy::getCreatedAt);
        Page<Strategy> result = strategyMapper.selectPage(new Page<>(page, size), wrapper);
        return toCardPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LikeResultVO toggleLike(Long id) {
        Long userId = requireLogin();
        Strategy strategy = getActiveStrategy(id);
        boolean nowLiked;
        StrategyLike exist = strategyLikeMapper.selectOne(new LambdaQueryWrapper<StrategyLike>()
                .eq(StrategyLike::getStrategyId, id)
                .eq(StrategyLike::getUserId, userId));
        if (exist != null) {
            strategyLikeMapper.deleteById(exist.getId());
            nowLiked = false;
        } else {
            StrategyLike like = new StrategyLike();
            like.setStrategyId(id);
            like.setUserId(userId);
            try {
                strategyLikeMapper.insert(like);
                nowLiked = true;
            } catch (DuplicateKeyException e) {
                strategyLikeMapper.delete(new LambdaQueryWrapper<StrategyLike>()
                        .eq(StrategyLike::getStrategyId, id)
                        .eq(StrategyLike::getUserId, userId));
                nowLiked = false;
            }
        }
        strategyMapper.update(null, new LambdaUpdateWrapper<Strategy>()
                .eq(Strategy::getId, id)
                .setSql(nowLiked ? "like_count = like_count + 1" : "like_count = GREATEST(like_count - 1, 0)"));
        int likeCount = Math.max(0, strategy.getLikeCount() + (nowLiked ? 1 : -1));
        return new LikeResultVO(nowLiked, likeCount);
    }

    private Page<StrategyCardVO> toCardPage(Page<Strategy> result) {
        Page<StrategyCardVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(toCards(result.getRecords()));
        return voPage;
    }

    private List<StrategyCardVO> toCards(List<Strategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> userIds = strategies.stream().map(Strategy::getUserId).collect(Collectors.toSet());
        Set<Long> gameIds = strategies.stream()
                .map(Strategy::getGameId)
                .filter(gid -> gid != null && gid > 0)
                .collect(Collectors.toSet());
        Map<Long, UserBriefVO> userMap = strategyAssembler.userMap(userIds);
        Map<Long, GameBriefVO> gameMap = strategyAssembler.gameMap(gameIds);
        Set<Long> likedIds = likedStrategyIds(strategies.stream().map(Strategy::getId).toList());
        return strategies.stream().map(s -> {
            StrategyCardVO vo = new StrategyCardVO();
            vo.setId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setCategory(s.getCategory());
            vo.setCover(s.getCover());
            vo.setSummary(s.getSummary());
            vo.setViewCount(s.getViewCount());
            vo.setLikeCount(s.getLikeCount());
            vo.setCommentCount(s.getCommentCount());
            vo.setCreatedAt(s.getCreatedAt());
            vo.setAuthor(userMap.getOrDefault(s.getUserId(),
                    UserBriefVO.builder().id(s.getUserId()).nickname("未知用户").build()));
            vo.setGame(s.getGameId() == null ? null : gameMap.get(s.getGameId()));
            vo.setLiked(likedIds.contains(s.getId()));
            return vo;
        }).toList();
    }

    private Set<Long> likedStrategyIds(List<Long> strategyIds) {
        Long userId = UserHolder.getUserId();
        if (userId == null || strategyIds.isEmpty()) {
            return Set.of();
        }
        return strategyLikeMapper.selectList(new LambdaQueryWrapper<StrategyLike>()
                        .select(StrategyLike::getStrategyId)
                        .eq(StrategyLike::getUserId, userId)
                        .in(StrategyLike::getStrategyId, strategyIds))
                .stream()
                .map(StrategyLike::getStrategyId)
                .collect(Collectors.toSet());
    }

    private Strategy getActiveStrategy(Long id) {
        Strategy strategy = strategyMapper.selectById(id);
        if (strategy == null || strategy.getStatus() == null || strategy.getStatus() != 1) {
            throw new BizException(ResultCode.NOT_FOUND, "攻略不存在");
        }
        return strategy;
    }

    private Long requireLogin() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
