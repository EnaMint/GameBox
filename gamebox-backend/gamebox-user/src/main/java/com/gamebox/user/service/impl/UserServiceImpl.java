package com.gamebox.user.service.impl;

import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.security.UserHolder;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.user.dto.UserUpdateDTO;
import com.gamebox.user.entity.User;
import com.gamebox.user.mapper.FollowMapper;
import com.gamebox.user.mapper.UserMapper;
import com.gamebox.user.service.UserService;
import com.gamebox.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FollowMapper followMapper;

    @Override
    public UserVO me() {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        UserVO vo = toVO(user);
        Long viewerId = UserHolder.getUserId();
        vo.setFollowed(viewerId != null && !viewerId.equals(id)
                && followMapper.selectFollow(viewerId, id) != null);
        return vo;
    }

    @Override
    public UserVO updateMe(UserUpdateDTO dto) {
        Long userId = requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : user.getNickname());
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public List<UserBriefVO> batch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.selectByIds(ids).stream()
                .map(u -> UserBriefVO.builder()
                        .id(u.getId())
                        .nickname(u.getNickname())
                        .avatar(u.getAvatar())
                        .build())
                .toList();
    }

    private Long requireUserId() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private UserVO toVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .followCount(user.getFollowCount())
                .fansCount(user.getFansCount())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
