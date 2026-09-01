package com.gamebox.strategy.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StrategyDetailVO extends StrategyCardVO {

    private Long gameId;

    private String content;
}
