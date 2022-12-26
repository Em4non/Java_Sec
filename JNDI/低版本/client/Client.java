package JNDI.低版本.client;

import javax.naming.Context;
import javax.naming.InitialContext;

public class Client {
    public static void main(String[] args) throws Exception {
        String url = "rmi://127.0.0.1:1099/feng";
        //新建一个上下文对象
        Context context = new InitialContext();
        //从url获取具体的内容
        context.lookup(url);

    }
}