package com.gamebox.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ApplyAuditDTO {

    @NotBlank(message = "操作不能为空")
    @Pattern(regexp = "approve|reject", message = "操作仅支持 approve 或 reject")
    private String action;
}
