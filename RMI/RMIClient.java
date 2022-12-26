package RMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

// 客户端

public class RMIClient {
    public static void main(String[] args){
        try {
            HelloInterface h = (HelloInterface) Naming.lookup("rmi://localhost:1099/hello"); // 寻找RMI实例远程对象
            System.out.println(h.Hello("lkf"));
        }catch (MalformedURLException e) {
            System.out.println("url格式异常");
        } catch (RemoteException e) {
            System.out.println("创建对象异常");
        } catch (NotBoundException e) {
            System.out.println("对象未绑定");
        }
    }
}