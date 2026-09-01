package com.gamebox.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGameVO {

    private Long id;
    private Long gameId;
    private String gameName;
    private String gameCover;
    private String genre;
    private Integer status;
    private BigDecimal playHours;
    private Integer rating;
    private String remark;
    private LocalDateTime createdAt;
}
