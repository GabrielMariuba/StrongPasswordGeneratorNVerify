# StrongPasswordGeneratorNVerify
 
![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)
![Status](https://img.shields.io/badge/status-open%20to%20contributions-brightgreen.svg)
 
Programa em **Java** que verifica a força de uma senha e gera senhas fortes aleatórias — combinando regras de composição, detecção de padrões previsíveis e checagem contra vazamentos reais, através da API pública do **Have I Been Pwned**.
 
Este é um projeto **open source**. Contribuições, sugestões e relatos de problemas são bem-vindos — veja a seção [Como contribuir](#como-contribuir).
 
---
 
## Funcionalidades
 
- **Validação rápida** (`isPwStrong`): devolve `true` ou `false` com base em regras de composição e detecção de sequências/repetições.
- **Validação detalhada** (`validPassword`): além das regras acima, também consulta a API do Have I Been Pwned e explica cada regra não cumprida.
- **Gerador de senhas** (`genStrongPw`): cria senhas aleatórias que atendem às regras de composição, com tamanho configurável (entre 8 e 20, padrão 12).
- **Menu interativo** no terminal, que só encerra quando a ação escolhida é concluída, e volta a perguntar se a opção digitada for inválida.
---
 
## Regras de uma senha forte
 
| Regra | Detalhe |
|---|---|
| Tamanho | Mínimo de **8 caracteres** |
| Letra minúscula | Pelo menos **1** (`a-z`) |
| Letra maiúscula | Pelo menos **1** (`A-Z`) |
| Números | Pelo menos **2** (`0-9`) |
| Caracteres especiais | Pelo menos **2**, dentre `@ # $ % ^ & + = ! . , _ -` |
| Caracteres permitidos | Apenas letras sem acento, dígitos e os símbolos acima |
| Espaços | **Não permitidos** |
| Sequências | Bloqueadas sequências de 3+ caracteres consecutivos, crescentes ou decrescentes (ex: `123`, `cba`) |
| Repetições | Bloqueados 3+ caracteres repetidos seguidos (ex: `aaa`, `111`) |
| Vazamentos conhecidos | A senha é consultada na base do **Have I Been Pwned** — se já apareceu em algum vazamento, é recusada |
 
> A checagem de vazamentos usa a técnica de **k-Anonymity**: apenas os 5 primeiros caracteres do hash SHA-1 da senha são enviados à API, nunca a senha em si.
 
---
 
## Requisitos
 
- **JDK 17 ou superior** (o código usa `java.net.http.HttpClient`, disponível desde o Java 11, e declara membros estáticos em uma classe interna não estática, um recurso liberado a partir do Java 16)
- **Conexão com a internet**, apenas para a checagem de vazamentos (`validPassword`). As demais funcionalidades funcionam totalmente offline.
Para conferir a instalação:
 
```bash
javac -version
java -version
```
 
---
 
## Como compilar e executar
 
Na pasta onde está o arquivo `verifySenhas.java`:
 
```bash
javac verifySenhas.java
java verifySenhas
```
 
---
 
## Como usar
 
Ao executar, o programa exibe o menu e repete a pergunta até receber uma opção válida:
 
```
1 - Verificar uma senha
2 - Gerar uma senha forte aleatória
Escolha uma opção:
```
 
### Opção 1: verificar uma senha
 
Digite a senha e o programa mostra o resultado da validação. Exemplo com a senha `abc`:
 
```
Resultado da validação:
- A senha deve ter no mínimo 8 caracteres.
- A senha precisa conter caracteres maiúsculos.
- A senha precisa conter no mínimo 2 números.
- A senha precisa ter no minimo 2 caracteres especiais. (Use: @#$%^&+=!.,_-)
 
É uma senha forte? false
```
 
Para uma senha que cumpre todas as regras de composição e nunca apareceu em um vazamento, o resultado é `Senha forte!` seguido de `É uma senha forte? true`.
 
### Opção 2: gerar uma senha forte
 
Informe o tamanho desejado — entre **8 e 20** — ou pressione **Enter** para usar o padrão de 12 caracteres. Digitar um valor não numérico ou menor que 8 faz o programa pedir novamente, sem encerrar.
 
---
 
## Usando os métodos em outro código
 
Os métodos são `static` e podem ser chamados diretamente:
 
```java
boolean forte = verifySenhas.isPwStrong("Abc12@#xy");         // checagem offline (regex + sequência)
String relatorio = verifySenhas.validPassword("abc");         // lista de problemas + checagem de vazamento
String senha = verifySenhas.genStrongPw();                    // 12 caracteres
String senhaLonga = verifySenhas.genStrongPw(20);              // 20 caracteres
```
 
`genStrongPw(int)` lança `IllegalArgumentException` se o tamanho for menor que 8 ou maior que 20. Quem chama o método fora do `main` (que já trata o limite mínimo) deve tratar essa exceção.
 
---
 
## Como a checagem de vazamentos funciona
 
A classe interna `VerificadorVazamento` implementa o fluxo de **k-Anonymity** da API Pwned Passwords:
 
1. Calcula o hash **SHA-1** da senha.
2. Envia à API apenas os **5 primeiros caracteres** do hash.
3. A API devolve todos os hashes que começam com esse prefixo.
4. O programa compara o restante do hash localmente, sem nunca expor a senha completa pela rede.
Se a API estiver indisponível ou a chamada falhar por qualquer motivo, `contarVazamentos` retorna `-1` e o programa **não bloqueia a senha por esse motivo** (comportamento *fail-open*, para não travar o usuário por uma instabilidade de rede).
 
---
 
## Como o gerador funciona
 
1. Sorteia **1 minúscula, 1 maiúscula, 2 caracteres especiais e 2 números**, garantindo o mínimo exigido pelas regras de composição.
2. Completa o restante do tamanho com caracteres sorteados de **qualquer categoria**.
3. **Embaralha** tudo com o algoritmo de Fisher-Yates, para que a posição de cada tipo de caractere não seja previsível.
Todo o sorteio usa `java.security.SecureRandom`, adequado para fins criptográficos.
 
---
 
## Pontos de atenção
 
Estas são particularidades do comportamento atual, úteis de ter em mente:
 
- **`isPwStrong` não consulta a API de vazamentos.** Apenas `validPassword` faz essa checagem. Ou seja, uma senha vazada mas estruturalmente forte pode retornar `true` em `isPwStrong` e, ao mesmo tempo, aparecer como reprovada no relatório de `validPassword`. Se o objetivo é que as duas funções concordem sempre, vale replicar a chamada a `VerificadorVazamento.contarVazamentos` dentro de `isPwStrong`.
- **`genStrongPw` não é validado contra sequência/repetição após a geração.** O método garante a composição exigida pela regex, mas não há uma nova checagem (nem nova tentativa) caso o embaralhamento produza, por acaso, uma sequência como `123` ou uma repetição como `aaa`. Na prática isso é raro, mas pode acontecer.
- **Tamanho acima de 20 no menu não é tratado.** O laço de leitura do tamanho (opção 2) só repete a pergunta para valores não numéricos ou menores que 8; um valor maior que 20 passa pela validação do menu e só é barrado dentro de `genStrongPw`, que lança `IllegalArgumentException` sem tratamento no `main` — o programa encerra com erro nesse caso.
- **Sem timeout configurado no `HttpClient`.** Se a API do Have I Been Pwned travar (em vez de simplesmente falhar), a chamada pode ficar bloqueada por um tempo indefinido antes de cair no bloco `catch`.
- **Sem cache de consultas.** Verificar a mesma senha várias vezes dispara uma nova chamada à API a cada vez.
Nenhum desses pontos impede o uso do programa — são oportunidades de refinamento caso o projeto evolua.
 
---
 
## Segurança e limitações
 
Este projeto é **educacional**. Ele não armazena nem faz hash de senhas para fins de autenticação (o hash SHA-1 é usado apenas para consultar a API de vazamentos), e o `Scanner` exibe a senha na tela enquanto ela é digitada.
 
**Não abra uma issue pública para reportar uma vulnerabilidade de segurança.** Consulte o [SECURITY.md](SECURITY.md) para o canal correto, além de detalhes e recomendações de uso em produção.
 
---
 
## Como contribuir
 
Toda contribuição é bem-vinda, de correções de bugs a novas funcionalidades e melhorias na documentação.
 
1. Faça um **fork** do repositório.
2. Crie uma branch a partir da `main` com um nome descritivo: `git checkout -b fix/validacao-tamanho-maximo` ou `git checkout -b feature/cache-de-vazamentos`.
3. Faça suas alterações. Se for corrigir um bug, procure cobrir o caso com um teste manual descrito na própria descrição do Pull Request (o projeto ainda não tem testes automatizados — ver [Ideias para contribuir](#ideias-para-contribuir)).
4. Commits claros e no imperativo ajudam na revisão: `Corrige tratamento de tamanho maior que 20`.
5. Abra um **Pull Request** descrevendo o que mudou e por quê. Se resolver uma issue existente, referencie-a (`Closes #12`).
### Reportando bugs ou sugerindo melhorias
 
Use a aba **Issues** do repositório para:
- Relatar um comportamento inesperado (inclua passos para reproduzir, a entrada usada e a versão do Java).
- Sugerir uma nova funcionalidade ou melhoria.
Problemas de **segurança** são a exceção: não abra issue pública para eles — siga o processo descrito no [SECURITY.md](SECURITY.md).
 
### Ideias para contribuir
 
Pontos levantados na seção [Pontos de atenção](#pontos-de-atenção) são bons primeiros problemas para quem quiser contribuir, além de:
 
- [ ] Adicionar testes automatizados (JUnit) para `isPwStrong`, `validPassword` e `genStrongPw`.
- [ ] Fazer `isPwStrong` também consultar a API de vazamentos, para ficar consistente com `validPassword`.
- [ ] Tratar tamanhos maiores que 20 no menu, antes de chamar `genStrongPw`.
- [ ] Adicionar timeout nas chamadas HTTP e, opcionalmente, um cache simples para evitar repetir consultas da mesma senha.
- [ ] Validar a senha gerada por `genStrongPw` contra sequências e repetições, com nova tentativa em caso de falha.
- [ ] Internacionalizar as mensagens (hoje fixas em português).
---
 
## Agradecimentos
 
- [Have I Been Pwned / Pwned Passwords](https://haveibeenpwned.com/Passwords) — API pública usada na checagem de vazamentos, mantida por Troy Hunt.
---
 
## Autor
 
[@GabrielMariuba](https://github.com/GabrielMariuba)
 
---
 
## Licença

MIT License

Copyright (c) 2026 Gabriel H. Mariuba Campos

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

