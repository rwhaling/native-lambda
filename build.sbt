enablePlugins(ScalaNativePlugin)
scalaVersion := "2.11.12"
nativeGC := "immix"
// nativeCompileOptions ++= Seq("-I/opt/llvm-3.9.0/bin/../lib64/clang/3.9.0/include")
nativeLinkingOptions ++= Seq("-static-libstdc++")
// libraryDependencies += "io.rwhaling" %%% "re2s" % "0.1-SNAPSHOT"
// libraryDependencies += "com.softwarmill.sttp" %%% "core" % "1.5.1"