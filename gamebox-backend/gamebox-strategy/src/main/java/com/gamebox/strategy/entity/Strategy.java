package com.gamebox.strategy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_strategy")
public class Strategy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long gameId;

    private String title;

    private String category;

    private String cover;

    private String summary;

    private String content;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
