package com.bumblebee.spring.ioc.beanFactory;

import com.bumblebee.spring.ioc.beanDependecyCircle.BeanA;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class FactoryBeanA implements FactoryBean<BeanA> {
    @Override
    public BeanA getObject() throws Exception {
        System.out.println("getObject");
        return new BeanA();
    }

    @Override
    public Class<BeanA> getObjectType() {
        return BeanA.class;
    }

    @Override
    public String toString() {
        return "FactoryBeanA{}";
    }
}
