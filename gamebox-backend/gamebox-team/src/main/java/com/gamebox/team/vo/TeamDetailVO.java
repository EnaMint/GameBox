package com.gamebox.team.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamDetailVO extends TeamCardVO {

    private String content;
    private Integer myApplication;
}
