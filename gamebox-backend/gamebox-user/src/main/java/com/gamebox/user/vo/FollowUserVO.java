package com.gamebox.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUserVO {

    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    /** 当前浏览者是否已关注该用户 */
    private Boolean followed;
}
