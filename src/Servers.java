import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class ThreadServer implements Runnable{
    Utilizadores users;
    Double preco; // ou valor
    String owner_user_server;
    Socket cs;
    int ticket_atual;
    String id_name;
    
    public ThreadServer(Utilizadores usersn, Double precon, String owner, Socket csn, int ticket_atualn, String id_namen){
        users = usersn;
        preco = precon;
        owner_user_server = owner;
        cs = csn;
        ticket_atual = ticket_atualn;
        id_name = id_namen;
    }
    
    public void run(){
        long tempoInicio = System.currentTimeMillis();
        try{
            PrintWriter out = new PrintWriter(cs.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            
            String mensagem = "Adquiriu um server do tipo " + id_name + ". Para sair insira o seu identificador: " + ticket_atual;
            out.println(mensagem);
            
            out.println(ticket_atual);

            in.readLine();
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            long tempoTotal = System.currentTimeMillis()-tempoInicio;
            Double precoFinal = (preco * tempoTotal)/3600000.0;

            users.getUtilizador(owner_user_server).setDebt(precoFinal);
        }
        
    }
}

class ValoresLeilao{
    Lock lockvl = new ReentrantLock();
    Map <String,Double> valores_leilao;
    
    ValoresLeilao(){
        valores_leilao = new HashMap<>();
    }
    
    public void lockValores(){lockvl.lock();}
    
    public void unlockValores(){lockvl.unlock();}
    
    public void adiciona(String user, Double valor){
        valores_leilao.put(user,valor);
    }
    
    public void remove(String user){
        valores_leilao.remove(user);
    }
    
    public String maiorLicitacao(){
        Double maior = 0.0;
        String userMaior = "";
        
        for(Map.Entry<String, Double> pair : valores_leilao.entrySet()){
            if(pair.getValue() > maior){
                maior = pair.getValue();
                userMaior = pair.getKey();
            }
        }
        
        return userMaior;
    }
}

class Server{
    Lock l = new ReentrantLock();
    Condition leilao_server_ocup = l.newCondition();
    Condition pedido_server_ocup = l.newCondition();
    String id_name; // nome do servidor
    Double preco; // preço nominal deste tipo de servidor
    final int total_servers_pedido; // numero de servidores para reservas a pedido
    final int total_servers_leilao; // numero de servidores para reservas a leilão
    volatile int num_servers_pedido; // numero de servidores em uso para reservas a pedido
    volatile int num_servers_leilao; // numero de servidores em uso para reservas a leilao
    volatile int ticket_atual; // identificador
    Utilizadores users;
    ValoresLeilao valores_leilao;
    int num_pedido; // numero de threads à espera de obterem reservas a pedido
    int num_leilao; // numero de threads à espera de obterem reservas a leilao
    String proximo_a_entrar; // proxima thread a entrar, user com a maior oferta do momento em espera
    
    
    public Server(String id_namen, Double precon, Utilizadores usersn, int num_pedido, int num_leilao){
        id_name = id_namen;
        preco = precon;
        total_servers_pedido = num_pedido;
        total_servers_leilao = num_leilao;
        num_servers_pedido = 0;
        num_servers_leilao = 0;
        ticket_atual = 0;
        users = usersn;
        num_pedido = 0;
        num_leilao = 0;
        valores_leilao = new ValoresLeilao();
        proximo_a_entrar = "";
    }
    
    public String getId_name(){return id_name;}
    
    public int reservaPedido() throws InterruptedException{
        l.lock();
        try{
            num_pedido++;
            while(num_servers_pedido == total_servers_pedido)
                pedido_server_ocup.await();
            
            num_servers_pedido++;
            num_pedido--;
            ticket_atual++;
            return ticket_atual;
        } finally{
            l.unlock();
        }
    }
    
    public int reservaLeilao(Double valor, String owner_user_thread) throws InterruptedException{
        l.lock();
        try{
            num_leilao++;
            
            valores_leilao.lockValores();
            try{
            valores_leilao.adiciona(owner_user_thread,valor);
            } finally{
                valores_leilao.unlockValores();
            }
            
            if(num_leilao == 1) proximo_a_entrar = owner_user_thread;
            while((num_servers_leilao == total_servers_leilao) || (!owner_user_thread.equals(proximo_a_entrar)))
                leilao_server_ocup.await();
            
            num_servers_leilao++;
            num_leilao--;
            ticket_atual++;
            return ticket_atual;
        }
        finally{l.unlock();}
    }
    
    public void atribuiServidorPedido(int ticket, String owner_user_thread, Socket csn) throws InterruptedException{
        Thread t = new Thread(new ThreadServer(users,preco,owner_user_thread,csn,ticket,id_name));
        t.start();
        t.join();
        
        l.lock();
        try{
            num_servers_pedido--;
            pedido_server_ocup.signal();
        } finally{
            l.unlock();
        }
    }
    
    public void atribuiServidorLeilao(int ticket, String owner_user_thread, Double valor, Socket csn) throws InterruptedException{
        Thread t = new Thread(new ThreadServer(users,valor,owner_user_thread,csn,ticket_atual,id_name));
        t.start();
        t.join();
        
        l.lock();
        try{
            num_servers_leilao--;
            
            valores_leilao.lockValores();
            try{
                valores_leilao.remove(owner_user_thread);
                proximo_a_entrar = valores_leilao.maiorLicitacao();
            } finally{
                valores_leilao.unlockValores();
            }
            
            leilao_server_ocup.signalAll();
        } finally{
            l.unlock();
        }
    }
}

class Servers{
    Map<String,Server> server;
    
    public Servers(Utilizadores users){
        server = new HashMap<>();
        
        Server s1 = new Server("S",0.99,users,5,5);
        Server s2 = new Server("M",1.99,users,3,3);
        Server s3 = new Server("L",2.99,users,1,2);
        Server s4 = new Server("XL",3.99,users,4,1);
        Server s5 = new Server("XXL",4.99,users,6,7);
        
        server.put(s1.getId_name(),s1);
        server.put(s2.getId_name(),s2);
        server.put(s3.getId_name(),s3);
        server.put(s4.getId_name(),s4);
        server.put(s5.getId_name(),s5);
    }
    
    public void reservaPedido(String id_name, String owner_user_thread, Socket cs){
        try{
            Server s = server.get(id_name);
            int ticket = s.reservaPedido();
            s.atribuiServidorPedido(ticket,owner_user_thread,cs);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void reservaLeilao(String id_name, Double valor, String owner_user_thread, Socket cs){
        try{
            Server s = server.get(id_name);
            int ticket = s.reservaLeilao(valor, owner_user_thread);
            s.atribuiServidorLeilao(ticket,owner_user_thread,valor,cs);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
