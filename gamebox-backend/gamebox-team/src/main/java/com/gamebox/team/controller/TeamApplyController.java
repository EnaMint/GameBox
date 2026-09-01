package com.gamebox.team.controller;

import com.gamebox.common.result.R;
import com.gamebox.team.dto.ApplyAuditDTO;
import com.gamebox.team.dto.ApplyDTO;
import com.gamebox.team.service.TeamApplyService;
import com.gamebox.team.vo.ApplyVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamApplyController {

    private final TeamApplyService teamApplyService;

    @GetMapping("/applications/mine")
    public R<List<ApplyVO>> myApplications() {
        return R.ok(teamApplyService.myApplications());
    }

    @PostMapping("/{id}/apply")
    public R<Void> apply(@PathVariable Long id, @Valid @RequestBody(required = false) ApplyDTO dto) {
        teamApplyService.apply(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/apply")
    public R<Void> withdraw(@PathVariable Long id) {
        teamApplyService.withdraw(id);
        return R.ok();
    }

    @GetMapping("/{id}/applications")
    public R<List<ApplyVO>> applications(@PathVariable Long id) {
        return R.ok(teamApplyService.applicationsOfPost(id));
    }

    @PutMapping("/application/{id}")
    public R<Void> audit(@PathVariable Long id, @Valid @RequestBody ApplyAuditDTO dto) {
        teamApplyService.audit(id, dto);
        return R.ok();
    }
}
