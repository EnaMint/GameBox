package com.gamebox.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.user.entity.UserGame;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserGameMapper extends BaseMapper<UserGame> {
}
