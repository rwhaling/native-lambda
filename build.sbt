enablePlugins(ScalaNativePlugin)
scalaVersion := "2.11.12"
nativeGC := "immix"
// nativeCompileOptions ++= Seq("-I/opt/llvm-3.9.0/bin/../lib64/clang/3.9.0/include")
nativeLinkingOptions ++= Seq("-static-libstdc++")
// libraryDependencies += "io.rwhaling" %%% "re2s" % "0.1-SNAPSHOT"
libraryDependencies += "com.softwaremill.sttp" % "core_native0.3_2.11" % "1.5.0"
libraryDependencies += "io.argonaut" % "argonaut_native0.3_2.11" % "6.2.2"

resolvers += Resolver.sonatypeRepo("releases")
