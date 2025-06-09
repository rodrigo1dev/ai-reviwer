package org.example.codereview.model;

import lombok.Data;

@Data
public class PullRequest {
    private Long number;
    private String title;
    private String body;
    private String diffUrl;
    private User user;
}