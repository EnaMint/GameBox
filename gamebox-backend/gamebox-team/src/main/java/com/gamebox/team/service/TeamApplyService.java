package com.gamebox.team.service;

import com.gamebox.team.dto.ApplyAuditDTO;
import com.gamebox.team.dto.ApplyDTO;
import com.gamebox.team.vo.ApplyVO;

import java.util.List;

public interface TeamApplyService {

    void apply(Long postId, ApplyDTO dto);

    void withdraw(Long postId);

    List<ApplyVO> applicationsOfPost(Long postId);

    void audit(Long applicationId, ApplyAuditDTO dto);

    List<ApplyVO> myApplications();
}
