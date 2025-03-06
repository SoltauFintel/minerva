package minerva.mask;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class FeatureFieldsHtmlTest {

	@Test
	public void none() {
		String text = "";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(0, vt.size());
	}

	@Test
	public void whitespace() {
		String text = " ";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(0, vt.size());
	}

	@Test
	public void one() {
		String text = "A / B";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(1, vt.size());
		Assert.assertEquals("A / B", vt.get(0));
	}

	@Test
	public void two() {
		String text = "reports / Test 1, reports / Test 2";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(2, vt.size());
		Assert.assertEquals("reports / Test 1", vt.get(0));
		Assert.assertEquals("reports / Test 2", vt.get(1));
	}

	@Test
	public void two_simpleComma_simpleSlash() {
		String text = "reports / Test 1,2, reports / Test 3/4";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(2, vt.size());
		Assert.assertEquals("reports / Test 1,2", vt.get(0));
		Assert.assertEquals("reports / Test 3/4", vt.get(1));
	}
	
	// Wenn ein ", " innerhalb einer Testfall-Bezeichnung enthalten ist, muss clever getrennt werden.
	// Ab hier kommen die speziellen Testfälle.

	@Test
	public void two_1COMMA() {
		String text = "reports / Test 1, 20, reports / Test 3";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(2, vt.size());
		Assert.assertEquals("reports / Test 1, 20", vt.get(0));
		Assert.assertEquals("reports / Test 3", vt.get(1));
	}

	@Test
	public void two_2COMMA() {
		String text = "reports / Test 1, 2, 4, reports / Test 3";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(2, vt.size());
		Assert.assertEquals("reports / Test 1, 2, 4", vt.get(0));
		Assert.assertEquals("reports / Test 3", vt.get(1));
	}

	@Test
	public void two_2COMMA_viceversa() {
		String text = "reports / Test 3, reports / Test 1, 2, 4";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(2, vt.size());
		Assert.assertEquals("reports / Test 3", vt.get(0));
		Assert.assertEquals("reports / Test 1, 2, 4", vt.get(1));
	}

	@Test
	public void two_emptyCOMMA() {
		String text = "reports / Test 1, , reports / Test 2";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(2, vt.size());
		Assert.assertEquals("reports / Test 1, ", vt.get(0));
		Assert.assertEquals("reports / Test 2", vt.get(1));
	}

	@Test
	public void three_2COMMA() {
		String text = "reports / Test 1, 2, 4, reports / Test 3, reports / Test 6, 2";
		List<String> vt = FeatureFieldsHtml.split(text);

		Assert.assertEquals(3, vt.size());
		Assert.assertEquals("reports / Test 1, 2, 4", vt.get(0));
		Assert.assertEquals("reports / Test 3", vt.get(1));
		Assert.assertEquals("reports / Test 6, 2", vt.get(2));
	}
}
