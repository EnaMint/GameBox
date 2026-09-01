package com.gamebox.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_game")
public class Game {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String cover;
    private String genre;
    private String platform;
    private String description;
    private LocalDateTime createdAt;
}
