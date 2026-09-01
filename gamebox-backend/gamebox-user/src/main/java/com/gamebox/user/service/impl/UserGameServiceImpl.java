package com.gamebox.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.user.dto.UserGameSaveDTO;
import com.gamebox.user.entity.Game;
import com.gamebox.user.entity.UserGame;
import com.gamebox.user.mapper.GameMapper;
import com.gamebox.user.mapper.UserGameMapper;
import com.gamebox.user.service.UserGameService;
import com.gamebox.user.vo.UserGameVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserGameServiceImpl implements UserGameService {

    private final UserGameMapper userGameMapper;
    private final GameMapper gameMapper;

    @Override
    public Page<UserGameVO> list(Integer status, Integer page, Integer size) {
        Long userId = requireUserId();
        LambdaQueryWrapper<UserGame> wrapper = new LambdaQueryWrapper<UserGame>()
                .eq(UserGame::getUserId, userId)
                .eq(status != null, UserGame::getStatus, status)
                .orderByDesc(UserGame::getCreatedAt);
        Page<UserGame> result = userGameMapper.selectPage(new Page<>(page, size), wrapper);

        List<Long> gameIds = result.getRecords().stream()
                .map(UserGame::getGameId).distinct().toList();
        Map<Long, Game> gameMap = gameIds.isEmpty() ? Map.of()
                : gameMapper.selectByIds(gameIds).stream()
                        .collect(Collectors.toMap(Game::getId, Function.identity()));

        Page<UserGameVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(ug -> toVO(ug, gameMap.get(ug.getGameId())))
                .toList());
        return voPage;
    }

    @Override
    public UserGameVO check(Long gameId) {
        Long userId = requireUserId();
        UserGame userGame = userGameMapper.selectOne(new LambdaQueryWrapper<UserGame>()
                .eq(UserGame::getUserId, userId)
                .eq(UserGame::getGameId, gameId));
        if (userGame == null) {
            return null;
        }
        return toVO(userGame, gameMapper.selectById(gameId));
    }

    @Override
    public void save(UserGameSaveDTO dto) {
        Long userId = requireUserId();
        if (dto.getGameId() == null) {
            throw BizException.of("游戏ID不能为空");
        }
        Game game = gameMapper.selectById(dto.getGameId());
        if (game == null) {
            throw new BizException(ResultCode.NOT_FOUND, "游戏不存在");
        }
        Long count = userGameMapper.selectCount(new LambdaQueryWrapper<UserGame>()
                .eq(UserGame::getUserId, userId)
                .eq(UserGame::getGameId, dto.getGameId()));
        if (count > 0) {
            throw BizException.of("该游戏已在你的游戏库中");
        }
        UserGame userGame = new UserGame();
        userGame.setUserId(userId);
        userGame.setGameId(dto.getGameId());
        userGame.setStatus(dto.getStatus());
        userGame.setPlayHours(dto.getPlayHours() == null ? BigDecimal.ZERO : dto.getPlayHours());
        userGame.setRating(dto.getRating() == null ? 0 : dto.getRating());
        userGame.setRemark(dto.getRemark() == null ? "" : dto.getRemark());
        try {
            userGameMapper.insert(userGame);
        } catch (DuplicateKeyException e) {
            // 并发下唯一键冲突兜底
            throw BizException.of("该游戏已在你的游戏库中");
        }
    }

    @Override
    public void update(Long id, UserGameSaveDTO dto) {
        Long userId = requireUserId();
        UserGame userGame = getOwnedRecord(id, userId);
        userGame.setStatus(dto.getStatus());
        if (dto.getPlayHours() != null) {
            userGame.setPlayHours(dto.getPlayHours());
        }
        if (dto.getRating() != null) {
            userGame.setRating(dto.getRating());
        }
        if (dto.getRemark() != null) {
            userGame.setRemark(dto.getRemark());
        }
        userGameMapper.updateById(userGame);
    }

    @Override
    public void delete(Long id) {
        Long userId = requireUserId();
        getOwnedRecord(id, userId);
        userGameMapper.deleteById(id);
    }

    private UserGame getOwnedRecord(Long id, Long userId) {
        UserGame userGame = userGameMapper.selectById(id);
        if (userGame == null) {
            throw new BizException(ResultCode.NOT_FOUND, "记录不存在");
        }
        if (!userGame.getUserId().equals(userId)) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
        return userGame;
    }

    private Long requireUserId() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private UserGameVO toVO(UserGame userGame, Game game) {
        return UserGameVO.builder()
                .id(userGame.getId())
                .gameId(userGame.getGameId())
                .gameName(game == null ? "" : game.getName())
                .gameCover(game == null ? "" : game.getCover())
                .genre(game == null ? "" : game.getGenre())
                .status(userGame.getStatus())
                .playHours(userGame.getPlayHours())
                .rating(userGame.getRating())
                .remark(userGame.getRemark())
                .createdAt(userGame.getCreatedAt())
                .build();
    }
}
