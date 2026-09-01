package com.gamebox.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.R;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.user.service.GameService;
import com.gamebox.user.vo.GameVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/list")
    public R<Page<GameVO>> list(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String genre,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "12") Integer size) {
        return R.ok(gameService.list(keyword, genre, page, size));
    }

    @GetMapping("/{id}")
    public R<GameVO> getById(@PathVariable Long id) {
        return R.ok(gameService.getById(id));
    }

    @GetMapping("/inner/batch")
    public R<List<GameBriefVO>> batch(@RequestParam(required = false) String ids) {
        return R.ok(gameService.batch(parseIds(ids)));
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        List<Long> idList;
        try {
            idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .distinct()
                    .toList();
        } catch (NumberFormatException e) {
            throw BizException.of("ids格式错误");
        }
        if (idList.size() > 50) {
            throw BizException.of("一次最多查询50个");
        }
        return idList;
    }
}
