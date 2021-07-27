/*
 * #%L
 * Native library loader for extracting and loading native libraries from Java.
 * %%
 * Copyright (C) 2010 - 2021 Board of Regents of the University of
 * Wisconsin-Madison and Glencoe Software, Inc.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.nativelib;

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a utility for loading native libraries.
 * <p>
 * Native libraries should be packaged into a single jar file, with the
 * following directory and file structure:
 *
 * <pre>
 * natives
 *   linux_32
 *     libxxx[-vvv].so
 *   linux_64
 *     libxxx[-vvv].so
 *   linux_arm
 *     libxxx[-vvv].so
 *   linux_arm64
 *     libxxx[-vvv].so
 *   osx_32
 *     libxxx[-vvv].dylib
 *   osx_64
 *     libxxx[-vvv].dylib
 *   windows_32
 *     xxx[-vvv].dll
 *   windows_64
 *     xxx[-vvv].dll
 *   aix_32
 *     libxxx[-vvv].so
 *     libxxx[-vvv].a
 *   aix_64
 *     libxxx[-vvv].so
 *     libxxx[-vvv].a
 * </pre>
 * <p>
 * Here "xxx" is the name of the native library and "-vvv" is an optional
 * version number.
 * <p>
 * Current approach is to unpack the native library into a temporary file and
 * load from there.
 *
 * @author Aivar Grislis
 */
public final class NativeLibraryUtil {

	public static final String DELIM = "/";
	public static final String DEFAULT_SEARCH_PATH = "natives" + DELIM;

	private static final Logger LOGGER = LoggerFactory.getLogger(
		"org.scijava.nativelib.NativeLibraryUtil");

	private static class OsInfoHolder {
		private static final OsInfoHolder INSTANCE = new OsInfoHolder();
		private final OsInfo osInfo;

		OsInfoHolder() {
			this.osInfo = OsInfoFactory.fromCurrent();
		}
	}

	private NativeLibraryUtil() {
		// utility class.
	}

	/**
	 * Returns the path to the native library.
	 * 
	 * @param searchPath the path to search for &lt;platform&gt; directory.
	 * 			Pass in <code>null</code> to get default path
	 * 			(natives/&lt;platform&gt;).
	 *
	 * @return path
	 */
	public static List<String> getPlatformLibraryPath(final String searchPath, final OsInfo osInfo) {
		final DefaultOsInfoPathMapping instance = DefaultOsInfoPathMapping.INSTANCE;
		final Map<OsInfo, List<String>> mapping = instance.getMapping();
		// TODO: Allow users to add mappings from a config (.properties) file or from a parameter.
		final List<String> archPaths = mapping.get(osInfo);

		if (null == searchPath) {
			return unmodifiableList(archPaths);
		}

		String fixedSearchPath = searchPath;
		if (!searchPath.endsWith(DELIM)) {
			fixedSearchPath = searchPath + DELIM;
		}

		final List<String> searchPaths = new ArrayList<String>();
		for (final String archPath : archPaths) {
			searchPaths.add(fixedSearchPath + archPath + DELIM);
		}

		return unmodifiableList(searchPaths);
	}

	/**
	 * Returns the Maven-versioned file name of the native library. In order for
	 * this to work Maven needs to save its version number in the jar manifest.
	 * The version of the library-containing jar and the version encoded in the
	 * native library names should agree.
	 *
	 * <pre>
	 * {@code
	 * <build>
	 *   <plugins>
	 *     <plugin>
	 *       <artifactId>maven-jar-plugin</artifactId>
	 *         <inherited>true</inherited> *
	 *         <configuration>
	 *            <archive>
	 *              <manifest>
	 *                <packageName>com.example.package</packageName>
	 *                <addDefaultImplementationEntries>true</addDefaultImplementationEntries> *
	 *              </manifest>
	 *           </archive>
	 *         </configuration>
	 *     </plugin>
	 *   </plugins>
	 * </build>
	 *
	 * * = necessary to save version information in manifest
	 * }
	 * </pre>
	 *
	 * @param libraryJarClass any class within the library-containing jar
	 * @param libName name of library
	 * @return The Maven-versioned file name of the native library.
	 */
	public static String getVersionedLibraryName(final Class<?> libraryJarClass,
		String libName)
	{
		final String version =
			libraryJarClass.getPackage().getImplementationVersion();
		if (null != version && version.length() > 0) {
			libName += "-" + version;
		}
		return libName;
	}

	/**
	 * Loads the native library. Picks up the version number to specify from the
	 * library-containing jar.
	 *
	 * @param libraryJarClass any class within the library-containing jar
	 * @param libName name of library
	 * @return whether or not successful
	 */
	public static boolean loadVersionedNativeLibrary(
		final Class<?> libraryJarClass, String libName)
	{
		// append version information to native library name
		libName = getVersionedLibraryName(libraryJarClass, libName);

		return loadNativeLibrary(libraryJarClass, libName);
	}

	/**
	 * Loads the native library.
	 *
	 * @param jniExtractor the extractor to use
	 * @param libName name of library
	 * @param searchPaths a list of additional paths to search for the library
	 * @return whether or not successful
	 */
	public static boolean loadNativeLibrary(final JniExtractor jniExtractor,
		final String libName, final String... searchPaths)
	{
		try {
			final List<String> libPaths = searchPaths == null ?
					new LinkedList<String>() :
					new LinkedList<String>(Arrays.asList(searchPaths));
			libPaths.add(0, NativeLibraryUtil.DEFAULT_SEARCH_PATH);
			// for backward compatibility
			libPaths.add(1, "");
			libPaths.add(2, "META-INF" + NativeLibraryUtil.DELIM + "lib");
			// NB: Although the documented behavior of this method is to load
			// native library from META-INF/lib/, what it actually does is
			// to load from the root dir. See: https://github.com/scijava/
			// native-lib-loader/blob/6c303443cf81bf913b1732d42c74544f61aef5d1/
			// src/main/java/org/scijava/nativelib/NativeLoader.java#L126

			// search in each path in {natives/, /, META-INF/lib/, ...}
			for (final String libPath : libPaths) {
				final List<String> archPathes = NativeLibraryUtil.getPlatformLibraryPath(libPath, OsInfoHolder.INSTANCE.osInfo);
				for (final String archPath : archPathes) {
					final File extracted = jniExtractor.extractJni(
							archPath,
							libName);
					if (extracted != null) {
						System.load(extracted.getAbsolutePath());
						return true;
					}
				}
			}
		} catch (final UnsatisfiedLinkError e) {
			LOGGER.debug("Problem with library", e);
		} catch (final IOException e) {
			LOGGER.debug("Problem with extracting the library", e);
		}
		return false;
	}

	/**
	 * Loads the native library.
	 *
	 * @param libraryJarClass any class within the library-containing jar
	 * @param libName name of library
	 * @return whether or not successful
	 */
	@Deprecated
	public static boolean loadNativeLibrary(final Class<?> libraryJarClass,
		final String libName)
	{
		try {
			return NativeLibraryUtil.loadNativeLibrary(new DefaultJniExtractor(libraryJarClass), libName);
		}
		catch (final IOException e) {
			LOGGER.debug("IOException creating DefaultJniExtractor", e);
		}
		return false;
	}
}
