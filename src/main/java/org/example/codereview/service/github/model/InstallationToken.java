package org.example.codereview.service.github.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstallationToken {
    @JsonProperty("token")
    private String token;

    @JsonProperty("expires_at")
    private String expiresAt;
}
