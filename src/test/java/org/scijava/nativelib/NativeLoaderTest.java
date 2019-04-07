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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeLoaderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(NativeLoaderTest.class);

	@Test(expected = IOException.class)
	public void exampleHowToUse() throws Exception {
		NativeLoader.loadLibrary("mylib");
		// expect IOException, because this lib does not exist
	}

	@Test
	public void testExtracting() throws Exception {
		final OsInfo osInfo = DefaultOsInfo.LINUX_X86_64.getOsInfo();
		final String pathForOsInfo = NativeOsArchUtil.getPathForOsInfo(osInfo);

		// see if dummy is correctly extracted
		final JniExtractor jniExtractor = new DefaultJniExtractor(null);
		final String libPath = String.format("natives/%s", pathForOsInfo);
		final File extracted = jniExtractor.extractJni(libPath + "", "dummy");
		LOGGER.debug("File to extracted jar: [{}].", extracted.getAbsolutePath());

		FileInputStream in = null;
		try {
			in = new FileInputStream(extracted);
			final byte[] buffer = new byte[32];
			in.read(buffer, 0, buffer.length);
			assertTrue(new String(buffer).trim().equals("native-lib-loader"));
		} finally {
			if (in != null) { in.close(); }
		}
	}
}
