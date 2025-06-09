package org.example.codereview.service.openai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.codereview.config.ReviewPromptConfig;
import org.example.codereview.service.openai.model.ChatCompletionRequest;
import org.example.codereview.service.openai.model.ChatCompletionResponse;
import org.example.codereview.service.openai.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {
    
    private final WebClient openAiWebClient;
    private final ReviewPromptConfig promptConfig;

    @Value("${openai.api.model}")
    private String model;
    
    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;

    private static final int MAX_CONTENT_LENGTH = 12000;

    public Mono<String> analyzeCode(String codeContent) {
        String truncatedContent = truncateContent(codeContent);
        log.debug("Analyzing code, original length: {}, truncated length: {}",
                 codeContent.length(), truncatedContent.length());

        return openAiWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest(truncatedContent))
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .map(response -> response.getChoices().get(0).getMessage().getContent())
                .doOnSuccess(review -> log.info("Successfully generated code review"))
                .doOnError(error -> log.error("Error calling OpenAI API: {}", error.getMessage()));
    }

    private ChatCompletionRequest createRequest(String codeContent) {
        return new ChatCompletionRequest(
            model,
            List.of(
                new Message("system", promptConfig.getSystemMessage()),
                new Message("user", codeContent)
            ),
            maxTokens
        );
    }

    private String truncateContent(String content) {
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }

        int truncatePoint = content.lastIndexOf("\n", MAX_CONTENT_LENGTH);
        return content.substring(0, truncatePoint == -1 ? MAX_CONTENT_LENGTH : truncatePoint)
               + "\n\n[Conteúdo truncado devido a limitações de tamanho...]";
    }
}