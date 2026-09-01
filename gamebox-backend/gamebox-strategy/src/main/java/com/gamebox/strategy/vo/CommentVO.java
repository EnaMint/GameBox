package com.gamebox.strategy.vo;

import com.gamebox.common.vo.UserBriefVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;

    private String content;

    private LocalDateTime createdAt;

    private UserBriefVO user;
}
