package com.hbl.cameraxapp.test;


import org.junit.Test;

import static org.junit.Assert.*;

public class AnimalTest {

    @Test
    public void testInitCode() {
        Animal animal = new Animal();
        animal.setName("tom");
        animal.move();
        syout("class type animal", animal);

        Animal jessy = new Animal() {
            {
                setName("jessy");
            }
        };
        jessy.move();
        syout("class type jessy", jessy);

        Animal lll = new Animal() {
            {
                setName("lll");
            }
            @Override
            public void move() {
                System.out.println(getName() + " animal move");
            }
        };
        syout("class type lll",lll);

    }

    private void syout(String s, Animal animal) {
        System.out.println(s +"  "+ animal.getClass().getName());
        System.out.println(animal);
    }
}
