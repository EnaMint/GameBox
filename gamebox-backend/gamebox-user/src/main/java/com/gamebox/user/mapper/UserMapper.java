package com.gamebox.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE t_user SET follow_count = follow_count + 1 WHERE id = #{userId}")
    int incrFollowCount(@Param("userId") Long userId);

    @Update("UPDATE t_user SET fans_count = fans_count + 1 WHERE id = #{userId}")
    int incrFansCount(@Param("userId") Long userId);

    @Update("UPDATE t_user SET follow_count = GREATEST(follow_count - 1, 0) WHERE id = #{userId}")
    int decrFollowCount(@Param("userId") Long userId);

    @Update("UPDATE t_user SET fans_count = GREATEST(fans_count - 1, 0) WHERE id = #{userId}")
    int decrFansCount(@Param("userId") Long userId);
}
