package RMI;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

// 服务端

public class RMIServer {
    public static void main(String[] args) {
        try {
            HelloInterface h  = new RemoteHelloWorld(); // 创建远程对象HelloImp对象实例
            LocateRegistry.createRegistry(1099); // 获取RMI服务注册器
            Naming.rebind("rmi://localhost:1099/hello",h); // 绑定远程对象HelloImp到RMI服务注册器
            System.out.println("RMIServer start successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}