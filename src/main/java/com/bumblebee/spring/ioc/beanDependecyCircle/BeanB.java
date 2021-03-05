package com.bumblebee.spring.ioc.beanDependecyCircle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BeanB {

    @Autowired
    BeanA beanA;

    @Override
    public String toString() {
        return "BeanB{" +
                "beanA=" + "A" +
                '}';
    }
}
