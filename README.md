# StrongPasswordGeneratorNVerify

Programa em **Java** para **verificar se uma senha é forte** e para **gerar senhas fortes aleatórias**, usando apenas a biblioteca padrão da linguagem.

> 🚧 **Status: em desenvolvimento.**
> O projeto **ainda não está concluído**. Ele é **funcional**, mas apresenta alguns defeitos conhecidos que serão corrigidos. Veja a seção [Defeitos conhecidos](#defeitos-conhecidos-a-corrigir).

---

## Funcionalidades

- **Validação rápida** (`isPwStrong`): devolve `true` ou `false` usando uma única regex com *lookaheads*.
- **Validação detalhada** (`validPassword`): lista exatamente quais regras a senha não cumpre.
- **Gerador de senhas** (`genStrongPw`): cria senhas aleatórias que atendem às regras, com tamanho configurável (mínimo 8, padrão 12).
- **Menu no terminal** para escolher entre verificar ou gerar uma senha.

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

---

## Requisitos

- **JDK 11 ou superior** (o código usa `String.isBlank()`, disponível a partir do Java 11)
- Um terminal (Windows, macOS ou Linux)

Para conferir a instalação:

```bash
javac -version
java -version
```

> É necessário o **JDK** (que inclui o `javac`), não apenas o JRE.

---

## Como compilar e executar

Na pasta onde está o arquivo `verifySenhas.java`:

```bash
javac verifySenhas.java
java verifySenhas
```

---

## Como usar

Ao executar, o programa exibe o menu:

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
- A senha precisa conter caracteres maiusculos.
- A senha precisa conter no mínimo 2 números.
- A senha precisa ter no minimo 2 caracteres especiais. (Use: @#$%^&+=!.,_-)

É uma senha forte? false
```

Para uma senha que cumpre todas as regras, o resultado é `Senha forte!` seguido de `É uma senha forte? true`.

### Opção 2: gerar uma senha forte

Informe o tamanho desejado (mínimo 8) ou pressione **Enter** para usar o padrão de 12 caracteres. O programa mostra a senha sugerida, que muda a cada execução.

---

## Usando os métodos em outro código

Os métodos são `static` e podem ser chamados diretamente:

```java
boolean forte = verifySenhas.isPwStrong("Abc12@#x");   // true
String relatorio = verifySenhas.validPassword("abc");  // lista de problemas
String senha = verifySenhas.genStrongPw();             // 12 caracteres
String senhaLonga = verifySenhas.genStrongPw(20);      // 20 caracteres
```

`genStrongPw(int)` lança `IllegalArgumentException` se o tamanho for menor que 8. Quem chama o método deve tratar esse caso.

---

## Como o gerador funciona

1. Sorteia **1 minúscula, 1 maiúscula, 2 números e 2 caracteres especiais**, garantindo o mínimo exigido pelas regras.
2. Completa o restante do tamanho com caracteres sorteados de **qualquer categoria**.
3. **Embaralha** tudo com o algoritmo de Fisher-Yates, para que a posição de cada tipo de caractere não seja previsível.

Todo o sorteio usa `java.security.SecureRandom`, adequado para fins criptográficos (ao contrário de `java.util.Random`).

---

## Defeitos conhecidos (a corrigir)

O projeto funciona, mas estes pontos ainda precisam de correção:

**Menu e entrada do usuário (`main`)**

- [x] O programa **encerra após uma única ação**. Não há laço: ele deveria só fechar quando a necessidade do usuário for atendida.
- [x] Uma **opção inválida** no menu não gera nenhuma resposta; o programa apenas termina.
- [x] Digitar um tamanho **menor que 8** na geração faz o programa encerrar com erro (`IllegalArgumentException` sem tratamento). O ideal é avisar e pedir o valor de novo, para não gerar erro por uma distração simples.
- [x] Digitar algo que **não seja número** como tamanho causa `NumberFormatException` sem tratamento.

**Validação**

- [ ] Uma senha com espaço recebe **duas mensagens** parecidas (caractere não permitido e espaço).
- [ ] A mensagem de "caracteres não permitidos" cita apenas os símbolos, sem mencionar que letras sem acento e números também são aceitos.
- [ ] As regras estão **duplicadas**: uma vez na regex de `isPwStrong` e outra nas verificações de `validPassword`. Alterar uma regra exige mudar os dois lugares.
- [ ] Há erros de ortografia em algumas mensagens (por exemplo, "mínusculos" e "maiusculos").

**Melhorias gerais**

- [ ] Renomear a classe para `VerifySenhas`, seguindo a convenção de nomes do Java (classes em PascalCase).
- [ ] Bloquear senhas comuns (`Senha@123`, `123456`, etc.) e sequências (`1234`, `abcd`).
- [ ] Ler a senha sem exibi-la na tela, com `System.console().readPassword()`.
- [ ] Definir um tamanho máximo de senha.
- [ ] Adicionar testes automatizados (JUnit).

---

## Segurança e limitações

Este projeto é **educacional** e **não deve ser usado em produção**. O código não armazena nem faz hash de senhas, e o `Scanner` exibe a senha na tela enquanto ela é digitada.

Consulte o [SECURITY.md](SECURITY.md) para os detalhes, as recomendações e como reportar vulnerabilidades.

---

## Autor

[@GabrielMariuba](https://github.com/GabrielMariuba)

---

## Licença

MIT License

Copyright (c) 2026 GabrielMariuba

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

