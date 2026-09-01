package com.gamebox.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    @Size(max = 32, message = "昵称不能超过32个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Size(max = 200, message = "个人简介不能超过200个字符")
    private String bio;
}
