package uk.dansiviter.logging;

public interface Emitter<T> extends AutoCloseable {
  void init(AsyncHandler handler);

  int submit(T item);

  @Override
  void close();
}
