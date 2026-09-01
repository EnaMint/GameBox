package com.gamebox.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_user_game")
public class UserGame {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long gameId;
    private Integer status;
    private BigDecimal playHours;
    private Integer rating;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
