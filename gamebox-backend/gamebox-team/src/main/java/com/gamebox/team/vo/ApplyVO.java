package com.gamebox.team.vo;

import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyVO {

    private Long id;
    private String message;
    private Integer status;
    private LocalDateTime createdAt;
    private UserBriefVO user;
    private Long postId;
    private String postTitle;
    private Integer postStatus;
    private GameBriefVO game;
}
