package uk.dansiviter.juli.processor;

import com.oracle.svm.core.annotate.AutomaticFeature;
import java.lang.Override;
import java.lang.String;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.annotation.processing.Generated;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import uk.dansiviter.juli.BaseLog;
import uk.dansiviter.juli.LogProducer;
import uk.dansiviter.juli.annotations.Log;
import uk.dansiviter.juli.annotations.Message;

@Generated(
    value = "uk.dansiviter.juli.processor.LogProcessor",
    comments = "https://juli.dansiviter.uk/"
)
public final class Good$impl implements BaseLog, Good {
  private static final AtomicBoolean ONCE__foo = new AtomicBoolean();

  private final Log log;

  private final Logger delegate;

  public final String key;

  public Good$impl(String name) {
    this.log = Good.class.getAnnotation(Log.class);
    this.key = LogProducer.key(Good.class, name);
    this.delegate = delegate(name);
  }

  /**
   * @returns the annotation instance.
   */
  @Override
  public final Log log() {
    return this.log;
  }

  /**
   * @returns the delegate logger.
   */
  @Override
  public final Logger delegate() {
    return this.delegate;
  }

  @Override
  public void foo(String world) {
    if (!isLoggable(Message.Level.INFO)) {
      return;
    }
    if (ONCE__foo.getAndSet(true)) {
      return;
    }
    logp(Message.Level.INFO, "hello {0}", world);
  }

  @AutomaticFeature
  public static final class GraalFeature implements Feature {
    @Override
    public final void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
      var clazz = access.findClassByName("uk.dansiviter.juli.processor.Good$impl");
      RuntimeReflection.register(clazz);
      RuntimeReflection.register(clazz.getDeclaredConstructors());
      RuntimeReflection.register(clazz.getDeclaredFields());
      RuntimeReflection.register(clazz.getDeclaredMethods());
    }
  }
}
