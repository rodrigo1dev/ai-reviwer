package org.example.codereview.model.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PullRequestWebhookRequest {
    private String action;
    @JsonProperty("pull_request")
    private PullRequestData pullRequest;
    private Repository repository;

    @Data
    public static class PullRequestData {
        private long number;
    }

    @Data
    public static class Repository {
        private String name;
        private Owner owner;
    }

    @Data
    public static class Owner {
        private String login;
    }
}
