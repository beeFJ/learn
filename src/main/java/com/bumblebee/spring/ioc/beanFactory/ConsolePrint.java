package com.bumblebee.spring.ioc.beanFactory;

import org.springframework.stereotype.Component;

@Component
public class ConsolePrint implements IPrint {
    @Override
    public void print(Integer condition, String message) {
        System.out.println("console print");
    }

    @Override
    public boolean verify(Integer condition) {
        if (condition > 0) {
            return true;
        }
        return false;
    }
}
