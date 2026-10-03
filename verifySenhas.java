
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.regex.Pattern;

public class verifySenhas {
    private static final String MAIUSCULA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULA = "abcdefghijklmnopqrstuvwxyz";
    private static final String ESPECIAIS = "@#$%^&+=!.,_-";
    private static final String NUMEROS = "0123456789";
    private static final String TODOS_CARACTERES = MAIUSCULA + MINUSCULA + ESPECIAIS + NUMEROS;

    private static final int TAMANHO_PADRAO = 12;

    private static final String REGEX_STRONG_PASSWORD = 
    "^(?=(?:.*\\d){2,})(?=.*[a-z])(?=.*[A-Z])(?=(?:.*[@#$%^&+=!.,_-]){2,})[A-Za-z\\d@#$%^&+=!.,_-]{8,}$";

    private static final SecureRandom RANDOM = new SecureRandom();

    public class VerificadorVazamento {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * Verifica se a senha já apareceu em vazamentos conhecidos.
     * Retorna quantas vezes ela foi vista (0 = nunca vazou).
     */
    public static int contarVazamentos(String password) {
        try {
            String hashCompleto = sha1(password);
            String prefixo = hashCompleto.substring(0, 5);
            String sufixo = hashCompleto.substring(5);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.pwnedpasswords.com/range/" + prefixo))
                    .header("Add-Padding", "true") // mitiga ataques de análise de tamanho da resposta
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Aviso: API retornou status " + response.statusCode() + ". Pulando verificação de vazamento.");
                return -1; // -1 indica "não foi possível verificar"
            }

            for (String linha : response.body().split("\n")) {
                String[] partes = linha.trim().split(":");
                if (partes.length == 2 && partes[0].equalsIgnoreCase(sufixo)) {
                    return Integer.parseInt(partes[1]);
                }
            }

            return 0; // não encontrado = nunca vazou (até onde se sabe)

        } catch (Exception e) {
            System.err.println("Aviso: erro ao consultar API de vazamentos (" + e.getMessage() + "). Continuando sem essa verificação.");
            return -1;
        }
    }

