package com.gamebox.team.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebox.team.dto.StatusUpdateDTO;
import com.gamebox.team.dto.TeamCreateDTO;
import com.gamebox.team.vo.TeamCardVO;
import com.gamebox.team.vo.TeamDetailVO;

public interface TeamPostService {

    Page<TeamCardVO> list(Long gameId, Integer status, Integer page, Integer size);

    TeamDetailVO detail(Long id);

    Long create(TeamCreateDTO dto);

    void updateStatus(Long id, StatusUpdateDTO dto);

    void delete(Long id);

    Page<TeamCardVO> my(Integer page, Integer size);
}
