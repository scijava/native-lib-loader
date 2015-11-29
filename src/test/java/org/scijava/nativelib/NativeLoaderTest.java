package org.scijava.nativelib;

import org.testng.annotations.Test;

import java.io.IOException;

public class NativeLoaderTest {

    @Test(expectedExceptions = IOException.class)
    public void example_how_to_use() throws Exception {

        NativeLoader.loadLibrary("mylib");

        // expect IOException, because this lib does not exists
    }
}