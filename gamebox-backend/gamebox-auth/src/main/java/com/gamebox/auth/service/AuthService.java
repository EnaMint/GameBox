package com.gamebox.auth.service;

import com.gamebox.auth.dto.LoginDTO;
import com.gamebox.auth.dto.RegisterDTO;
import com.gamebox.auth.vo.LoginVO;

public interface AuthService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);
}
