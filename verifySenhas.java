
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

    public static boolean isPwStrong(String password) {
        if (password == null) {
            return false;
        }
        return Pattern.matches(REGEX_STRONG_PASSWORD, password);
    }
    
    public static String validPassword(String password){
        StringBuilder error = new StringBuilder();

        if (password == null || password.length() < 8){
            error.append("- A senha deve ter no mínimo 8 caracteres.\n");
        }

        if(password==null || !password.matches(".*[a-z].*")){
            error.append("- A senha deve conter caracteres mínusculos.\n");
        }
        if(password==null || !password.matches(".*[A-Z].*")){
            error.append("- A senha precisa conter caracteres maiusculos.\n");
        }
        if(password==null || !password.matches("(?=(?:.*\\d){2,}).*")){
            error.append("- A senha precisa conter no mínimo 2 números.\n");
        }
        if(password==null || !password.matches("(?=(?:.*[@#$%^&+=!.,_-]){2,}).*")){
            error.append("- A senha precisa ter no minimo 2 caracteres especiais. (Use: @#$%^&+=!.,_-)\n");
        }
        if(password != null && !password.matches("[A-Za-z\\d@#$%^&+=!.,_-]+")){
            error.append("- A senha contém caracteres não permitidos. (Use apenas: @#$%^&+=!.,_-)\n");
        }
        if(password==null || password.contains(" ")){
            error.append("- A senha não pode conter espaços.\n");
        }
        return error.length() == 0 ? "Senha forte!" : error.toString();
    }

    public static String genStrongPw(){
        return genStrongPw(TAMANHO_PADRAO);
    }

    public static String genStrongPw(int lngth){
        if (lngth < 8) {
            throw new IllegalArgumentException("O tamanho mínimo para uma senha forte é 8.");
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
                System.out.print("Digite o tamanho desejado (mínimo 8, Enter para usar 12): ");
                String entradaTamanho = sc.nextLine();
                int tamanho = entradaTamanho.isBlank()
                    ? TAMANHO_PADRAO 
                    : Integer.parseInt(entradaTamanho);

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