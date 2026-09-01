package com.gamebox.team.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.result.R;
import com.gamebox.team.dto.StatusUpdateDTO;
import com.gamebox.team.dto.TeamCreateDTO;
import com.gamebox.team.service.TeamPostService;
import com.gamebox.team.vo.TeamCardVO;
import com.gamebox.team.vo.TeamDetailVO;
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
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamPostService teamPostService;

    @GetMapping("/list")
    public R<Page<TeamCardVO>> list(@RequestParam(required = false) Long gameId,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(teamPostService.list(gameId, status, page, size));
    }

    @GetMapping("/{id}")
    public R<TeamDetailVO> detail(@PathVariable Long id) {
        return R.ok(teamPostService.detail(id));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody TeamCreateDTO dto) {
        return R.ok(teamPostService.create(dto));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        teamPostService.updateStatus(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        teamPostService.delete(id);
        return R.ok();
    }

    @GetMapping("/my")
    public R<Page<TeamCardVO>> my(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(teamPostService.my(page, size));
    }
}
