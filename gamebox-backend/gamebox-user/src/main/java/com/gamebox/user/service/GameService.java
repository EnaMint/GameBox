package com.gamebox.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.user.vo.GameVO;

import java.util.List;

public interface GameService {

    Page<GameVO> list(String keyword, String genre, String tag, Integer page, Integer size);

    GameVO getById(Long id);

    List<GameBriefVO> batch(List<Long> ids);

    List<String> tags();
}
