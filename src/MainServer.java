import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;
import java.util.Map;
import java.util.HashMap;

class MainServer{
    
    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(9999);
        
        Utilizadores users = new Utilizadores();
        
        Servers servers = new Servers(users);
        
        while(true){
            Socket cs = ss.accept();
            new Thread(new MainThread(cs, users, servers)).start();
        }
    }
    
}