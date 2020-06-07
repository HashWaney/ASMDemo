package me.hash.java.processor;

import me.hash.java.processor.annotation.Cost;

public class MyClass {

    @Cost
    public void showTime(String msg) {
        System.out.println("show() is show Time");
    }
}
