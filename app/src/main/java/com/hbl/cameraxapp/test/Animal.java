package com.hbl.cameraxapp.test;

public class Animal {
    {
        System.out.println("animal init");
    }

    public Animal() {
        System.out.println("animal constructor");
    }

    public void move() {
        System.out.println(name + " animal move");
    }

    public String getName() {
        return name;
    }

    private String name;

    public void setName(String name) {
        this.name = name;
    }
}
