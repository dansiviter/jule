![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dansiviter/juli/Java%20CI?style=flat-square) [![Known Vulnerabilities](https://snyk.io/test/github/dansiviter/juli/badge.svg?style=flat-square)](https://snyk.io/test/github/dansiviter/juli) ![Sonar Coverage](https://img.shields.io/sonar/coverage/dansiviter_juli?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)


# Java Util Logging Improver (JULI) #

Although rarely the preferred framework `java.util.logging` (JUL) is embedded into Java so aids minimal applications but is not the easiest to use. This library lightly wraps it to make life a little easier:
* Speed up development by simplifying logging,
* Improve type safety to reduce errors,
* Simplify unit testing, especially with CDI,
* Improve memory and CPU performance by:
  * Deferring array initialisation and auto-boxing until actually needed,
  * Async `java.util.logging.Handler` implementations,
* Automatic unwrapping of `java.util.function.Supplier` and `java.util.Optional` parameters.

> :information_source: [JBoss Logging Tools](https://github.com/jboss-logging/jboss-logging-tools) was the inspiration but designed to be even simpler, leaner and not dependent on JBoss Log Manager.

First import dependencies:

```xml
<dependency>
  <groupId>uk.dansiviter.juli</groupId>
  <artifactId>core</artifactId>
  <version>${juli.version}</version>
</dependency>
<dependency>
  <groupId>uk.dansiviter.juli</groupId>
  <artifactId>processor</artifactId>
  <version>${juli.version}</version>
  <scope>provided</scope> <!-- only needed during compilation -->
  <optional>true</optional>
</dependency>
```

Define a logger interface:
```java
package com.foo;
...

@Log
public interface MyLog {
  @Message("Hello {0}")
  void hello(String name);

  @Message(value = "Oh no! {0}", level = Level.ERROR)
  void error(String name, Throwable t);  // <- Throwables must be last parameter

  @Message("Hello {0}")
  void unwrap(Supplier<String> name);  // <- value will be unwrapped on the calling thread

  @Message(value = "Number {0}", Level.DEBUG)
  void number(int value);  // <- primitives only auto-boxed if the log level is consumed

}
```

This will generate a class `com.foo.MyLog$log` which actually does the logging.

To get an instance use `uk.dansiviter.juli.LogProducer`:
```java
public class MyClass {
  private final static MyLog LOG = LogProducer.log(MyLog.class);

  public void myMethod() {
    LOG.hello("foo");
  }
}
```

> :information_source: The log levels are reduced to just `ERROR`, `WARN`, `INFO`, `DEBUG` and `TRACE` as, frankly, that's all you really need.

## CDI ##

This can perform automatic injection of dependencies via CDI:

```xml
<dependency>
  <groupId>uk.dansiviter.juli</groupId>
  <artifactId>cdi</artifactId>
  <version>${juli.version}</version>
</dependency>
```

```java
@ApplicationScoped
public class MyClass {
  @Inject
  private MyLog log;

  public void myMethod() {
    log.hello("foo");
  }
}
```

This will inject a `@Dependent` scoped instance of the logger to prevent proxying.


## Asynchronous Handlers ##

JUL handlers are all synchronous which puts IO directly within the path of execution; this is a bottleneck. To address this, this library has a very simple `uk.dansiviter.juli.AsyncHandler` implementation that uses `java.util.concurrent.Flow` to asynchronously process log events. Using this can dramatically improve the performance of 'chatty' logging at the expense of a little memory and CPU. There is only one concrete implementation as the moment which is `uk.dansiviter.juli.AsyncConsoleHandler` which can be used as a direct replacement for `java.util.logging.ConsoleHandler`.

> :warning: If the buffer saturates, then the much of the performance benefits of the asynchronous handler can be lost. However, it _should_ still outperform a synchronous implementation.

> :information_source: `com.lmax:disruptor` has been trialed as an alternative to using Java 9 `Flow` but its bulk (~90KB) and no significant out-of-the-box performance improvement is has been discounted ([#6](../../issues/6)).
