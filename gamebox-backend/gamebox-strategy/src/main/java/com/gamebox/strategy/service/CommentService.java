package com.gamebox.strategy.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.strategy.dto.CommentCreateDTO;
import com.gamebox.strategy.vo.CommentVO;

public interface CommentService {

    Page<CommentVO> pageComments(Long strategyId, Integer page, Integer size);

    CommentVO addComment(Long strategyId, CommentCreateDTO dto);

    void delete(Long id);
}
