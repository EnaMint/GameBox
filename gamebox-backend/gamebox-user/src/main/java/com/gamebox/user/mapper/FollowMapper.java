package com.gamebox.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.user.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    @Select("SELECT id FROM t_follow WHERE user_id = #{userId} AND follow_user_id = #{followUserId} LIMIT 1")
    Long selectFollow(@Param("userId") Long userId, @Param("followUserId") Long followUserId);

    @Select("<script>SELECT follow_user_id FROM t_follow WHERE user_id = #{userId} " +
            "AND follow_user_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Long> selectFollowedTargets(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
