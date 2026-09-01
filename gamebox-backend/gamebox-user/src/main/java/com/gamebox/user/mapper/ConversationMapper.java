package com.gamebox.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.user.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    @Select("SELECT * FROM t_conversation WHERE user_a = #{a} AND user_b = #{b} LIMIT 1")
    Conversation selectByPair(@Param("a") Long a, @Param("b") Long b);

    @Select("SELECT * FROM t_conversation WHERE user_a = #{userId} OR user_b = #{userId} ORDER BY last_at DESC, id DESC")
    List<Conversation> selectByUser(@Param("userId") Long userId);

    @Update("UPDATE t_conversation SET last_message = #{lastMessage}, last_at = NOW() WHERE id = #{id}")
    int updateLast(@Param("id") Long id, @Param("lastMessage") String lastMessage);
}
