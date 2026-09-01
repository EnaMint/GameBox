package com.gamebox.team.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_team_post")
public class TeamPost {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long gameId;
    private String title;
    private String content;
    private Integer memberLimit;
    private Integer memberCount;
    private Integer needVoice;
    private String playTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
