package com.bumblebee.spring.ioc.beanFactory;

public interface ISpi<T> {
    boolean verify(T condition);
}
