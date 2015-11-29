package org.scijava.nativelib;

import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.assertThat;

public class NativeLibraryUtilTest {

    @Test
    public void if_no_version_was_found_library_name_is_returned () throws Exception {
        String versionedLibraryName = NativeLibraryUtil.getVersionedLibraryName(NativeLibraryUtil.class, "native-lib-loader");

        assertThat(versionedLibraryName).isEqualTo("native-lib-loader");
    }
}