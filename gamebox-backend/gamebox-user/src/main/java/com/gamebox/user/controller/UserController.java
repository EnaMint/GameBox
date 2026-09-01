package com.gamebox.user.controller;

import com.gamebox.common.exception.BizException;
import com.gamebox.common.result.R;
import com.gamebox.common.vo.UserBriefVO;
import com.gamebox.user.dto.UserUpdateDTO;
import com.gamebox.user.service.UserService;
import com.gamebox.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public R<UserVO> me() {
        return R.ok(userService.me());
    }

    @GetMapping("/{id}")
    public R<UserVO> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @PutMapping("/me")
    public R<UserVO> updateMe(@Valid @RequestBody UserUpdateDTO dto) {
        return R.ok(userService.updateMe(dto));
    }

    @GetMapping("/inner/batch")
    public R<List<UserBriefVO>> batch(@RequestParam(required = false) String ids) {
        return R.ok(userService.batch(parseIds(ids)));
    }

    private List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }
        List<Long> idList;
        try {
            idList = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .distinct()
                    .toList();
        } catch (NumberFormatException e) {
            throw BizException.of("ids格式错误");
        }
        if (idList.size() > 50) {
            throw BizException.of("一次最多查询50个");
        }
        return idList;
    }
}
