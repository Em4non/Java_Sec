# 一些Java的基础知识

对Java安全入门所需要的一些Java基础知识进行一些补充

# Java程序基本结构

- 运行已编译的程序时，Java虚拟机总是从指定类的main方法开始执行
- Java用双引号界定字符串
- 用（.）进行类底下的方法调用
- 在声明一个变量后，必须对其进行初始化，否则无法使用
- 用final指示常量，final表示这个常量只能被赋值一次

Java的注释：

- `//`
- `/**/`
- `/** */ `文档注释

# 对象和类

Java是一种面向对象进行编程的语言**（object-oriented programming,OOP）**

由类构造对象的过程称为创建类的实例

对象中的数据称为该实例中的字段

操作数据的过程（或者可以理解为一个类中的操作函数）称为方法

所有的类都默认继承Object类

**Java总是采用按值调用，并且只支持按值调用，也就是说，方法得到的是所有参数值的一个拷贝**，具体来讲，在某个方法内对参数值进行修改是不会改变参数原值的，因为在推出方法后会对修改的参数进行销毁

即对象引用是按值传递的

- 方法不能修改基本数据类型的参数。
- 方法可以改变对象参数的状态。
- 方法不能让一个对象参数引用一个新的对象。

简单的一个例子，在如下代码中，虽然我在swap中交换了a和b的对象，但是只是交换了他们的副本，他们的原对象并没有改变

```java
class test {
    int a;
    test(int t) {
        a = t;
    }
}
public class Main {
    static void swap(test a, test b) {
        test t;
        t = a;
        a = b;
        b = t;
    }
    public static void main(String[] args) {
        test a = new test(1);
        test b = new test(2);
        swap(a, b);
        System.out.println(a.a);
        System.out.println(b.a);
    }
}
```

Java有四种访问修饰符**【public】、【protected】、【default】、【private】**

