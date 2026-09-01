package com.gamebox.strategy.vo;

import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StrategyCardVO {

    private Long id;

    private String title;

    private String category;

    private String cover;

    private String summary;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private LocalDateTime createdAt;

    private UserBriefVO author;

    private GameBriefVO game;

    private Boolean liked;
}
