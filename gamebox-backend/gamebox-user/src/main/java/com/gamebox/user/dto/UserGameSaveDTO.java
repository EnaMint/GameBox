package com.gamebox.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserGameSaveDTO {

    private Long gameId;

    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态取值1-4")
    @Max(value = 4, message = "状态取值1-4")
    private Integer status;

    @Min(value = 0, message = "游戏时长不能为负")
    private BigDecimal playHours;

    @Min(value = 0, message = "评分取值0-5")
    @Max(value = 5, message = "评分取值0-5")
    private Integer rating;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
