package com.gamebox.record.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.result.R;
import com.gamebox.record.dto.RecordCreateDTO;
import com.gamebox.record.service.RecordService;
import com.gamebox.record.vo.LikeResultVO;
import com.gamebox.record.vo.RecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping("/feed")
    public R<Page<RecordVO>> feed(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(recordService.feed(page, size));
    }

    @GetMapping("/my")
    public R<Page<RecordVO>> my(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(recordService.my(page, size));
    }

    @GetMapping("/user/{userId}")
    public R<Page<RecordVO>> userRecords(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(recordService.userRecords(userId, page, size));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody RecordCreateDTO dto) {
        return R.ok(recordService.create(dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        recordService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/like")
    public R<LikeResultVO> like(@PathVariable Long id) {
        return R.ok(recordService.toggleLike(id));
    }
}
