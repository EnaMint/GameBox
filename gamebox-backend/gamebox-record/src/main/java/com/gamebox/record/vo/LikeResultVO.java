package com.gamebox.record.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResultVO {

    private Boolean liked;

    private Integer likeCount;
}
