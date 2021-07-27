package org.scijava.nativelib;

public final class OsInfoFactory {

	private OsInfoFactory() {
		// util class.
	}

	public static OsInfo fromCurrent() {
		final String osName = System.getProperty("os.name");
		final String family = NativeOsArchUtil.determineOsFamily(osName);
		final String arch = System.getProperty("os.arch");

		final String normalizedArch = NativeOsArchUtil.normalizeArchitecture(arch);

		final int bitness = NativeOsArchUtil.determineBitness();

		return new OsInfo(family, normalizedArch, bitness);
	}

	public static OsInfo from(final String osFamily, final String architecture, final int bitness) {
		final String normalizedArch = NativeOsArchUtil.normalizeArchitecture(architecture);

		return new OsInfo(osFamily, normalizedArch, bitness);
	}

	public static OsInfo fromOsName(final String osName, final String architecture, final int bitness) {
		final String osFamily = NativeOsArchUtil.determineOsFamily(osName);

		return from(osFamily, architecture, bitness);
	}
}
