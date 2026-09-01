package com.gamebox.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.user.entity.Game;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GameMapper extends BaseMapper<Game> {
}
