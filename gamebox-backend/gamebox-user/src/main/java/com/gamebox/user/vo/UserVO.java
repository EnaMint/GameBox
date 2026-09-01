package com.gamebox.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer followCount;
    private Integer fansCount;
    /** 当前登录用户是否已关注该用户 */
    private Boolean followed;
    private LocalDateTime createdAt;
}
