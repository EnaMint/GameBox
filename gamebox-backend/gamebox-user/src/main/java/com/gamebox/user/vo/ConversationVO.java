package com.gamebox.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    private Long peerId;
    private String peerNickname;
    private String peerAvatar;
    private String lastMessage;
    private LocalDateTime lastAt;
    private Long unread;
    /** 对方是否已向我发过消息（回复后即解除2条限制） */
    private Boolean peerReplied;
}
