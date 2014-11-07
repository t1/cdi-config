package com.github.t1.config;

import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        Properties p = new Properties();
        p.put("foo", "bar");
        p.store(System.out, "the-comment");
        p.storeToXML(System.out, "the-comment");
    }
}
