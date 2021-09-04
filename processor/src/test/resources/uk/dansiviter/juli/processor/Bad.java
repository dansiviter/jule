package uk.dansiviter.juli.processor;

import uk.dansiviter.juli.annotations.Log;
import uk.dansiviter.juli.annotations.Message;

@Log
interface Foo {
	@Message("")
  void empty();

  void notAnnotated();
}
