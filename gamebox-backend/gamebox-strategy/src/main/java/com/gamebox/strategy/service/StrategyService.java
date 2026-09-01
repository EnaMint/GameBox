package com.gamebox.strategy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.strategy.dto.StrategyCreateDTO;
import com.gamebox.strategy.dto.StrategyUpdateDTO;
import com.gamebox.strategy.vo.LikeResultVO;
import com.gamebox.strategy.vo.StrategyCardVO;
import com.gamebox.strategy.vo.StrategyDetailVO;

public interface StrategyService {

    Page<StrategyCardVO> pageList(String category, Long gameId, String keyword, String sort, Integer page, Integer size);

    StrategyDetailVO detail(Long id);

    Long create(StrategyCreateDTO dto);

    void update(Long id, StrategyUpdateDTO dto);

    void delete(Long id);

    Page<StrategyCardVO> myList(Integer page, Integer size);

    LikeResultVO toggleLike(Long id);
}
