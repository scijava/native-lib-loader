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

package org.scijava.nativelib;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NativeLoaderTest {

	@Rule
	public TemporaryFolder tmpTestDir = new TemporaryFolder();

	// Creates a temporary jar with a dummy lib in it for testing extractiong
	private void createJar() throws Exception {
		// create a jar file...
		File dummyJar = tmpTestDir.newFile("dummy.jar");
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		JarOutputStream target = new JarOutputStream(new FileOutputStream(dummyJar), manifest);

		// with a dummy binary in it
		File source = new File(String.format("natives/%s/%s",
				NativeLibraryUtil.getArchitecture().name().toLowerCase(),
				NativeLibraryUtil.getPlatformLibraryName("dummy")));
		JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
		entry.setTime(System.currentTimeMillis());
		target.putNextEntry(entry);

		// fill the file...
		byte[] buffer = "native-lib-loader".getBytes();
		target.write(buffer, 0, buffer.length);
		target.closeEntry();
		target.close();

		// and add to classpath as if it is a dependency of the project
		Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
		addURLMethod.setAccessible(true);
		addURLMethod.invoke(ClassLoader.getSystemClassLoader(), new Object[]{ dummyJar.toURI().toURL() });
	}

	@Test(expected = IOException.class)
	public void exampleHowToUse() throws Exception {
		NativeLoader.loadLibrary("mylib");
		// expect IOException, because this lib does not exist
	}

	@Test
	public void testExtracting() throws Exception {
		// NB: one may want to find a way to remove the used (deleted) jars from
		// classpath. Otherwise, ClassLoader.getResource will not discover the new
		// jar if there is another test.
		createJar();
		// see if dummy is correctly extracted
		JniExtractor jniExtractor = new DefaultJniExtractor(null);
		String libPath = String.format("natives/%s",
				NativeLibraryUtil.getArchitecture().name().toLowerCase());
		File extracted = jniExtractor.extractJni(libPath + "", "dummy");

		FileInputStream in = new FileInputStream(extracted);
		byte[] buffer = new byte[32];
		in.read(buffer, 0, buffer.length);
		in.close();
		assertTrue(new String(buffer).trim().equals("native-lib-loader"));
	}
}
