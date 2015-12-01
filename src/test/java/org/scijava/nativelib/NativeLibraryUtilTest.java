package org.scijava.nativelib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NativeLibraryUtilTest {

    @Test
    public void ifNoVersionWasFoundLibraryNameIsReturned() throws Exception {
        final String versionedLibraryName = NativeLibraryUtil.getVersionedLibraryName(NativeLibraryUtil.class, "native-lib-loader");
        assertEquals("native-lib-loader", versionedLibraryName);
    }
}