/*
 * #%L
 * Native library loader for extracting and loading native libraries from Java.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

// This code is derived from Richard van der Hoff's mx-native-loader project:
// http://opensource.mxtelecom.com/maven/repo/com/wapmx/native/mx-native-loader/1.7/
// See NOTICE.txt for details.

// Copyright 2006 MX Telecom Ltd

package org.scijava.nativelib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Richard van der Hoff (richardv@mxtelecom.com)
 */
public abstract class BaseJniExtractor implements JniExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		"org.scijava.nativelib.BaseJniExtractor");
	protected static final String JAVA_TMPDIR = "java.io.tmpdir";
	protected static final String ALTR_TMPDIR = "./tmplib";
	protected static final String TMP_PREFIX = "nativelib-loader_";
	private static final String LEFTOVER_MIN_AGE = "org.scijava.nativelib.leftoverMinAgeMs";
	private static final long LEFTOVER_MIN_AGE_DEFAULT = 5 * 60 * 1000; // 5 minutes

	private Class<?> libraryJarClass;

	/**
	 * We use a resource path of the form META-INF/lib/${mx.sysinfo}/ This way
	 * native builds for multiple architectures can be packaged together without
	 * interfering with each other And by setting mx.sysinfo the jvm can pick the
	 * native libraries appropriate for itself.
	 */
	private String[] nativeResourcePaths;

	public BaseJniExtractor() throws IOException {
		init(null);
	}

	public BaseJniExtractor(final Class<?> libraryJarClass) throws IOException {
		init(libraryJarClass);
	}

	private void init(final Class<?> libraryJarClass) throws IOException {
		this.libraryJarClass = libraryJarClass;

		final String mxSysInfo = MxSysInfo.getMxSysInfo();

		if (mxSysInfo != null) {
			nativeResourcePaths =
				new String[] { "META-INF/lib/" + mxSysInfo + "/", "META-INF/lib/" };
		}
		else {
			nativeResourcePaths = new String[] { "META-INF/lib/" };
		}
		// clean up leftover libraries from previous runs
		deleteLeftoverFiles();
	}

	private static boolean deleteRecursively(final File directory) {
		if (directory == null) return true;
		final File[] list = directory.listFiles();
		if (list == null) return true;
		for (final File file : list) {
			if (file.isFile()) {
				if (!file.delete()) return false;
			}
			else if (file.isDirectory()) {
				if (!deleteRecursively(file)) return false;
			}
		}
		return directory.delete();
	}

	protected static File getTempDir() throws IOException {
		// creates a temporary directory for hosting extracted files
		// If system tempdir is not available, use tmplib
		File tmpDir = new File(System.getProperty(JAVA_TMPDIR, ALTR_TMPDIR));
		if (!tmpDir.isDirectory()) {
			tmpDir.mkdirs();
			if (!tmpDir.isDirectory())
				throw new IOException("Unable to create temporary directory " + tmpDir);
		}
		return Files.createTempDirectory(tmpDir.toPath(), TMP_PREFIX).toFile();
	}

	/**
	 * this is where native dependencies are extracted to (e.g. tmplib/).
	 * 
	 * @return native working dir
	 */
	public abstract File getNativeDir();

	/**
	 * this is where JNI libraries are extracted to (e.g.
	 * tmplib/classloaderName.1234567890000.0/).
	 * 
	 * @return jni working dir
	 */
	public abstract File getJniDir();

	/** {@inheritDoc} */
	public File extractJni(final String libPath, final String libname)
		throws IOException
	{
		String mappedlibName = System.mapLibraryName(libname);
		LOGGER.debug("mappedLib is " + mappedlibName);
		/*
		 * On Darwin, the default mapping is to .jnilib; but we use .dylibs so that library interdependencies are
		 * handled correctly. if we don't find a .jnilib, try .dylib instead.
		 */
		URL lib = null;

		// if no class specified look for resources in the jar of this class
		if (null == libraryJarClass) {
			libraryJarClass = this.getClass();
		}

		// foolproof
		String combinedPath = (libPath.endsWith("/") ? libPath : libPath + "/") + mappedlibName;
		lib = libraryJarClass.getClassLoader().getResource(combinedPath);
		if (null == lib) {
			/*
			 * On OS X, the default mapping changed from .jnilib to .dylib as of JDK 7, so
			 * we need to be prepared for the actual library and mapLibraryName disagreeing
			 * in either direction. 
			 */
			final String altLibName;
			if (mappedlibName.endsWith(".jnilib")) {
				altLibName =
					mappedlibName.substring(0, mappedlibName.length() - 7) + ".dylib";
			}
			else if (mappedlibName.endsWith(".dylib")) {
				altLibName =
					mappedlibName.substring(0, mappedlibName.length() - 6) + ".jnilib";
			}
			else {
				altLibName = null;
			}
			if (altLibName != null) {
				lib = getClass().getClassLoader().getResource(libPath + altLibName);
				if (lib != null) {
					mappedlibName = altLibName;
				}
			}
		}

		if (null != lib) {
			LOGGER.debug("URL is " + lib.toString());
			LOGGER.debug("URL path is " + lib.getPath());
			return extractResource(getJniDir(), lib, mappedlibName);
		}
		LOGGER.info("Couldn't find resource " + combinedPath);
		throw new IOException("Couldn't find resource " + combinedPath);
	}

	/** {@inheritDoc} */
	public void extractRegistered() throws IOException {
		LOGGER.debug("Extracting libraries registered in classloader " +
			this.getClass().getClassLoader());
		for (final String nativeResourcePath : nativeResourcePaths) {
			final Enumeration<URL> resources =
				this.getClass().getClassLoader().getResources(
					nativeResourcePath + "AUTOEXTRACT.LIST");
			while (resources.hasMoreElements()) {
				final URL res = resources.nextElement();
				extractLibrariesFromResource(res);
			}
		}
	}

	private void extractLibrariesFromResource(final URL resource)
		throws IOException
	{
		LOGGER.debug("Extracting libraries listed in " + resource);
		BufferedReader reader = null;
		try {
			reader =
				new BufferedReader(
					new InputStreamReader(resource.openStream(), "UTF-8"));
			for (String line; (line = reader.readLine()) != null;) {
				URL lib = null;
				for (final String nativeResourcePath : nativeResourcePaths) {
					lib =
						this.getClass().getClassLoader().getResource(
							nativeResourcePath + line);
					if (lib != null) break;
				}
				if (lib != null) {
					extractResource(getNativeDir(), lib, line);
				}
				else {
					throw new IOException("Couldn't find native library " + line +
						"on the classpath");
				}
			}
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * Extract a resource to the tmp dir (this entry point is used for unit
	 * testing)
	 * 
	 * @param dir the directory to extract the resource to
	 * @param resource the resource on the classpath
	 * @param outputName the filename to copy to (within the tmp dir)
	 * @return the extracted file
	 * @throws IOException
	 */
	File extractResource(final File dir, final URL resource,
		final String outputName) throws IOException
	{

		final InputStream in = resource.openStream();
		// TODO there's also a getResourceAsStream

		// make a lib file with exactly the same lib name
		final File outfile = new File(getJniDir(), outputName);
		LOGGER.debug("Extracting '" + resource + "' to '" +
			outfile.getAbsolutePath() + "'");

		// copy resource stream to temporary file
		final FileOutputStream out = new FileOutputStream(outfile);
		copy(in, out);
		out.close();
		in.close();

		// note that this doesn't always work:
		outfile.deleteOnExit();

		return outfile;
	}

	/**
	 * Looks in the temporary directory for leftover versions of temporary shared
	 * libraries.
	 * <p>
	 * If a temporary shared library is in use by another instance it won't
	 * delete.
	 * <p>
	 * An old library will be deleted only if its last modified date is at least
	 * LEFTOVER_MIN_AGE milliseconds old (default to 5 minutes)
	 * This was introduced to avoid a possible race condition when two instances (JVMs) run the same unpacking code
	 * and one of which manage to delete the extracted file of the other before the other gets a chance to load it
	 * <p>
	 * Another issue is that createTempFile only guarantees to use the first three
	 * characters of the prefix, so I could delete a similarly-named temporary
	 * shared library if I haven't loaded it yet.
	 */
	void deleteLeftoverFiles() {
		final File tmpDirectory = new File(System.getProperty(JAVA_TMPDIR, ALTR_TMPDIR));
		final File[] folders = tmpDirectory.listFiles(new FilenameFilter() {

			public boolean accept(final File dir, final String name) {
				return name.startsWith(TMP_PREFIX);
			}
		});
		if (folders == null) return;
		long leftoverMinAge = getLeftoverMinAge();
		for (final File folder : folders) {
			// attempt to delete
			long age = System.currentTimeMillis() - folder.lastModified();
			if (age < leftoverMinAge) {
				LOGGER.debug("Not deleting leftover folder {}: is {}ms old", folder, age);
				continue;
			}
			LOGGER.debug("Deleting leftover folder: {}", folder);
			deleteRecursively(folder);
		}
	}

	long getLeftoverMinAge() {
		try {
			return Long.parseLong(System.getProperty(LEFTOVER_MIN_AGE, String.valueOf(LEFTOVER_MIN_AGE_DEFAULT)));
		} catch (NumberFormatException e) {
			LOGGER.error("Cannot load leftover minimal age system property", e);
			return LEFTOVER_MIN_AGE_DEFAULT;
		}
	}
	/**
	 * copy an InputStream to an OutputStream.
	 * 
	 * @param in InputStream to copy from
	 * @param out OutputStream to copy to
	 * @throws IOException if there's an error
	 */
	static void copy(final InputStream in, final OutputStream out)
		throws IOException
	{
		final byte[] tmp = new byte[8192];
		int len = 0;
		while (true) {
			len = in.read(tmp);
			if (len <= 0) {
				break;
			}
			out.write(tmp, 0, len);
		}
	}
}
