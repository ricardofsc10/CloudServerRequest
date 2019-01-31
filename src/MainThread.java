import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;
import java.util.Map;
import java.util.HashMap;

class MainThread implements Runnable{
    private PrintWriter out;
    private BufferedReader in;
    Socket cs;
    Utilizadores users;
    Servers servers;
    String owner_user_thread;
    
    public MainThread(Socket csn, Utilizadores usersn, Servers serversn){
        cs = csn;
        users = usersn;
        servers = serversn;
        owner_user_thread = null;
    }
    
    private int autenticacao(){
        int validado = 0;
        try{
            String current_username;
            String current_password;

            current_username = in.readLine();
            current_password = in.readLine();
                
            validado = users.autenticacao(current_username, current_password);
            out.println(validado);
            
            if(validado == 1) owner_user_thread = current_username;

        } catch(Exception e){
            e.printStackTrace();
        }
        
        return validado;
    }
    
    private void registo(){
        try{
            String current_username;
            String current_password;

            current_username = in.readLine();
            current_password = in.readLine();
            
            Utilizador novo = new Utilizador(current_username, current_password, 0.0);
                
            boolean registado = users.addUtilizador(novo);
            out.println(registado);

        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void reservaPedido() throws IOException{
        String current_server;
        
        current_server = in.readLine();
        
        servers.reservaPedido(current_server,owner_user_thread,cs);
        
    }
    
    private void reservaLeilao() throws IOException{
        String current_server;
        String current_valor;
        
        current_server = in.readLine();
        current_valor = in.readLine();
        
        servers.reservaLeilao(current_server,Double.parseDouble(current_valor), owner_user_thread, cs);
        
    }
    
    private void contaCorrente(){
        
        Utilizador novo = users.getUtilizador(owner_user_thread);
    
        out.println(novo.getUsername());
        out.println(novo.getDebt());
    }
    
    public void run(){
        try{
            out = new PrintWriter(cs.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            
            boolean autenticado = false;
            
            while(true){
                String current_escolha = in.readLine();
                int escolha = Integer.parseInt(current_escolha);

                if(escolha == 1) registo();
                if(escolha == 2) {
                    if(autenticacao() == 1){
                        autenticado = true;
                        break;
                    }
                }
                if(escolha == 3) break;
            }
            
            if(autenticado)
                while(autenticado){
                    String current_escolha = in.readLine();
                    int escolha = Integer.parseInt(current_escolha);

                    if(escolha == 1) reservaPedido();
                    if(escolha == 2) reservaLeilao();
                    if(escolha == 3) contaCorrente();
                    if(escolha == 4) return;
                }
            
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            try{
                Utilizador u = users.getUtilizador(owner_user_thread);
                if(u!=null)u.setAutenticado(false);
                in.close();
                out.close();
                cs.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}