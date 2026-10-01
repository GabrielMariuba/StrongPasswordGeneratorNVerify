# Política de Segurança

Este documento descreve como reportar vulnerabilidades no **StrongPasswordGeneratorNVerify**, quais decisões de segurança o código adota e quais são as limitações conhecidas.

> ⚠️ **Aviso de escopo:** projeto educacional, **em desenvolvimento** e **sem auditoria**. Não use o código como está para proteger sistemas reais. Veja [Limitações conhecidas](#limitações-conhecidas).

---

## Versões suportadas

| Versão | Suporte a correções de segurança |
|--------|----------------------------------|
| `main` (última versão) | ✅ Sim |
| Versões anteriores | ❌ Não |

---

## Como reportar uma vulnerabilidade

**Não abra uma issue pública** para problemas de segurança.

Envie um e-mail para: **`mariubacode@gmail.com`**

Se o recurso de relato privado de vulnerabilidades estiver habilitado no repositório (aba **Security** do GitHub), você também pode usá-lo.

Inclua, se possível:

- Descrição do problema e do impacto
- Passos para reproduzir (entrada usada, versão do Java, sistema operacional)
- Método ou trecho de código afetado
- Sugestão de correção, se tiver

**O que esperar:**

- Confirmação de recebimento em até **7 dias**
- Avaliação inicial e plano de correção em até **30 dias**
- Crédito público pela descoberta, se você quiser

---

## O que o projeto faz (e não faz)

- **`isPwStrong(String)`**: valida a senha com uma regex.
- **`validPassword(String)`**: verifica cada regra e explica o que falta.
- **`genStrongPw(int)`**: gera uma senha aleatória que atende às regras.

O projeto **não** armazena, transmite nem faz hash de senhas.

Regras aplicadas: mínimo de 8 caracteres, 1+ minúscula, 1+ maiúscula, 2+ números, 2+ caracteres especiais (`@#$%^&+=!.,_-`), somente esses caracteres (sem acentos), sem espaços.

---

## Decisões de segurança adotadas

- **`SecureRandom`** em vez de `Random`: o sorteio dos caracteres usa gerador criptograficamente seguro.
- **Embaralhamento Fisher-Yates**: depois de garantir o mínimo de cada categoria, a senha é embaralhada de forma uniforme, sem padrão previsível de posições.
- **Tratamento de `null`**: `isPwStrong` e `validPassword` não quebram com entrada nula.
- **Sem persistência**: o código não grava senhas em arquivo, banco de dados ou log.

---

## Limitações conhecidas

O projeto está em desenvolvimento. Estas limitações são conhecidas:

### Qualidade da validação

- **Regras de composição não garantem senhas boas.** Uma senha como `Senha@12#` cumpre as regras, mas é previsível. Não há verificação contra listas de senhas comuns ou vazadas.
- **Sem detecção de padrões fracos**: sequências (`1234`, `abcd`), repetições (`aaaa`) e padrões de teclado (`qwerty`) não são bloqueados.
- **Conjunto de caracteres restrito**: acentos e outros símbolos são rejeitados.
- **Sem tamanho máximo**: entradas muito longas são processadas sem limite. Em um sistema real, defina um teto (por exemplo, 128 caracteres).
- **Regras duplicadas** entre `isPwStrong` (regex única) e `validPassword` (verificações separadas): uma alteração feita em só um dos lugares pode gerar resultados divergentes.

### Manuseio da senha

- **O `Scanner` exibe a senha na tela** enquanto ela é digitada. Para leitura segura no terminal, use `System.console().readPassword()`.
- **Uso de `String` para senhas**: `String` é imutável e pode ficar na memória até o coletor de lixo agir. Para dados sensíveis, prefira `char[]` e zere o array após o uso.
- **A senha gerada é impressa no console**, o que pode deixá-la no histórico do terminal ou em logs.

### Robustez

- **Entradas inválidas no menu não são tratadas**: um tamanho menor que 8 gera `IllegalArgumentException` e um valor não numérico gera `NumberFormatException`, encerrando o programa. Isso é um problema de robustez, não de exposição de dados, e está na lista de correções do [README](README.md).

---

## Se for usar em um sistema real

1. **Nunca guarde senhas em texto puro.** Use um algoritmo de hash próprio para senhas, como **Argon2id**, **bcrypt** ou **scrypt**, com salt único. Não use MD5, SHA-1 ou SHA-256 puro.
2. **Priorize o tamanho à complexidade.** O NIST (SP 800-63B) recomenda senhas longas e verificação contra listas de senhas vazadas, em vez de regras obrigatórias de composição.
3. **Bloqueie senhas comuns e vazadas**, por exemplo com o serviço *Have I Been Pwned* (k-anonymity).
4. **Limite tentativas de login** e considere autenticação em dois fatores.
5. **Use HTTPS/TLS** ao transmitir senhas.
6. **Não registre senhas em logs** nem em mensagens de erro.

---

## Dependências

Apenas a biblioteca padrão do Java (`java.security`, `java.util`, `java.util.regex`). Sem dependências externas. Mantenha o **JDK atualizado** para receber correções de segurança da plataforma.

---

## Para contribuidores

- Não inclua senhas reais, chaves ou credenciais em commits, testes ou exemplos.
- Ao alterar a regex, teste com entradas muito longas e com caracteres especiais para evitar problemas de desempenho (*ReDoS*).
- Mudanças em `genStrongPw` devem manter o uso de `SecureRandom`.

---

*Última atualização: 28/09/2026*
