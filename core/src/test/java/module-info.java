open module uk.dansiviter.juli {
	requires transitive java.logging;

	requires org.junit.jupiter;
	requires org.hamcrest;
	requires org.mockito;
	requires org.mockito.junit.jupiter;
	requires net.bytebuddy;

	exports uk.dansiviter.juli;
	exports uk.dansiviter.juli.annotations;
}
