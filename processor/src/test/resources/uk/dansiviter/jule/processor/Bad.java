package uk.dansiviter.jule.processor;

import uk.dansiviter.jule.annotations.Log;
import uk.dansiviter.jule.annotations.Message;

@Log
interface Foo {
	@Message("")
  void empty();

  void notAnnotated();
}
