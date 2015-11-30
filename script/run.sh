#!/usr/bin/env bash
sbt package
scalac -Xplugin:target/typed-annotation-plugin-0.1.0-SNAPSHOT.jar src/test/scala/com/github/tkqubo/typed_annotation/TypedAnnotationPluginTest.scala src/main/java/com/github/tkqubo/typed_annotation/TypedAnnotation.java
