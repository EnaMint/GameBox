package com.gamebox.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.result.R;
import com.gamebox.user.service.FollowService;
import com.gamebox.user.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/user/follow/{id}")
    public R<Void> follow(@PathVariable Long id) {
        followService.follow(id);
        return R.ok();
    }

    @DeleteMapping("/user/follow/{id}")
    public R<Void> unfollow(@PathVariable Long id) {
        followService.unfollow(id);
        return R.ok();
    }

    @GetMapping("/user/follow/check/{id}")
    public R<Map<String, Boolean>> check(@PathVariable Long id) {
        return R.ok(Map.of("followed", followService.isFollowing(id)));
    }

    @GetMapping("/user/{id}/follows")
    public R<Page<FollowUserVO>> following(@PathVariable Long id,
                                           @RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "20") long size) {
        return R.ok(followService.followingList(id, page, size));
    }

    @GetMapping("/user/{id}/fans")
    public R<Page<FollowUserVO>> fans(@PathVariable Long id,
                                      @RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size) {
        return R.ok(followService.fansList(id, page, size));
    }
}
