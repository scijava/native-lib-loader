//
// DefaultJniExtractor.java
//

/*
Native library loader for extracting and loading native libraries from Java.

Copyright (c) 2010 - 2014, Board of Regents of the University of
Wisconsin-Madison and Glencoe Software, Inc.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are
those of the authors and should not be interpreted as representing official
policies, either expressed or implied, of any organization.
*/

// This code is derived from Richard van der Hoff's mx-native-loader project:
// http://opensource.mxtelecom.com/maven/repo/com/wapmx/native/mx-native-loader/1.7/
// See NOTICE.txt for details.

// Copyright 2009 MX Telecom Ltd

package org.scijava.nativelib;

import java.io.File;
import java.io.IOException;

/**
 * JniExtractor suitable for single application deployments per virtual machine
 * <p>
 * WARNING: This extractor can result in UnsatisifiedLinkError if it is used in more than one classloader.
 * @author Richard van der Hoff <richardv@mxtelecom.com>
 */
public class DefaultJniExtractor extends BaseJniExtractor {
    
    /**
     * this is where native dependencies are extracted to (e.g. tmplib/).
     */
    private File nativeDir;

    public DefaultJniExtractor() throws IOException {
        super(null);
        init("tmplib");
    }

    public DefaultJniExtractor(Class libraryJarClass, String tmplib) throws IOException {
        super(libraryJarClass);
        init(tmplib);
    }
    
    void init(String tmplib) throws IOException {
        nativeDir = new File(System.getProperty("java.library.tmpdir", tmplib));
        // Order of operations is such that we do not error if we are racing with another thread to create the directory.
        nativeDir.mkdirs();
        if (!nativeDir.isDirectory()) {
            throw new IOException("Unable to create native library working directory " + nativeDir);
        }
    }
    
    @Override
    public File getJniDir() {
        return nativeDir;
    }

    @Override
    public File getNativeDir() {
        return nativeDir;
    }

}
