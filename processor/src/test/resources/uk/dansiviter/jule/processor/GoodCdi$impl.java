package uk.dansiviter.jule.processor;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import java.lang.Override;
import java.lang.String;
import java.util.logging.Logger;
import javax.annotation.processing.Generated;
import uk.dansiviter.jule.BaseJulLog;
import uk.dansiviter.jule.annotations.Log;

@Generated(
    value = "uk.dansiviter.jule.processor.LogProcessor",
    comments = "https://jule.dansiviter.uk",
    date = "2023-02-01T01:02:03.000004Z"
)
@Dependent
public final class GoodCdi$impl implements BaseJulLog, GoodCdi {
  private final Log log;

  public final String key;

  private final Logger delegate;

  public GoodCdi$impl(String name, String key) {
    this.log = GoodCdi.class.getAnnotation(Log.class);
    this.key = key;
    this.delegate = delegate(name);
  }

  @Inject
  public GoodCdi$impl(InjectionPoint ip) {
    this(ip.getMember().getDeclaringClass().getName(), "GoodCdi$impl");
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
}
