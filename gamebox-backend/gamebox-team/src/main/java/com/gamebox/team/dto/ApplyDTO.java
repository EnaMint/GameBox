package com.gamebox.team.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplyDTO {

    @Size(max = 500, message = "留言不能超过500字")
    private String message;
}
