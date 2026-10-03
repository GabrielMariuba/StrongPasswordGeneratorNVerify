# Política de Segurança

Este documento descreve como reportar vulnerabilidades no **StrongPasswordGeneratorNVerify**, as decisões de segurança que o código adota, suas limitações conhecidas e recomendações para quem quiser usá-lo como base de algo real.

> ⚠️ **Aviso de escopo:** projeto **educacional e open source**, sem auditoria formal. Leia [Limitações conhecidas](#limitações-conhecidas) antes de usar como está em qualquer sistema que proteja dados reais.

---

## Versões suportadas

| Versão | Suporte a correções de segurança |
|--------|----------------------------------|
| `main` (última versão) | ✅ Sim |
| Forks e branches de terceiros | ❌ Não |
| Versões anteriores / tags antigas | ❌ Não |

---

## Como reportar uma vulnerabilidade

Por ser um projeto **open source**, **nunca** abra uma issue pública, um Pull Request ou comente em uma issue/PR existente para relatar um problema de segurança — isso expõe a falha antes de existir uma correção.

Use um destes canais privados:

1. **GitHub Security Advisories** (preferencial, se habilitado no repositório): aba **Security → Report a vulnerability**. Permite relatar, discutir e coordenar a correção de forma privada antes da divulgação pública.
2. **E-mail**: **`mariubacode@gmail.com`**.

Inclua, se possível:

- Descrição do problema e do impacto (ex: a senha vaza para um terceiro, uma validação pode ser contornada, etc.)
- Passos para reproduzir (entrada usada, versão do Java, sistema operacional)
- Método ou trecho de código afetado (ex: `genStrongPw`, `VerificadorVazamento.contarVazamentos`)
- Sugestão de correção, se tiver

**O que esperar:**

- Confirmação de recebimento em até **7 dias**
- Avaliação inicial e plano de correção em até **30 dias**
- Crédito público pela descoberta (no changelog ou na seção de agradecimentos do README), se você quiser
- Divulgação coordenada: a falha só é tornada pública (issue, changelog, advisory) depois que a correção for publicada

---

## O que o projeto faz (e não faz)

- **`isPwStrong(String)`**: validação offline — regex de composição + detecção de sequências/repetições.
- **`validPassword(String)`**: validação completa — todas as regras de composição, sequências/repetições **e** consulta à API pública do Have I Been Pwned (Pwned Passwords) para checar vazamentos conhecidos.
- **`genStrongPw(int)`**: gera uma senha aleatória que atende às regras de composição, tamanho entre 8 e 20.

Regras aplicadas: mínimo de 8 caracteres, 1+ minúscula, 1+ maiúscula, 2+ números, 2+ caracteres especiais (`@#$%^&+=!.,_-`), somente esses caracteres, sem espaços, sem sequências (`123`, `abc`) nem repetições (`aaa`) de 3+ caracteres, e (apenas em `validPassword`) sem histórico de vazamento conhecido.

O projeto **não** armazena, transmite para fins de autenticação, nem persiste senhas em disco.

---

## Decisões de segurança adotadas

- **`SecureRandom`** em vez de `Random`: o sorteio dos caracteres usa gerador criptograficamente seguro.
- **Embaralhamento Fisher-Yates**: depois de garantir o mínimo de cada categoria, a senha é embaralhada de forma uniforme, sem padrão previsível de posições.
- **Checagem de vazamentos com k-Anonymity**: a senha nunca é enviada inteira para a API do Have I Been Pwned. Apenas os 5 primeiros caracteres do hash SHA-1 são transmitidos; a comparação do restante é feita localmente. O SHA-1 aqui serve só para consultar esse índice público — **não** é usado para proteger ou armazenar a senha, então a fragilidade conhecida do SHA-1 para esse fim específico não se aplica.
- **Fail-open na checagem de vazamentos**: se a API estiver indisponível, `contarVazamentos` retorna `-1` e a senha não é bloqueada por esse motivo. Isso prioriza a disponibilidade do programa sobre o rigor da checagem — uma troca deliberada, não um descuido (veja [Limitações](#limitações-conhecidas) para o outro lado dessa escolha).
- **Tratamento de `null`**: `isPwStrong` e `validPassword` não quebram com entrada nula.
- **Sem persistência**: o código não grava senhas em arquivo, banco de dados ou log.

---

## Limitações conhecidas

### Inconsistência entre validações

- **`isPwStrong` não consulta a API de vazamentos** — só `validPassword` faz essa checagem. Uma senha vazada, mas estruturalmente forte, pode retornar `true` em `isPwStrong` e ao mesmo tempo ser reprovada por `validPassword`. Quem usa `isPwStrong` isoladamente para decidir se aceita uma senha **não** está protegido contra reuso de senha vazada.

### Robustez

- **Tamanho acima de 20 não é tratado no menu**: o laço de leitura da opção 2 só repete a pergunta para valores não numéricos ou menores que 8. Um valor maior que 20 chega a `genStrongPw`, que lança `IllegalArgumentException` sem tratamento no `main` — o programa encerra com erro.
- **`genStrongPw` não revalida a senha gerada** contra sequência/repetição depois do embaralhamento. É raro, mas estatisticamente possível que o resultado contenha um trecho como `123` ou `aaa`.

### Dependência de um serviço externo

- **Sem timeout configurado no `HttpClient`** usado para consultar a API: se o serviço travar (em vez de simplesmente falhar ou recusar a conexão), a chamada pode ficar bloqueada por tempo indefinido.
- **Sem cache**: validar a mesma senha repetidas vezes dispara uma nova chamada HTTP a cada vez, consumindo a cota de uso da API desnecessariamente.
- **Disponibilidade de terceiro**: a função de checagem de vazamentos depende inteiramente da API pública do Have I Been Pwned estar no ar e responder dentro de um tempo razoável. O comportamento fail-open (ver acima) evita travar o usuário, mas também significa que, durante uma indisponibilidade da API, a checagem de vazamento simplesmente não acontece — sem aviso visível de que ela foi pulada, além da mensagem no `stderr`.

### Qualidade da validação

- **Conjunto de caracteres restrito**: acentos e símbolos fora da lista são rejeitados.
- **Sem bloqueio de senhas comuns por lista local**: diferente de uma blocklist estática, a proteção aqui depende inteiramente de a senha já ter sido registrada em um vazamento conhecido pela API consultada. Uma senha nova, previsível mas nunca vazada (ex: `Empresa@2026`), pode passar.

### Manuseio da senha

- **O `Scanner` exibe a senha na tela** enquanto ela é digitada. Para leitura segura no terminal, use `System.console().readPassword()`.
- **Uso de `String` para senhas**: `String` é imutável e pode permanecer na memória até o coletor de lixo agir. Para dados sensíveis, prefira `char[]` e zere o array após o uso.

---

## Se for usar em um sistema real

1. **Nunca guarde senhas em texto puro.** Use um algoritmo de hash próprio para senhas, como **Argon2id**, **bcrypt** ou **scrypt**, com salt único. O SHA-1 usado neste projeto serve apenas para consultar a API de vazamentos — não é adequado para armazenar credenciais.
2. **Resolva a inconsistência entre `isPwStrong` e `validPassword`** antes de usar qualquer uma das duas isoladamente como critério de aceitação.
3. **Adicione timeout e tratamento explícito de falha de rede** nas chamadas HTTP, para não depender do comportamento padrão do `HttpClient`.
4. **Considere fail-closed** (rejeitar a senha também quando a API falhar) se o seu caso de uso tolera menos risco do que indisponibilidade — a escolha atual do projeto é fail-open.
5. **Limite tentativas de login** e considere autenticação em dois fatores.
6. **Use HTTPS/TLS** ao transmitir senhas (a chamada à API já usa HTTPS; garanta que o restante do seu sistema também use).
7. **Não registre senhas em logs** nem em mensagens de erro.

---

## Dependências

Biblioteca padrão do Java (`java.security`, `java.util`, `java.util.regex`, `java.net.http`) e a **API pública Pwned Passwords** (`api.pwnedpasswords.com`), consultada em tempo de execução via HTTPS — não é uma dependência de build, mas uma dependência de disponibilidade em produção. Sem dependências de terceiros no código (sem Maven/Gradle). Mantenha o **JDK atualizado** (17+) para receber correções de segurança da plataforma e do `HttpClient`.

---

## Para contribuidores

- Não inclua senhas reais, chaves ou credenciais em commits, testes ou exemplos.
- Ao alterar a regex, teste com entradas muito longas e com caracteres especiais para evitar problemas de desempenho (*ReDoS*).
- Mudanças em `genStrongPw` devem manter o uso de `SecureRandom`.
- Mudanças em `VerificadorVazamento` devem manter o fluxo de k-Anonymity (nunca enviar a senha completa, nem o hash completo, à API).
- Pull Requests que **corrigem** uma vulnerabilidade de segurança devem ser combinados antes pelo canal privado descrito em [Como reportar uma vulnerabilidade](#como-reportar-uma-vulnerabilidade), não abertos diretamente como PR público.

---

*Última atualização: 03/10/2026*