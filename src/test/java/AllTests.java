package test.java;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HPQCJAPITest.class, JUnitPosterTest.class, JUnitReaderTest.class })
public class AllTests {

}
