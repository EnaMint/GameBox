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
public class TeamCardVO {

    private Long id;
    private String title;
    private Integer memberLimit;
    private Integer memberCount;
    private Integer needVoice;
    private String playTime;
    private Integer status;
    private LocalDateTime createdAt;
    private GameBriefVO game;
    private UserBriefVO leader;
}
