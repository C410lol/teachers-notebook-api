package com.api.notebook.utils;

import java.util.Random;

public class CodeGenerator {

    private static final Random random = new Random();

    public static int generateCode() {
        return random.nextInt(1000, 9999);
    }

}
