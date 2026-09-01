package com.gamebox.strategy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StrategyCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @NotBlank(message = "分类不能为空")
    @Size(max = 20, message = "分类不能超过20个字符")
    private String category;

    private Long gameId;

    private String cover;

    @Size(max = 300, message = "摘要不能超过300个字符")
    private String summary;

    @NotBlank(message = "正文不能为空")
    private String content;
}
