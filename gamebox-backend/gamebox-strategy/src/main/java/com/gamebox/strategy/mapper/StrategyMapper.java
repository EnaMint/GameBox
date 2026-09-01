package com.gamebox.strategy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.strategy.entity.Strategy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StrategyMapper extends BaseMapper<Strategy> {

    @Update("UPDATE t_strategy SET view_count = view_count + 1 WHERE id = #{id} AND status = 1")
    int incrementViewCount(@Param("id") Long id);
}
