package uk.dansiviter.jule.processor;

import uk.dansiviter.jule.annotations.Logger;
import uk.dansiviter.jule.annotations.Message;

@Logger
interface Bad {
	@Message("")
  void empty();

  void notAnnotated();
}
