enablePlugins(ScalaNativePlugin)
scalaVersion := "2.11.12"
nativeGC := "immix"
nativeLinkingOptions ++= Seq("-static-libstdc++", "-L/lib/")
libraryDependencies += "com.softwaremill.sttp" % "core_native0.3_2.11" % "1.5.0"
libraryDependencies += "io.argonaut" % "argonaut_native0.3_2.11" % "6.2.2"

resolvers += Resolver.sonatypeRepo("releases")
