package com.bumblebee.spring.ioc.beanFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpiFactoryBean<T> implements FactoryBean<T> {
    private List<ISpi> iSpis;

    public SpiFactoryBean(ApplicationContext applicationContext) {
        final Map<String, ISpi> iSpiMap = applicationContext.getBeansOfType(ISpi.class);
        iSpis = new ArrayList<>(iSpiMap.values());
    }

    @Override
    public T getObject() throws Exception {
        InvocationHandler invocationHandler = (Object proxy, Method method, Object[] args) -> {
            Object ret = null;
            for (ISpi iSpi : iSpis) {
                if (iSpi.verify(args[0])) {
                    ret = method.invoke(iSpi, args);
                }
            }
            return ret;
        };
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{IPrint.class}, invocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

}
