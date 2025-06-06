---
sidebar_label: Compliance Tests
title: Compliance Tests
---

The Smithy prelude has support for compliance testing for services that use `HTTP` as their protocol. It is built on top of regular traits, you can read more about it [over here](https://awslabs.github.io/smithy/2.0/additional-specs/http-protocol-compliance-tests.html).

Basically, you annotate operations and/or structures (that depends on the type of test being defined) and protocol implementors can generate tests cases to ensure their implementation behaves correctly.

Smithy4s publishes a module that you can use to write compliance test if you're implementing a protocol. Add the following to your dependencies if you use `sbt`:

```scala
"com.disneystreaming.smithy4s" %% "smithy4s-compliance-tests" % smithy4sVersion.value
```

If you use `mill`:

```scala
ivy"com.disneystreaming.smithy4s::smithy4s-compliance-tests:${smithy4sVersion()}",
```

The rest of this document will demonstrate how to write a Smithy specification that specify HTTP compliance tests, and then how to use the module mentioned above to run the tests.

_Note: currently, only `httprequesttests-trait` are supported. other traits support will be integrated soon._

## Example specification

```kotlin
$version: "2"

namespace smithy4s.hello

use smithy4s.api#simpleRestJson
use smithy.test#httpRequestTests

@simpleRestJson
service HelloWorldService {
  version: "1.0.0",
  operations: [Hello]
}
@httpRequestTests([
    {
        id: "hello-success"
        protocol: simpleRestJson
        method: "POST"
        uri: "/World"
        params: { name: "World" }
    },
    {
        id: "hello-fails"
        protocol: simpleRestJson
        method: "POST"
        uri: "/fail"
        params: { name: "World" }
    }
])
@http(method: "POST", uri: "/{name}", code: 200)
operation Hello {
  input := {
    @httpLabel
    @required
    name: String
  },
  output := {
    @required
    message: String
  }
}
```

We have a very simple specification: one operation with basic input and output shapes. We've added a `httpRequestTests` to define a compliance test for protocol implementors.

## Testing the protocol

The service in the specification is annotated with the `simpleRestJson` protocol definition. We'll use the `compliance-tests` module to make sure this protocol can handle such an operation.

_Note: the following code and the `compliance-tests` module do not depend on a specific test frameworks. If you want to hook it into your test framework, it is easy to do so but it's outside the scope of this document. Refer to [this example](https://github.com/disneystreaming/smithy4s/blob/932e169b48c0df9e53601004037ef1dc888ecab0/modules/compliance-tests/test/src/smithy4s/compliancetests/WeaverComplianceTest.scala) to see how we did it for `Weaver` in this project._

First, some imports and an implicit `CompatEffect` that is required because we support both cats-effect 2 and 3.

```scala mdoc:silent
import cats.effect._
import org.http4s._
import org.http4s.client.Client
import smithy4s.compliancetests._
import smithy4s.example._
import smithy4s.http4s._
```

Then, you can create and instance of `ClientHttpComplianceTestCase` while selecting the protocol to use and the service to test:

```scala mdoc:silent
val testGenerator = new ClientHttpComplianceTestCase[
    smithy4s.api.SimpleRestJson,
    HelloWorldServiceGen,
    HelloWorldServiceOperation
  ](
    smithy4s.api.SimpleRestJson()
  ) {
    import org.http4s.implicits._
    private val baseUri = uri"http://localhost/"
    def getClient(app: HttpApp[IO]): Resource[IO, HelloWorldService[IO]] =
      SimpleRestJsonBuilder(HelloWorldServiceGen)
        .client(Client.fromHttpApp(app))
        .uri(baseUri)
        .resource
  }

val tests: List[ComplianceTest[IO]] = testGenerator.allClientTests()
```

Now, you can iterate over the test cases and do what you want. This is where you would hook in the test framework of your choice, but in the following example, we're just going to print the result:

```scala mdoc:silent
import cats.syntax.traverse._
import cats.effect.unsafe.implicits.global

val runTests: IO[List[String]] = tests
  .map { tc =>
    tc.run.map {
      case Left(value) =>
        s"Failed ${tc.name} with the following message: $value"
      case Right(_) => s"Success ${tc.name}"

    }
  }
  .sequence
```

Will produce the following when executed:

```scala mdoc:passthrough
println("```")
println(runTests.map(_.mkString("\n")).unsafeRunSync())
println("```")
```