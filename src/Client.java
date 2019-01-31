import java.io.*;
import java.net.*;

public class Client{
    private static PrintWriter out;
    private static BufferedReader in;
    private static BufferedReader sin;
    
    private static int autenticacao() throws IOException{
        String current_username;
        String current_password;

        System.out.print("\nInsira o seu Username: ");
        current_username = sin.readLine();
        out.println(current_username);

        System.out.print("Insira a sua Password: ");
        current_password = sin.readLine();
        out.println(current_password);
        
        int res = Integer.parseInt(in.readLine());
        
        if(res == 0 || res == 3)System.out.println("Credenciais Inválidas.");
        if(res == 1) System.out.println("Login efetuado com sucesso.");
        if(res == 2) System.out.println("Utilizador já está com sessão iniciada.");
        
        return res;   
    }
    
    private static boolean registo() throws IOException{
            
        String current_username;
        String current_password;

        System.out.print("\nInsira o seu Username: ");
        current_username = sin.readLine();
        out.println(current_username);

        System.out.print("Insira a sua Password: ");
        current_password = sin.readLine();
        out.println(current_password);
        
        boolean res = false;
        String resposta = in.readLine();
        
        if (resposta.equalsIgnoreCase("true") || resposta.equalsIgnoreCase("false")) res = Boolean.valueOf(resposta);
        
        if(!res)System.out.println("Utilizador já existe.");
        else System.out.println("Registo efetuado com sucesso.");
        
        return res;   
    }
    
    private static void reservaPedido() throws IOException{
        
        System.out.println("\nInsira o nome do Server que deseja reservar:");
        System.out.println("Server S : 0.99€");
        System.out.println("Server M : 1.99€");
        System.out.println("Server L : 2.99€");
        System.out.println("Server XL : 3.99€");
        System.out.println("Server XXL : 4.99€");
        System.out.println("Sair");
        System.out.print("Resposta: ");
        
        String resposta = sin.readLine();
        if(resposta.equals("Sair")) return;
        out.println(1);
        out.println(resposta);

        System.out.println(in.readLine());
        String ticket = in.readLine();
        
        while(!sin.readLine().equals(ticket));
        
        out.println(ticket);
        
    }
    
    private static void reservaLeilao() throws IOException{
        
        System.out.println("\nInsira o nome do Server que deseja reservar:");
        System.out.println("Server S");
        System.out.println("Server M");
        System.out.println("Server L");
        System.out.println("Server XL");
        System.out.println("Server XXL");
        System.out.println("Sair");
        System.out.print("Resposta: ");
        
        String resposta = sin.readLine();
        if(resposta.equals("Sair")) return;
        out.println(2);
        out.println(resposta);
        
        System.out.println("Qual o preço que deseja pagar (€/hora):");
        System.out.print("Resposta: ");
        String valor = sin.readLine();
        out.println(valor);
        
        System.out.println(in.readLine());
        String ticket = in.readLine();
        
        while(!sin.readLine().equals(ticket));
        
        out.println(ticket);
    }
    
    private static void contaCorrente() throws IOException{
    
        out.println(3);
        
        System.out.println("\nUsername: " + in.readLine());
        System.out.println("Débito: " + in.readLine());
    }
    
    private static void menuInicial(){
        System.out.println("\nO que deseja fazer:");
        System.out.println("1. Registo");
        System.out.println("2. Autenticação");
        System.out.println("3. Sair");
        System.out.print("Resposta: ");
    }
    
    private static void menuPrincipal(){
        System.out.println("\nO que deseja fazer:");
        System.out.println("1. Reservar um servidor a pedido");
        System.out.println("2. Reservar uma instância em leilão");
        System.out.println("3. Consultar a sua conta corrente");
        System.out.println("4. Sair");
        System.out.print("Resposta: ");
    }
    
    public static void main(String args[]) throws IOException, UnknownHostException{
        Socket cs = new Socket("127.0.0.1", 9999);
        try{
            out = new PrintWriter(cs.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            sin = new BufferedReader(new InputStreamReader(System.in));

            int login = 0;

            while(true){
                menuInicial();

                String resposta = sin.readLine();
                int escolha = Integer.parseInt(resposta);
                out.println(escolha);

                if(escolha == 1) registo();
                if(escolha == 2){
                    login = autenticacao();
                    if(login == 1) break;
                }
                if(escolha == 3) break;
            }

            if(login==1)
                while(login==1){
                    menuPrincipal();

                    String resposta = sin.readLine();
                    int escolha = Integer.parseInt(resposta);

                    if(escolha == 1) reservaPedido();
                    if(escolha == 2) reservaLeilao();
                    if(escolha == 3) contaCorrente();
                    if(escolha == 4) {out.println(4); break;}
                }
        
        } finally{
            in.close();
            out.close();
            cs.close();
        }
    }
}