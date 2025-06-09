
package org.example.codereview.service.openai.model;

import lombok.Data;
import java.util.List;

@Data
public class ChatCompletionResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }
}