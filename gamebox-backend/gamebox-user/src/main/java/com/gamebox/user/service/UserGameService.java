package com.gamebox.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.user.dto.UserGameSaveDTO;
import com.gamebox.user.vo.UserGameVO;

public interface UserGameService {

    Page<UserGameVO> list(Integer status, Integer page, Integer size);

    UserGameVO check(Long gameId);

    void save(UserGameSaveDTO dto);

    void update(Long id, UserGameSaveDTO dto);

    void delete(Long id);
}
