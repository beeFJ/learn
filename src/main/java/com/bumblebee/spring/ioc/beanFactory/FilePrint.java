package com.bumblebee.spring.ioc.beanFactory;

import org.springframework.stereotype.Component;

@Component
public class FilePrint implements IPrint {
    @Override
    public void print(Integer condition, String message) {
        System.out.println("file print");
    }

    @Override
    public boolean verify(Integer condition) {
        if (condition < 0) {
            return true;
        }
        return false;
    }
}
