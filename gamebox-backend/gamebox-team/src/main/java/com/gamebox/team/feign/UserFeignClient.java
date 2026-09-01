package com.gamebox.team.feign;

import com.gamebox.common.result.R;
import com.gamebox.common.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "gamebox-user", contextId = "userFeignClient", path = "/user")
public interface UserFeignClient {

    @GetMapping("/inner/batch")
    R<List<UserBriefVO>> batch(@RequestParam("ids") String ids);
}
