package com.gamebox.strategy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_strategy_comment")
public class StrategyComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long strategyId;

    private Long userId;

    private Long parentId;

    private String content;

    private LocalDateTime createdAt;
}
