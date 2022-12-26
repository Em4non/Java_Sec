package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

// 定义一个远程接口，继承java.rmi.Remote接口

public interface HelloInterface extends Remote {
    String Hello(String age)throws RemoteException;
}
