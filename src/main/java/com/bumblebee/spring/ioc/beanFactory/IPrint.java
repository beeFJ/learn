package com.bumblebee.spring.ioc.beanFactory;

public interface IPrint extends ISpi<Integer> {

    void print(Integer level, String message);
}
