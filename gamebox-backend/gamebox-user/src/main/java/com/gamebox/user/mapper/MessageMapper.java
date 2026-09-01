package com.gamebox.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamebox.user.entity.Message;
import com.gamebox.user.vo.UnreadStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT COUNT(*) FROM t_message WHERE conversation_id = #{conversationId} AND from_user_id = #{fromUserId}")
    long countBySender(@Param("conversationId") Long conversationId, @Param("fromUserId") Long fromUserId);

    @Update("UPDATE t_message SET read_flag = 1 WHERE conversation_id = #{conversationId} AND to_user_id = #{userId} AND read_flag = 0")
    int markRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM t_message WHERE to_user_id = #{userId} AND read_flag = 0")
    long unreadCount(@Param("userId") Long userId);

    @Select("SELECT conversation_id AS conversationId, COUNT(*) AS cnt FROM t_message " +
            "WHERE to_user_id = #{userId} AND read_flag = 0 GROUP BY conversation_id")
    List<UnreadStat> unreadStats(@Param("userId") Long userId);

    @Select("<script>SELECT DISTINCT conversation_id FROM t_message WHERE to_user_id = #{userId} " +
            "AND conversation_id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<Long> conversationsWithReply(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
