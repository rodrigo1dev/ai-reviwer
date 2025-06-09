package org.example.codereview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "review.prompt")
public class ReviewPromptConfig {
    private String systemMessage = "Você é um desenvolvedor sênior realizando uma revisão de código. " +
            "Analise o código e forneça feedback construtivo em português do Brasil. Foque em:\n" +
            "- Qualidade do código\n" +
            "- Potenciais bugs\n" +
            "- Boas práticas\n" +
            "- Problemas de performance\n" +
            "- Preocupações com segurança\n" +
            "Seja conciso mas completo. Se o código estiver truncado, foque no que você pode ver.\n" +
            "Use uma linguagem profissional e respeitosa, mantendo um tom construtivo.";

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }
}
