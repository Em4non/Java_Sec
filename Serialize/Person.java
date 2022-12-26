package Serialize;
import java.io.*;

public class Person implements java.io.Serializable {
    public String name;
    public int age;
    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject("This is a object");//在默认的序列化方法执行完成后，又额外写入了一个字符串
    }
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        String message = (String) s.readObject();
        System.out.println(message);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Person person=new Person("zjm",18);
        ByteArrayOutputStream buff=new ByteArrayOutputStream();
        ObjectOutputStream out=new ObjectOutputStream(buff);
        out.writeObject(person);
        ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(buff.toByteArray()));
        Person newPerson= (Person) in.readObject();
    }

}