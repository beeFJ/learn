package com.bumblebee.spring.ioc;

import com.bumblebee.spring.ioc.beanFactory.IPrint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 一. Spring bean的生命周期
 *  1.1 实例化
 *  1.2 填充属性
 *  1.3 初始化
 *      1.3.1 调用Aware回调接口，设置对应属性
 *          1.3.1.1 bean如果实现了BeanNameAware接口，调用回调方法setBeanName，将bean的ID传入
 *          1.3.1.2 bean如果实现了BeanFactoryAware接口，调用回调方法setBeanFactory，将bean的工厂传入
 *          1.3.1.3 bean如果实现了ApplicationContextAware接口，调用回调方法setApplicationContext，将applicationContext传入
 *      1.3.2 调用BeanPostProcessor的postProcessBeforeInitialization，前置处理bean对象
 *      1.3.3 调用InitializingBean的afterPropertiesSet，设置属性
 *      1.3.4 调用自定义的init-method
 *      1.3.5 调用BeanPostProcessor的postProcessAfterInitialization，后置处理bean对象
 *
 * 二. Spring 解决bean循环依赖
 *  1. 只能解决单例bean的setter方法注入，不能解决非单例的循环引用，不能解决构造器注入
 *  2. 三级缓存
 *      2.1 第一级：singletonObjects --- 缓存已初始化完成的单例bean
 *      2.2 第二级：earlySingletonObjects --- 缓存已经实例化但尚未依赖注入的单例bean
 *      2.3 第三级：singletonFactories --- 缓存能获取已实例化的bean的单例工厂
 *  3. 具体流程 A -> B , B -> A
 *      3.1 doGetBean获取bean A,首先getSingleton尝试从三级缓存中获取bean A, 没有获取到
 *      3.2 doCreateBean创建bean A, 首先createBeanInstance实例化bean A, 将bean A的单例工厂存入第三级缓存中，然后填充依赖的属性, 发现依赖bean B, 就开始获取bean B
 *      3.3 同样doGetBean获取bean B, 首先getSingleton尝试从三级缓存中获取bean B, 没有获取到
 *      3.4 doCreateBean创建bean B, 首先createBeanInstance实例化bean B, 将bean B的单例工厂存入三级缓存中，然后填充依赖的属性, 发现依赖bean A, 就开始获取bean A
 *      3.5 doGetBean获取bean A,首先getSingleton尝试从三级缓存中获取bean A, 由于之前3.2中已经存入了bean A的单例工厂至第三级缓存中，所以直接调用其单例工厂获取bean A, 并将其存入第二级缓存中，即已实例化但未初始化的单例bean
 *      3.6 将3.5中获取的bean A注入bean B，然后bean B继续依赖注入以及初始化
 *      3.7 将3.6中初始化完成的bean B注入到bean A中，然后bean A继续依赖注入以及初始化
 *  4. 二级缓存存在的意义
 *      为了解决循环依赖中，依赖不到aop生成的代理类
 *      当A为代理类时，三级缓存的ObjectFactory的getObject会调用所有的SmartInstantiationAwareBeanPostProcessor的getEarlyBeanReference,如果需要生成代理，会返回实际的代理类引用，
 *      然后getSingletonObject依次从一级缓存,二级缓存,三级缓存中获取，调用三级缓存的ObjectFactory的getObject得到实际代理类，将其缓存在二级缓存中
 *
 * 三. FactoryBean
 *  是一个工厂bean，能够生产一类对象，一般用于复杂对象的生成。工厂bean生成实际bean的时候，是通过调用FactoryBean的getObject方法生成的
 *  工厂bean的name以&开头，如果需要工厂实例，则name前加上&即可
 *
 * 四. BeanDefinition
 *  1. 主要保存了bean从实例化->填充属性->初始化整个阶段所需要的元数据，包含如何创建它，它的依赖，以及它的初始化过程
 *  2. GenericBeanDefinition RootBeanDefinition ChildBeanDefinition AnnotationGenericBeanDefinition ScannedGenericBeanDefinition
 *
 * 五. BeanDefinitionReader BeanDefinitionParser
 *
 * 六. Spring获取bean的流程-doGetBean
 *  1. 获取真实的bean name，将FactoryBean的bean name的&去掉
 *  2. 从缓存中获取bean，依次从一级缓存，二级缓存，三级缓存中获取，一级缓存存储已初始化完成的bean，二级缓存存储实例化但未填充属性和初始化的bean，三级缓存存储能获取刚实例化的bean的ObjectFactory
 *  三级缓存主要是为了解决bean的循环依赖，二级缓存的存在是为了解决代理对象的循环依赖，因为正常的aop代理是在ioc容器对象完成了正常的生命周期（实例化-属性填充-初始化）之后，才进行代理对象的生成，所以循环依赖代理对象时，不违背Spring的设计原则，就用了三级缓存来获取代理对象
 *  三级缓存存储的ObjectFactory是一个函数式接口，它的方法里通过遍历调用SmartInstantiationAwareBeanPostProcessor的getEarlyReference，其中的AbstractAutoProxyCreator会生成代理对象
 *  3. 如果从缓存中获取到了bean
 *      3.1 根据传入的beanName判断是否是FactoryBean，如果是FactoryBean且获取的beanName以&开头，则直接返回工厂bean，否则调用FactoryBean的getObject返回实际的bean
 *  4. 缓存中没有获取到bean，则进入创建bean的流程。
 *      4.1 根据beanName获取对应的RootBeanDefinition
 *      4.2 检查得到的RootBeanDefinition是否为抽象bean，如果为抽象bean，则抛出异常
 *      4.3 先保证depends-on的bean实例并初始化
 *      4.4 先解析bean的Class
 *      4.5 实例化bean
 *      4.6 填充属性
 *      4.7 bean初始化，
 *          如果bean实现了Aware接口，则调用Aware接口的回调接口；
 *          如果存在BeanPostProcessor，遍历调用各个BeanPostProcessor的postProcessorBeforeInitialization；
 *          如果Bean实现了InitialingBean，则调用其回调方法afterPropertiesSet；
 *          如果bean定义了自定义的init-method，则调用其初始化方法；
 *          最后再遍历回调BeanPostProcessor的postProcessAfterInitialization
 *  5. 此时获取bean的流程就结束了
 *
 * 七. ApplicationContext的refresh
 *  1.
 *
 */
public class Ioc {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("com.bumblebee.spring");
        context.refresh();

//        testBeanLifecycle(context);
//        testBeanCircleDependency(context);
//        testFactoryBean(context);
    }

    /**
     * 1. bean生命周期
     * @param context
     */
    private static void testBeanLifecycle(ApplicationContext context) {
        context.getBean("springIocBean");
    }

    /**
     * 2. bean循环依赖
     * @param context
     */
    private static void testBeanCircleDependency(ApplicationContext context) {
        final Object beanA = context.getBean("beanA");
        final Object beanB = context.getBean("beanB");
        System.out.println(beanA);
        System.out.println(beanB);
    }

    private static void testFactoryBean(ApplicationContext context) {
        final Object factoryBeanA = context.getBean("factoryBeanA");
        System.out.println(factoryBeanA);
        final Object factoryBeanA2 = context.getBean("factoryBeanA");
        System.out.println(factoryBeanA2);
        final Object factoryBeanAA = context.getBean("&factoryBeanA");
        System.out.println(factoryBeanAA);

        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        final IPrint spiFactoryBean = (IPrint) context.getBean("spiFactoryBean");
        spiFactoryBean.print(1, "111");
    }
}
