package org.scijava.nativelib;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.scijava.nativelib.NativeOsArchUtil.*;

import java.util.Collections;
import java.util.List;

public enum DefaultOsInfo {
	/* 32 bit */
	AIX_PPC_32(OS_FAMILY_AIX, ARCH_PPCLE32, 32, LegacyConstants.LIST_AIX_32),
	LINUX_ARM_32(OS_FAMILY_LINUX, ARCH_ARM32, 32, LegacyConstants.LIST_LINUX_ARM),
	LINUX_X86_32(OS_FAMILY_LINUX, ARCH_X86_32, 32, LegacyConstants.LIST_LINUX_32),
	MACOS_PPC_32(OS_FAMILY_OSX, ARCH_PPCLE32, 32, LegacyConstants.LIST_OSX_32),
	SOLARIS_SPARC_32(OS_FAMILY_SOLARIS, ARCH_SPARC32, 32, LegacyConstants.LIST_SOLARIS_32),
	WINDOWS_X86_32(OS_FAMILY_WINDOWS, ARCH_X86_32, 32, LegacyConstants.LIST_WINDOWS_32),
	/* 64 bit */
	AIX_PPC64_64(OS_FAMILY_AIX, ARCH_PPC64, 64, LegacyConstants.LIST_AIX_64),
	LINUX_X86_64(OS_FAMILY_LINUX, ARCH_X86_64, 64, LegacyConstants.LIST_LINUX_64),
	LINUX_AARCH_64(OS_FAMILY_LINUX, ARCH_AARCH64, 64, LegacyConstants.LIST_LINUX_ARM_64),
	LINUX_PPC64LE_64(OS_FAMILY_LINUX, ARCH_PPCLE64, 64, LegacyConstants.LIST_LINUX_64),
	MACOS_X86_64(OS_FAMILY_OSX, ARCH_X86_64, 64, LegacyConstants.LIST_OSX_64),
	SOLARIS_SPARCV9_64(OS_FAMILY_SOLARIS, ARCH_SPARC64, 64, LegacyConstants.LIST_SOLARIS_64),
	WINDOWS_X86_64(OS_FAMILY_WINDOWS, ARCH_X86_64, 64, LegacyConstants.LIST_WINDOWS_64),
	WINDOWS_EM64T_64(OS_FAMILY_WINDOWS, ARCH_X86_64, 64, LegacyConstants.LIST_WINDOWS_64),
	WINDOWS_IA64_64(OS_FAMILY_WINDOWS, ARCH_IA64, 64, LegacyConstants.LIST_WINDOWS_64);

	private final OsInfo osInfo;
	private final List<String> legacyPaths;

	/**
	 * Creates a DefaultOsInfo-Item.
	 *
	 * @param family       the os family. Use {@link NativeOsArchUtil#determineOsFamily(String)}.
	 * @param architecture the architecture as reported by {@code os.name}.
	 * @param bitness      the bitness (usually 32 or 64).
	 * @param legacyPaths  a list of legacy path fragmets for compatiblity. Use {@link Collections#emptyList()} if in doubt.
	 * @throws NullPointerException if any parameter is null.
	 */
	DefaultOsInfo(final String family, final String architecture, final int bitness, final List<String> legacyPaths) {
		this.osInfo = OsInfoFactory.from(family, architecture, bitness);
		this.legacyPaths = unmodifiableList(legacyPaths);
	}

	public OsInfo getOsInfo() {
		return this.osInfo;
	}

	public List<String> getLegacyPaths() {
		return this.legacyPaths;
	}

	@Override
	public String toString() {
		return "DefaultOsInfo{" +
				"osInfo=" + this.osInfo +
				"legacyPaths=" + this.legacyPaths +
				"} " + super.toString();
	}

	private static class LegacyConstants {

		public static final List<String> LIST_AIX_32 = asList("aix_32");
		public static final List<String> LIST_LINUX_ARM = asList("linux_arm");
		public static final List<String> LIST_LINUX_32 = asList("linux_32");
		public static final List<String> LIST_OSX_32 = asList("osx_32");
		public static final List<String> LIST_SOLARIS_32 = asList("solaris_32");
		public static final List<String> LIST_WINDOWS_32 = asList("windows_32");
		public static final List<String> LIST_AIX_64 = asList("aix_64");
		public static final List<String> LIST_LINUX_64 = asList("linux_64");
		public static final List<String> LIST_LINUX_ARM_64 = asList("linux_arm64");
		public static final List<String> LIST_OSX_64 = asList("osx_64");
		public static final List<String> LIST_WINDOWS_64 = asList("windows_64");
		public static final List<String> LIST_SOLARIS_64 = asList("solaris_64");
	}
}
