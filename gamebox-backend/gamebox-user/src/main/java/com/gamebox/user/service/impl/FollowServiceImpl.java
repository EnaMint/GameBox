package com.gamebox.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.user.entity.Follow;
import com.gamebox.user.entity.User;
import com.gamebox.user.mapper.FollowMapper;
import com.gamebox.user.mapper.UserMapper;
import com.gamebox.user.service.FollowService;
import com.gamebox.user.vo.FollowUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void follow(Long targetId) {
        Long userId = requireUserId();
        if (userId.equals(targetId)) {
            throw BizException.of("不能关注自己");
        }
        User target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (followMapper.selectFollow(userId, targetId) != null) {
            throw BizException.of("已经关注过该用户");
        }
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowUserId(targetId);
        try {
            followMapper.insert(follow);
        } catch (DuplicateKeyException e) {
            throw BizException.of("已经关注过该用户");
        }
        userMapper.incrFollowCount(userId);
        userMapper.incrFansCount(targetId);
    }

    @Override
    @Transactional
    public void unfollow(Long targetId) {
        Long userId = requireUserId();
        int deleted = followMapper.delete(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, targetId));
        if (deleted > 0) {
            userMapper.decrFollowCount(userId);
            userMapper.decrFansCount(targetId);
        }
    }

    @Override
    public boolean isFollowing(Long targetId) {
        Long userId = requireUserId();
        return followMapper.selectFollow(userId, targetId) != null;
    }

    @Override
    public Page<FollowUserVO> followingList(Long userId, long page, long size) {
        Page<Follow> p = followMapper.selectPage(new Page<>(page, size),
                Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getUserId, userId)
                        .orderByDesc(Follow::getId));
        return buildVOPage(p, p.getRecords().stream().map(Follow::getFollowUserId).toList());
    }

    @Override
    public Page<FollowUserVO> fansList(Long userId, long page, long size) {
        Page<Follow> p = followMapper.selectPage(new Page<>(page, size),
                Wrappers.<Follow>lambdaQuery()
                        .eq(Follow::getFollowUserId, userId)
                        .orderByDesc(Follow::getId));
        return buildVOPage(p, p.getRecords().stream().map(Follow::getUserId).toList());
    }

    private Page<FollowUserVO> buildVOPage(Page<Follow> p, List<Long> ids) {
        Page<FollowUserVO> out = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        if (ids.isEmpty()) {
            out.setRecords(List.of());
            return out;
        }
        Map<Long, User> userMap = userMapper.selectByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Long viewer = UserHolder.getUserId();
        Set<Long> followedSet = viewer == null ? Set.of()
                : new HashSet<>(followMapper.selectFollowedTargets(viewer, ids));
        List<FollowUserVO> records = ids.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .map(u -> FollowUserVO.builder()
                        .id(u.getId())
                        .nickname(u.getNickname())
                        .avatar(u.getAvatar())
                        .bio(u.getBio())
                        .followed(followedSet.contains(u.getId()))
                        .build())
                .toList();
        out.setRecords(records);
        return out;
    }

    private Long requireUserId() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }
}
