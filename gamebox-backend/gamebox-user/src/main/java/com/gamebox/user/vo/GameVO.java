package com.gamebox.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameVO {

    private Long id;
    private String name;
    private String cover;
    private String genre;
    private String tags;
    private String platform;
    private String description;
}
