package com.gamebox.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.user.entity.Game;
import com.gamebox.user.mapper.GameMapper;
import com.gamebox.user.service.GameService;
import com.gamebox.user.vo.GameVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameMapper gameMapper;

    @Override
    public Page<GameVO> list(String keyword, String genre, String tag, Integer page, Integer size) {
        LambdaQueryWrapper<Game> wrapper = new LambdaQueryWrapper<Game>()
                .like(keyword != null && !keyword.isBlank(), Game::getName, keyword == null ? null : keyword.trim())
                .eq(genre != null && !genre.isBlank(), Game::getGenre, genre == null ? null : genre.trim())
                .apply(tag != null && !tag.isBlank(), "FIND_IN_SET({0}, tags)", tag == null ? null : tag.trim())
                .orderByAsc(Game::getCreatedAt);
        Page<Game> result = gameMapper.selectPage(new Page<>(page, size), wrapper);
        Page<GameVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public GameVO getById(Long id) {
        Game game = gameMapper.selectById(id);
        if (game == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return toVO(game);
    }

    @Override
    public List<GameBriefVO> batch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return gameMapper.selectByIds(ids).stream()
                .map(g -> GameBriefVO.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .cover(g.getCover())
                        .build())
                .toList();
    }

    @Override
    public List<String> tags() {
        List<Game> games = gameMapper.selectList(new LambdaQueryWrapper<Game>()
                .select(Game::getTags)
                .orderByAsc(Game::getCreatedAt));
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        for (Game game : games) {
            if (game.getTags() == null || game.getTags().isBlank()) {
                continue;
            }
            for (String t : game.getTags().split(",")) {
                if (!t.isBlank()) {
                    tags.add(t.trim());
                }
            }
        }
        return new ArrayList<>(tags);
    }

    private GameVO toVO(Game game) {
        return GameVO.builder()
                .id(game.getId())
                .name(game.getName())
                .cover(game.getCover())
                .genre(game.getGenre())
                .tags(game.getTags())
                .platform(game.getPlatform())
                .description(game.getDescription())
                .build();
    }
}