![](https://raw.githubusercontent.com/Em4non/image-hosting/master/20220919125134.png)

简单的来说

public：公共的，被其修饰的类或者方法可以跨类和跨包访问

protected：只能被类本身或者继承他的类访问

default：默认访问权限，只能在同一个包中访问

private：只能被类本身访问

# 反射

能够分析类能力的程序称为**反射**。

那么想要了解反射，就必须从**正射**开始解释

当我们在实例化一个类的时候，我们必须先知道这个类是什么类，然后具体实例化，再对其进行各种操作

正常情况下，如果我们要调用一个对象的方法，或者访问一个对象的字段，通常会传入对象实例：

```java
import com.itranswarp.learnjava.Person;
public class Main {
    String getFullName(Person p) {
        return p.getFirstName() + " " + p.getLastName();//获取person实例化对象p的各种字段
    }
}
```

类似于如下进行实例化的操作，我们称作为正射

```java
Apple apple = new Apple(); //直接初始化，「正射」
apple.setPrice(4);
```

而**反射**则是一开始并不知道我要初始化的类对象是什么，自然也无法使用 new 关键字来创建对象了

```java
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
public class Apple {
    private int price;
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public static void main(String[] args) throws Exception {
        //使用反射调用
        Class test = Class.forName("Apple");//获取类的Class对象实例（也就是进行动态加载class）
        
        Method setPriceMethod = test.getMethod("setPrice", int.class);//获取方法的 Method 对象
        
        Constructor appleConstructor = test.getConstructor();//根据Class对象实例，获取Constructor对象
        
        Object appleObj = appleConstructor.newInstance();//使用Constructor对象的newInstance方法获取反射对象
        
        setPriceMethod.invoke(appleObj, 14);//利用 invoke 方法调用方法
        
        Method getPriceMethod = test.getMethod("getPrice");
        System.out.println("Apple Price:" + getPriceMethod.invoke(appleObj));
    }
}
```

反射机制可以用来：

- 在运行时分析类的能力。
- 在运行时检查对象。
- 实现泛型数组操作代码。
- 利用Method对象，这个对象很像C++中的函数指针

除了`int`等基本类型外，Java的其他类型全部都是`class`

class在java程序运行的过程中进行动态加载，每加载一个class，就创建一个Class类型的实例

例如：**String类**，当加载String类时，JVM将String.class读取到内存中，并创建一个Class实例、

这个实例存在于JVM内部，构造方法为private，每个Class实例都指向一个数据类型（class或者interface）

一个`Class`实例包含了该`class`的所有完整信息：

```ascii
┌───────────────────────────┐
│      Class Instance       │──────> String
├───────────────────────────┤
│name = "java.lang.String"  │
├───────────────────────────┤
│package = "java.lang"      │
├───────────────────────────┤
│super = "java.lang.Object" │
├───────────────────────────┤
│interface = CharSequence...│
├───────────────────────────┤
│field = value[],hash,...   │
├───────────────────────────┤
│method = indexOf()...      │
└───────────────────────────┘
```

通过JVM创建的这个Class实例，我们可以访问到该实例对应的信息，包括方法，类名等等，这种获取信息的方式就叫反射

## 访问实例

获取其实例的方法有三个

方法一：直接通过一个`class`的静态变量`class`获取：

```java
Class cls = String.class;
```

方法二：如果我们有一个实例变量，可以通过该实例变量提供的`getClass()`方法获取：

```java
String s = "Hello";
Class cls = s.getClass();
```

方法三：如果知道一个`class`的完整类名，可以通过静态方法`Class.forName()`获取：

```java
Class cls = Class.forName("java.lang.String");
```

## 访问字段

- Field getField(name)：根据字段名获取某个public的field（包括父类）
- Field getDeclaredField(name)：根据字段名获取当前类的某个field（不包括父类）
- Field[] getFields()：获取所有public的field（包括父类）
- Field[] getDeclaredFields()：获取当前类的所有field（不包括父类）

设置字段值是通过`Field.set(Object, Object)`实现的，其中第一个`Object`参数是指定的实例，第二个`Object`参数是待修改的值

```java
public class Main {

    public static void main(String[] args) throws Exception {
        Person p = new Person("Xiao Ming");//创建一个实例对象
        System.out.println(p.getName()); // "Xiao Ming"
        Class c = p.getClass();//获取Person类的Class对象实例
        Field f = c.getDeclaredField("name");//获取Person的name字段
        f.setAccessible(true);//无论该字段为何种类型，都允许访问
        f.set(p, "Xiao Hong");//修改字段值
        System.out.println(p.getName()); // "Xiao Hong"
    }
}

class Person {
    private String name;
    public Person(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
}
```

## 调用方法

我们之前说过，通过Class实例进行反射，可以获取这个类的所有信息，刚才讲了字段，接下来就是如何获取这个类的方法（Method）

- `Method getMethod(name, Class...)`：获取某个`public`的`Method`（包括父类）
- `Method getDeclaredMethod(name, Class...)`：获取当前类的某个`Method`（不包括父类）
- `Method[] getMethods()`：获取所有`public`的`Method`（包括父类）
- `Method[] getDeclaredMethods()`：获取当前类的所有`Method`（不包括父类）

那么这个获取到的Method对象，其中有什么功能呢，一个`Method`对象包含一个方法的所有信息：

- `getName()`：返回方法名称，例如：`"getScore"`
- `getReturnType()`：返回方法返回值类型，也是一个Class实例，例如：`String.class`
- `getParameterTypes()`：返回方法的参数类型，是一个Class数组，例如：`{String.class, int.class}`
- `getModifiers()`：返回方法的修饰符，它是一个`int`，不同的bit表示不同的含义

那我们该如何进行调用呢，下面举一个简单的例子

```java
//正射调用
String s = "Hello world";
String r = s.substring(6); // "world"

//反射调用
// String对象:
String s = "Hello world";
// 获取String substring(int)方法，参数为int:
Method m = String.class.getMethod("substring", int.class);
// 在s对象上调用该方法并获取结果:
String r = (String) m.invoke(s, 6);
```

对`Method`实例使用`invoke`就相当于调用该方法，`invoke`方法的第一个参数是对象实例，即在哪个实例上调用该方法，后面的可变参数要与获取的方法参数一致，否则将报错

需要注意，在反射调用方法时，仍遵循多态原则

## 调用构造方法

通过反射来创建新的实例，可以调用Class提供的newInstance()方法：

```java
Person p = Person.class.newInstance();
```

通过Class实例获取Constructor的方法如下：

- `getConstructor(Class...)`：获取某个`public`的`Constructor`
- `getDeclaredConstructor(Class...)`：获取某个`Constructor`
- `getConstructors()`：获取所有`public`的`Constructor`
- `getDeclaredConstructors()`：获取所有`Constructor`

## 获取继承关系

有了`Class`实例，我们还可以获取它的父类的`Class`

```java
public class Main {
    public static void main(String[] args) throws Exception {
        Class i = Integer.class;
        Class n = i.getSuperclass();
        System.out.println(n);
        Class o = n.getSuperclass();
        System.out.println(o);
        System.out.println(o.getSuperclass());
    }
}
```

## 动态代理

先看一下`class`和`interface`的区别：

- 可以实例化`class`（非`abstract`）
- 不能实例化`interface`

有没有可能不编写实现类，直接在运行期创建某个`interface`的实例，这就需要Java提供的动态代理（Dynamic Proxy）机制，可以在运行时动态创建某个`interface`的实例

所谓动态，相对而言就是静态

定义接口：

```java
public interface Hello {
    void morning(String name);
}
```

编写实现类：

```java
public class HelloWorld implements Hello {
    public void morning(String name) {
        System.out.println("Good morning, " + name);
    }
}
```

创建实例，转型为接口并调用：

```java
Hello hello = new HelloWorld();
hello.morning("Bob");
```

在运行期动态创建一个`interface`实例的方法如下：

1. 定义一个`InvocationHandler`实例，它负责实现接口的方法调用；

2. 通过`Proxy.newProxyInstance()`创建`interface`实例，它需要3个参数：

   - 使用的`ClassLoader`，通常就是接口类的`ClassLoader`

   - 需要实现的接口数组，至少需要传入一个接口进去

   - 用来处理接口方法调用的`InvocationHandler`实例

3. 将返回的`Object`强制转型为接口。

举个例子

```java
public class Main {
    public static void main(String[] args) {
        InvocationHandler handler = new InvocationHandler() {
            @Override//重写invoke方法，通过动态代理对调用的方法进行处理
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println(method);
                if (method.getName().equals("morning")) {
                    System.out.println("Good morning, " + args[0]);
                }
                return null;
            }
        };
        Hello hello = (Hello) Proxy.newProxyInstance(
            Hello.class.getClassLoader(), // 传入ClassLoader
            new Class[] { Hello.class }, // 传入要实现的接口
            handler); // 传入处理调用方法的InvocationHandler
        hello.morning("Bob");
    }
}

interface Hello {
    void morning(String name);
}
```

# 异常

在Java中，当程序在运行过程中出错的时候，或者说不能以我们预期的方法完成它的任务时，可以通过另外一个路径退出方法。在这种情况下，方法并不返回任何值，而是抛出了一个封装了错误信息的对象，异常处理机制开始搜索能够处理这种异常状况的异常处理器

异常对象都是派生于`Throwable`类的一个类实例。如果Java中内置的异常类不能满足需求，用户还可以创建自己的异常类。

所有的异常都是有`Throwable`继承而来，但在下一层立即分解为两个分支：`Error`和`Exception`

`Erroe`类层次结构描述了Java运行时系统的内部错误和资源耗尽错误

`Exception`类层次结构又分解为两个分支：一个分支派生于`RuntimeException`；另一个分支包含其他异常。一般的规则是由编程错误导致的异常属于`RuntimeException`；如果程序本身没有问题，但由于像`I/O`错误这类问题导致的异常属于其他异常（`IOException`）

Java语言规范将派生于`Error`类或`RuntimeException`类的所有异常称为非检查型异常，所有其他的异常称为检查型异常

















