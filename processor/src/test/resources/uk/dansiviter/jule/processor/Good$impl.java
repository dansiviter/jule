package uk.dansiviter.jule.processor;

import java.lang.Override;
import java.lang.String;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.annotation.processing.Generated;
import uk.dansiviter.jule.BaseJulLog;
import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;

@Generated(
    value = "uk.dansiviter.jule.processor.LogProcessor",
    comments = "https://jule.dansiviter.uk/"
)
public final class Good$impl implements BaseJulLog, Good {
  private static final AtomicBoolean ONCE__foo = new AtomicBoolean();

  private final Log log;

  public final String key;

  private final Logger delegate;

  public Good$impl(String name, String key) {
    this.log = Good.class.getAnnotation(Log.class);
    this.key = key;
    this.delegate = delegate(name);
  }

  /**
   * @returns the delegate logger.
   */
  @Override
  public final Logger delegate() {
    return this.delegate;
  }

  /**
   * @returns the annotation instance.
   */
  @Override
  public final Log log() {
    return this.log;
  }

  @Override
  public void foo(String world) {
    if (!isLoggable(Message.Level.INFO)) {
      return;
    }
    if (ONCE__foo.getAndSet(true)) {
      return;
    }
    logp(Message.Level.INFO, "hello %s", world);
  }
}
