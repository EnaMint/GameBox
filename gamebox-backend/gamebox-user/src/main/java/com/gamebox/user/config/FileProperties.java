package com.gamebox.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "gamebox.upload")
public class FileProperties {

    private String dir;
    private String urlPrefix;
}
