package com.gamebox.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
