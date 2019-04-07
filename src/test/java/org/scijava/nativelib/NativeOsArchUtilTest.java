package org.scijava.nativelib;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeOsArchUtilTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(NativeOsArchUtilTest.class);

  @Test
  public void testExpectedPaths_aix64() {
    final OsInfo aixPpc64 = OsInfoFactory.from("aix", "ppc64", 64);
    final String aixPpc64path = NativeOsArchUtil.getPathForOsInfo(aixPpc64);

    assertThat(aixPpc64path, is("aix-ppc_64-64"));
  }

  @Test
  public void testPathSanity() {
    final DefaultOsInfo[] osInfoList = DefaultOsInfo.values();

    for (final DefaultOsInfo defaultEntry : osInfoList) {
      final OsInfo osInfo = defaultEntry.getOsInfo();
      final String path = NativeOsArchUtil.getPathForOsInfo(osInfo);
      LOGGER.debug("Path [{}] for os [{}].", path, osInfo);
      assertThat(path.split("-").length, (anyOf(is(3), is(4))));
    }

  }

}
