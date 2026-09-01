package com.gamebox.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.user.dto.MessageSendDTO;
import com.gamebox.user.vo.ConversationVO;
import com.gamebox.user.vo.MessageVO;

import java.util.List;

public interface MessageService {

    List<ConversationVO> conversations();

    Page<MessageVO> chat(Long peerId, long page, long size);

    MessageVO send(MessageSendDTO dto);

    long unreadCount();
}
