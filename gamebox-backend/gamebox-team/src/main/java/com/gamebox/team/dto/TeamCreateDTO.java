package com.gamebox.team.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamCreateDTO {

    @NotNull(message = "请选择游戏")
    private Long gameId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100字")
    private String title;

    @Size(max = 2000, message = "内容不能超过2000字")
    private String content;

    @Min(value = 2, message = "队伍人数不能少于2人")
    @Max(value = 20, message = "队伍人数不能超过20人")
    private Integer memberLimit;

    @Min(value = 0, message = "语音参数无效")
    @Max(value = 1, message = "语音参数无效")
    private Integer needVoice;

    @Size(max = 64, message = "游戏时间不能超过64字")
    private String playTime;
}
