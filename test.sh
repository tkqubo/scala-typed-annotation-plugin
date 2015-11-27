sbt package
#scalac -Xplugin:target/typed-annotation-0.1.0-SNAPSHOT.jar src/main/scala/com/github/tkqubo/typed_annotation/TypedAnnotationPlugin.scala src/main/java/com/github/tkqubo/typed_annotation/Typed.java
scalac -Xplugin:target/typed-annotation-0.1.0-SNAPSHOT.jar src/test/scala/com/github/tkqubo/typed_annotation/TypedAnnotationPluginTest.scala src/main/java/com/github/tkqubo/typed_annotation/Typed.java
