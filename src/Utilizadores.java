import java.io.*;
import java.net.*;
import java.util.concurrent.locks.*;
import java.util.Map;
import java.util.HashMap;

class Utilizador{
    String username;
    String password;
    Double debt;
    boolean autenticado;
    
    public Utilizador(String nomen, String passwordn, Double dividan){
        username = nomen;
        password = passwordn;
        debt = dividan;
        autenticado = false;
    }
    
    public String getUsername(){return username;}
    
    public String getPassword(){return password;}
    
    public Double getDebt(){return debt;}
    
    public void setDebt(Double acrescimo){debt += acrescimo;}
    
    public boolean getAutenticado(){return autenticado;}
    
    public void setAutenticado(boolean autenticadon){autenticado = autenticadon;}
}

class Utilizadores{
    Map<String,Utilizador> user;
    
    public Utilizadores(){
        user = new HashMap<>();
        
        String nome = "joaop21";
        Utilizador novo = new Utilizador(nome,"joaopedro",0.0);
        user.put(nome,novo);
        
        Utilizador novo2 = new Utilizador("cenas","cenas",0.0);
        user.put("cenas",novo2);
        
    }
    
    public synchronized int autenticacao(String username, String password){
        Utilizador u = user.get(username);
        if(u == null) return 0;
        if(u.getAutenticado() == true) return 2;
        if(u.getPassword().equals(password)){
            u.setAutenticado(true);
            return 1;
        }
        else return 3;
    }
    
    public Utilizador getUtilizador(String utilizador){
        return user.get(utilizador);
    }
    
    public synchronized boolean addUtilizador(Utilizador novo){
        boolean existe = user.containsKey(novo.getUsername());
        if(existe) return false;
        else{
            user.put(novo.getUsername(), novo);
            return true;
        }
    }
}
