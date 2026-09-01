package com.gamebox.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.user.vo.FollowUserVO;

public interface FollowService {

    void follow(Long targetId);

    void unfollow(Long targetId);

    boolean isFollowing(Long targetId);

    Page<FollowUserVO> followingList(Long userId, long page, long size);

    Page<FollowUserVO> fansList(Long userId, long page, long size);
}
