package org.scijava.nativelib;

import java.io.IOException;

import org.junit.Test;

public class NativeLoaderTest {

    @Test(expected = IOException.class)
    public void exampleHowToUse() throws Exception {
        NativeLoader.loadLibrary("mylib");
        // expect IOException, because this lib does not exist
    }
}