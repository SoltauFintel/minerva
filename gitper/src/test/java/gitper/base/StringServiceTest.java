package gitper.base;

import org.junit.Assert;
import org.junit.Test;

public class StringServiceTest {

	@Test
	public void compareVersions() {
		Assert.assertTrue(StringService.compareVersions("4.01.1", "4.01.1"));
		Assert.assertFalse(StringService.compareVersions("4.01.1", "4.01.2"));
		Assert.assertTrue(StringService.compareVersions("4.01", "4.01.0"));
		Assert.assertTrue(StringService.compareVersions("4.01.0", "4.01"));
		Assert.assertTrue(StringService.compareVersions("3.37.14", "3.37.14.0"));
		Assert.assertTrue(StringService.compareVersions("4.01.6.0", "4.01.6"));
		Assert.assertFalse(StringService.compareVersions("4.01.6.00", "4.01.6"));
		Assert.assertFalse(StringService.compareVersions("4.01.6.0", "4.01.6.1"));
		Assert.assertFalse(StringService.compareVersions("4.01.6.0", "4.01.7"));
	}
}
