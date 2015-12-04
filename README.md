# Scala Type-Safe Annotation Plugin
[![License: MIT](http://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Circle CI](https://img.shields.io/circleci/project/tkqubo/scala-typed-annotation-plugin/master.svg)](https://circleci.com/gh/tkqubo/scala-typed-annotation-plugin)
[![Coverage Status](https://coveralls.io/repos/tkqubo/scala-typed-annotation-plugin/badge.svg?branch=master&service=github)](https://coveralls.io/github/tkqubo/scala-typed-annotation-plugin?branch=master)

Your annotations now become type-safe

# Usage

1. Annotate your annotation with `TypedAnnotation`, providing the type you want to endorse on the target annotation.

```
@TypedAnnotation(classOf[Long])
class max(val value: Long) extends StaticAnnotation
```

2. Then you can get the compilation error when you put that annotation on a member variable with a wrong type

```scala
object Test {
  @max(255)
  var value: String = null // compilation error, because Long and String doesn't match
}
```

3. That's it!


