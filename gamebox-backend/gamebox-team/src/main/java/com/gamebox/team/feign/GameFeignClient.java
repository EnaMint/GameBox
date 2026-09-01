package com.gamebox.team.feign;

import com.gamebox.common.result.R;
import com.gamebox.common.vo.GameBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "gamebox-user", contextId = "gameFeignClient", path = "/game")
public interface GameFeignClient {

    @GetMapping("/inner/batch")
    R<List<GameBriefVO>> batch(@RequestParam("ids") String ids);
}