    private static String sha1(String texto) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(texto.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) {
            hex.append(String.format("%02X", b)); // maiúsculo, igual ao formato da API
        }
        return hex.toString();
    }
}

    public static boolean isPwStrong(String password) {
    if (password == null) {
        return false;
    }
    if (!Pattern.matches(REGEX_STRONG_PASSWORD, password)) {
        return false;
    }
    if (temSequenciaOuRepeticao(password)) {
        return false;
    }
    return true;
}

        

    private static boolean temSequenciaOuRepeticao(String password) {
    if (password == null || password.length() < 3) return false;

    for (int i = 0; i < password.length() - 2; i++) {
        char a = password.charAt(i);
        char b = password.charAt(i + 1);
        char c = password.charAt(i + 2);

        // Repetição: "aaa", "111"
        if (a == b && b == c) {
            return true;
        }

        // Sequência crescente: "123", "abc"
        if (b == a + 1 && c == b + 1) {
            return true;
        }

        // Sequência decrescente: "321", "cba"
        if (b == a - 1 && c == b - 1) {
            return true;
        }
    }
    return false;
}
    
    public static String validPassword(String password){
        StringBuilder error = new StringBuilder();
        int vezesVazada = VerificadorVazamento.contarVazamentos(password);

        if (password == null || password.length() < 8){
            error.append("- A senha deve ter no mínimo 8 caracteres.\n");
        }

        if(password==null || !password.matches(".*[a-z].*")){
            error.append("- A senha deve conter caracteres minúsculos.\n");
        }
        if(password==null || !password.matches(".*[A-Z].*")){
            error.append("- A senha precisa conter caracteres maiúsculos.\n");
        }
        if(password==null || !password.matches("(?=(?:.*\\d){2,}).*")){
            error.append("- A senha precisa conter no mínimo 2 números.\n");
        }
        if(password==null || !password.matches("(?=(?:.*[@#$%^&+=!.,_-]){2,}).*")){
            error.append("- A senha precisa ter no minimo 2 caracteres especiais. (Use: @#$%^&+=!.,_-)\n");
        }
        if(password != null && !password.matches("[A-Za-z\\d@ #$%^&+=!.,_-]+")){
            error.append("- A senha contém caracteres não permitidos. (Use apenas: @#$%^&+=!.,_-)\n");
        }
        if(password==null || password.contains(" ")){
            error.append("- A senha não pode conter espaços.\n");
        }
        if (temSequenciaOuRepeticao(password)) {
            error.append("- A senha não pode conter sequências (ex: 123, abc) nem repetições (ex: aaa).\n");
        }
        if (vezesVazada > 0) {
            error.append("- Essa senha já apareceu em " + vezesVazada + " vazamentos conhecidos. Escolha outra.\n");   
        }
    // vezesVazada == -1 significa que a API falhou; nesse caso, o programa
    // continua sem bloquear por esse motivo (fail-safe, para não travar o
    // usuário por causa de uma instabilidade de rede)

    return error.length() == 0 ? "Senha forte!" : error.toString();
    }

    public static String genStrongPw(){
        return genStrongPw(TAMANHO_PADRAO);
    }

    public static String genStrongPw(int lngth){
        if (lngth < 8) {
            throw new IllegalArgumentException("O tamanho mínimo para uma senha forte é 8.");
        }
        if (lngth > 20) {
            throw new IllegalArgumentException("O tamanho máximo para uma senha é 20.");
        }
        StringBuilder password = new StringBuilder(lngth);

        password.append(rndmChar(MINUSCULA));
        password.append(rndmChar(MAIUSCULA));
        password.append(rndmChar(ESPECIAIS));
        password.append(rndmChar(NUMEROS));
        password.append(rndmChar(ESPECIAIS));
        password.append(rndmChar(NUMEROS));


        for (int i = password.length(); i < lngth; i++) {
            password.append(rndmChar(TODOS_CARACTERES));
        }
        
        return mix(password.toString());
    }

    private static char rndmChar(String conjunto){
         int indice = RANDOM.nextInt(conjunto.length());
        return conjunto.charAt(indice);
    }

    // Embaralha os caracteres da senha com trocas aleatórias e retorna uma nova String.
    private static String mix (String text){
        char[] caracteres = text.toCharArray();

        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = temp;
        }

        return new String(caracteres);
    }
    
   public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean opcaoValida = false;

        while(!opcaoValida) {
            System.out.println("1 - Verificar uma senha");
            System.out.println("2 - Gerar uma senha forte aleatória");
            System.out.print("Escolha uma opção: ");
            String opcao = sc.nextLine();

            switch(opcao){
            case "1":
                System.out.print("Digite uma senha para verificar: ");
                String senha = sc.nextLine();
                System.out.println("\nResultado da validação:");
                System.out.println(validPassword(senha));
                System.out.println("\nÉ uma senha forte? " + isPwStrong(senha));
                opcaoValida = true;
                break;

            case "2":
            int tamanho;
            while (true) { 
                System.out.println("Digite o tamanho desejado (min: 8, ENTER: 12): ");
                String entradaTamanho = sc.nextLine();
            
            
            if(entradaTamanho.isBlank()){
                tamanho = TAMANHO_PADRAO;
                break;
            }

            try{
                tamanho = Integer.parseInt(entradaTamanho);
            } catch (NumberFormatException erro){
                System.out.println("Digite um número inteiro válido.");
                continue;
            }

            if(tamanho<8){
                System.out.println("O tamanho mínimo é 8 caracteres.");
                continue;
            }
            break;
        }

            String senhaGerada = genStrongPw(tamanho);
            System.out.println("\nSenha sugerida: " + senhaGerada);
            System.out.println("É forte? " + isPwStrong(senhaGerada));
            opcaoValida = true;
            break;
            

           default:
                System.out.println("Escolha uma opção entre 1 e 2.");
             break;
            }   
        }
        sc.close();
    }
}
