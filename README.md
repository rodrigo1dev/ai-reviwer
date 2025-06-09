# Code Review Agent

Uma API REST com Spring Boot que automatiza code reviews usando OpenAI e GitHub Apps. O sistema realiza análises automatizadas de código em pull requests, fornecendo feedback construtivo em português do Brasil.

## Requisitos

- Java 21+
- Maven
- Conta no GitHub
- Chave de API da OpenAI
- GitHub App configurado

## Configuração do GitHub App

1. Crie um novo GitHub App:
    - Acesse GitHub.com > Settings > Developer settings > GitHub Apps
    - Clique em "New GitHub App"
    - Preencha:
        - Nome do App (ex: "Code Review Bot")
        - Homepage URL (pode ser a URL do seu repositório)
        - Webhook URL (URL onde sua aplicação estará rodando + /api/review/webhook)
            - Exemplo: Se estiver usando ngrok, use o formato https://seu-dominio.ngrok.app/api/review/webhook
    - Em Permissions:
        - Repository permissions:
            - Pull requests: Read & Write (para ler PRs e postar comentários)
            - Contents: Read (para ler o código)
    - Em Subscribe to events:
        - Selecione "Pull requests"
    - Clique em "Create GitHub App"

2. Após criar o App:
    - Anote o App ID mostrado na página
    - Gere uma chave privada clicando em "Generate a private key"
    - Instale o App no(s) repositório(s) desejado(s)
    - Anote o Installation ID (número que aparece na URL após instalar o App)

## Configuração da Aplicação

1. Clone o repositório:
   ```bash
   git clone [url-do-repositorio]
   cd CodeReviewAgent
   ```

2. Configure as variáveis de ambiente:
   ```bash
   export GITHUB_APP_ID="seu-app-id"
   export GITHUB_APP_INSTALLATION_ID="seu-installation-id"
   export GITHUB_APP_PRIVATE_KEY="$(cat caminho/para/seu/arquivo.private-key.pem)"
   export OPENAI_API_KEY="sua-chave-openai"
   ```

3. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   ```

## Uso

O sistema é automatizado e irá:
1. Monitorar eventos de pull request nos repositórios configurados
2. Realizar análise automática quando:
    - Um PR é aberto
    - Um PR é reaberto
    - Novos commits são adicionados ao PR

O feedback será postado como um comentário no PR, incluindo análise de:
- Qualidade do código
- Potenciais bugs
- Boas práticas
- Problemas de performance
- Preocupações com segurança

## Configuração do Prompt

O sistema usa um prompt personalizado para gerar os code reviews. Você pode personalizar o prompt editando a propriedade `review.prompt.system-message` no arquivo `application.yml`.

## Desenvolvimento Local

Para desenvolvimento local, você pode usar o ngrok para expor sua aplicação:
```bash
ngrok http 8080
```

Atualize a Webhook URL no GitHub App com a URL fornecida pelo ngrok.

**Importante:** Certifique-se de que a URL do ngrok esteja corretamente formatada, incluindo o caminho `/api/review/webhook`.

## Personalização

- O formato do comentário pode ser personalizado no GitHubService
- O prompt do code review pode ser ajustado no arquivo de configuração
- Os limites de tamanho do código e tokens podem ser configurados nas propriedades da aplicação

## Logs

A aplicação possui logs detalhados que podem ser configurados em `application.yml`:
```yaml
logging:
  level:
    org.example.codereview: INFO  # ou DEBUG para mais detalhes
    reactor.netty.http.client: DEBUG  # para debug de requisições HTTP
```
