package org.scijava.nativelib;

import java.util.Locale;

public class OsInfo {

	private final String osFamily;
	private final String architecture;
	private final int bitness;
	private final String special;

	public OsInfo(final String osFamily, final String architecture, final int bitness) {
		this.osFamily = osFamily.toLowerCase(Locale.ENGLISH);
		this.architecture = architecture.toLowerCase(Locale.ENGLISH);
		this.bitness = bitness;
		this.special = "";
	}

	public OsInfo(final String osFamily, final String architecture, final int bitness, final String special) {
		this.osFamily = osFamily.toLowerCase(Locale.ENGLISH);
		this.architecture = architecture.toLowerCase(Locale.ENGLISH);
		this.bitness = bitness;
		this.special = special;
	}

	public String getArchitecture() {
		return this.architecture;
	}

	public int getBitness() {
		return this.bitness;
	}

	public String getSpecial() {
		return this.special;
	}

	public String getOsFamily() {
		return this.osFamily;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		final OsInfo osInfo = (OsInfo) other;

		if (this.bitness != osInfo.bitness) {
			return false;
		}
		if (this.osFamily != null ? !this.osFamily.equals(osInfo.osFamily) : osInfo.osFamily != null) {
			return false;
		}
		if (this.architecture != null ? !this.architecture.equals(osInfo.architecture) : osInfo.architecture != null) {
			return false;
		}

		return this.special != null ? this.special.equals(osInfo.special) : osInfo.special == null;

	}

	@Override
	public int hashCode() {
		int result = this.osFamily != null ? this.osFamily.hashCode() : 0;
		result = 31 * result + (this.architecture != null ? this.architecture.hashCode() : 0);
		result = 31 * result + this.bitness;
		result = 31 * result + (this.special != null ? this.special.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "OsInfo{" +
				", osFamily='" + this.osFamily + '\'' +
				", architecture='" + this.architecture + '\'' +
				", bitness=" + this.bitness +
				", special='" + this.special + '\'' +
				'}';
	}
}
