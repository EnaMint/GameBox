package com.gamebox.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.result.R;
import com.gamebox.user.dto.MessageSendDTO;
import com.gamebox.user.service.MessageService;
import com.gamebox.user.vo.ConversationVO;
import com.gamebox.user.vo.MessageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    public R<List<ConversationVO>> conversations() {
        return R.ok(messageService.conversations());
    }

    @GetMapping("/with/{peerId}")
    public R<Page<MessageVO>> chat(@PathVariable Long peerId,
                                   @RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "50") long size) {
        return R.ok(messageService.chat(peerId, page, size));
    }

    @PostMapping("/send")
    public R<MessageVO> send(@Valid @RequestBody MessageSendDTO dto) {
        return R.ok(messageService.send(dto));
    }

    @GetMapping("/unread/count")
    public R<Map<String, Long>> unreadCount() {
        return R.ok(Map.of("count", messageService.unreadCount()));
    }
}
