package com.gamebox.user.service;

import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.user.dto.UserUpdateDTO;
import com.gamebox.user.vo.UserVO;

import java.util.List;

public interface UserService {

    UserVO me();

    UserVO getById(Long id);

    UserVO updateMe(UserUpdateDTO dto);

    List<UserBriefVO> batch(List<Long> ids);
}
