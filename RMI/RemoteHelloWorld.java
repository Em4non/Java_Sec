package RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// 远程接口实现类，继承UnicastRemoteObject类和Hello接口

public class RemoteHelloWorld extends UnicastRemoteObject implements HelloInterface {

    private static final long serialVersionUID = 1L;

    protected RemoteHelloWorld() throws RemoteException {
        super(); // 调用父类的构造函数
    }

    @Override
    public String Hello(String age) throws RemoteException {
        return "Hello" + age; // 改写Hello方法
    }
}