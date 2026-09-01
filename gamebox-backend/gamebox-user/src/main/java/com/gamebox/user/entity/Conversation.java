package com.gamebox.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_conversation")
public class Conversation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userA;
    private Long userB;
    private String lastMessage;
    private LocalDateTime lastAt;
    private LocalDateTime createdAt;
}
