# Spring源码

## Inversion of Control

### 什么是IoC

> 原来我们需要自己手动创建对象

```java
Test test = new Test();
```

> 控制反转 -> 获取对象的方式被反转了

![image-20230206220403788](https://article.biliimg.com/bfs/article/598b3491595f3a3696c4966056717d7911b83640.png)

> **IoC容器是工厂模式的实例**
>
> **IoC容器负责来创建类实例对象，需要从IoC容器中get获取。IoC容器我们也称为Bean工厂**
>
> **`bean`：组件,也就是类的对象!**

![image-20230206233921048](https://article.biliimg.com/bfs/article/8c7455b7034df73b9f96f672c4016e9ca99768ca.png)

### IoC的优点

- 代码更加简洁，不需要new对象
- 面向接口编程，使用者与具体类，解耦，易扩展、替换实现者
- 可以方便进行AOP编程

### IoC容器做了什么工作

> **负责创建，管理类实例，向使用者提供实例**

![image-20230206233240548](https://article.biliimg.com/bfs/article/8157754d5c5d0fa0cfb9280fc6864b60071323a0.png)

## 手写IoC实现

### BeanFactory作用

> - 创建-管理`Bean`，同时对外提供`Bean`的实例

### BeanFactory设计 

> - 由于BeanFactory需要对外提供`Bean`实例，所以需要一个`getBean()`方法
>
> - BeanFactory需要知道生产哪个类型的`bean`，所以需要接受`bean`的名称来获取到对应的bean类型，返回对应的实例
> - 由于`bean`是有多个以`key-value`形式记录在Map中，所以返回类型是不确定且不唯一的，我们使用`Object`类型作为返回类型
>   - 这边作为接口防止实现类中抛出异常，咱们手动`throws Exception`，这样无论实现类中抛出什么异常都可以接住了	

```java
/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:08 AM
 */
public interface BeanFactory {
    Object getBean(String beanName) throws Exception;
}
```

> 上面定义了Bean工厂对外提供`bean`实例的方法，那么BeanFactory怎么知道该创建哪个对象，又是怎么创建对象的呢？
>
> **首先我们得把Bean的定义信息告诉`BeanFactory`,然后`BeanFactory`根据Bean的定义信息来生成对应的bean实力对象**
>
> - 因此我们需要定义一个模型（`BeanDefinition`）来表示`如何创建Bean实例的信息`
> - 另外，`BeanFactory`也需要提供一个入参来接收`Bean的定义信息`

### BeanDefinition

#### Bean定义的作用

> 告诉`BeanFactory`应该如何创建某个类的`Bean实力`

#### 获取实例的方式有哪些

##### 构造方法

> `BeanDefinition`需要`给BeanFactory`提供`类名`

```java
Person person = new Person();
```

##### 工厂静态方法

> `BeanDefinition`需要`给BeanFactory`提供`工厂类名`、`工厂方法名`

```java
public interface PersonFactory {
    public static Person getPerson(){
      return new Person();
    }
}
```

##### 工厂成员方法

> `BeanDefinition`需要`给BeanFactory`提供`工厂bean名`、`工厂方法名`

```java
public interface PersonFactory {
    public Person getPerson(){
      return new Person();
    }
}
```

> 因此我们需要在`BeanDefinition`接口中具备以下几种功能

![image-20230214004511598](https://article.biliimg.com/bfs/article/6eac69528b22ea29a06f35d9b4b96b3f5a440bb4.png)

```java
public interface BeanDefinition {
    Class<?> getBeanClass();

    String getFactoryBeanName();

    String getFactoryMethodName();
}
```

#### BeanDefinition增强功能

> - 在现有基础上增加单例对象的初始化和销毁功能等
> - 同时创建一个通用实现类`GenericBeanDefinition`

![image-20230214023112763](https://article.biliimg.com/bfs/article/9c97a8fd005a97b5d3114146afcb206d25f978cb.png)

```java
package com.rhys.spring.IoC;

import org.apache.commons.lang.StringUtils;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:09 AM
 */
public interface BeanDefinition {

    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * 对外提供具体的Bean类
     *
     * @param
     * @return java.lang.Class<?>
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    Class<?> getBeanClass();

    /**
     * 对外提供工厂bean名
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return java.lang.String
     */
    String getFactoryBeanName();

    /**
     * 对外提供工厂方法名
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return java.lang.String
     */
    String getFactoryMethodName();

    /**
     * 初始化方法
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return java.lang.String
     */
    String getInitMethodName();

    /**
     * 销毁方法
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return java.lang.String
     */
    String getDestroyMethodName();

    /**
     * 作用域
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return java.lang.String
     */
    String getScope();

    /**
     * 是否是单例
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return boolean
     */
    boolean isSingleton();

    /**
     * 是否是原型
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return boolean
     */
    boolean isPrototype();

    /**
     * 是否是主要
     * @author Rhys.Ni
     * @date 2023/2/14
     * @param
     * @return boolean
     */
    boolean isPrimary();


    /**
     * 校验bean定义的合法性
     *
     * @param
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/7
     */
    default boolean validate() {
        //没定义class,工厂bean或工厂方法没指定的均不合法
        if (this.getBeanClass() == null) {
            if (StringUtils.isBlank(this.getFactoryBeanName()) || StringUtils.isBlank(this.getFactoryMethodName())) {
                return false;
            }
        }

        //定义了类，又定义工厂bean认为不合法，这里都不为空则满足以上条件，任意一个为空则校验通过
        return this.getBeanClass() == null || StringUtils.isBlank(this.getFactoryBeanName());
    }
}

```

#### BeanDefinition默认实现

![image-20230301020607759](https://article.biliimg.com/bfs/article/0b6a01b83fe9dbf1e8f51a14a37cbf72aaad5fc5.png)

```java
public class GenericBeanDefinition implements BeanDefinition {

    private Class<?> beanClass;

    private String scope = SCOPE_SINGLETON;

    private String factoryBeanName;

    private String factoryMethodName;

    private String initMethodName;

    private String destoryMethodName;

    private boolean primary;

    /**
     * 对外提供具体的Bean类
     *
     * @return java.lang.Class<?>
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    /**
     * 对外提供工厂bean名
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }

    /**
     * 对外提供工厂方法名
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }

    /**
     * 初始化方法
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public String getInitMethodName() {
        return this.initMethodName;
    }

    /**
     * 销毁方法
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public String getDestroyMethodName() {
        return this.destoryMethodName;
    }

    /**
     * 作用域
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public String getScope() {
        return this.scope;
    }

    /**
     * 是否是单例
     *
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope);
    }

    /**
     * 是否是原型
     *
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    /**
     * 是否是主要
     *
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public boolean isPrimary() {
        return this.primary;
    }


    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public void setScope(String scope) {
        if (StringUtils.isNotBlank(scope)) {
            this.scope = scope;
        }
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    public void setDestoryMethodName(String destoryMethodName) {
        this.destoryMethodName = destoryMethodName;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GenericBeanDefinition that = (GenericBeanDefinition) o;

        return new EqualsBuilder()
                .append(primary, that.primary)
                .append(beanClass, that.beanClass)
                .append(scope, that.scope)
                .append(factoryBeanName, that.factoryBeanName)
                .append(factoryMethodName, that.factoryMethodName)
                .append(initMethodName, that.initMethodName)
                .append(destoryMethodName, that.destoryMethodName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(beanClass).append(scope)
                .append(factoryBeanName)
                .append(factoryMethodName)
                .append(initMethodName)
                .append(destoryMethodName)
                .append(primary)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "GenericBeanDefinition{" +
                "beanClass=" + beanClass +
                ", scope='" + scope + '\'' +
                ", factoryBeanName='" + factoryBeanName + '\'' +
                ", factoryMethodName='" + factoryMethodName + '\'' +
                ", initMethodName='" + initMethodName + '\'' +
                ", destoryMethodName='" + destoryMethodName + '\'' +
                ", primary=" + primary +
                '}';
    }
}
```

### BeanDefinitionRegistry

> 清楚了`BeanDefinition`与`BeanFactory`后，那么他们之间有什么关联呢？
>
> - 需要定义一个`BeanDefinitionRegistry`用来实现`BeanDefinition`的注册功能
> - `BeanDefinitionRegistry`需要具备`注册BeanDefinition`和`获取BeanDefinition`能力
> - 为了能区分每个`Bean`的定义信息，还需要给每个`Bean`定义一个唯一名称

![image-20230216112804729](https://article.biliimg.com/bfs/article/3089c16936978dcc654c271f688b5f62954749ee.png)

```java
/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:09 AM
 */
public interface BeanDefinitionRegistry {

    /**
     * 注册BeanDefinition
     *
     * @param beanName       - bean名称
     * @param beanDefinition - bean定义
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    void registryBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * 获取BeanDefinition
     *
     * @param beanName - bean名称
     * @return com.rhys.spring.IoC.BeanDefinition
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * 判断是否已经存在
     *
     * @param beanName - bean名称
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    boolean containsBeanDefinition(String beanName);
}
```

### DefaultBeanFactory实现

> 综上，目前已经实现的相关功能设计如下：
>
> - 已经设计好了`BeanFactory`、`BeanDefinition`、`BeanDefinitionRegistry`
> - 现在需要实现一个默认的Bean工厂`DefaultBeanFactory`

![image-20230301021118560](https://article.biliimg.com/bfs/article/f243aefcbd373e891781f846450f6f278d9eb473.png)

> 实现`BeanDefinitionRegistry`,这里有以下几个注意点
>
> - 如何存储`BeanDefinition`
>   - 使用`Map<String,Object>`
> - `beanName`重名怎么办
>   - Spring中`默认`是`不可覆盖`，直接抛异常
>   - 可通过参数 `spring.main.allow-bean-definition-overriding: true `来允许覆盖
> - 先实现`registryBeanDefinition`、`getBeanDefinition`、`containsBeanDefinition`
>   - 这里需要先定义一个`beanDefinitionMap`来存放合法的`beanDefinition`
>   - 后面比较以及获取`beanDefinition`都以这个Map中的数据为基准

```java
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    /**
     * 注册BeanDefinition
     *
     * @param beanName       bean名称
     * @param beanDefinition bean定义
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public void registryBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegistryException {
        //入参判空
        Objects.requireNonNull(beanName, "beanName不能为空");
        Objects.requireNonNull(beanDefinition, "beanDefinition不能为空");

        //校验bean合法性  beanDefinition named a is invalid
        if (!beanDefinition.validate()) {
            throw new BeanDefinitionRegistryException("beanDefinition named " + beanName + " is invalid !");
        }

        //校验beanName是否已存在，重复则抛异常
        if (this.containsBeanDefinition(beanName)) {
            throw new BeanDefinitionRegistryException(beanName + " Already exists ! " + this.getBeanDefinition(beanName));
        }

        //存储成功注册的bean以及beanDefinition
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    /**
     * 获取BeanDefinition
     *
     * @param beanName bean名称
     * @return com.rhys.spring.beans.BeanDefinition
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        //从beanDefinitionMap中获取注册成功的bean定义
        return this.beanDefinitionMap.get(beanName);
    }

    /**
     * 判断是否已经存在
     *
     * @param beanName bean名称
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/14
     */
    @Override
    public boolean containsBeanDefinition(String beanName) {
        //对比beanDefinitionMap中是否存在相同key
        return this.beanDefinitionMap.containsKey(beanName);
    }
}
```

> 实现初始化方法的执行,在下面`getBean`方法中将`创建实例`时会执行所传入的`BeanDefinition`中的`初始化方法`,所以先实现一下

```java
/**
 * 初始化方法
 *
 * @param beanDefinition
 * @param instance
 * @return void
 * @author Rhys.Ni
 * @date 2023/2/17
 */
private void init(BeanDefinition beanDefinition, Object instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (StringUtils.isNotBlank(beanDefinition.getInitMethodName())) {
        //获取所传入实例的初始化方法名称进行调用
        Method method = instance.getClass().getMethod(beanDefinition.getInitMethodName(), null);
        method.invoke(instance, null);
    }
}
```

> 实现`BeanFactory`的`getBean`方法
>
> 这里边要注意`单例的实现`:
>
> - 单例存储在哪里
>   - 与`BeanDefinition`存储方式相同,定义一个私有成员变量`singletonBeanMap`来存储单例数据
> - 怎么保证单例
>   - DCL单例

```java
/**
 * 获取Bean实例
 *
 * @param beanName bean名称
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/16
 */
@Override
public Object getBean(String beanName) throws Exception {
    return this.doGetBean(beanName);
}

private Object doGetBean(String beanName) throws Exception {
    //校验beanName
    Objects.requireNonNull(beanName, "beanName can not be empty !");

    //从单例Bean所存储的Map中根据beanName获取一下这个Bean实例，获取到直接返回
    Object beanInstance = singletonBeanMap.get(beanName);
    if (beanInstance != null) {
        return beanInstance;
    }

    //没获取到则根据BeanDefinition创建Bean实例并且存放到Map中
    BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
    Objects.requireNonNull(beanDefinition, "beanDefinition named " + beanName + " is invalid !");

    if (beanDefinition.isSingleton()) {
        synchronized (this.singletonBeanMap) {
            //双重检查，再次根据beanName从单例Map中尝试获取Bean实例，如果还是没有获取到再开始创建
            beanInstance = this.singletonBeanMap.get(beanName);
            if (beanInstance == null) {
                //创建实例
                beanInstance = createInstance(beanDefinition);
                //存到singletonBeanMap中
                singletonBeanMap.put(beanName, beanInstance);
            }
        }
    } else {
        //如果不要求为单例则直接创建,不用往单例BeanMap中存，所以不关心是否存在
        beanInstance = createInstance(beanDefinition);
    }
    return beanInstance;
}


private Object createInstance(BeanDefinition beanDefinition) throws Exception {
    //获取bean定义对应的类(类即类型)
    Class<?> beanClass = beanDefinition.getBeanClass();
    Object beanInstance = null;

    //根据beanClass中能获取到的信息来创建实例,beanClass为空则直接通过工厂bean方式创建实例
    if (beanClass != null) {
        //获取并判断工厂成员方法名是否为空，为空则根据构造方法创建实例，否则直接根据工厂静态方法创建实例
        if (StringUtils.isBlank(beanDefinition.getFactoryMethodName())) {
            beanInstance = this.createInstanceByConstructor(beanDefinition);
        } else {
            beanInstance = this.createInstanceByStaticFactoryMethod(beanDefinition);
        }
    } else {
        beanInstance = this.createInstanceByFactoryBean(beanDefinition);
    }

    //创建完实例执行初始化方法
    this.init(beanDefinition, beanInstance);

    return beanInstance;
}

/**
 * 工厂bean方式创建实例
 *
 * @param beanDefinition
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/20
 */
private Object createInstanceByFactoryBean(BeanDefinition beanDefinition) throws Exception {
    //根据工厂bean名称创建实例
    Object bean = this.doGetBean(beanDefinition.getFactoryBeanName());
    //再通过工厂bean的类获取工厂Bean成员方法名称创建最终的Bean
    Method method = bean.getClass().getMethod(beanDefinition.getFactoryBeanName(), null);
    return method.invoke(bean, null);
}

/**
 * 静态工厂方法
 *
 * @param beanDefinition
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/20
 */
private Object createInstanceByStaticFactoryMethod(BeanDefinition beanDefinition) throws Exception {
    Class<?> beanClass = beanDefinition.getBeanClass();
    Method method = beanClass.getMethod(beanDefinition.getFactoryMethodName(), null);
    return method.invoke(beanClass, null);
}

/**
 * 构造函数创建实例
 *
 * @param beanDefinition
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/20
 */
private Object createInstanceByConstructor(BeanDefinition beanDefinition) throws Exception {
    //除了InstantiationException和IllegalAccessException异常外，为了避免相关的程序使用不当可能会存在某些危险的操作从而引发安全问题，这里需要捕获一下SecurityException
    try {
        return beanDefinition.getBeanClass().newInstance();
    } catch (SecurityException exception) {
        logger.error("create instance error beanDefinition:{}, exception:{}", beanDefinition, exception);
        throw exception;
    }
}


/**
 * 初始化方法
 *
 * @param beanDefinition
 * @param instance
 * @return void
 * @author Rhys.Ni
 * @date 2023/2/17
 */
private void init(BeanDefinition beanDefinition, Object instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (StringUtils.isNotBlank(beanDefinition.getInitMethodName())) {
        //获取所传入实例的初始化方法名称进行调用
        Method method = instance.getClass().getMethod(beanDefinition.getInitMethodName(), null);
        method.invoke(instance, null);
    }
}
```

> 实现容器关闭是执行单例的销毁操作

```java
@Override
public void close() {
  //执行单例的销毁方法
  for (Map.Entry<String, BeanDefinition> entry : this.beanDefinitionMap.entrySet()) {
    String beanName = entry.getKey();
    BeanDefinition beanDefinition = entry.getValue();

    if (beanDefinition.isSingleton() && StringUtils.isNotBlank(beanDefinition.getDestroyMethodName())) {
      Object instance = this.singletonBeanMap.get(beanName);
      try {
        //获取bean的销毁方法名通过invoke执行
        Method method = instance.getClass().getMethod(beanDefinition.getDestroyMethodName(), null);
        method.invoke(instance, null);
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
        logger.error("bean destruction method named [" + beanName + "] failed to execute ! exception:{}",e);
      }
    }
  }
}
```

> - 所谓`单例`必然是只有一个对象，所以需要将单例Bean的实例化提前到初始化阶段，在系统启动的时候就去将单例的对象创建出来，避免在运行时调用`doGetBean`方法判断为单例再去执行创建
> - 因此我们需要实现一个`PreBuildBeanFactory`，将单例对象的创建操作放到这个里面去实现
> - 这种方案缺点就是增加了系统启动时的性能消耗
> - 优点就是减少了系统运行时的性能消耗
> - 因为系统启动的时候是`单线程`,提前创建单例也很好的解决了`getBean`的时候单例的保证,`双重检查`的时候也不会有问题了

![image-20230222170836138](https://article.biliimg.com/bfs/article/1278df1be57738088174eec9d914699bb0cd6e45.png)

```java
public class PreBuildBeanFactory extends DefaultBeanFactory {
    private static final Logger logger = LoggerFactory.getLogger(PreBuildBeanFactory.class);

    /**
     * <p>
     * <b>提前实例化单例</b>
     * </p >
     *
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/2/22
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    public void instantiateSingleton() throws Exception {
        synchronized (this.beanDefinitionMap) {
            for (Map.Entry<String, BeanDefinition> entry : this.beanDefinitionMap.entrySet()) {
                String beanName = entry.getKey();
                BeanDefinition beanDefinition = entry.getValue();
                //判断是否为单例
                if (beanDefinition.isSingleton()) {
                    //执行doGetBean进行单例对象的创建以及存储
                    this.getBean(beanName);
                }
            }
        }
    }
}
```

### BeanAlias别名实现

> Bean除了标识唯一的名称外，还可以有很多个别名，别名具备以下几个特点
>
> - 可以存在多个别名
> - 可以在有别名的基础上，给别名起别名
> - 别名需要保证唯一
>
> 因此，我们需要一个具备 `别名注册`相关功能的接口如下设计

![image-20230228003858909](https://article.biliimg.com/bfs/article/e4a7f11e3d30a22f26dbdaae6cbdeeb5964e43de.png)

> 别名的别名大概如下

![image-20230301024040172](https://article.biliimg.com/bfs/article/aa9757c476d5d817efeed702718bc1821aea2f2c.png)

```java
package com.rhys.spring.IoC;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/27 1048 PM
 */
public interface AliasRegistry {
    /**
     * 别名注册
     *
     * @param name
     * @param alias
     * @return void
     * @author Rhys.Ni
     * @date 2023/2/27
     */
    void registerAlias(String name, String alias);

    /**
     * 是否为别名
     *
     * @param name
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/2/27
     */
    boolean isAlias(String name);

    /**
     * 获取所有别名
     *
     * @param name
     * @return java.lang.String[]
     * @author Rhys.Ni
     * @date 2023/2/27
     */
    String[] getAliases(String name);

    /**
     * 获取原名
     *
     * @param name
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/2/27
     */
    String getOriginalName(String name);

    /**
     * 别名注销
     *
     * @param alias
     * @return void
     * @author Rhys.Ni
     * @date 2023/2/27
     */
    void removeAlias(String alias);
}
```

> `DefaultBeanFactory`中具体实现如下

```java
/**
 * 别名注册
 *
 * @param beanName
 * @param alias
 * @return void
 * @author Rhys.Ni
 * @date 2023/2/27
 */
@Override
public void registerAlias(String beanName, String alias) throws Exception {
    Objects.requireNonNull(beanName, "beanName cannot be empty !");
    Objects.requireNonNull(alias, "alias cannot be empty !");

    //跟已有的所有别名进行对比，校验唯一性，存在重复的直接抛异常
    if (this.checkAliasExists(alias)) {
        throw new AliasRegistryException("alias: [" + alias + "] already exists !");
    }

    //bean没有起过别名则直接添加，否则获取已定义的别名数组进行操作
    if (!aliasMap.containsKey(beanName)) {
        //不存在重复的别名直接添加
        String[] aliasArray = {alias};
        aliasMap.put(beanName, aliasArray);
    } else {
        String[] aliasArray = aliasMap.get(beanName);

        //将原数组所有数据拷贝至新数组中并且把新注册的别名添加到新数组最后
        String[] newAliasArray = Arrays.copyOf(aliasArray, aliasArray.length + 1);
        newAliasArray[aliasArray.length] = alias;

        //重置该key的值
        aliasMap.replace(beanName, newAliasArray);
    }
}

/**
 * 是否为别名
 *
 * @param name
 * @return boolean
 * @author Rhys.Ni
 * @date 2023/2/27
 */
@Override
public boolean isAlias(String name) {
    Objects.requireNonNull(name, "name cannot be empty !");

    //如果key存在该名称，说明是别名的别名，一定存在
    if (aliasMap.containsKey(name)) {
        return true;
    }
    return checkAliasExists(name);
}

/**
 * 获取所有别名
 *
 * @param name
 * @return java.lang.String[]
 * @author Rhys.Ni
 * @date 2023/2/27
 */
@Override
public String[] getAliases(String name) {
    return aliasMap.get(name);
}

/**
 * 获取原名
 *
 * @param name
 * @return java.lang.String
 * @author Rhys.Ni
 * @date 2023/2/27
 */
@Override
public String getOriginalName(String name) {
    Objects.requireNonNull(name, "alias cannot be empty !");

    //遍历aliasMap 找包含alias的key返回
    String beanName = null;

    for (Map.Entry<String, String[]> entry : this.aliasMap.entrySet()) {
        String key = entry.getKey();
        String[] value = entry.getValue();

        for (String alias : value) {
            if (alias.equals(name)) {
                beanName = key;
                break;
            }
        }
    }
    return beanName;
}

/**
 * 别名注销
 *
 * @param alias
 * @return void
 * @author Rhys.Ni
 * @date 2023/2/27
 */
@Override
public void removeAlias(String alias) {
    Objects.requireNonNull(alias, "alias cannot be empty !");

    for (Map.Entry<String, String[]> entry : this.aliasMap.entrySet()) {
        String key = entry.getKey();
        List<String> list = Arrays.asList(entry.getValue());
        for (String str : list) {
            if (str.equals(alias)) {
                list.remove(str);
                //检查是否有别名的别名，一同删除
                if (aliasMap.containsKey(str)) {
                    aliasMap.remove(str);
                }
            }
        }
        String[] newArray = (String[]) list.toArray();

        //重置该key的值
        aliasMap.replace(key, newArray);
    }
}

/**
 * 校验唯一性
 *
 * @param alias
 * @return boolean
 * @author Rhys.Ni
 * @date 2023/2/28
 */
private boolean checkAliasExists(String alias) {
    for (Map.Entry<String, String[]> entry : aliasMap.entrySet()) {
        String[] value = entry.getValue();
        for (String str : value) {
            if (str.equals(alias)) {
                return true;
            }
        }
    }
    return false;
}
```

### BeanType获取Bean实例对象

> 为了解决只能单一的通过`beanName`获取bean对象，中间需要进行强制类型转换，可能会出现异常情况，因此增加根据`Type`获取实例对象，`BeanFactory`修改设计如下
>
> - 提前把`Type`和`Bean`关系找出来缓存到`Map`中
>   - 在`BeanFactory`中新增一个`getType`方法提供`根据beanName获取beanClass类型`的功能
>   - 再到`DefaultBeanFactory`中添加一个`typeMap`容器，Map数据结构设计为`Map<Class<?>, Set<String>>`
>   - 最后通过`buildTypeMap()`方法来构建`Type-Bean`之间的映射关系
> - 因为`Class`可能对应`多个Type`,需要通过`Primary`来处理

![image-20230301031436711](https://article.biliimg.com/bfs/article/75cb17fd1383a1ec9a67f02ae3cf8bb440758315.png)

> `BeanFactory`增加功能方法

```java
package com.rhys.spring.IoC;

import java.util.List;
import java.util.Map;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:08 AM
 */
public interface BeanFactory {
    /**
     * 获取Bean实例
     *
     * @param beanName bean名称
     * @return java.lang.Object
     * @author Rhys.Ni
     * @date 2023/2/16
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 根据beanName获取Type
     *
     * @param beanName
     * @return java.lang.Class<?>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    Class<?> getType(String beanName) throws Exception;

    /**
     * 根据Type获取bean实例
     *
     * @param c
     * @return T
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    <T> T getBean(Class<T> c) throws Exception;

    /**
     * 根据Type获取bean实例
     *
     * @param c
     * @return java.util.Map<java.lang.String, T>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    <T> Map<String, T> getBeanOfType(Class<T> c) throws Exception;

    /**
     * 根据Type获取bean集合
     *
     * @param c
     * @return java.util.List<T>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    <T> List<T> getBeanListOfType(Class<T> c) throws Exception;
}
```

> `DefaultBeanFactory`中具体实现如下

```java
/**
 * 根据beanName获取Type
 *
 * @param beanName
 * @return java.lang.Class<?>
 * @author Rhys.Ni
 * @date 2023/3/1
 */
@Override
public Class<?> getType(String beanName) throws Exception {
    //根据beanName到beanDefinitionMap中获取对应的BeanDefinition
    BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
    //获取到具体的Type
    Class<?> beanClass = beanDefinition.getBeanClass();
    if (beanClass != null) {
        //factoryMethodName不为空则通过静态工厂方法构造对象,否则通过构造方法创建实例（beanClass本身就是Type）
        if (StringUtils.isNotBlank(beanDefinition.getFactoryMethodName())) {
            //反射得到Method,最终回去Method返回类型
            beanClass = beanClass.getDeclaredMethod(beanDefinition.getFactoryMethodName(), null).getReturnType();
        }
    } else {
        //beanClass为空，需要获取到工厂Bean的beanClass
        beanClass = this.getType(beanDefinition.getFactoryBeanName());
        //再通过反射得到Method,最终得到工厂方法的返回类型
        beanClass = beanClass.getDeclaredMethod(beanDefinition.getFactoryMethodName(), null).getReturnType();
    }
    return beanClass;
}

/**
 * 根据Type获取bean实例
 *
 * @param c
 * @return T
 * @author Rhys.Ni
 * @date 2023/3/1
 */
@Override
public <T> T getBean(Class<T> c) throws Exception {

    Set<String> beanNames = this.typeMap.get(c);
    if (beanNames != null) {
        //如果有只有一个直接获取Bean实例返回，否则遍历找出Primary
        if (beanNames.size() == 1) {
            return (T) this.getBean(beanNames.iterator().next());
        } else {
            BeanDefinition beanDefinition;
            String primaryName = null;
            StringBuilder beanNameStr = new StringBuilder();
            // 获得所有对应的BeanDefinition
            for (String beanName : beanNames) {
                beanDefinition = this.getBeanDefinition(beanName);
                if (beanDefinition != null && beanDefinition.isPrimary()) {
                    //存在多个Primary的Bean实例抛异常
                    if (primaryName != null) {
                        throw new PrimaryException("Bean of type 【" + c + "】 has more than one Primary !");
                    } else {
                        primaryName = beanName;
                    }
                }
                beanNameStr.append(" " + beanName);
            }

            //有primary则返回，没有则抛异常
            if (primaryName != null) {
                return (T) this.getBean(primaryName);
            } else {
                throw new PrimaryException("Multiple beans of type【" + c + "】exist but no Primary is found !");
            }
        }
    }
    return null;
}

/**
 * 根据Type获取bean实例
 *
 * @param c
 * @return java.util.Map<java.lang.String, T>
 * @author Rhys.Ni
 * @date 2023/3/1
 */
@Override
public <T> Map<String, T> getBeanOfType(Class<T> c) throws Exception {
    Set<String> beanNames = this.typeMap.get(c);
    if (beanNames != null) {
        HashMap<String, T> map = new HashMap<>();
        for (String beanName : beanNames) {
            map.put(beanName, (T) this.getBean(beanName));
        }
        return map;
    }
    return null;
}

/**
 * 根据Type获取bean集合
 *
 * @param c
 * @return java.util.List<T>
 * @author Rhys.Ni
 * @date 2023/3/1
 */
@Override
public <T> List<T> getBeanListOfType(Class<T> c) throws Exception {
    Set<String> beanNames = this.typeMap.get(c);
    if (beanNames != null) {
        List<T> beanList = new ArrayList<>();
        for (String beanName : beanNames) {
            beanList.add((T) this.getBean(beanName));
        }
        return beanList;
    }
    return null;
}


/**
 * 注册typeMap
 * @author Rhys.Ni
 * @date 2023/3/3
 * @param
 * @return void
 */
public void registerTypeMap() throws Exception {
    //获取type - name 映射关系，在所有的Bean定义信息都注册完成后执行
    for (String beanName : this.beanDefinitionMap.keySet()) {
        Class<?> type = this.getType(beanName);
        //映射本类
        this.registerTypeMap(beanName, type);
        //映射父类
        this.registerSuperClassTypeMap(beanName, type);
        //映射接口
        this.registerInterfaceTypeMap(beanName, type);
    }
}

/**
 * 注册typeMap 建立映射关系
 * @author Rhys.Ni
 * @date 2023/3/3
 * @param beanName
 * @param type
 * @return void
 */
private void registerTypeMap(String beanName, Class<?> type) {
    Set<String> beanNames2Type = this.typeMap.get(type);
    if (beanNames2Type != null) {
        //重置beanName - type 映射关系
        beanNames2Type = new HashSet<>();
        this.typeMap.put(type, beanNames2Type);
    }
    beanNames2Type.add(beanName);
}

/**
 * 递归注册父类实现的接口
 * @author Rhys.Ni
 * @date 2023/3/3
 * @param beanName
 * @param type
 * @return void
 */
private void registerSuperClassTypeMap(String beanName, Class<?> type) {
    Class<?> superclass = type.getSuperclass();
    if (superclass != null && !superclass.equals(Objects.class)) {
        this.registerTypeMap(beanName, superclass);
        //递归找父类
        this.registerSuperClassTypeMap(beanName, superclass);
        //找父类实现的接口
        this.registerInterfaceTypeMap(beanName, superclass);
    }
}


/**
 * 递归注册父接口
 * @author Rhys.Ni
 * @date 2023/3/3
 * @param beanName
 * @param type
 * @return void
 */
private void registerInterfaceTypeMap(String beanName, Class<?> type) {
    Class<?>[] interfaces = type.getInterfaces();
    if (interfaces.length > 0) {
        for (Class<?> anInterface : interfaces) {
            this.registerTypeMap(beanName, anInterface);
            //递归找父接口
            this.registerInterfaceTypeMap(beanName, anInterface);
        }
    }
}
```

### 总结

#### IoC核心类图总览

> -  BeanDefinition通过BeanDefinitionRegistry与BeanFactory关联起来
> - BeanFactory可以通过BeanDefinitionRegistry去找到想要的BeanDefinition
> - GenericBeanDefinition是BeanDefinition的默认实现
> - BeanDefinitionRegistry通过AliasRegistry实现了别名的处理（别名判断逻辑后续补充）
> - 最后在DeafultBeanFactory中进行BeanFactory、BeanDefinitionRegistry所有逻辑实现
> - PreBuildBeanFactory实现了单例Bean实例化提前到启动阶段

![image-20230301032645220](https://article.biliimg.com/bfs/article/9574c691f6eadc197717c7fa48260169fc300276.png)

## Dependency Injection

### 什么是DI

> - DI即为`依赖注入`，基于IoC所得到的Bean对象相关属性的赋值
>
> - 对象之间的依赖由容器在运行期间决定，即容器动态的将某个依赖注入到对象之中
>
> **依赖注入的本质是`给有参构造方法赋值`、`给属性赋值`**

### 哪些地方会有依赖

> - 构造参数依赖
> - 属性依赖

### 参数值|属性值有哪些

> `直接值`和`Bean依赖`
>
> 现有TestA类和TestB类 如下
>
> - `TestA`类中`a`、`b`、`c`这类的属性赋值就属于直接值
> - 然而`testB`属性对应的是`TestB`对象，这个对象的值（Bean实例）肯定是来自于`IoC`中,所以这就属于Bean依赖

```java
/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/8 12:21 AM
 */
public class TestA {
    private String a;
    private int b;
    private char c;
    private TestB testB;


    public TestA(String a, int b, char c, TestB testB) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.testB = testB;
    }
}

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/8 12:21 AM
 */
public class TestB {
  //...
}
```

### 构造注入

> 在前面实现的`DefaultBeanFactory`中的`createInstanceByConstructor`是根据无参构造创建的一个实例
>
> - 当我们现在需要通过有参构造进行创建实例的时候就不适用了，需要做一下优化
>   -  通过BeanDefinition获取到BeanClass之后
>   - 通过反射的内容去找到有参的构造对象
>   - 找到有参构造对象后结合传过来的实参得到对应的结果
> - 那么构造器中所关联的实参则需要在`BeanDefinition`接口中新增一个`getConstructorArgumentValues() : Object[]`方法
> - 同时在`GenericBeanDefinition`中实现`Getter/Setter`

#### 优化构造方法创建Bean

![image-20230313035207643](https://article.biliimg.com/bfs/article/f21aea0e23531a21eb73b6bad9dbd278f6d41722.png)

##### BeanDefinition优化

![image-20230330050657835](https://article.biliimg.com/bfs/article/ced358f0440fecb5af48a386d6d41b308bcaf6c0.png)

```java
package com.rhys.spring.IoC;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:09 AM
 */
public interface BeanDefinition {

    /**
     * 获取构造参数对应值
     * @author Rhys.Ni
     * @date 2023/3/8
     * @param
     * @return java.util.List<?>
     */
    Object[] getConstructorArgumentValues();
}

```

> 同时在`GenericBeanDefinition`中新增以下实现

```java
package com.rhys.spring.IoC;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/14 2:32 AM
 */
public class GenericBeanDefinition implements BeanDefinition {

    private Object[] constructorArgumentValues;


    /**
     * 获取构造参数对应值
     *
     * @return java.util.List<?>
     * @author Rhys.Ni
     * @date 2023/3/8
     */
    @Override
    public Object[] getConstructorArgumentValues() {
        return constructorArgumentValues;
    }

    public void setConstructorArgumentValues(Object[] constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }
}
```

##### BeanReference

> 用来说明bean依赖的：也就是这个属性依赖哪个类型的Bean
>
> **如果是`List<TestB> testbList`这种`直接值是数组或者集合`等，同时`容器中的元素是Bean依赖`，针对这种情况元素值还是需要用BeanReference来处理的,只是BeanFactory在处理时需要遍历替换**

![image-20230313020237840](https://article.biliimg.com/bfs/article/0de6c3a1be90a3d92d2598977af44df2f4c02185.png)

```java

/***
 * 依赖注入中描述Bean依赖
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/8 3:27 AM
 */
public class BeanReference {
    private String beanName;

    private Class<?> type;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public BeanReference(String beanName) {
        super();
        this.beanName = beanName;
    }

    public BeanReference(Class<?> type) {
        this.type = type;
    }
}
```



##### DefaultBeanFactory优化

> `DefaultBeanFactory`中修改`createInstanceByConstructor`如下:
>
> 上一版构造方法创建是通过`无参构造方法`来处理的，我们需要兼容通过`有参构造方法`来实现。

```java
/**
 * 构造函数创建实例
 *
 * @param beanDefinition
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/20
 */
private Object createInstanceByConstructor(BeanDefinition beanDefinition) throws Exception {
    //除了InstantiationException和IllegalAccessException异常外，为了避免相关的程序使用不当可能会存在某些危险的操作从而引发安全问题，这里需要捕获一下SecurityException
    try {
        //根据当前类所具有的有参构造方法，找到对应的构造对象
        Object[] args = this.getConstructorArgumentValues(beanDefinition);
        //推断调用哪个构造方法创建实例
        return this.determineConstructor(beanDefinition, args).newInstance(args);
    } catch (SecurityException exception) {
        logger.error("create instance error beanDefinition:{}, exception:{}", beanDefinition, exception);
        throw exception;
    }
}

/**
 * 获取合适的构造方法
 *
 * @param beanDefinition bean定义
 * @param args 参数列表
 * @return java.lang.reflect.Constructor<?>
 * @author Rhys.Ni
 * @date 2023/3/8
 */
private Constructor<?> determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Exception {
    Constructor<?> constructor = null;

    //没有参数就提供无参构造方法
    if (args == null) {
        return beanDefinition.getBeanClass().getConstructor(null);
    }

    //先根据参数类型进行精确匹配
    Class<?>[] paramTypes = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
        paramTypes[i] = args[i].getClass();
    }

    try {
        constructor = beanDefinition.getBeanClass().getConstructor(paramTypes);
    } catch (Exception e) {
        //无需处理该异常,让其能顺利进行后续逻辑
    }

    //没有精确匹配到构造方法，直接获取所有构造方法进行遍历
    if (constructor == null) {
        //outerCycle是为循环做的标记，因为这里涉及嵌套循环，如果不对外循环进行标记，那么在内循环中将不好控制continue与break的作用范围
        outerCycle:
        for (Constructor<?> cst : beanDefinition.getBeanClass().getConstructors()) {
            Class<?>[] parameterTypes = cst.getParameterTypes();
            //通过参数个数进行过滤
            if (parameterTypes.length == args.length) {
                //再对比形参类型与实参类型
                for (int i = 0; i < parameterTypes.length; i++) {
                    //isAssignableFrom：判断当前的Class对象所表示的类，是不是参数中传递的Class对象所表示的类的父类，超接口，或者是相同的类型。是则返回true，否则返回false
                    if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                        //只要有一个参数类型不匹配则跳过外循环的本轮循环
                        continue outerCycle;
                    }
                }
                //只要匹配上也直接结束外循环
                constructor = cst;
                break outerCycle;
            }
        }
    }

    //如果此时构造函数任然为空，则说明没有匹配上，直接抛异常
    if (constructor == null) {
        throw new Exception("there is no corresponding constructor for this bean definition:[" + beanDefinition + "]");
    }
    return constructor;
}

/**
 * 获取参数列表
 *
 * @param beanDefinition bean定义
 * @return java.lang.Object[]
 * @author Rhys.Ni
 * @date 2023/3/8
 */
private Object[] getConstructorArgumentValues(BeanDefinition beanDefinition) throws Exception {
    List<?> argumentValues = beanDefinition.getConstructorArgumentValues();
    if (CollectionUtils.isEmpty(argumentValues)) {
        return null;
    }

    Object[] args = new Object[argumentValues.size()];
    for (int i = 0; i < argumentValues.size(); i++) {
        args[i] = getOneArgumentRealValue(argumentValues.get(i));
    }

    return args;
}

/**
 * 获取真的参数值
 *
 * @param originalValue 原始值
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/3/8
 */
private Object getOneArgumentRealValue(Object originalValue) throws Exception {
    //处理BeanReference,得到真正的Bean实例，从而获取真正的参数值
    Object realValue = null;
    if (originalValue != null) {
        //根据originalValue的类型决定怎么找到真正的
        if (originalValue instanceof BeanReference) {
            BeanReference beanReference = (BeanReference) originalValue;
            //获取bean的两种方式 根据beanName/beanType
            if (StringUtils.isNotBlank(beanReference.getBeanName())) {
                realValue = this.getBean(beanReference.getBeanName());
            } else if (beanReference.getType() != null) {
                realValue = this.getBean(beanReference.getType());
            }
        } else if (originalValue instanceof Object[]) {
						
        } else if (originalValue instanceof Collection) {

        } else if (originalValue instanceof Map) {

        } else {
            realValue = originalValue;
        }
    }
    return realValue;
}
```

##### 构造注入测试

> 新建以下两个测试类

```java
public class TestA {
    private String name;
    private TestB testB;


    public TestA(String name, TestB testB) {
        this.name = name;
        this.testB = testB;
        System.out.println("调用了含有testB参数的构造方法");
    }

    public TestA(TestB testB) {
        this.testB = testB;
    }

    public void execute() {
        System.out.println("aBean execute:" + this.name + "\n testB.name:" + this.testB.getName());
    }
}
```

```java
public class TestB {

    private String name;

    public TestB(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
```

> 新建测试类用JUnit模拟

```java
public class DITest {

    private static PreBuildBeanFactory beanFactory = new PreBuildBeanFactory();

    @Test
    public void testDI() throws Exception {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        //设置BeanClass
        beanDefinition.setBeanClass(TestA.class);
        //定义构造参数依赖
        List<Object> args = new ArrayList<>();
        args.add("testABean");
        //Bean依赖通过BeanReference处理,这里的BeanName得和注册器中的BeanName一致，否则空指针
        args.add(new BeanReference("bBean"));
        //定义beanDefinition的构造参数列表
        beanDefinition.setConstructorArgumentValues(args);
        //注册BeanDefinition
        beanFactory.registryBeanDefinition("aBean", beanDefinition);

        //配置TestBBean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(TestB.class);

        args = new ArrayList<>();
        args.add("testBBean");

        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("bBean", beanDefinition);

        beanFactory.instantiateSingleton();

        TestA testA = (TestA) beanFactory.getBean("aBean");
        testA.execute();
    }
}
```

#### 优化静态工厂方法和工厂成员方法创建Bean

![image-20230313035043512](https://article.biliimg.com/bfs/article/528f634b8b3d01b218cd2e5c9341f90049713125.png)

##### BeanDefinition优化

> 新增如下代码

```java
/**
 * 用于BeanFactory获取工厂方法
 *
 * @param
 * @return java.lang.reflect.Method
 * @author Rhys.Ni
 * @date 2023/3/13
 */
Method getFactoryMethod();

/**
 * 用于BeanFactory获取具体的构造函数
 * @author Rhys.Ni
 * @date 2023/3/13
 * @param
 * @return java.lang.reflect.Constructor<?>
 */
Constructor<?> getConstructor();

/**
 * 用于BeanFactory设置具体的构造函数
 * @author Rhys.Ni
 * @date 2023/3/13
 * @param constructor 构造函数
 * @return void
 */
void setConstructor(Constructor<?> constructor);

/**
 * 用于BeanFactory设置工厂方法
 * @author Rhys.Ni
 * @date 2023/3/13
 * @param factoryMethod 工厂方法
 * @return void
 */
void setFactoryMethod(Method factoryMethod);
```

##### GenericBeanDefinition优化

> 新增如下代码

```java
/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/14 2:32 AM
 */
public class GenericBeanDefinition implements BeanDefinition {

    private Constructor<?> constructor;

    private Method factoryMethod;


        /**
     * 用于BeanFactory获取工厂方法
     *
     * @return java.lang.reflect.Method
     * @author Rhys.Ni
     * @date 2023/3/13
     */
    @Override
    public Method getFactoryMethod() {
        return factoryMethod;
    }

    /**
     * 用于BeanFactory获取具体的构造函数
     *
     * @return java.lang.reflect.Constructor<?>
     * @author Rhys.Ni
     * @date 2023/3/13
     */
    @Override
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * 用于BeanFactory设置具体的构造函数
     *
     * @param constructor 构造函数
     * @return void
     * @author Rhys.Ni
     * @date 2023/3/13
     */
    @Override
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * 用于BeanFactory设置工厂方法
     *
     * @param factoryMethod 工厂方法
     * @return void
     * @author Rhys.Ni
     * @date 2023/3/13
     */
    @Override
    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }
}
```

##### DefaultBeanFactory优化

> 需要根据对应的构造参数来推断对应的工厂方法,优化`createInstanceByFactoryBean`和`createInstanceByStaticFactoryMethod`方法如下：

```java
/**
 * 工厂bean方式创建实例
 *
 * @param beanDefinition
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/20
 */
private Object createInstanceByFactoryBean(BeanDefinition beanDefinition) throws Exception {
    Object[] args = this.getConstructorArgumentValues(beanDefinition);
    //匹配确定具体的工厂方法
    Method method = this.determineFactoryMethod(beanDefinition, args, this.getType(beanDefinition.getFactoryBeanName()));
    //根据工厂方法得到具体的工厂Bean
    Object factoryBean = this.doGetBean(beanDefinition.getFactoryBeanName());
    return method.invoke(factoryBean, null);
}

/**
 * 静态工厂方法
 *
 * @param beanDefinition
 * @return java.lang.Object
 * @author Rhys.Ni
 * @date 2023/2/20
 */
private Object createInstanceByStaticFactoryMethod(BeanDefinition beanDefinition) throws Exception {
    Object[] args = this.getConstructorArgumentValues(beanDefinition);
    //beanClass也作为type
    Class<?> beanClass = beanDefinition.getBeanClass();
    Method method = this.determineFactoryMethod(beanDefinition, args, beanClass);
    return method.invoke(beanClass, args);
}

/**
 * 确定使用那个工厂方法
 * @author Rhys.Ni
 * @date 2023/3/13
 * @param beanDefinition Bean定义
 * @param args 参数列表
 * @param beanClass Bean类型
 * @return java.lang.reflect.Method
 */
private Method determineFactoryMethod(BeanDefinition beanDefinition, Object[] args, Class<?> beanClass) throws Exception {

    String methodName = beanDefinition.getFactoryMethodName();
    //无参则直接使用无参构造方法
    if (args == null) {
        return beanClass.getMethod(methodName, null);
    }

    Method method = null;
    //原型Bean从第二次开始获取Bean实例的时候就可以直接获取第一次缓存的构造方法
    method = beanDefinition.getFactoryMethod();
    if (method != null) {
        return method;
    }

    //判定工厂方法的逻辑同构造方法的判定逻辑,先根据实参类型进行精确匹配查找
    Class[] paramTypes = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
        paramTypes[i] = args[i].getClass();
    }

    try {
        method = beanClass.getMethod(methodName, paramTypes);
    } catch (Exception e) {
        //捕获异常并放行，防止异常影响后续遍历匹配
    }

    //精确匹配未找到
    if (method == null) {
        //获得所有方法，遍历，通过方法名、参数数量过滤，再比对形参类型与实参类型
        outerCycle:
        for (Method declaredMethod : beanClass.getDeclaredMethods()) {
            //匹配方法名
            if (!declaredMethod.getName().equals(methodName)) {
                continue;
            }

            //方法名匹配上根据参数个数进行过滤
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == args.length) {
                for (int i = 0; i < parameterTypes.length; i++) {
                    //对比每个属性的类型,不匹配则直接对比下一个同名方法的参数
                    if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                        continue outerCycle;
                    }
                }

                //方法名，参数列表全部吻合直接结束对比
                method = declaredMethod;
                break outerCycle;
            }
        }
    }

    //如果此时method仍为空则直接抛出异常
    if (method == null) {
        throw new Exception("static factory method does not exist in the beanDefinition :" + beanDefinition);
    }

    //第一次找到，如果是原型Bean直接缓存本次找到的静态工厂方法
    //下次构造实例对象时,直接在BeanDefinition中获取该静态工厂方法
    if (beanDefinition.isPrototype()){
        beanDefinition.setFactoryMethod(method);
    }

    return method;
}
```

#### 缓存设计

> 在以上代码中我们用到了构造器缓存以及工厂方法缓存
>
> 因为在创建实例的过程中要去推断使用有参构造还是无参构造，如果使用有参构造还需要根据参数列表去遍历精确匹配和模糊匹配，如果每次创建Bean实例都来遍历匹配一次显然性能是非常低的，工厂方法创建Bean实例推断工厂方法的时候逻辑也类似，因此对`Constructor`和`Method`增加了缓存，只要第一次创建过Bean实例，直接将对应的构造器或工厂方法set到BeanDefinition中，下次再创建的时候从BeanDefinition中就可以获取到直接使用了，不需要反复推断，从而达到提升性能的目的

![image-20230330050811601](https://article.biliimg.com/bfs/article/eeb62bd0fcb2d0b9234c213df0630050c1f0c91c.png)

#### 循环依赖

> 如图：`TestA`创建需要依赖`TestB`,`TestB`创建需要依赖`TestC`,而`TestC`创建又需要依赖`TestA`，这样相互依赖最终没法完整创建导致失败

![image-20230313035616533](https://article.biliimg.com/bfs/article/4da7dbddb30f5989d69a35440237677c671bfaa7.png)

> - **构造注入中是无法解决循环依赖问题的**
> - **只能检测是否存在循环依赖然后抛出异常**

```JAVA
public class TestA {

    public static void main(String[] args) {
        new TestB();
    }
}

class TestB{
    private TestC testc = new TestC();
}

class TestC{
    private  TestB testB = new TestB();
}
```

##### DefaultBeanFactory优化

> - 在创建一个Bean的时候记录一下这个Bean，代表这个Bean正在创建中
>- 然后在getBean的时候判断记录中是否有该Bean
> - 如果有就判断为循环依赖，并抛出异常
> - 记录器的数据结构用`Set<String>`
> - 当这个Bean创建完成后我们再移除这个Bean的记录

![image-20230313235240910](https://article.biliimg.com/bfs/article/3d691dd99585cdf115463d2e4a04bd3e350b91c8.png)

> 这个记录器我们需要做成线程私有给每个线程之间数据做隔离

![image-20230314001552873](https://article.biliimg.com/bfs/article/b9a602e1b5f25201308ea8d3b9bf37750227b688.png)

> 如果不做线程私有的话则会出现以下情况：
>
> - 线程1 中想创建`TestA`实例，将记录添加到集合中
> - 这时线程2中想创建`TestB`实例，将`testB`记录添加到集合中，
> - 发现想要成功创建`TestB`要依赖`TestA`，则开始创建`TestA`
> - 当去记录中判断是否存在beanName为`testA`的记录时发现已经存在了
> - 这时`线程2`中的实例创建就会被判定为循环依赖，创建失败！
> - 但是实际上两个线程中只是想各自创建一个Bean实例
> - 由于这个记录表不是线程私有的，数据共享导致误判断成了循环依赖。

![image-20230314011904664](https://article.biliimg.com/bfs/article/f056bbe851c1ab6aef9a7a7807f94cbc06a6abbe.png)

> doGetBean时检测循环依赖

![image-20230314001659862](https://article.biliimg.com/bfs/article/044e011c69ab0d44c9c1ff6ea3d3c3141623f506.png)

```java
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {
  
  ThreadLocal<Set<String>> buildingBeansRecorder = new ThreadLocal<>();
	
  //省略...
  
  private Object doGetBean(String beanName) throws Exception {

      //省略...

      //没获取到则根据BeanDefinition创建Bean实例并且存放到Map中
      BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
      Objects.requireNonNull(beanDefinition, "beanDefinition named " + beanName + " is invalid !");

      //检测循环依赖
      Set<String> buildingBeans = buildingBeansRecorder.get();
      if (buildingBeans == null) {
          buildingBeans = new HashSet<>();
          this.buildingBeansRecorder.set(buildingBeans);
      }

      //检测到记录不为空则进行模糊匹配,只要记录中包含本次要创建的Bean则直接抛出异常，认为循环依赖了
      if (buildingBeans.contains(beanName)) {
          throw new Exception(beanName + " bean has cyclic dependencies !");
      }

      //记录中不存在改Bean则添加记录
      buildingBeans.add(beanName);

      if (beanDefinition.isSingleton()) {

      //省略...

      }

      //创建实例完成后移除创建中记录
      buildingBeans.remove(beanName);

      return beanInstance;
  }
  
  //省略...
  
}
```

### 属性注入

> 属性依赖就是某个属性依赖某个值

```java
public class A {
    private String name;
    private int age;
    private char cup;
    private List<B> bList;
}
```

#### PropertyName

> - 定义一个实体类 `PropertyValue`来记录相关的属性和值
> - 获取实例对象后根据相关的配置来给对应的属性来赋值

![image-20230314030055510](https://article.biliimg.com/bfs/article/6dd7f19d4ed2606dcfeb025448c01f030d44b5d2.png)

```java
package com.rhys.spring.DI;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/14 3:08 AM
 */
public class PropertyValue {
    private String name;
    private Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

```

#### BeanDefinition优化

> 增加属性值获取

![image-20230330050839465](https://article.biliimg.com/bfs/article/163f28ec4b7b7706ef7cd637d441b4693b5e4c8a.png)

```java
package com.rhys.spring.IoC;

import com.rhys.spring.DI.PropertyValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/14 2:32 AM
 */
public class GenericBeanDefinition implements BeanDefinition {

    private List<PropertyValue> propertyValues;


    /**
     * 获取属性值
     *
     * @return java.util.List<com.rhys.spring.DI.PropertyValue>
     * @author Rhys.Ni
     * @date 2023/3/14
     */
    @Override
    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }


    public void setPropertyValues(List<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }
}
```

#### BeanFactory优化

> 在得到bean实例后设置属性依赖

```java
private Object createInstance(BeanDefinition beanDefinition) throws Exception {
		//省略...

    //设置属性依赖
    this.setPropertyDIValues(beanDefinition,beanInstance);

    //创建完实例执行初始化方法
    this.init(beanDefinition, beanInstance);

    return beanInstance;
}

/**
 * 反射设置属性依赖
 * @author Rhys.Ni
 * @date 2023/3/14
 * @param beanDefinition bean定义
 * @param beanInstance bean实例
 * @return void
 */
private void setPropertyDIValues(BeanDefinition beanDefinition, Object beanInstance) throws Exception {
    if (!CollectionUtils.isEmpty(beanDefinition.getPropertyValues())){
        for (PropertyValue propertyValue : beanDefinition.getPropertyValues()) {
            if (!StringUtils.isBlank(propertyValue.getName())) {
                Class<?> clazz = beanInstance.getClass();
                //根据属性名获取对应属性
                Field field = clazz.getDeclaredField(propertyValue.getName());
                //暴力访问
                field.setAccessible(true);
                //处理得到真正的bean引用值给bean实例属性
                field.set(beanInstance,this.getOneArgumentRealValue(propertyValue.getValue()));
            }
        }
    }
}
```

#### 属性循环依赖

> 属性循环依赖如下

```java
A a = new A();
B b = new B();
a.setB(b);
b.setA(a);
```

##### 提前暴露构建中的bean实例

> 若果单纯在IoC中是不好处理这种情况的，最终只会出现以下无限循环现象，始终得不到`a实例`的返回

![image-20230314040731065](https://article.biliimg.com/bfs/article/07bea1ea4e4ec20e8448e48db35faf5b1a07a4bb.png)

> 所以我们需要增加一个容器来缓存`正在构建的A对象`，然后再到创建`b实例`的时候去容器中获取提前缓存的`A对象`给到`B`
>
> 这样就能得到完整的`b实例`，从而得到完整的`a实例`返回，如下图

![image-20230314041739272](https://article.biliimg.com/bfs/article/72b3be32e27ea295a5787e7ef884bc8ffe35e691.png)

> 这里的容器数据结构定义为`ThreadLocal<Map<String, Object>> earlyExposeBuildingBeans`

```java
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {
  
  private ThreadLocal<Map<String, Object>> earlyExposeBuildingBeans = new ThreadLocal<>();

	private Object doGetBean(String beanName) throws Exception {
        //省略...
    
        //属性依赖时的循环依赖
        //从提前暴露的bean实例缓存中获取bean实例
        beanInstance = this.getFromEarlyExposeBuildingBeans(beanName);
        if (beanInstance != null) {
            return beanInstance;
        }

        //没获取到则根据BeanDefinition创建Bean实例并且存放到Map中
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Objects.requireNonNull(beanDefinition, "beanDefinition named " + beanName + " is invalid !");

        //检测循环依赖
        Set<String> buildingBeans = buildingBeansRecorder.get();
        if (buildingBeans == null) {
            buildingBeans = new HashSet<>();
            this.buildingBeansRecorder.set(buildingBeans);
        }

        //省略...
    
        return beanInstance;
    }


    /**
     * <p>
     * <b>创建实例</b>
     * </p >
     *
     * @param beanDefinition <span style="color:#e38b6b;">bean定义</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/2/22
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">N倪倪</a>
     */
    private Object createInstance(String beanName, BeanDefinition beanDefinition) throws Exception {
        //省略...

        //提前暴露bean实例
        this.doEarlyExposeBuildingBeans(beanName, beanInstance);

        //设置属性依赖
        this.setPropertyDIValues(beanDefinition, beanInstance);

        //创建完成后移除缓存中提前暴露的bean实例
        this.removeEarlyExposeBuildingBeans(beanName);

        //创建完实例执行初始化方法
        this.init(beanDefinition, beanInstance);

        return beanInstance;
    }

    /**
     * 移除提前暴露的bean实例
     *
     * @param beanName
     * @return void
     * @author Rhys.Ni
     * @date 2023/3/14
     */
    private void removeEarlyExposeBuildingBeans(String beanName) {
        earlyExposeBuildingBeans.get().remove(beanName);
    }

    /**
     * 提前暴露bean实例
     *
     * @param beanName
     * @param beanInstance
     * @return void
     * @author Rhys.Ni
     * @date 2023/3/14
     */
    private void doEarlyExposeBuildingBeans(String beanName, Object beanInstance) {
        Map<String, Object> buildingBeansMap = earlyExposeBuildingBeans.get();
        if (buildingBeansMap == null) {
            buildingBeansMap = new HashMap<>();
            earlyExposeBuildingBeans.set(buildingBeansMap);
        }
        buildingBeansMap.put(beanName, beanInstance);
    }

    /**
     * 从缓存中获取
     *
     * @param beanName
     * @return java.lang.Object
     * @author Rhys.Ni
     * @date 2023/3/14
     */
    private Object getFromEarlyExposeBuildingBeans(String beanName) {
        Map<String, Object> buildingBeans = earlyExposeBuildingBeans.get();
        return buildingBeans == null ? null : buildingBeans.get(beanName);
    }
}
```

### 总结

#### IoC和DI类图总览

![image-20230314043342726](https://article.biliimg.com/bfs/article/c537266fd74d956a871e5a3374ce32265d2a5303.png)

## Aspect Oriented Programming

> **面向切面编程，在不改变类的代码的情况下，对类方法进行功能的增强**

![image-20230314141613895](https://article.biliimg.com/bfs/article/d08ee6719b89d5b0d492f40dd56fe984022d605d.png)

### 程序执行流程

> - 在我们OOP中有一个待执行的正常的流程有`testA()、testB()、testC()`几个方法
> - `Advice`就是我们需要增强的通知内容，对`testA`增强还是对`testB()`增强
> - 具体想增强什么方法，是通过`PonintCut`根据`JoinPoints连接点`来定位到具体的方法的

![image-20230315161934124](https://article.biliimg.com/bfs/article/b6e4a35109e15e2996c2ed54b2a0e5f089528d54.png)

### Advice

#### 面向接口编程

> **通知：进行功能增强**

#### Advice的特点

##### 用户性

> 由用户提供增强的逻辑代码

##### 变化性

> 不同的增强需求会有不同的逻辑

##### 多重性

> 同一个切入点可以有多重增强

##### 可选时机

> 可选择在方法执行前、后、异常时进行功能的增强

```java
public void Test(){
    //MethodBefore 方法前置增强
    try{
        //执行被增强的方法
        String str = this.test();
       	//AfterReturn 方法后置增强
    }catch(Exception e){
        //异常处理增强
    }finally{
        //After最终增强
    }
}
```

### Advice设计

![image-20230320165456408](https://article.biliimg.com/bfs/article/4f51b9a81c715b3e1f848bff2d8843ffaa78ccb9.png)



### 5种通知分析

> 由于通知是由用户提供，我们使用，主要是用户提供就突出了 `多变性`。
>
> - 为了我们能够更好的识别用户提供的东西以及让我们代码隔绝用户提供的`多变性`
>
> - 这里我们我们需要设计一个空的`Advice`接口
> - 然后定义标准的接口方法，让用户来实现它，提供各种具体的增强内容

#### 前置增强

> 在方法执行前进行增强，目的是对方法进行增强，应该需要的是方法相关的信息

##### 方法参数设计

> * 方法本身 Method
> * 方法所属的对象 Object
> * 方法的参数 Object[]
> * 方法的返回值 Object （可能没有）

##### 方法返回值设计

> **在方法执行前进行增强，不需要返回值**

```java
/**
 * <p>
 * <b>前置增强接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/14 15:57
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface MethodBeforeAdvice extends Advice {

    /**
     * <p>
     * <b>提供前置增强</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @throws Throwable <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    void before(Method method, Object[] args, Object target) throws Throwable;
}
```

#### 后置增强

> 在方法执行后进行增强

##### 方法参数设计

> * 方法本身 Method
> * 方法所属的对象 Object
> * 方法的参数 Object[]
> * 方法的返回值 Object

##### 方法返回值设计

> 需要看是否允许在After中更改返回的结果，如果规定只可用、不可修改返回值就不需要返回值

```java
/**
 * <p>
 * <b>后置增强接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/14 15:57
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface AfterReturnAdvice extends Advice {

    /**
     * <p>
     * <b>提供后置增强</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @param returnValue <span style="color:#e38b6b;">返回值</span>
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @throws Throwable <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
```

#### 环绕通知

> 包裹方法进行增强

##### 方法参数设计

> * 方法本身 Method
> * 方法所属的对象 Object
> * 方法的参数 Object[]

##### 方法返回值设计

> 方法被它包裹，即方法将由它来执行，它需要返回方法的返回值

```java
/**
 * <p>
 * <b>环绕通知接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/14 16:23
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface MethodInterceptorAdvice extends Advice {

    /**
     * <p>
     * <b>对方法进行环绕（前置、后置）增强、异常处理增强，方法实现中需调用目标方法</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    Object invoke(Method method, Object[] args, Object target) throws Throwable;
}
```

#### 异常通知

> 异常通知增强：对方法执行时的异常，进行增强处理

##### 方法参数设计

> * 一定需要Exception
> * 可能需要方法本身 Method
> * 可能需要方法所属的对象 Object
> * 可能需要方法的参数 Object[]

##### 方法返回值设计

> 需要看是否允许在After中更改返回的结果，如果规定只可用、不可修改返回值就不需要返回值

```java
/**
 * <p>
 * <b>异常通知接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/14 16:33
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface ThrowsAdvice extends Advice{

    /**
     * <p>
     * <b>提供异常处理增强</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @param e <span style="color:#e38b6b;">捕获的异常</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Throwable <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    void afterThrowing(Method method, Object[] args, Object target,Exception e) throws Throwable;
}
```

#### 最终通知

> 在方法执行后进行增强

##### 方法参数设计

> * 方法本身 Method
> * 方法所属的对象 Object
> * 方法的参数 Object[]
> * 方法的返回值 Object （可能没有）

##### 方法返回值设计

> 需要看是否允许在After中更改返回的结果，如果规定只可用、不可修改返回值就不需要返回值

```java
/**
 * <p>
 * <b>最终通知接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/14 15:57
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface AfterAdvice extends Advice {

    /**
     * <p>
     * <b>提供最终增强</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @param returnValue <span style="color:#e38b6b;">返回值</span>
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @throws Throwable <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    void after(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
```

### Pointcuts

> **切入点：对类方法增强,可选择增强的方法**

#### pointcut特点

##### 用户性

> 由用户指定

##### 变化性

> 用户可灵活指定

##### 多点性

> 用户可以选择在多个点上进行增强

#### 方法签名

> - 为用户提供一个东西，让他们可以灵活地指定多个方法点，而且我们还能看懂
>   - 指定哪些方法，如何来指定一个方法
>   - 如果有重载的情况怎么办
> - 因此我们需要的其实就是一个`完整的方法签名`如下：

```java
com.rhys.spring.aop.pointcut.TestA.test(TestB,Date)
com.rhys.spring.aop.pointcut.TestA.test(TestB,TestC,Date)
```

#### 多点性和灵活性设计

> - 可以选择某个包下的某个类的某个方法
> - 可以选择某个包下的所有类中的所有方法
> - 可以选择某个包下的所有类中的`test`关键字为前缀的方法
> - 可以选择某个包下以`Service`关键字结尾的类中的`test`关键字为前缀的方法
>
> **因此，我们需要一种表达式能支持以上匹配的**

#### 匹配规则

##### 包名

> 支持模糊匹配并且有父子特点

##### 类名

> 支持模糊匹配

##### 方法名

> 支持模糊匹配

##### 参数类型

> 支持多个参数

#### [AspectJ表达式](http://www.eclipse.org/aspectj)

> `切入点表达式`要匹配的对象就是`目标方法的方法名`,所以`execution表达式`中明显就是`方法的签名`
>
> - 访问权限类型：`modifiers-pattern`（可省略）
> - 返回值类型：`ret-type-pattern`
> - 全限定性类名：`declaring-type-pattern`（可省略）
> - 方法名(参数名)：`name-pattern(param-pattern)`
> - 抛出异常类型：`throws-pattern`（可省略）

##### 符号含义

| 符号 |                             含义                             |
| :--: | :----------------------------------------------------------: |
|  *   |                         0到多个字符                          |
|  ..  | 方法参数中表示任意多个参数，用在包名后表示当前包及其子包路径 |
|  +   |    用在类名后表示当前类及子类，用在接口后表示接口及实现类    |

##### 表达式语法

> 指定切入点为`任意公共方法`

```java
execution(public * *(..))
```

> 指定切入点为任何一个`以"test"开始`的方法

```java
execution(* test *(..))
```

> 指定切入点为定义在`service包里的任意类的任意方法`

```java
execution(* com.rhys.spring.service.*.*(..))
```

> 指定切入点为定义在`service包或者子包里的任意类的任意方法`,当".."出现在类名中时，后面必须跟"*"，表示包、子包下的所有类

```java
execution(* com.rhys.spring.service..*.*(..))
```

> 指定`只有一级包下的serivce子包下所有类(接口)中的所有方法`为切入点

```java
execution(* *.service.*.*(..))
```

> 指定`所有包下的serivce子包下所有类(接口)中的所有方法`为切入点

```java
execution(* *..service.*.*(..)) 
```

#### PointCut实现

> 需要提供`匹配类`和`匹配方法`两个能力，如下设计以及实现

![image-20230320041143650](https://article.biliimg.com/bfs/article/cc9734a68438b9db5fff5418ce2dd832f9261cf3.png)

##### PointCut接口

```java
/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/20 4:03 AM
 */
public interface PointCut {
    /**
     * 匹配类
     *
     * @param targetClass 需要进行匹配的类型
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    boolean matchClass(Class<?> targetClass);

    /**
     * 匹配方法
     *
     * @param method      需要进行匹配的方法
     * @param targetClass 需要进行匹配的类型
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    boolean matchMethod(Method method, Class<?> targetClass);
}
```

##### AspectJ表达式实现PointCut

> 引入`AspectJ`依赖

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.1</version>
</dependency>
```

> 代码实现

```java
package com.rhys.spring.aop.pointcut;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/20 4:07 AM
 */
public class AspectJExpressionPointCut implements PointCut {
    /**
     * 切点解析器
     */
    public static PointcutParser pointcutParser = PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();

    private PointcutExpression pointcutExpression;

    private String expression;

    public AspectJExpressionPointCut(String expression) {
        this.pointcutExpression = pointcutParser.parsePointcutExpression(expression);
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    /**
     * 匹配类
     *
     * @param targetClass 需要进行匹配的类型
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    @Override
    public boolean matchClass(Class<?> targetClass) {
        return pointcutExpression.couldMatchJoinPointsInType(targetClass);
    }

    /**
     * 匹配方法
     *
     * @param method      需要进行匹配的方法
     * @param targetClass 需要进行匹配的类型
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    @Override
    public boolean matchMethod(Method method, Class<?> targetClass) {
        ShadowMatch shadowMatch = pointcutExpression.matchesMethodExecution(method);
        return shadowMatch.alwaysMatches();
    }
}
```

> 实现类都是要生成Bean对象的，最终根据`advice`和`pointCut`组成切面
>
> - 根据`adviceBeanName`找到通知者
> - 根据`expression`则可以找到表达式
>
> 因此我们需要一个通知者将这两块组合起来

![image-20230320231842921](https://article.biliimg.com/bfs/article/0621c193f7c2a0e379245adc9c597f76495286bd.png)

#### Advisor设计

> 为用户提供更简单的外观，`Advisor(通知者)`组合Advice和Pointcut
>
> - 当用户使用`AspectJ`表达式来指定切入点事就用`AspectJPointCutAdvisor`这个实现
> - 只需要配置好该类的Bean，指定AdviceBeanName和expression即可

![image-20230320234755303](https://article.biliimg.com/bfs/article/12c40a481c9f0324faf7358e9e13569ba57ff1bb.png)

##### Advisor接口

```java

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/20 11:45 PM
 */
public interface Advisor {
    /**
     * 获取通知类的Bean名
     * @author Rhys.Ni
     * @date 2023/3/20
     * @param
     * @return java.lang.String
     */
    String getAdviceBeanName();

    /**
     * 获取表达式
     * @author Rhys.Ni
     * @date 2023/3/20
     * @param
     * @return java.lang.String
     */
    String getExpression();
}
```

##### PointCutAdvisor接口

```java
package com.rhys.spring.aop.advisor;

import com.rhys.spring.aop.pointcut.PointCut;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/20 11:46 PM
 */
public interface PointCutAdvisor extends Advisor {
    /**
     * 获取切点
     * @author Rhys.Ni
     * @date 2023/3/20
     * @param
     * @return com.rhys.spring.aop.pointcut.PointCut
     */
    PointCut getPointCut();
}
```

##### AspectJPointCutAdvisor实现

```java
package com.rhys.spring.aop.advisor;

import com.rhys.spring.aop.pointcut.AspectJExpressionPointCut;
import com.rhys.spring.aop.pointcut.PointCut;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/20 11:49 PM
 */
public class AspectJPointCutAdvisor implements PointCutAdvisor {

    private String adviceBeanName;

    private String expression;

    private AspectJExpressionPointCut aspectJExpressionPointCut;


    public AspectJPointCutAdvisor(String adviceBeanName, String expression) {
        this.adviceBeanName = adviceBeanName;
        this.expression = expression;
        this.aspectJExpressionPointCut = new AspectJExpressionPointCut(this.expression);
    }

    /**
     * 获取通知类的Bean名
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    @Override
    public String getAdviceBeanName() {
        return this.adviceBeanName;
    }

    /**
     * 获取表达式
     *
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    @Override
    public String getExpression() {
        return this.expression;
    }

    /**
     * 获取切点
     *
     * @return com.rhys.spring.aop.pointcut.PointCut
     * @author Rhys.Ni
     * @date 2023/3/20
     */
    @Override
    public PointCut getPointCut() {
        return this.aspectJExpressionPointCut;
    }
}
```

##### 扩展

> 不同的表达式扩展形式也有很多，如果用正则表达式则可以如下扩展

![image.png](https://article.biliimg.com/bfs/article/dbed9f45c991eab8775e5599edaa529be7f06f7d.png)



![image.png](https://article.biliimg.com/bfs/article/1cfcd7a2417ddb26f3588b73c8d05139a18c4b3c.png)

### Weaving

> **织入：不改变原类的代码实现增强**，
>
> - 负责将用户提供的`Advice通知`增强到`Pointcuts的指定方法中`，将切入点所对应的方法（Bean对象）与切入点关联起来
> - 创建Bean的时候，在Bean执行初始化后通过代理进行增强
> - 需要对Bean类及方法挨个匹配用户配置的切面，如果匹配到切面则需要增强

#### 织入设计

> 根据AOP的使用流程
>
> - 用户负责配置切面
> - 织入就在初始化后判断判断Bean是否需要增强
> - 如果需要增强则通过代理进行增强，最后返回`代理对象实例`
> - 不需要增强的话则直接返回`原始对象实例`

![image-20230322013928793](https://article.biliimg.com/bfs/article/d548154d22b163b4976289ba5caaf455b1082159.png)

> 根据下图Bean创建流程思考：如果我们直接把逻辑写在`BeanFactory`中的话，将来可能会有更多的处理逻辑加入到Bean的生成过程中，就会出现不断地修改`BeanFactory`中的代码

![image.png](https://article.biliimg.com/bfs/article/2aa37940e54b3aaf6686e43b4d346d64ea4d47a1.png)

> 针对以下特殊情况：
>
> - 在初始化前后有些情况下需要做AOP增强，有些情况不需要做增强
> - 增强的逻辑越来越多，不方便扩展
>
> 因此我们需要考虑使用`观察者模式`，通过在各个节点加入扩展点，然后加入注册机制

##### 观察者模式

![image-20230323144802004](https://article.biliimg.com/bfs/article/b042b6b1725dd7cfd3ba5d49b18b3f6be1e183e7.png)



##### BeanPostProcessor设计

> - 我们需要应用观察者模式添加一个`Bean的后置处理器(BeanPostProcessor)`
>
> - 我们想让这个`后置处理器`生效，想让`BeanFactory`知道，我们需要在`BeanFactory`中添加`registerBeanProcessor`注册器，并在`DefaultBeanFactory`中进行实现
> - 为了缓存注册成功的`beanPostProcessor`，在IOC中我们还得添加一个容器来接住，数据结构定义为`List<BeanPostProcessor> beanPostProcesssor`

![image-20230328005651416](https://article.biliimg.com/bfs/article/4d6bcd710202266e0e8e4f1cb0c27d1504be7a3b.png)

#### 织入实现

##### BeanPostProcessor接口

```java
package com.rhys.spring.IoC;

/**
 * <p>
 * <b>初始化之前和初始化之后要执行的方法</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/27 9:43
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface BeanPostProcessor {

    /**
     * <p>
     * <b>初始化之前执行</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @param bean     <span style="color:#e38b6b;">bean实例</span>
     * @param beanName <span style="color:#e38b6b;">bean名称</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }


    /**
     * <p>
     * <b>初始化之后执行</b>
     * </p >
     *
     * @param bean     <span style="color:#e38b6b;">bean实例</span>
     * @param beanName <span style="color:#e38b6b;">bean名称</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}

```

> `AdvisorAutoProxyCreator`作为`BeanPostProcessor`的默认实现，首先自身也是Bean,要交给IoC管理的，
>
> 自身是无法感知到当前所处的环境，所以需要通过一个反向感知接口`BeanFactoryAware`去感知`AdvisorAutoProxyCreator`当前所处的状态

##### 反向感知接口设计

![image-20230328013956884](https://article.biliimg.com/bfs/article/746d5e5a8d473643e70600ca76f67842c7c85f43.png)

###### Aware接口实现

```java 
/**
 * <p>
 * <b>反向感知顶层接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/27 15:12
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface Aware {
}                             
```

###### BeanFactoryAware接口实现

```java
package com.rhys.spring.IoC;

/**
 * <p>
 * <b>Bean工厂反向感知接口</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/3/27 10:34
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public interface BeanFactoryAware extends Aware {

    /**
     * <p>
     * <b>设置Bean工厂</b>
     * </p >
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @param beanFactory <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    void setBeanFactory(BeanFactory beanFactory);
}
```

##### AdvisorAutoProxyCreator实现

```java
package com.rhys.spring.aop.weaving;

import com.rhys.spring.IoC.BeanFactory;
import com.rhys.spring.IoC.BeanFactoryAware;
import com.rhys.spring.IoC.BeanPostProcessor;
import com.rhys.spring.aop.advisor.Advisor;
import com.rhys.spring.aop.advisor.PointCutAdvisor;
import com.rhys.spring.aop.pointcut.PointCut;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 12:36 AM
 */
public class AdvisorAutoProxyCreator implements BeanPostProcessor, BeanFactoryAware {
    private BeanFactory beanFactory;

    private List<Advisor> advisorList;

    private volatile boolean getAllAdvisors = false;

    /**
     * <p>
     * <b>初始化之后执行</b>
     * </p >
     *
     * @param bean     <span style="color:#e38b6b;">bean实例</span>
     * @param beanName <span style="color:#e38b6b;">bean名称</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        //先判断Bean是否需要增强
        List<Advisor> advisors = getMatchedAdvisors(bean, beanName);

        //如果有匹配的切面，则代表需要创建代理实例实现增强
        if (CollectionUtils.isNotEmpty(advisors)) {
            bean = this.createProxy(bean, beanName, advisors);
        }

        return bean;
    }

    /**
     * 创建代理实例
     *
     * @param bean 原始bean实例
     * @param beanName bean名称
     * @param advisors 切面通知者
     * @return java.lang.Object
     * @author Rhys.Ni
     * @date 2023/3/28
     */
    private Object createProxy(Object bean, String beanName, List<Advisor> advisors) {
        //doProcess...
        return bean;
    }

    /**
     * 匹配通知者
     *
     * @param bean     bean实例
     * @param beanName bean名称
     * @return java.util.List<com.rhys.spring.aop.advisor.Advisor>
     * @author Rhys.Ni
     * @date 2023/3/28
     */
    private List<Advisor> getMatchedAdvisors(Object bean, String beanName) throws Exception {
        //第一次执行时，先从BeanFactory中得到用户配置的所有通知者
        if (!getAllAdvisors) {
            synchronized (this) {
                if (!getAllAdvisors) {
                    //获取所有通知者并改getAllAdvisors状态为true代表获取过了所有通知者
                    advisorList = this.beanFactory.getBeanListOfType(Advisor.class);
                    getAllAdvisors = true;
                }
            }
        }

        //如果没有获取到切面配置直接返回
        if (CollectionUtils.isEmpty(advisorList)) {
            return null;
        }

        //存在切面配置则获取BeanClass和其对应的所有方法
        Class<?> beanClass = bean.getClass();
        List<Method> methods = this.getAllMethodForClass(beanClass);

        //存放匹配上的通知者
        List<Advisor> matchedAdvisors = new ArrayList<>();

        //匹配Advisor
        for (Advisor advisor : this.advisorList) {
            if (advisor instanceof PointCutAdvisor && isPointCutMatchBean((PointCutAdvisor) advisor, beanClass, methods)) {
                matchedAdvisors.add(advisor);
            }
        }

        return matchedAdvisors;
    }

    /**
     * 切入点Bean是否匹配
     *
     * @param advisor   切入点通知者
     * @param beanClass bean类型
     * @param methods   beanClass的本类以及所有实现接口的方法
     * @return boolean
     * @author Rhys.Ni
     * @date 2023/3/28
     */
    private boolean isPointCutMatchBean(PointCutAdvisor advisor, Class<?> beanClass, List<Method> methods) {
        PointCut pointCut = advisor.getPointCut();
        //先判断类是否匹配
        if (!pointCut.matchClass(beanClass)) {
            return false;
        }
        //再判断方法是否匹配
        for (Method method : methods) {
            if (!pointCut.matchMethod(method, beanClass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取类的所有方法
     *
     * @param beanClass bean类型
     * @return java.util.List<java.lang.reflect.Method>
     * @author Rhys.Ni
     * @date 2023/3/28
     */
    private List<Method> getAllMethodForClass(Class<?> beanClass) {
        //获取本类以及所实现接口的方法
        LinkedList<Method> methods = new LinkedList<>();
        //获取类的所有接口
        Set<Class<?>> classes = new LinkedHashSet<>(ClassUtils.getAllInterfacesForClassAsSet(beanClass));
        //将本类一并添加进集合中进行方法收集操作
        classes.add(beanClass);
        for (Class<?> clazz : classes) {
            //获取所有已声明的方法
            Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(clazz);
            methods.addAll(Arrays.asList(allDeclaredMethods));
        }
        return methods;
    }

    /**
     * <p>
     * <b>设置Bean工厂</b>
     * </p >
     *
     * @param beanFactory <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
```

##### BeanFactory接口优化

> `BeanFactory`接口中继承`BeanPostProcessor`并添加`beanPostProcessor注册器

```java
package com.rhys.spring.IoC;

import java.util.List;
import java.util.Map;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:08 AM
 */
public interface BeanFactory{

    /**
     * <p>
     * <b>beanPostProcessor注册器</b>
     * </p >
     *
     * @param beanPostProcessor <span style="color:#e38b6b;">bean执行器</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
```

##### DefaultBeanFactory优化

```java
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry, Closeable {
        private List<BeanPostProcessor> beanPostProcessorList = Collections.synchronizedList(new ArrayList<>());

        /**
     * <p>
     * <b>beanPostProcessor注册器</b>
     * </p >
     *
     * @param beanPostProcessor <span style="color:#e38b6b;">bean执行器</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessorList.add(beanPostProcessor);
        //如果beanPostProcessor是BeanFactoryAware类型则将当前的Bean工厂对象给beanPostProcessor做绑定，这就是反向感知的一个过程
        if (beanPostProcessor instanceof BeanFactoryAware) {
            ((BeanFactoryAware) beanPostProcessor).setBeanFactory(this);
        }
    }

    /**
     * <p>
     * <b>应用Bean初始化前的处理</b>
     * </p >
     *
     * @param beanInstance <span style="color:#e38b6b;">Bean实例</span>
     * @param beanName     <span style="color:#e38b6b;">Bean名称</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private Object applyPostProcessBeforeInitialization(Object beanInstance, String beanName) throws Exception {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            beanInstance = beanPostProcessor.postProcessBeforeInitialization(beanInstance, beanName);
        }
        return beanInstance;
    }

    /**
     * <p>
     * <b>应用Bean初始化之后的处理</b>
     * </p >
     *
     * @param beanInstance <span style="color:#e38b6b;">Bean实例</span>
     * @param beanName     <span style="color:#e38b6b;">Bean名称</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private Object applyPostProcessAfterInitialization(Object beanInstance, String beanName) {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            beanInstance = beanPostProcessor.postProcessAfterInitialization(beanInstance, beanName);
        }
        return beanInstance;
    }
    
    /*
    * 创建实例
    */
    private Object createInstance(String beanName, BeanDefinition beanDefinition) throws Exception {
        //省略...

        //应用Bean初始化前的处理
        beanInstance = this.applyPostProcessBeforeInitialization(beanInstance, beanName);

        //创建完实例执行初始化方法
        this.init(beanDefinition, beanInstance);

        //应用Bean初始化之后的处理
        beanInstance = this.applyPostProcessAfterInitialization(beanInstance, beanName);

        return beanInstance;
    }
}
```

##### 代理对象

> 为其他对象提供一种代理以控制对这个对象的访问。在某些情况下，一个对象不适合或者不能直接引用另一个对象，而代理对象可以在调用者和目标对象之间起到中介的作用

##### 代理实现方法

###### JDK动态代理

> 在运行时，对接口创建代理对象

![image.png](https://article.biliimg.com/bfs/article/1ea5795f5af74835c102108f7afbc4be14fe8c6c.png)

###### Cglib动态代理

![image.png](https://article.biliimg.com/bfs/article/b018376b4c1fc375c0bc784b440a8dd26d54772a.png)

###### Javassist

> 引入`Javassist`依赖

```xml
<dependency>
    <groupId>org.javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.22.0-GA</version>
</dependency>
```

> 测试代码

```java
public class JavassistTest {
    public static void main(String[] args) throws Exception{
        ClassPool pool = ClassPool.getDefault();
        Loader loader = new Loader(pool);
        CtClass ct = pool.makeClass("zyer");
        
        CtField field = new CtField(CtClass.intType,"age",ct);
        field.setModifiers(AccessFlag.PUBLIC);
        ct.addField(field);
        
        CtConstructor constructor = CtNewConstructor.make("public GeneratedClass(int age){this.age=age;}",ct);
        ct.addConstructor(constructor);
        
        CtMethod method = CtNewMethod.make("public void hello(int age){System.out.println(age);}",ct);
        ct.addMethod(method);
        
        String cmd = "public static void main(String[] args) {System.out.println(\"my name is zyer\");}";
        CtMethod method1 = CtNewMethod.make(cmd,ct);
        ct.addMethod(method1);
        ct.writeFile("/Users/zyer/Downloads/untitled/out/production/untitled/");
        
        Class name = loader.loadClass("zyer");
        Constructor constructor1 = name.getDeclaredConstructor(int.class);
        Object obj = constructor1.newInstance(1);
        Method method2 = name.getDeclaredMethod("hello",int.class);
        method2.invoke(obj,123);
        Method method3 = name.getDeclaredMethod("main",String[].class);
        method3.invoke(null,(Object) new String[]{});
    }
}
```

###### ASM

##### 代理实现设计

> 通过`抽象`、`面向接口编程`来支持不同实现的代码，从而具备灵活扩展的特性，设计如下：

![image-20230330035947951](https://article.biliimg.com/bfs/article/77950eeb7b7bcee0612dee56bdbc20341c7efeca.png)

```java
package com.rhys.spring.aop.weaving.proxy;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 4:34 AM
 */
public interface AopProxy {
    Object getProxy();

    Object getProxy(ClassLoader classLoader);
}
```

##### 代理对象创建设计

> - JDK动态代理实现`InvocationHandler`
> - CgLib动态代理实现`MethodInterceptor`

![image-20230330041625124](https://article.biliimg.com/bfs/article/8694b6f7e5ff83bd0c44b92832fe2482bb284740.png)

###### CglibDynamicAopProxy实现

```java
package com.rhys.spring.aop.weaving.proxy;

import com.rhys.spring.IoC.BeanDefinition;
import com.rhys.spring.IoC.BeanFactory;
import com.rhys.spring.IoC.DefaultBeanFactory;
import com.rhys.spring.aop.advisor.Advisor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 4:34 AM
 */
public class CglibDynamicAopProxy implements AopProxy, MethodInterceptor {
    private static final Log logger = LogFactory.getLog(CglibDynamicAopProxy.class);

    private static Enhancer enhancer = new Enhancer();

    private String beanName;

    private Object target;

    private List<Advisor> matchAdvisors;

    private BeanFactory beanFactory;

    public CglibDynamicAopProxy(String beanName, Object target, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        this.beanName = beanName;
        this.target = target;
        this.matchAdvisors = matchAdvisors;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getProxy() {
        return this.getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        logger.info("Cglib创建代理：" + target);

        //获取目标对象类型
        Class<?> targetClass = this.target.getClass();
        enhancer.setSuperclass(targetClass);
        enhancer.setInterfaces(this.getClass().getInterfaces());
        enhancer.setCallback(this);

        Constructor<?> constructor = null;
        try {
            constructor = targetClass.getConstructor(new Class<?>[]{});
        } catch (NoSuchMethodException | SecurityException e) {
            //不处理继续往下走
        }

        //此时如果构造器不为空，直接创建代理对象，否则根据Bean定义获取对应参数类型和参数列表
        if (constructor!=null){
            return enhancer.create();
        }else {
            BeanDefinition beanDefinition = ((DefaultBeanFactory) beanFactory).getBeanDefinition(beanName);
            return enhancer.create(beanDefinition.getConstructor().getParameterTypes(),beanDefinition.getRealConstructorArgumentValues());
        }
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return AopProxyUtils.applyAdvices(target,method,args,matchAdvisors,proxy,beanFactory);
    }
}
```

###### JDKDynamicAopProxy实现

```java
package com.rhys.spring.aop.weaving.proxy;

import com.rhys.spring.IoC.BeanFactory;
import com.rhys.spring.aop.advisor.Advisor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/30 5:16 AM
 */
public class JDKDynamicAopProxy implements AopProxy, InvocationHandler {
    private static final Log logger = LogFactory.getLog(JDKDynamicAopProxy.class);

    private String beanName;

    private Object target;

    private List<Advisor> matchAdvisors;

    private BeanFactory beanFactory;

    public JDKDynamicAopProxy(String beanName, Object target, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        this.beanName = beanName;
        this.target = target;
        this.matchAdvisors = matchAdvisors;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getProxy() {
        return this.getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        logger.info("JDK创建代理：" + target);
        return Proxy.newProxyInstance(classLoader, target.getClass().getInterfaces(), this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return AopProxyUtils.applyAdvices(target,method,args,matchAdvisors,proxy,beanFactory);
    }
}
```

##### 增强逻辑实现

> 无论我们选择哪种代理方式来生成代理对象，最终的增强逻辑都是相同的，因此我们最好将相同的逻辑抽出来，设计如下：
>
> - 增加一个`AopProxyUtils`工具类，将这部分逻辑封装到这里面供最终使用

![image-20230330042447312](https://article.biliimg.com/bfs/article/9095984fc63addbbce782960f52cbf0f9ca13f66.png)

###### AopProxyUtils实现

```java
package com.rhys.spring.aop.weaving.proxy;

import com.rhys.spring.IoC.BeanFactory;
import com.rhys.spring.aop.advisor.Advisor;
import com.rhys.spring.aop.advisor.PointCutAdvisor;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 4:38 AM
 */
public class AopProxyUtils {

    /**
     * 对方法应用advices增强
     *
     * @param target        目标对象
     * @param method        方法
     * @param args          参数列表
     * @param matchAdvisors 用于匹配的切面缓存
     * @param proxy         代理对象
     * @param beanFactory   bean工厂
     * @return java.lang.Object
     * @author Rhys.Ni
     * @date 2023/3/30
     */
    public static Object applyAdvices(Object target, Method method, Object[] args, List<Advisor> matchAdvisors, Object proxy, BeanFactory beanFactory) throws Throwable {
        //获取对当前方法进行增强的Advice通知
        List<Object> advices = AopProxyUtils.getShouldApplyAdvices(target.getClass(), method, matchAdvisors, beanFactory);

        //如果匹配到存在增强的advice,责任链式执行增强
        if (CollectionUtils.isNotEmpty(advices)) {
            AopAdviceChainInvocation chainInvocation = new AopAdviceChainInvocation(proxy, target, method, args, advices);
            return chainInvocation.invoke();
        } else {
            return method.invoke(target, args);
        }
    }

    /**
     * 获取对当前方法进行增强的Advice通知
     *
     * @param clazz         目标对象
     * @param method        方法
     * @param matchAdvisors 用于匹配的切面缓存
     * @param beanFactory   bean工厂
     * @return java.util.List<java.lang.Object>
     * @author Rhys.Ni
     * @date 2023/3/30
     */
    private static List<Object> getShouldApplyAdvices(Class<?> clazz, Method method, List<Advisor> matchAdvisors, BeanFactory beanFactory) throws Exception {
        if (matchAdvisors.isEmpty()) {
            return null;
        }
        //缓存匹配的切面并收集返回
        List<Object> advisors = new ArrayList<>();
        for (Advisor advisor : matchAdvisors) {
            if (advisor instanceof PointCutAdvisor && ((PointCutAdvisor)advisor).getPointCut().matchMethod(method,clazz)){
                advisors.add(beanFactory.getBean(advisor.getAdviceBeanName()));
            }
        }
        return advisors;
    }
}
```

##### 应用Advice增强实现逻辑

![image-20230330043549199](https://article.biliimg.com/bfs/article/065553249ddd243bd04670cbe3e91d6c7df7e6ad.png)

###### AopAdviceChainInvocation实现

```java
package com.rhys.spring.aop.weaving.proxy;

import com.rhys.spring.aop.advice.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/30 5:31 AM
 */
public class AopAdviceChainInvocation {
    private static final Log logger = LogFactory.getLog(AopAdviceChainInvocation.class);

    private static Method invokeMethod;

    static {
        try {
            invokeMethod = AopAdviceChainInvocation.class.getMethod("invoke", null);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("AopAdviceChainInvocation Exception:{}", e);
        }
    }

    /**
     * 责任链执行索引记录
     */
    private int i = 0;
    private Object proxy;
    private Object target;
    private Method method;
    private Object[] args;
    private List<Object> advices;

    public AopAdviceChainInvocation(Object proxy, Object target, Method method, Object[] args, List<Object> advices) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.advices = advices;
    }

    public Object invoke() throws Throwable {
        if (i < this.advices.size()) {
            Object advice = this.advices.get(i++);
            //判断通知类型属性哪种则执行对应的增强逻辑
            if (advice instanceof MethodBeforeAdvice) {
                ((MethodBeforeAdvice) advice).before(method, args, target);
            } else if (advice instanceof MethodInterceptorAdvice) {
                //这里的方法和目标对象传的是这里的链invoke方法和链对象
                return ((MethodInterceptorAdvice) advice).invoke(invokeMethod, null, this);
            } else if (advice instanceof AfterReturnAdvice) {
                //先得到结果再进行后置增强
                Object returnValue = this.invoke();
                ((AfterReturnAdvice) advice).afterReturning(returnValue, method, args, target);
                return returnValue;
            } else if (advice instanceof AfterAdvice) {
                //最终通知增强在finally中进行
                Object returnValue = null;
                try {
                    returnValue = this.invoke();
                } finally {
                    ((AfterAdvice) advice).after(returnValue, method, args, target);
                }
                return returnValue;
            } else if (advice instanceof ThrowsAdvice) {
                //异常通知在catch中进行
                try {
                    return this.invoke();
                } catch (Exception e) {
                    ((ThrowsAdvice) advice).afterThrowing(method, args, target, e);
                }
            }
            return this.invoke();
        } else {
            return this.method.invoke(target, args);
        }
    }
}

```

##### AopProxy使用设计

> 通过工厂模式来创建使用AopProxy

![image-20230330044150713](https://article.biliimg.com/bfs/article/74196ae914fce78d577520ba358945780e940058.png)

###### AopProxyFactory接口实现

```java
package com.rhys.spring.aop.weaving.proxy;

import com.rhys.spring.IoC.BeanFactory;
import com.rhys.spring.aop.advisor.Advisor;

import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 4:28 AM
 */
public interface AopProxyFactory {
   /**
    * 创建Aop代理    * @author Rhys.Ni
    * @date 2023/3/30
    * @param bean bean实例
    * @param beanName bean名称
    * @param matchAdvisors 用于匹配的切面通知缓存
    * @param beanFactory bean工厂
    * @return org.springframework.aop.framework.AopProxy
    */
    AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory);

    /**
     * 获得默认的AopProxyFactory实例
     * @author Rhys.Ni
     * @date 2023/3/30
     * @param
     * @return com.rhys.spring.aop.weaving.proxy.AopProxyFactory
     */
    static AopProxyFactory getDefaultAopProxyFactory(){
        return new DefaultAopProxyFactory();
    }
}
```

###### AopProxyFactory默认实现

```java
package com.rhys.spring.aop.weaving.proxy;

import com.rhys.spring.IoC.BeanFactory;
import com.rhys.spring.aop.advisor.Advisor;

import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 4:30 AM
 */
public class DefaultAopProxyFactory implements AopProxyFactory {
    @Override
    public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        //判断用JDK动态代理还是用Cglib
        if (shouldUseJDKDynamicProxy(bean, beanName)) {
            return new JDKDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        } else {
            return new CglibDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        }
    }

    private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
        //doProcess...后续实现
        return false;
    }
}
```

##### AdvisorAutoProxyCreator优化

> 在前面我们并没有具体实现`createProxy`方法的逻辑，现在有了代理部分的逻辑就可以通过AopProxyFactory完成选择和创建代理对象的工作
>
> - 另外，为了避免循环依赖，我们不能对Advisor和Advice类型的Bean做处理，所以`postProcessAfterInitialization`方法中需要加上相关逻辑判断

```java
public class AdvisorAutoProxyCreator implements BeanPostProcessor, BeanFactoryAware {
  	//...省略其他方法
  
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        //不能对Advisor和Advice类型的Bean做处理
        if (bean instanceof Advisor || bean instanceof Advice) {
            return bean;
        }

        //先判断Bean是否需要增强
        List<Advisor> advisors = getMatchedAdvisors(bean, beanName);

        //如果有匹配的切面，则代表需要创建代理实例实现增强
        if (CollectionUtils.isNotEmpty(advisors)) {
            bean = this.createProxy(bean, beanName, advisors);
        }

        return bean;
    }
    
    //...省略其他方法
    
    private Object createProxy(Object bean, String beanName, List<Advisor> advisors) {
        //通过AopProxyFactory完成选择和创建代理对象的工作
        return AopProxyFactory.getDefaultAopProxyFactory().createAopProxy(bean, beanName, advisors, beanFactory).getProxy();
    }
  
  	//...省略其他方法
}
```

### AOP测试

> 新增Advice实现类如下

```java
package com.rhys.spring.aop.impl;

import com.rhys.spring.aop.advice.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 3:42 AM
 */
public class MyBeforeAdvice implements MethodBeforeAdvice {
    /**
     * <p>
     * <b>提供前置增强</b>
     * </p >
     *
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args   <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @throws Throwable <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println(this + "对" + target + "进行前置增强");
    }
}

```

```java
package com.rhys.spring.aop.impl;

import com.rhys.spring.aop.advice.AfterReturnAdvice;

import java.lang.reflect.Method;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 3:44 AM
 */
public class MyAfterReturningAdvice implements AfterReturnAdvice {
    /**
     * <p>
     * <b>提供后置增强</b>
     * </p >
     *
     * @param returnValue <span style="color:#e38b6b;">返回值</span>
     * @param method      <span style="color:#e38b6b;">被增强的方法</span>
     * @param args        <span style="color:#e38b6b;">方法参数</span>
     * @param target      <span style="color:#e38b6b;">方法所属对象</span>
     * @throws Throwable <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println(this + "对" + target + "进行后置增强,返回值：" + returnValue);
    }
}

```

```java
package com.rhys.spring.aop.impl;

import com.rhys.spring.aop.advice.MethodInterceptorAdvice;

import java.lang.reflect.Method;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 3:46 AM
 */
public class MyMethodInterceptor implements MethodInterceptorAdvice {
    /**
     * <p>
     * <b>对方法进行环绕（前置、后置）增强、异常处理增强，方法实现中需调用目标方法</b>
     * </p >
     *
     * @param method <span style="color:#e38b6b;">被增强的方法</span>
     * @param args   <span style="color:#e38b6b;">方法参数</span>
     * @param target <span style="color:#e38b6b;">方法所属对象</span>
     * @return <span style="color:#ffcb6b;"> java.lang.Object</span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/14
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public Object invoke(Method method, Object[] args, Object target) throws Throwable {
        System.out.println(this + "对" + target + "进行环绕增强-方法前置增强");
        Object o = method.invoke(target, args);
        System.out.println(this + "对" + target + "进行环绕增强-方法后置增强");
        return o;
    }
}

```

> 测试代码如下

```java
import com.rhys.spring.DI.BeanReference;
import com.rhys.spring.IoC.BeanPostProcessor;
import com.rhys.spring.IoC.GenericBeanDefinition;
import com.rhys.spring.IoC.PreBuildBeanFactory;
import com.rhys.spring.aop.advisor.AspectJPointCutAdvisor;
import com.rhys.spring.aop.impl.MyAfterReturningAdvice;
import com.rhys.spring.aop.impl.MyBeforeAdvice;
import com.rhys.spring.aop.impl.MyMethodInterceptor;
import com.rhys.spring.aop.weaving.AdvisorAutoProxyCreator;
import com.rhys.spring.demo.TestA;
import com.rhys.spring.demo.TestB;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/28 3:34 AM
 */
public class AOPTest {
    static PreBuildBeanFactory beanFactory = new PreBuildBeanFactory();

    @Test
    public void testAop() throws Exception {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

        //配置TestABean
        beanDefinition.setBeanClass(TestA.class);
        //定义构造参数依赖
        List<Object> args = new ArrayList<>();
        args.add("testABean");
        //Bean依赖通过BeanReference处理
        args.add(new BeanReference("bBean"));
        //定义beanDefinition的构造参数列表
        beanDefinition.setConstructorArgumentValues(args);
        //注册BeanDefinition
        beanFactory.registryBeanDefinition("aBean", beanDefinition);

        //配置TestBBean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(TestB.class);
        args = new ArrayList<>();
        args.add("testBBean");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("bBean", beanDefinition);

        //前置增强advice bean注册
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MyBeforeAdvice.class);
        beanFactory.registryBeanDefinition("myBeforeAdvice", beanDefinition);

        //环绕增强advice bean注册
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MyMethodInterceptor.class);
        beanFactory.registryBeanDefinition("myMethodInterceptor", beanDefinition);

        //后置增强advice bean注册
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(MyAfterReturningAdvice.class);
        beanFactory.registryBeanDefinition("myAfterReturningAdvice", beanDefinition);


        //注册Advisor（通知者/切面）bean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AspectJPointCutAdvisor.class);
        args = new ArrayList<>();
        args.add("myBeforeAdvice");
        //对TestA类中的以test为前缀的所有方法进行前置增强
        args.add("execution(* com.rhys.spring.demo.TestA.test*(..))");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aspectJPointCutAdvisor1", beanDefinition);

        //环绕切面
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AspectJPointCutAdvisor.class);
        args = new ArrayList<>();
        args.add("myMethodInterceptor");
        //对TestA类中的以test为前缀的所有方法进行环绕增强
        args.add("execution(* com.rhys.spring.demo.TestA.test*(..))");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aspectJPointCutAdvisor2", beanDefinition);

        //后置切面
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AspectJPointCutAdvisor.class);
        args = new ArrayList<>();
        args.add("myAfterReturningAdvice");
        //对TestA类中的以test为前缀的所有方法进行后置增强
        args.add("execution(* com.rhys.spring.demo.TestA.test*(..))");
        beanDefinition.setConstructorArgumentValues(args);
        beanFactory.registryBeanDefinition("aspectJPointCutAdvisor3", beanDefinition);



        //配置AdvisorAutoProxyCreator的Bean
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(AdvisorAutoProxyCreator.class);
        beanFactory.registryBeanDefinition("advisorAutoProxyCreator", beanDefinition);

        //生成Type映射
        beanFactory.registerTypeMap();

        //注册完所需要的BeanDefinition后，且在生成普通Bean实例前，从BeanFactory中获得用户配置的所有BeanPostProcessor类型的Bean实例，注册到BeanFactory
        List<BeanPostProcessor> beanPostProcessorList = beanFactory.getBeanListOfType(BeanPostProcessor.class);
        if (!CollectionUtils.isEmpty(beanPostProcessorList)) {
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanFactory.registerBeanPostProcessor(beanPostProcessor);
            }
        }

        //提前实例化Bean
        beanFactory.instantiateSingleton();

        TestA testA = (TestA) beanFactory.getBean("aBean");
        testA.execute();
        System.out.println("--------------------------------");

        testA.testInit();
        System.out.println("--------------------------------");

        testA.testDestroy();
    }
}
```

## Bean定义配置化

> 在上方AOP的测试中可以发现代码调用非常繁琐且冗余代码非常多,如果这样提供给用户使用的话，可想而知，用户体验将非常不好，需要用户操作设置的内容非常多，因此，我们需要给使用者提供一些便捷的方式例如`注解/XML配置文件`，让用户体验更好，配置更高效

### XML配置文件实现

> - 定义XML规范
>
> - 解析XML，完成Bean定义的注册

![image-20230412015011071](https://article.biliimg.com/bfs/article/e6e1cb0c0977a7166769520385938d0d0df00144.png)

#### XML解析器

> 解析指定的xml文件

![image-20230418010630873](https://article.biliimg.com/bfs/article/c64a70328349a4242ccd9c9ee7eef63d787b5426.png)

###  注解方式实现

> - 定义注解
> - 扫描、解析注解，完成Bean定义的注册

#### 定义注解类

##### @Bean

> 标注在工厂方法，用于生产工厂Bean

```java
import org.springframework.core.annotation.AliasFor;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:10 AM
 */
public @interface Bean {
    @AliasFor("value")
    String value() default "";
    @AliasFor("name")
    String name() default "";
    String initMethod() default "";
    String destroyMethod() default "";
}
```

##### @Scope

> BeanName Scope

```java
import com.rhys.spring.IoC.BeanDefinition;
import java.lang.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:11 AM
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {
    String value() default BeanDefinition.SCOPE_SINGLETON;
}
```

##### @Primary

> BeanName Primary

```java
import java.lang.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:11 AM
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {
}
```

##### @Qualifier

```java
import java.lang.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:11 AM
 */

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {
    String value() default "";
}
```

##### @Component

> 是否标注这个注解取决于类要不要配置为Bean

```java
import java.lang.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:11 AM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
```

##### @Value

> 构造参数依赖

```java
import java.lang.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:12 AM
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    String value();
}
```

##### @Autowired

> 构造参数依赖

```java
import java.lang.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:10 AM
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    boolean required() default true;
}
```

#### 扫描解析注册

![image.png](https://article.biliimg.com/bfs/article/edd9441cf88896d43202c438dc29f5c395ad7e7e.png)

##### 类扫描器

![image-20230418021743722](https://article.biliimg.com/bfs/article/efae54c83df4a1e9aa5eb970d8bc162ae8563815.png)

###### 扫描包下类

![image-20230428133752868](C:\Users\admin\AppData\Roaming\Typora\typora-user-images\image-20230428133752868.png)

```java
public void scan(String... basePackages) {
    if (basePackages != null && basePackages.length > 0) {
        for (String basePackage : basePackages) {
            //递归扫描包目录下.class文件
            //组合包路径+class文件名 得到全限定类名
            //使用类加载器获取对应类名的Class对象
            //解析Class上的注解，获得Bean定义信息，注册Bean定义

            //递归扫描包目录下.class文件
            Set<File> files = this.doScan(basePackage);
            //得到Class对象，解析注解注册BeanDefinition
            this.readAndRegistryBeanDefinition(files);
        }
    }
}


private Set<File> doScan(String basePackage) {
    //扫描包下的类，将包名转为路径名
    String basePackagePath = "/" + StringUtils.replace(basePackage, ".", "/");
    //得到对应包目录
    File dir = new File(this.getClass().getResource(basePackagePath).getPath());
    //缓存找到的class文件
    Set<File> fileSet = new HashSet<>();
    //检索class文件
    this.retrieveClassFiles(dir, fileSet);

    return fileSet;
}

/**
 * 检索Class文件
 *
 * @param dir
 * @param fileSet
 * @return void
 * @author Rhys.Ni
 * @date 2023/4/19
 */
private void retrieveClassFiles(File dir, Set<File> fileSet) {
    for (File file : dir.listFiles()) {
        if (file.isDirectory() && file.canRead()) {
            retrieveClassFiles(file, fileSet);
        }

        if (file.getName().endsWith(".class")) {
            fileSet.add(file);
        }
    }
}
```

###### 解析类上注解

![image.png](https://article.biliimg.com/bfs/article/07b9a8e69252439fa8494ce0f5a209156e2af811.png)

```java

/**
 * <p>
 * <b>解析类上注解</b>
 * </p >
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/23
 * @param files <span style="color:#e38b6b;">字段描述</span>
 * @return <span style="color:#ffcb6b;"></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private void readAndRegistryBeanDefinition(Set<File> files) {
    for (File file : files) {
        String className = getClassNameFromFile(file);
        try {
            //加载类
            Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
            Component component = clazz.getAnnotation(Component.class);
            //标注了@Component注解
            if (component != null) {
                //获取Component设置的beanName
                String beanName = component.value();
                //根据类名生成beanName
                if (StringUtils.isBlank(beanName)) {
                    beanName = this.generateBeanName(clazz);
                }

                //配置BeanDefinition
                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

                //设置BeanClass
                beanDefinition.setBeanClass(clazz);

                //设置Scope
                Scope scope = clazz.getAnnotation(Scope.class);
                if (scope != null) {
                    beanDefinition.setScope(scope.value());
                }

                //设置primary
                Primary primary = clazz.getAnnotation(Primary.class);
                if (primary != null) {
                    beanDefinition.setPrimary(true);
                }

                //处理构造方法
                this.handleConstructor(clazz, beanDefinition);

                //处理方法上注解
                this.handleMethod(clazz, beanDefinition, beanName);

                //处理属性依赖
                this.handlePropertyDI(clazz, beanDefinition);

                //注册BeanDefinition
                this.beanDefinitionRegistry.registryBeanDefinition(beanName, beanDefinition);
            }

        } catch (Exception e) {
            log.error("Read And Registry BeanDefinition Exception:{}{}", e.getMessage(), e.getStackTrace());
        }
    }
}

/**
 * <p>
 * <b>处理属性依赖</b>
 * </p >
 *
 * @param clazz          <span style="color:#e38b6b;">字段描述</span>
 * @param beanDefinition <span style="color:#e38b6b;">字段描述</span>
 * @return <span style="color:#ffcb6b;"></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/21
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private void handlePropertyDI(Class<?> clazz, GenericBeanDefinition beanDefinition) {
    List<PropertyValue> propertyValues = new ArrayList<>();
    beanDefinition.setPropertyValues(propertyValues);

    //在类属性上找@Autowired注解，找到了则优先处理Qualifier注解
    //标注了@Qualifier则直接将qualifier设置的value作为beanName,否则直接将属性类型作为引用类型
    for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(Autowired.class)) {
            BeanReference beanReference = null;
            Qualifier qualifier = field.getAnnotation(Qualifier.class);
            if (qualifier != null) {
                beanReference = new BeanReference(qualifier.value());
            } else {
                beanReference = new BeanReference(field.getType());
            }

            propertyValues.add(new PropertyValue(field.getName(), beanReference));
        }
    }
}

/**
 * <p>
 * <b>处理方法上注解</b>
 * </p >
 *
 * @param clazz          <span style="color:#e38b6b;">字段描述</span>
 * @param beanDefinition <span style="color:#e38b6b;">字段描述</span>
 * @param beanName       <span style="color:#e38b6b;">字段描述</span>
 * @return <span style="color:#ffcb6b;"></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/21
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private void handleMethod(Class<?> clazz, GenericBeanDefinition beanDefinition, String beanName) throws BeanDefinitionRegistryException {
    for (Method method : clazz.getMethods()) {
        if (method.isAnnotationPresent(PostConstruct.class)) {
            beanDefinition.setInitMethodName(method.getName());
        } else if (method.isAnnotationPresent(PreDestroy.class)) {
            beanDefinition.setDestroyMethodName(method.getName());
        } else if (method.isAnnotationPresent(Bean.class)) {
            //处理工厂方法
            this.handleFactoryMethod(method, clazz, beanName);
        }
    }
}

/**
 * <p>
 * <b>处理工厂方法</b>
 * </p >
 *
 * @param method   <span style="color:#e38b6b;">字段描述</span>
 * @param clazz    <span style="color:#e38b6b;">字段描述</span>
 * @param beanName <span style="color:#e38b6b;">字段描述</span>
 * @return <span style="color:#ffcb6b;"></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/21
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private void handleFactoryMethod(Method method, Class<?> clazz, String beanName) throws BeanDefinitionRegistryException {
    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    //处理静态工厂

    //静态工厂直接将class作为bean类型，反之则为成员工厂方法，需要指定beanName
    if (Modifier.isStatic(method.getModifiers())) {
        beanDefinition.setBeanClass(clazz);
    } else {
        beanDefinition.setFactoryBeanName(beanName);
    }
    beanDefinition.setFactoryMethod(method);
    beanDefinition.setFactoryMethodName(method.getName());

    //处理@Scope注解
    Scope scope = method.getAnnotation(Scope.class);
    if (scope != null) {
        beanDefinition.setScope(scope.value());
    }

    //处理@Primary注解
    Primary primary = method.getAnnotation(Primary.class);
    if (primary != null) {
        beanDefinition.setPrimary(true);
    }

    //处理@Bean注解
    Bean bean = method.getAnnotation(Bean.class);
    String beanNameStr = bean.name();
    if (StringUtils.isBlank(beanNameStr)) {
        beanNameStr = method.getName();
    }

    //处理初始化方法和销毁方法
    String initMethod = bean.initMethod();
    if (StringUtils.isNotBlank(initMethod)) {
        beanDefinition.setInitMethodName(initMethod);
    }

    String destroyMethod = bean.destroyMethod();
    if (StringUtils.isNotBlank(destroyMethod)) {
        beanDefinition.setDestroyMethodName(destroyMethod);
    }

    //处理参数依赖
    beanDefinition.setConstructorArgumentValues(this.handleMethodParameters(method.getParameters()));

    //注册BeanDefinition
    this.beanDefinitionRegistry.registryBeanDefinition(beanNameStr, beanDefinition);
}

/**
 * <p>
 * <b>处理构造方法，在构造方法上找@Autowired注解，如有，将这个构造方法设置到BeanDefinition</b>
 * </p >
 *
 * @param clazz          <span style="color:#e38b6b;">处理类</span>
 * @param beanDefinition <span style="color:#e38b6b;">Bean定义</span>
 * @return <span style="color:#ffcb6b;"></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/21
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private void handleConstructor(Class<?> clazz, GenericBeanDefinition beanDefinition) {
    Constructor<?>[] constructors = clazz.getConstructors();
    if (ArrayUtils.isNotEmpty(constructors)) {
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(Autowired.class) != null) {
                beanDefinition.setConstructor(constructor);
                //构造参数依赖处理
                beanDefinition.setConstructorArgumentValues(this.handleMethodParameters(constructor.getParameters()));
                break;
            }
        }
    }
}

/**
 * <p>
 * <b>构造参数依赖处理,遍历获取参数上的注解，及创建构造参数依赖</b>
 * </p >
 *
 * @param parameters <span style="color:#e38b6b;">参数列表</span>
 * @return <span style="color:#ffcb6b;"> java.util.List<?></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/21
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private List<?> handleMethodParameters(Parameter[] parameters) {
    List<Object> args = new ArrayList<>();
    for (Parameter parameter : parameters) {

        //找@Value注解
        Value valueAnnotation = parameter.getAnnotation(Value.class);
        if (valueAnnotation != null) {
            args.add(valueAnnotation.value());
            continue;
        }

        //找@Qualifier注解,不为空则使用value的值为beanName，否则直接使用参数的类型去查找对应的Bean
        Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
        if (qualifier != null) {
            args.add(new BeanReference(qualifier.value()));
        } else {
            args.add(new BeanReference(parameter.getType()));
        }
    }
    return args;
}

/**
 * <p>
 * <b>根据类名生成beanName</b>
 * </p >
 *
 * @param clazz <span style="color:#e38b6b;">类</span>
 * @return <span style="color:#ffcb6b;"></span>
 * @throws Exception <span style="color:#ffcb6b;">异常类</span>
 * @author <span style="color:#4585ff;">RhysNi</span>
 * @date 2023/4/21
 * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
private String generateBeanName(Class<?> clazz) {
    String clazzName = clazz.getName();
    return clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
}

/**
 * 根据文件路径截取类名
 *
 * @param file
 * @return java.lang.String
 * @author Rhys.Ni
 * @date 2023/4/19
 */
private String getClassNameFromFile(File file) {
    //获取绝对路径
    String absolutePath = file.getAbsolutePath();
    String name = absolutePath.substring(classPathAbsLength + 1, absolutePath.indexOf("."));
    return StringUtils.replace(name, File.separator, ".");
}
```

###### 完整类扫描器代码

```java
package com.rhys.spring.context;

import com.rhys.spring.DI.BeanReference;
import com.rhys.spring.DI.PropertyValue;
import com.rhys.spring.IoC.BeanDefinitionRegistry;
import com.rhys.spring.IoC.GenericBeanDefinition;
import com.rhys.spring.IoC.exception.BeanDefinitionRegistryException;
import com.rhys.spring.context.annotation.*;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:32 AM
 */
public class ClassPathBeanDefinitionScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassPathBeanDefinitionScanner.class);
    private BeanDefinitionRegistry beanDefinitionRegistry;

    private int classPathAbsLength = new File(ClassPathBeanDefinitionScanner.class.getResource("/").getPath()).getAbsolutePath().length();


    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry beanDefinitionRegistry) {
        this.beanDefinitionRegistry = beanDefinitionRegistry;
    }


    /**
     * <p>
     * <b>扫描路径下class文件</b>
     * </p >
     *
     * @param basePackages <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/23
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    public void scan(String... basePackages) {
        if (basePackages != null && basePackages.length > 0) {
            for (String basePackage : basePackages) {
                //递归扫描包目录下.class文件
                //组合包路径+class文件名 得到全限定类名
                //使用类加载器获取对应类名的Class对象
                //解析Class上的注解，获得Bean定义信息，注册Bean定义

                //递归扫描包目录下.class文件
                Set<File> files = this.doScan(basePackage);
                //得到Class对象，解析注解注册BeanDefinition
                this.readAndRegistryBeanDefinition(files);
            }
        }
    }

    /**
     * <p>
     * <b>解析类上注解</b>
     * </p >
     *
     * @param files <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/23
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private void readAndRegistryBeanDefinition(Set<File> files) {
        for (File file : files) {
            String className = getClassNameFromFile(file);
            try {
                //加载类
                Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
                Component component = clazz.getAnnotation(Component.class);
                //标注了@Component注解
                if (component != null) {
                    //获取Component设置的beanName
                    String beanName = component.value();
                    //根据类名生成beanName
                    if (StringUtils.isBlank(beanName)) {
                        beanName = this.generateBeanName(clazz);
                    }

                    //配置BeanDefinition
                    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();

                    //设置BeanClass
                    beanDefinition.setBeanClass(clazz);

                    //设置Scope
                    Scope scope = clazz.getAnnotation(Scope.class);
                    if (scope != null) {
                        beanDefinition.setScope(scope.value());
                    }

                    //设置primary
                    Primary primary = clazz.getAnnotation(Primary.class);
                    if (primary != null) {
                        beanDefinition.setPrimary(true);
                    }

                    //处理构造方法
                    this.handleConstructor(clazz, beanDefinition);

                    //处理方法上注解
                    this.handleMethod(clazz, beanDefinition, beanName);

                    //处理属性依赖
                    this.handlePropertyDI(clazz, beanDefinition);

                    //注册BeanDefinition
                    this.beanDefinitionRegistry.registryBeanDefinition(beanName, beanDefinition);
                }

            } catch (Exception e) {
                log.error("Read And Registry BeanDefinition Exception:{}{}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    /**
     * <p>
     * <b>处理属性依赖</b>
     * </p >
     *
     * @param clazz          <span style="color:#e38b6b;">字段描述</span>
     * @param beanDefinition <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/21
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private void handlePropertyDI(Class<?> clazz, GenericBeanDefinition beanDefinition) {
        List<PropertyValue> propertyValues = new ArrayList<>();
        beanDefinition.setPropertyValues(propertyValues);

        //在类属性上找@Autowired注解，找到了则优先处理Qualifier注解
        //标注了@Qualifier则直接将qualifier设置的value作为beanName,否则直接将属性类型作为引用类型
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                BeanReference beanReference = null;
                Qualifier qualifier = field.getAnnotation(Qualifier.class);
                if (qualifier != null) {
                    beanReference = new BeanReference(qualifier.value());
                } else {
                    beanReference = new BeanReference(field.getType());
                }

                propertyValues.add(new PropertyValue(field.getName(), beanReference));
            }
        }
    }

    /**
     * <p>
     * <b>处理方法上注解</b>
     * </p >
     *
     * @param clazz          <span style="color:#e38b6b;">字段描述</span>
     * @param beanDefinition <span style="color:#e38b6b;">字段描述</span>
     * @param beanName       <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/21
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private void handleMethod(Class<?> clazz, GenericBeanDefinition beanDefinition, String beanName) throws BeanDefinitionRegistryException {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                beanDefinition.setInitMethodName(method.getName());
            } else if (method.isAnnotationPresent(PreDestroy.class)) {
                beanDefinition.setDestroyMethodName(method.getName());
            } else if (method.isAnnotationPresent(Bean.class)) {
                //处理工厂方法
                this.handleFactoryMethod(method, clazz, beanName);
            }
        }
    }

    /**
     * <p>
     * <b>处理工厂方法</b>
     * </p >
     *
     * @param method   <span style="color:#e38b6b;">字段描述</span>
     * @param clazz    <span style="color:#e38b6b;">字段描述</span>
     * @param beanName <span style="color:#e38b6b;">字段描述</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/21
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private void handleFactoryMethod(Method method, Class<?> clazz, String beanName) throws BeanDefinitionRegistryException {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        //处理静态工厂

        //静态工厂直接将class作为bean类型，反之则为成员工厂方法，需要指定beanName
        if (Modifier.isStatic(method.getModifiers())) {
            beanDefinition.setBeanClass(clazz);
        } else {
            beanDefinition.setFactoryBeanName(beanName);
        }
        beanDefinition.setFactoryMethod(method);
        beanDefinition.setFactoryMethodName(method.getName());

        //处理@Scope注解
        Scope scope = method.getAnnotation(Scope.class);
        if (scope != null) {
            beanDefinition.setScope(scope.value());
        }

        //处理@Primary注解
        Primary primary = method.getAnnotation(Primary.class);
        if (primary != null) {
            beanDefinition.setPrimary(true);
        }

        //处理@Bean注解
        Bean bean = method.getAnnotation(Bean.class);
        String beanNameStr = bean.name();
        if (StringUtils.isBlank(beanNameStr)) {
            beanNameStr = method.getName();
        }

        //处理初始化方法和销毁方法
        String initMethod = bean.initMethod();
        if (StringUtils.isNotBlank(initMethod)) {
            beanDefinition.setInitMethodName(initMethod);
        }

        String destroyMethod = bean.destroyMethod();
        if (StringUtils.isNotBlank(destroyMethod)) {
            beanDefinition.setDestroyMethodName(destroyMethod);
        }

        //处理参数依赖
        beanDefinition.setConstructorArgumentValues(this.handleMethodParameters(method.getParameters()));

        //注册BeanDefinition
        this.beanDefinitionRegistry.registryBeanDefinition(beanNameStr, beanDefinition);
    }

    /**
     * <p>
     * <b>处理构造方法，在构造方法上找@Autowired注解，如有，将这个构造方法设置到BeanDefinition</b>
     * </p >
     *
     * @param clazz          <span style="color:#e38b6b;">处理类</span>
     * @param beanDefinition <span style="color:#e38b6b;">Bean定义</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/21
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private void handleConstructor(Class<?> clazz, GenericBeanDefinition beanDefinition) {
        Constructor<?>[] constructors = clazz.getConstructors();
        if (ArrayUtils.isNotEmpty(constructors)) {
            for (Constructor<?> constructor : constructors) {
                if (constructor.getAnnotation(Autowired.class) != null) {
                    beanDefinition.setConstructor(constructor);
                    //构造参数依赖处理
                    beanDefinition.setConstructorArgumentValues(this.handleMethodParameters(constructor.getParameters()));
                    break;
                }
            }
        }
    }

    /**
     * <p>
     * <b>构造参数依赖处理,遍历获取参数上的注解，及创建构造参数依赖</b>
     * </p >
     *
     * @param parameters <span style="color:#e38b6b;">参数列表</span>
     * @return <span style="color:#ffcb6b;"> java.util.List<?></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/21
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private List<?> handleMethodParameters(Parameter[] parameters) {
        List<Object> args = new ArrayList<>();
        for (Parameter parameter : parameters) {

            //找@Value注解
            Value valueAnnotation = parameter.getAnnotation(Value.class);
            if (valueAnnotation != null) {
                args.add(valueAnnotation.value());
                continue;
            }

            //找@Qualifier注解,不为空则使用value的值为beanName，否则直接使用参数的类型去查找对应的Bean
            Qualifier qualifier = parameter.getAnnotation(Qualifier.class);
            if (qualifier != null) {
                args.add(new BeanReference(qualifier.value()));
            } else {
                args.add(new BeanReference(parameter.getType()));
            }
        }
        return args;
    }

    /**
     * <p>
     * <b>根据类名生成beanName</b>
     * </p >
     *
     * @param clazz <span style="color:#e38b6b;">类</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/4/21
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    private String generateBeanName(Class<?> clazz) {
        String clazzName = clazz.getName();
        return clazzName.substring(0, 1).toLowerCase() + clazzName.substring(1);
    }

    /**
     * 根据文件路径截取类名
     *
     * @param file
     * @return java.lang.String
     * @author Rhys.Ni
     * @date 2023/4/19
     */
    private String getClassNameFromFile(File file) {
        //获取绝对路径
        String absolutePath = file.getAbsolutePath();
        String name = absolutePath.substring(classPathAbsLength + 1, absolutePath.indexOf("."));
        return StringUtils.replace(name, File.separator, ".");
    }

    private Set<File> doScan(String basePackage) {
        //扫描包下的类，将包名转为路径名
        String basePackagePath = "/" + StringUtils.replace(basePackage, ".", "/");
        //得到对应包目录
        File dir = new File(this.getClass().getResource(basePackagePath).getPath());
        //缓存找到的class文件
        Set<File> fileSet = new HashSet<>();
        //检索class文件
        this.retrieveClassFiles(dir, fileSet);

        return fileSet;
    }

    /**
     * 检索Class文件
     *
     * @param dir
     * @param fileSet
     * @return void
     * @author Rhys.Ni
     * @date 2023/4/19
     */
    private void retrieveClassFiles(File dir, Set<File> fileSet) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory() && file.canRead()) {
                retrieveClassFiles(file, fileSet);
            }

            if (file.getName().endsWith(".class")) {
                fileSet.add(file);
            }
        }
    }
}
```

### ApplicationContext上下文

> 由于我们需要自动创建Bean，那么根据Xml配置类去解析创建根据注解去解析创建不好决策，所以需要提供一个`门面模式`来统一对外提供功能，所以需要一个`ApplicationContext上下文`

![image-20230428134213919](https://article.biliimg.com/bfs/article/3d2b3af4800db84836a882015307fd773cee6527.png)

#### ApplicationContext接口

```java
package com.rhys.spring.context;

import com.rhys.spring.IoC.BeanFactory;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:22 AM
 */
public interface ApplicationContext extends BeanFactory {
}

```

#### AbstractApplicationContext抽象类

```java
package com.rhys.spring.context;

import com.rhys.spring.IoC.BeanPostProcessor;
import com.rhys.spring.IoC.PreBuildBeanFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:16 AM
 */
public abstract class AbstractApplicationContext implements ApplicationContext {
    protected PreBuildBeanFactory beanFactory;

    public AbstractApplicationContext() {
        this.beanFactory = new PreBuildBeanFactory();
    }

    /**
     * 注册Bean执行器
     *
     * @param
     * @return void
     * @author Rhys.Ni
     * @date 2023/4/19
     */
    private void doRegistryBeanPostProcessor() throws Throwable {
        List<BeanPostProcessor> beanPostProcessors = this.beanFactory.getBeanListOfType(BeanPostProcessor.class);
        if (CollectionUtils.isNotEmpty(beanPostProcessors)) {
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                this.beanFactory.registerBeanPostProcessor(beanPostProcessor);
            }
        }
    }

    protected void refresh() throws Throwable {
        this.beanFactory.registerTypeMap();
        this.doRegistryBeanPostProcessor();
        this.beanFactory.instantiateSingleton();
    }


    /**
     * 获取Bean实例
     *
     * @param beanName bean名称
     * @return java.lang.Object
     * @author Rhys.Ni
     * @date 2023/2/16
     */
    @Override
    public Object getBean(String beanName) throws Exception {
        return this.beanFactory.getBean(beanName);
    }

    /**
     * 根据beanName获取Type
     *
     * @param beanName
     * @return java.lang.Class<?>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    @Override
    public Class<?> getType(String beanName) throws Exception {
        return this.beanFactory.getType(beanName);
    }

    /**
     * 根据Type获取bean实例
     *
     * @param c
     * @return T
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    @Override
    public <T> T getBean(Class<T> c) throws Exception {
        return this.beanFactory.getBean(c);
    }

    /**
     * 根据Type获取bean实例
     *
     * @param c
     * @return java.util.Map<java.lang.String, T>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    @Override
    public <T> Map<String, T> getBeanOfType(Class<T> c) throws Exception {
        return this.beanFactory.getBeanOfType(c);
    }

    /**
     * 根据Type获取bean集合
     *
     * @param c
     * @return java.util.List<T>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    @Override
    public <T> List<T> getBeanListOfType(Class<T> c) throws Exception {
        return this.beanFactory.getBeanListOfType(c);
    }

    /**
     * <p>
     * <b>beanPostProcessor注册器</b>
     * </p >
     *
     * @param beanPostProcessor <span style="color:#e38b6b;">bean执行器</span>
     * @return <span style="color:#ffcb6b;"></span>
     * @throws Exception <span style="color:#ffcb6b;">异常类</span>
     * @author <span style="color:#4585ff;">RhysNi</span>
     * @date 2023/3/27
     * @CopyRight: <a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
     */
    @Override
    public void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanFactory.registerBeanPostProcessor(beanPostProcessor);
    }
}
```

#### AnnotationApplicationContext类

```java
package com.rhys.spring.context;


/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/4/19 3:29 AM
 */
public class AnnotationApplicationContext extends AbstractApplicationContext {
    public AnnotationApplicationContext(String... basePackages) throws Throwable {
        //找到所有被@Component修饰的Java类的BeanDefinition
        new ClassPathBeanDefinitionScanner(this.beanFactory).scan(basePackages);
        super.refresh();
    }
}

```

#### XmlApplicationContext类

> 后续实现

### Bean注解测试

> 在`TestA`类和`TestB`类加上相关注解如下

```java
package com.rhys.spring.demo;

import com.rhys.spring.context.annotation.*;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/8 12:21 AM
 */
@Component("testABean")
@Primary
public class TestA {
    private String name;
    private TestB testB;


    @Autowired
    public TestA(@Value("TestA_RhysNi") String name, @Qualifier("testBBean") TestB testB) {
        super();
        this.name = name;
        this.testB = testB;
        System.out.println("调用了含有testB参数的构造方法");
    }


    public TestA(TestB testB) {
        this.testB = testB;
    }

    public void execute() {
        System.out.println("aBean execute:" + this.name + "\n testB.name:" + this.testB.getName());
    }

    public void testInit() {
        System.out.println("aBean 执行了init()方法");
    }

    public void testDestroy() {
        System.out.println("aBean 执行了destroy()方法");
    }
}
```

```java
package com.rhys.spring.demo;

import com.rhys.spring.context.annotation.Autowired;
import com.rhys.spring.context.annotation.Component;
import com.rhys.spring.context.annotation.Value;

import java.lang.annotation.Retention;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/3/8 12:21 AM
 */
@Component("testBBean")
public class TestB {

    @Value("TestB_RhysNi")
    private String name;

    @Autowired
    public TestB(@Value("testBBean") String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

```

> 在`TestA`和`TestB`类同级目录下创建`TestApplicationContext`测试类
>
> - 因为我们要扫描.class文件，如果放在Test资源目录下读取的路径会是错误的，在获取.class文件时截取路径会有问题，这个问题仅存在于本地，在环境上不会有问题

```java
package com.rhys.spring.demo;

import com.rhys.spring.context.AnnotationApplicationContext;
import com.rhys.spring.context.ApplicationContext;

/**
 * <p>
 * <b>功能描述</b>
 * </p >
 *
 * @author : RhysNi
 * @version : v1.0
 * @date : 2023/4/23 14:06
 * @CopyRight :　<a href="https://blog.csdn.net/weixin_44977377?type=blog">倪倪N</a>
 */
public class TestApplicationContext {
    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new AnnotationApplicationContext("com.rhys.spring.demo");
        TestA testA = (TestA) applicationContext.getBean("testABean");
        testA.execute();
    }
}
```