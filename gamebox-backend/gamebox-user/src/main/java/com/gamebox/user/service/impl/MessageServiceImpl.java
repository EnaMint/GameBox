package com.gamebox.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.user.dto.MessageSendDTO;
import com.gamebox.user.entity.Conversation;
import com.gamebox.user.entity.Message;
import com.gamebox.user.entity.User;
import com.gamebox.user.mapper.ConversationMapper;
import com.gamebox.user.mapper.MessageMapper;
import com.gamebox.user.mapper.UserMapper;
import com.gamebox.user.service.MessageService;
import com.gamebox.user.vo.ConversationVO;
import com.gamebox.user.vo.MessageVO;
import com.gamebox.user.vo.UnreadStat;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    /** 对方回复前，单方最多可发送的消息条数 */
    private static final long MAX_MESSAGES_BEFORE_REPLY = 2;
    private static final int LAST_MESSAGE_MAX_LEN = 100;

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public List<ConversationVO> conversations() {
        Long userId = requireUserId();
        List<Conversation> convs = conversationMapper.selectByUser(userId);
        if (convs.isEmpty()) {
            return List.of();
        }
        List<Long> peerIds = convs.stream().map(c -> peerOf(c, userId)).distinct().toList();
        Map<Long, User> userMap = userMapper.selectByIds(peerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Long> unreadMap = messageMapper.unreadStats(userId).stream()
                .collect(Collectors.toMap(UnreadStat::getConversationId, UnreadStat::getCnt));
        Set<Long> repliedConvs = new HashSet<>(messageMapper.conversationsWithReply(userId,
                convs.stream().map(Conversation::getId).toList()));
        return convs.stream().map(c -> {
            Long peerId = peerOf(c, userId);
            User peer = userMap.get(peerId);
            return ConversationVO.builder()
                    .peerId(peerId)
                    .peerNickname(peer == null ? "未知用户" : peer.getNickname())
                    .peerAvatar(peer == null ? "" : peer.getAvatar())
                    .lastMessage(c.getLastMessage())
                    .lastAt(c.getLastAt())
                    .unread(unreadMap.getOrDefault(c.getId(), 0L))
                    .peerReplied(repliedConvs.contains(c.getId()))
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public Page<MessageVO> chat(Long peerId, long page, long size) {
        Long userId = requireUserId();
        Page<MessageVO> out = new Page<>(page, size);
        Conversation conv = findConversation(userId, peerId);
        if (conv == null) {
            out.setTotal(0);
            out.setRecords(List.of());
            return out;
        }
        messageMapper.markRead(conv.getId(), userId);
        Page<Message> p = messageMapper.selectPage(new Page<>(page, size),
                Wrappers.<Message>lambdaQuery()
                        .eq(Message::getConversationId, conv.getId())
                        .orderByDesc(Message::getId));
        List<MessageVO> records = new ArrayList<>(p.getRecords().size());
        for (int i = p.getRecords().size() - 1; i >= 0; i--) {
            records.add(toVO(p.getRecords().get(i)));
        }
        out.setTotal(p.getTotal());
        out.setRecords(records);
        return out;
    }

    @Override
    @Transactional
    public MessageVO send(MessageSendDTO dto) {
        Long userId = requireUserId();
        Long toUserId = dto.getToUserId();
        if (userId.equals(toUserId)) {
            throw BizException.of("不能给自己发私信");
        }
        User peer = userMapper.selectById(toUserId);
        if (peer == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        String content = dto.getContent().trim();
        if (content.isEmpty()) {
            throw BizException.of("消息内容不能为空");
        }
        Conversation conv = getOrCreateConversation(userId, toUserId);
        boolean peerReplied = messageMapper.countBySender(conv.getId(), toUserId) > 0;
        if (!peerReplied && messageMapper.countBySender(conv.getId(), userId) >= MAX_MESSAGES_BEFORE_REPLY) {
            throw BizException.of("对方回复前最多只能发送 2 条私信");
        }
        Message message = new Message();
        message.setConversationId(conv.getId());
        message.setFromUserId(userId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setReadFlag(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        conversationMapper.updateLast(conv.getId(), abbreviate(content));
        return toVO(message);
    }

    @Override
    public long unreadCount() {
        Long userId = requireUserId();
        return messageMapper.unreadCount(userId);
    }

    private Conversation findConversation(Long u1, Long u2) {
        return conversationMapper.selectByPair(Math.min(u1, u2), Math.max(u1, u2));
    }

    private Conversation getOrCreateConversation(Long u1, Long u2) {
        Conversation conv = findConversation(u1, u2);
        if (conv != null) {
            return conv;
        }
        conv = new Conversation();
        conv.setUserA(Math.min(u1, u2));
        conv.setUserB(Math.max(u1, u2));
        conv.setLastMessage("");
        conv.setLastAt(LocalDateTime.now());
        try {
            conversationMapper.insert(conv);
        } catch (DuplicateKeyException e) {
            conv = findConversation(u1, u2);
        }
        return conv;
    }

    private Long peerOf(Conversation c, Long userId) {
        return c.getUserA().equals(userId) ? c.getUserB() : c.getUserA();
    }

    private String abbreviate(String content) {
        String oneLine = content.replaceAll("\\s+", " ");
        return oneLine.length() <= LAST_MESSAGE_MAX_LEN ? oneLine
                : oneLine.substring(0, LAST_MESSAGE_MAX_LEN) + "…";
    }

    private MessageVO toVO(Message m) {
        return MessageVO.builder()
                .id(m.getId())
                .fromUserId(m.getFromUserId())
                .toUserId(m.getToUserId())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private Long requireUserId() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
