# Logger Wrap #

Log4J? SLF4J? Commons Logger? Flogger? How about another?!

Although rarely the preferred framework `java.util.Logger` is embedded into Java so aids minimal applications but is not the easiest to use. This library thinly wraps it to make life a little easier:
* Type safe logging,
* Simpler unit testing, especially with CDI,
* Defer array initialisation and auto-boxing until actually needed,
* Automatic unwrapping of `java.util.function.Supplier` and `java.util.Optional` parameters.

> :information_source: [JBoss Logging Tools](https://github.com/jboss-logging/jboss-logging-tools) was the inspiration but designed to be even simpler, leaner and not dependent on JBoss Log Manager.

First import dependencies:

```xml
<dependency>
  <groupId>uk.dansiviter.logging</groupId>
  <artifactId>core</artifactId>
  <version>{dsLogger.version}</version>
</dependency>
<dependency>
  <groupId>uk.dansiviter.logging</groupId>
  <artifactId>processor</artifactId>
  <version>{dsLogger.version}</version>
  <scope>provided</scope> <!-- only needed during compilation -->
  <optional>true</optional>
</dependency>
```

Then defined a logger `interface`:
```java
package com.foo;
...

@Log
public interface MyLog {
  @Message("Hello {0}")
  void hello(String name);

  @Message(value = "Oh no! {0}", level = Level.ERROR)
  void error(String name, Throwable t);  // <- Throwables must be last parameter
}
```

This will generate a class `com.foo.MyLog$log` which actually does the logging.

To get an instance use `uk.dansiviter.logging.LogProducer`:
```java
public class MyClass {
  private final static MyLog LOG = LogProducer.log(MyLog.class);

  public void myMethod() {
    LOG.hello("foo");
  }
}
```

## CDI ##

This can perform automatic injection of dependencies via CDI:

```xml
<dependency>
  <groupId>uk.dansiviter.logging</groupId>
  <artifactId>cdi</artifactId>
  <version>{dsLogger.version}</version>
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
