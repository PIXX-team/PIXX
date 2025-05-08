package com.ssafy.fourcut.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {
    private String domain;
    private String path;
    private String sameSite;
    private boolean httpOnly;
    private boolean secure;
}