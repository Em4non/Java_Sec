package JNDI.高版本;

import javax.naming.InitialContext;
public class client {
    public static void main(String[] args)throws  Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.lookup("rmi://127.0.0.1:1099/Exploit");
    }
}