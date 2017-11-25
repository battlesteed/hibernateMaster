package steed.hibernatemaster.sample;

import org.junit.Test;

import steed.util.base.BaseUtil;
import steed.util.logging.LoggerFactory;

public class TestLog {
	@Test
	public void testLog(){
		LoggerFactory.getLogger().debug("dddddd");
	}
}
