package com.mimir;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class MimirApplication {
    public static void main(String... args) {
        Quarkus.run(args);
    }
}
