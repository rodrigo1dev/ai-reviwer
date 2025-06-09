package org.example.codereview.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.codereview.model.webhook.PullRequestWebhookRequest;
import org.example.codereview.service.github.GitHubService;
import org.example.codereview.service.openai.OpenAIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private static final Set<String> SUPPORTED_ACTIONS = Set.of("opened", "synchronize", "reopened");

    private final GitHubService githubService;
    private final OpenAIService openAIService;

    @PostMapping
    public Mono<ResponseEntity<String>> reviewPullRequest(
            @RequestParam String owner,
            @RequestParam String repo,
            @RequestParam Integer pullNumber) {

        log.info("Starting review for PR #{} in {}/{}", pullNumber, owner, repo);

        return githubService.getPullRequestDiff(owner, repo, pullNumber)
                .flatMap(diff -> openAIService.analyzeCode(diff)
                        .flatMap(review -> githubService.submitReview(owner, repo, pullNumber, review)
                                .thenReturn(ResponseEntity.ok(review)))
                )
                .doOnSuccess(response -> log.info("Review submitted for PR #{}", pullNumber))
                .doOnError(error -> log.error("Review failed for PR #{}: {}", pullNumber, error.getMessage()))
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @PostMapping("/webhook")
    public Mono<ResponseEntity<Void>> handleWebhook(@RequestBody PullRequestWebhookRequest webhookRequest) {
        String action = webhookRequest.getAction();
        log.info("Received webhook event: {}", action);

        if (!SUPPORTED_ACTIONS.contains(action)) {
            log.info("Ignoring unsupported action: {}", action);
            return Mono.just(ResponseEntity.ok().build());
        }

        var pullRequest = webhookRequest.getPullRequest();
        var repo = webhookRequest.getRepository();

        return reviewPullRequest(
                repo.getOwner().getLogin(),
                repo.getName(),
                (int) pullRequest.getNumber()
        ).thenReturn(ResponseEntity.ok().build());
    }
}