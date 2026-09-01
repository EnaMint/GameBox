package com.gamebox.record.vo;

import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecordVO {

    private Long id;

    private List<String> images;

    private String content;

    private Integer likeCount;

    private LocalDateTime createdAt;

    private UserBriefVO user;

    private GameBriefVO game;

    private Boolean liked;
}
