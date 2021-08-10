[![](https://img.shields.io/maven-central/v/org.scijava/native-lib-loader.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.scijava%22%20AND%20a%3A%22native-lib-loader%22)
[![](https://github.com/scijava/native-lib-loader/actions/workflows/build-main.yml/badge.svg)](https://github.com/scijava/native-lib-loader/actions/workflows/build-main.yml)

# About native library loader

The native library loader is a utility that assists with loading native
libraries from Java. It provides the ability to painlessly identify, extract
and load the correct platform-specific native library from a JAR file.


##### License

Simplified BSD License


# Usage

### Add dependency

Search Maven Central for [latest version](http://search.maven.org/#search|ga|1|a:native-lib-loader)
and add a dependency to your pom.xml.

```xml
<dependency>
    <groupId>org.scijava</groupId>
    <artifactId>native-lib-loader</artifactId>
    <version>x.y.z</version>
</dependency>
```

### Package native libraries

Native libraries should be packaged into a single jar file, with the
following directory & file structure:

```
/natives
  /linux_32
     libxxx[-vvv].so
  /linux_64
     libxxx[-vvv].so
  /osx_32
     libxxx[-vvv].dylib
  /osx_64
     libxxx[-vvv].dylib
  /osx_arm64
     libxxx[-vvv].dylib
  /windows_32
     xxx[-vvv].dll
  /windows_64
     xxx[-vvv].dll
  /windows_arm64
     xxx[-vvv].dll
  /aix_32
     libxxx[-vvv].so
     libxxx[-vvv].a
  /aix_64
     libxxx[-vvv].so
     libxxx[-vvv].a
```

Here "xxx" is the name of the native library and "-vvv" is an optional version number.
Depending on the platform at runtime, a native library will be unpacked into a temporary file
and will be loaded from there.

The version information will be grabbed from the MANIFEST.mf file
from "Implementation-Version" entry. So it's recommended to follow Java's
[package version information](https://docs.oracle.com/javase/tutorial/deployment/jar/packageman.html)
convention. 

### Load library

If you want to load 'awesome.dll' (on Windows) or 'libawesome.so' (on Linux or AIX),
simply do like this ...

```Java
NativeLoader.loadLibrary("awesome");
```
