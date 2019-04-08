package org.scijava.nativelib;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Locale;

public final class NativeOsArchUtil {

	public static final String ARCH_X86_32 = "x86_32";
	public static final String ARCH_X86_64 = "x86_64";

	public static final String ITANIUM_32 = "itanium_32";
	public static final String ARCH_IA64 = "itanium_64";

	public static final String ARCH_SPARC32 = "sparc_32";
	public static final String ARCH_SPARC64 = "sparc_64";

	public static final String ARCH_ARM32 = "arm_32";
	public static final String ARCH_AARCH64 = "aarch_64";

	public static final String ARCH_PPC32 = "ppc_32";
	public static final String ARCH_PPC64 = "ppc_64";

	public static final String ARCH_PPCLE32 = "ppcle";
	public static final String ARCH_PPCLE64 = "ppcle_64";

	/**
	 * Aliases reported by JDKs for {@link #ARCH_X86_32}.
	 */
	public static final List<String> ALIASES_X86_32 = asList("x8632", "x86", "i386", "i486", "i586", "i686", "ia32", "x32");
	/**
	 * Aliases reported by JDKs for {@link #ARCH_X86_64}.
	 */
	public static final List<String> ALIASES_X86_64 = asList("x8664", "amd64", "ia32e", "em64t", "x64");

	/**
	 * Aliases reported by JDKs for {@link #ARCH_IA64}.
	 */
	public static final List<String> ALIASES_ITA64 = asList("ia64", "ia64w", "itanium64");

	/**
	 * Aliases reported by JDKs for {@link #ARCH_IA64}.
	 */
	public static final List<String> ALIASES_SPARC_32 = asList("sparc", "sparc32");

	/**
	 * Aliases reported by JDKs for {@link #ARCH_IA64}.
	 */
	public static final List<String> ALIASES_SPARC_64 = asList("sparcv9", "sparc64");

	/**
	 * Aliases reported by JDKs for {@link #ARCH_PPC32}.
	 */
	public static final List<String> ALIASES_PPC32 = singletonList("ppc");
	/**
	 * Aliases reported by JDKs for {@link #ARCH_PPC64}.
	 */
	public static final List<String> ALIASES_PPC64 = singletonList("ppc64");
	/**
	 * Aliases reported by JDKs for {@link #ARCH_PPCLE32}.
	 */
	public static final List<String> ALIASES_PPCLE32 = asList("ppcle", "ppc32le");
	/**
	 * Aliases reported by JDKs for {@link #ARCH_PPCLE64}.
	 */
	public static final List<String> ALIASES_PPCLE64 = singletonList("ppc64le");


	public static final String OS_FAMILY_LINUX = "linux";
	public static final String OS_FAMILY_WINDOWS = "windows";
	public static final String OS_FAMILY_OSX = "osx";
	public static final String OS_FAMILY_AIX = "aix";
	public static final String OS_FAMILY_SOLARIS = "solaris";
	public static final String OS_FAMILY_ZOS = "zos";


	private NativeOsArchUtil() {
		// utility class.
	}

	/**
	 * Tries to determine the OS family given a osName string.
	 *
	 * @param osName the osName returned by the {@code os.name} system property.
	 * @return the os family.
	 * @throws IllegalArgumentException if an unknown osName is given.
	 * @throws NullPointerException     if the {@code osName} parameter is {@code null}.
	 */
	public static String determineOsFamily(final String osName) {
		final String lowercaseOsName = osName.toLowerCase(Locale.ENGLISH);

		if (lowercaseOsName.contains(OS_FAMILY_SOLARIS) || lowercaseOsName.contains("sunos")) {
			return OS_FAMILY_SOLARIS;
		}

		if (lowercaseOsName.contains("nix") || lowercaseOsName.contains("nux")) {
			return OS_FAMILY_LINUX;
		}

		if (lowercaseOsName.contains("win")) {
			return OS_FAMILY_WINDOWS;
		}

		if (lowercaseOsName.contains("mac")) {
			return OS_FAMILY_OSX;
		}

		if (lowercaseOsName.contains("aix")) {
			return OS_FAMILY_AIX;
		}

		throw new IllegalStateException("OS family cannot be determined.");
	}

	public static int determineBitness() {
		// try the widely adopted sun specification first.
		String bitness = System.getProperty("sun.arch.data.model", "");

		if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
			return Integer.parseInt(bitness, 10);
		}

		// bitness from sun.arch.data.model cannot be used. Try the IBM specification.
		bitness = System.getProperty("com.ibm.vm.bitmode", "");

		if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
			return Integer.parseInt(bitness, 10);
		}

		// as a last resort, try to determine the bitness from the architecture.
		final String arch = determineCurrentArchitecture();
		return guessBitnessFromArchitecture(arch);
	}

	public static int guessBitnessFromArchitecture(final String arch) {
		if (arch.contains("64")) {
			return 64;
		}

		/*
		 * This guess might eventually lead to a UnsatisfiedLinkError.
		 * But that would be more helpful than any other error.
		 */
		return 32;
	}

	public static String determineCurrentArchitecture() {
		return System.getProperty("os.arch");
	}

	/**
	 * This method normalizes and returns the path fragment for the OS, architecture and bitness combination.
	 *
	 * <p>This method will also normalize mappings like i386 to x86 or amd64 for old java versions.</p>
	 *
	 * @param osInfo the osInfo object.
	 */
	public static String getPathForOsInfo(final OsInfo osInfo) {
		final String architecturePath = normalizeArchitecturePath(osInfo);

		final String basePath = String.format("%1$s-%2$s-%3$d",
				osInfo.getOsFamily(),
				architecturePath,
				osInfo.getBitness());

		final String special = osInfo.getSpecial();
		if (special.isEmpty()) {
			return basePath;
		}

		return basePath + "-" + special;
	}

	/**
	 * Normalize the architecture fragment.
	 *
	 * <p>Background: See https://bugs.openjdk.java.net/browse/JDK-6495159. The architecture output may be wrong.</p>
	 *
	 * @param osInfo the osInfo object.
	 * @return the correct (normalized) architecture fragment.
	 */
	public static String normalizeArchitecturePath(final OsInfo osInfo) {
		final String architecture = osInfo.getArchitecture();

		return normalizeArchitecture(architecture);
	}

	public static String normalizeArchitecture(final String architecture) {
		if (ALIASES_X86_32.contains(architecture)) {
			return ARCH_X86_32;
		}

		if (ALIASES_X86_64.contains(architecture)) {
			return ARCH_X86_64;
		}

		if ("ia64n".equals(architecture)) {
			return ITANIUM_32;
		}

		if (ALIASES_ITA64.contains(architecture)) {
			return ARCH_IA64;
		}

		if (ALIASES_SPARC_32.contains(architecture)) {
			return ARCH_SPARC32;
		}

		if (ALIASES_SPARC_64.contains(architecture)) {
			return ARCH_SPARC64;
		}

		if ("aarch64".equals(architecture)) {
			return ARCH_AARCH64;
		}

		if (asList("arm", "arm32").contains(architecture)) {
			return ARCH_ARM32;
		}

		if (ALIASES_PPC32.contains(architecture)) {
			return ARCH_PPC32;
		}

		if (ALIASES_PPC64.contains(architecture)) {
			return ARCH_PPC64;
		}

		if (ALIASES_PPCLE32.contains(architecture)) {
			return ARCH_PPCLE32;
		}

		if (ALIASES_PPCLE64.contains(architecture)) {
			return ARCH_PPCLE64;
		}

		return architecture;
	}

}
