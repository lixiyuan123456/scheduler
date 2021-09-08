package com.bountyhunter.galaxy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

public class Fuck {

    public static void main(String[] args) {
        Fuck fuck = new Fuck();
        List<Object> objects = Arrays.asList(new Object());
        fuck.process(objects);
        System.out.println(objects.get(0));
    }

    private void process(final List<Object> objects) {
        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);
            object.setCode(i);
            object.setContent("abcdefg");
        }
    }

    @ToString
    @Getter
    @Setter
    public static class Object {

        private int code;
        private String content;
    }
}
