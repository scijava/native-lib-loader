package org.scijava.nativelib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.*;

public enum DefaultOsInfoPathMapping {
	INSTANCE;

	private final Map<OsInfo, List<String>> mapping;

	DefaultOsInfoPathMapping() {
		this.mapping = createDefaultMapping();
	}

	private static Map<OsInfo, List<String>> createDefaultMapping() {
		final Map<OsInfo, List<String>> defaultMapping = new ConcurrentHashMap<OsInfo, List<String>>();

		for (final DefaultOsInfo defaultOsInfoValue : DefaultOsInfo.values()) {
			final OsInfo osInfo = defaultOsInfoValue.getOsInfo();
			/* Determine default path by convention. */
			final String pathForOsInfo = NativeOsArchUtil.getPathForOsInfo(osInfo);
			/* Get legacy pathes for compatiblity. */
			final List<String> additionalPathes = defaultOsInfoValue.getLegacyPaths();
			final List<String> allPathes = new ArrayList<String>(additionalPathes.size() + 1);
			allPathes.add(pathForOsInfo);
			allPathes.addAll(additionalPathes);

			defaultMapping.put(osInfo, unmodifiableList(allPathes));
		}

		return Collections.unmodifiableMap(defaultMapping);
	}

	public Map<OsInfo, List<String>> getMapping() {
		return this.mapping;
	}
}
