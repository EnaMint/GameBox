package com.gamebox.record.assembler;

import com.gamebox.common.result.R;
import com.gamebox.common.vo.GameBriefVO;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.record.feign.GameFeignClient;
import com.gamebox.record.feign.UserFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordAssembler {

    private final UserFeignClient userFeignClient;
    private final GameFeignClient gameFeignClient;

    public Map<Long, UserBriefVO> userMap(Collection<Long> ids) {
        List<Long> validIds = normalize(ids);
        if (validIds.isEmpty()) {
            return Map.of();
        }
        try {
            R<List<UserBriefVO>> result = userFeignClient.batch(joinIds(validIds));
            if (result == null || result.getData() == null) {
                return Map.of();
            }
            return result.getData().stream()
                    .filter(user -> user != null && user.getId() != null)
                    .collect(Collectors.toMap(UserBriefVO::getId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            log.warn("批量查询用户信息失败: {}", e.getMessage());
            return Map.of();
        }
    }

    public Map<Long, GameBriefVO> gameMap(Collection<Long> ids) {
        List<Long> validIds = normalize(ids);
        if (validIds.isEmpty()) {
            return Map.of();
        }
        try {
            R<List<GameBriefVO>> result = gameFeignClient.batch(joinIds(validIds));
            if (result == null || result.getData() == null) {
                return Map.of();
            }
            return result.getData().stream()
                    .filter(game -> game != null && game.getId() != null)
                    .collect(Collectors.toMap(GameBriefVO::getId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            log.warn("批量查询游戏信息失败: {}", e.getMessage());
            return Map.of();
        }
    }

    private List<Long> normalize(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }

    private String joinIds(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
