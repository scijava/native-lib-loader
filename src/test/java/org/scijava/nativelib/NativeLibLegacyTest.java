package org.scijava.nativelib;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class NativeLibLegacyTest {

	@Parameterized.Parameter()
	public DefaultOsInfo defaultOsInfo;

	@Parameterized.Parameter(1)
	public String legacyPath;

	@Parameterized.Parameters(name = "{index} : {0} => {1}")
	public static Object[][] parameters() {
		return new Object[][]{
				new Object[]{DefaultOsInfo.LINUX_X86_32, "linux_32"},
				new Object[]{DefaultOsInfo.LINUX_X86_64, "linux_64"},
				new Object[]{DefaultOsInfo.LINUX_ARM_32, "linux_arm"},
				new Object[]{DefaultOsInfo.LINUX_AARCH_64, "linux_arm64"},
				new Object[]{DefaultOsInfo.MACOS_PPC_32, "osx_32"},
				new Object[]{DefaultOsInfo.MACOS_X86_64, "osx_64"},
				new Object[]{DefaultOsInfo.WINDOWS_X86_32, "windows_32"},
				new Object[]{DefaultOsInfo.WINDOWS_X86_64, "windows_64"},
				new Object[]{DefaultOsInfo.WINDOWS_IA64_64, "windows_64"},
				new Object[]{DefaultOsInfo.AIX_PPC_32, "aix_32"},

		};
	}

	@Test
	public void testLibLegacyForLinux32() {
		final List<String> pathsForOsInfo = NativeLibraryUtil.getPlatformLibraryPath(null, this.defaultOsInfo.getOsInfo());
		assertThat("Pathes for [" + this.defaultOsInfo.name() + "] must contain [" + this.legacyPath + "] for compatiblity.",
				pathsForOsInfo, CoreMatchers.hasItem(this.legacyPath));

	}
}
