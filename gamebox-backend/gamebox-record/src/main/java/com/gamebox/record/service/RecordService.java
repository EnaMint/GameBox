package com.gamebox.record.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.record.dto.RecordCreateDTO;
import com.gamebox.record.vo.LikeResultVO;
import com.gamebox.record.vo.RecordVO;

public interface RecordService {

    Page<RecordVO> feed(Integer page, Integer size);

    Page<RecordVO> my(Integer page, Integer size);

    Page<RecordVO> userRecords(Long userId, Integer page, Integer size);

    Long create(RecordCreateDTO dto);

    void delete(Long id);

    LikeResultVO toggleLike(Long id);
}
