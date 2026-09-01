package com.gamebox.strategy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.result.R;
import com.gamebox.strategy.dto.CommentCreateDTO;
import com.gamebox.strategy.dto.StrategyCreateDTO;
import com.gamebox.strategy.dto.StrategyUpdateDTO;
import com.gamebox.strategy.service.CommentService;
import com.gamebox.strategy.service.StrategyService;
import com.gamebox.strategy.vo.CommentVO;
import com.gamebox.strategy.vo.LikeResultVO;
import com.gamebox.strategy.vo.StrategyCardVO;
import com.gamebox.strategy.vo.StrategyDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/strategy")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;
    private final CommentService commentService;

    @GetMapping("/list")
    public R<Page<StrategyCardVO>> list(@RequestParam(required = false) String category,
                                        @RequestParam(required = false) Long gameId,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(defaultValue = "new") String sort,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(strategyService.pageList(category, gameId, keyword, sort, page, size));
    }

    @GetMapping("/{id}")
    public R<StrategyDetailVO> detail(@PathVariable Long id) {
        return R.ok(strategyService.detail(id));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody StrategyCreateDTO dto) {
        return R.ok(strategyService.create(dto));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StrategyUpdateDTO dto) {
        strategyService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        strategyService.delete(id);
        return R.ok();
    }

    @GetMapping("/my")
    public R<Page<StrategyCardVO>> my(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(strategyService.myList(page, size));
    }

    @PostMapping("/{id}/like")
    public R<LikeResultVO> like(@PathVariable Long id) {
        return R.ok(strategyService.toggleLike(id));
    }

    @GetMapping("/{id}/comments")
    public R<Page<CommentVO>> comments(@PathVariable Long id,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size) {
        return R.ok(commentService.pageComments(id, page, size));
    }

    @PostMapping("/{id}/comments")
    public R<CommentVO> addComment(@PathVariable Long id, @Valid @RequestBody CommentCreateDTO dto) {
        return R.ok(commentService.addComment(id, dto));
    }
}
