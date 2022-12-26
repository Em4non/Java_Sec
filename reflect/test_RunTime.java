package reflect;

import java.lang.reflect.Method;
import java.lang.Runtime;
public class test_RunTime {
    public static void main(String[] args) throws Exception {

        /**
         * Class clazz = Class.forName("java.lang.Runtime");
         * Method execMethod = clazz.getMethod("exec", String.class);
         * Method getRuntimeMethod = clazz.getMethod("getRuntime");
         * Object runtime = getRuntimeMethod.invoke(clazz);
         * execMethod.invoke(runtime, "calc.exe");
         */



          //Runtime r=Runtime.getRuntime();
          //r.exec("calc.exe");


        //Class clazz = Class.forName("java.lang.Runtime");
        //clazz.getMethod("exec", String.class).invoke(clazz.newInstance(), "id");
    }

}
