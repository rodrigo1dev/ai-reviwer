package org.example.codereview.service.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.codereview.service.github.model.InstallationToken;
import org.example.codereview.service.github.model.ReviewRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {
    
    private final WebClient.Builder webClientBuilder;
    private final GitHubAppAuthService authService;

    @Value("${github.app.installation-id}")
    private String installationId;

    @Value("${github.app.name}")
    private String appName;

    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String GITHUB_API_VERSION = "application/vnd.github.v3+json";

    public Mono<String> getPullRequestDiff(String owner, String repo, Integer pullNumber) {
        return withGitHubClient(client -> client
            .get()
            .uri("/repos/{owner}/{repo}/pulls/{pullNumber}", owner, repo, pullNumber)
            .header(HttpHeaders.ACCEPT, "application/vnd.github.v3.diff")
            .retrieve()
            .bodyToMono(String.class)
            .doOnSubscribe(s -> log.info("Getting diff for PR #{} in {}/{}", pullNumber, owner, repo))
            .doOnError(e -> log.error("Error getting PR diff: {}", e.getMessage())));
    }

    public Mono<Void> submitReview(String owner, String repo, Integer pullNumber, String reviewComment) {
        var review = formatReviewComment(reviewComment);
        var reviewRequest = new ReviewRequest(review, "COMMENT");

        return withGitHubClient(client -> client
            .post()
            .uri("/repos/{owner}/{repo}/pulls/{pullNumber}/reviews", owner, repo, pullNumber)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(reviewRequest)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSubscribe(s -> log.info("Submitting review for PR #{} in {}/{}", pullNumber, owner, repo))
            .doOnSuccess(v -> log.info("Successfully submitted review comment"))
            .doOnError(e -> log.error("Error submitting review: {}", e.getMessage())));
    }

    private <T> Mono<T> withGitHubClient(java.util.function.Function<WebClient, Mono<T>> operation) {
        return getInstallationToken()
            .map(this::createGitHubClient)
            .flatMap(operation);
    }

    private WebClient createGitHubClient(String token) {
        return webClientBuilder
            .baseUrl(GITHUB_API_URL)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .defaultHeader(HttpHeaders.ACCEPT, GITHUB_API_VERSION)
            .build();
    }

    private Mono<String> getInstallationToken() {
        String jwt = authService.createJWT();

        return webClientBuilder.build()
            .post()
            .uri(GITHUB_API_URL + "/app/installations/" + installationId + "/access_tokens")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .header(HttpHeaders.ACCEPT, GITHUB_API_VERSION)
            .retrieve()
            .bodyToMono(InstallationToken.class)
            .map(InstallationToken::getToken)
            .doOnError(e -> log.error("Error getting installation token: {}", e.getMessage()));
    }

    private String formatReviewComment(String comment) {
        return String.format("[%s] Code Review\n\n%s", appName, comment);
    }
}