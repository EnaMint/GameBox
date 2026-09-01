package com.gamebox.record.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RecordCreateDTO {

    private Long gameId;

    @Size(max = 1000, message = "内容不能超过1000字")
    private String content;

    private List<String> images;
}
