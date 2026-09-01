package com.gamebox.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.result.R;
import com.gamebox.user.dto.UserGameSaveDTO;
import com.gamebox.user.service.UserGameService;
import com.gamebox.user.vo.UserGameVO;
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
@RequestMapping("/ugame")
@RequiredArgsConstructor
public class UserGameController {

    private final UserGameService userGameService;

    @GetMapping("/list")
    public R<Page<UserGameVO>> list(@RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "12") Integer size) {
        return R.ok(userGameService.list(status, page, size));
    }

    @GetMapping("/check/{gameId}")
    public R<UserGameVO> check(@PathVariable Long gameId) {
        return R.ok(userGameService.check(gameId));
    }

    @PostMapping
    public R<Void> save(@Valid @RequestBody UserGameSaveDTO dto) {
        userGameService.save(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UserGameSaveDTO dto) {
        userGameService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userGameService.delete(id);
        return R.ok();
    }
}
