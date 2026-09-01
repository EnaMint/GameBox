package com.gamebox.team.assembler;

import com.gamebox.common.result.R;
import com.gamebox.common.result.ResultCode;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.team.feign.GameFeignClient;
import com.gamebox.team.feign.UserFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamAssembler {

    private final UserFeignClient userFeignClient;
    private final GameFeignClient gameFeignClient;

    public Map<Long, UserBriefVO> userMap(Collection<Long> ids) {
        String idStr = joinIds(ids);
        if (idStr.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            R<List<UserBriefVO>> r = userFeignClient.batch(idStr);
            if (r != null && ResultCode.SUCCESS.getCode().equals(r.getCode()) && r.getData() != null) {
                return r.getData().stream()
                        .filter(u -> u != null && u.getId() != null)
                        .collect(Collectors.toMap(UserBriefVO::getId, Function.identity(), (a, b) -> a));
            }
            log.warn("批量查询用户信息失败, ids={}", idStr);
        } catch (Exception e) {
            log.warn("批量查询用户信息异常，降级为空结果, ids={}, msg={}", idStr, e.getMessage());
        }
        return Collections.emptyMap();
    }

    public Map<Long, GameBriefVO> gameMap(Collection<Long> ids) {
        String idStr = joinIds(ids);
        if (idStr.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            R<List<GameBriefVO>> r = gameFeignClient.batch(idStr);
            if (r != null && ResultCode.SUCCESS.getCode().equals(r.getCode()) && r.getData() != null) {
                return r.getData().stream()
                        .filter(g -> g != null && g.getId() != null)
                        .collect(Collectors.toMap(GameBriefVO::getId, Function.identity(), (a, b) -> a));
            }
            log.warn("批量查询游戏信息失败, ids={}", idStr);
        } catch (Exception e) {
            log.warn("批量查询游戏信息异常，降级为空结果, ids={}, msg={}", idStr, e.getMessage());
        }
        return Collections.emptyMap();
    }

    private String joinIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
