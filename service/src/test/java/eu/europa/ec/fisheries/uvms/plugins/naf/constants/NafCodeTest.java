package eu.europa.ec.fisheries.uvms.plugins.naf.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NafCodeTest {

	@Test
	public void testMatchesNormalCase() {
		String message = "SOMETEXT//FS/SWE//OTHERTEXT";
		assertTrue(NafCode.FLAG.matches(message));
		assertEquals("SWE", NafCode.FLAG.getValue(message));
	}
	
	@Test
	public void testMatchesEmptyValue() {
		String message = "SOMETEXT//FS///OTHERTEXT";
		assertFalse(NafCode.FLAG.matches(message));
	}
	
	@Test
	public void testMatchesPreviousDataIsEmpty() {
		String message = "SOMETEXT//RC///FS/SWE//OTHERTEXT";
		assertTrue(NafCode.FLAG.matches(message));
		assertEquals("SWE", NafCode.FLAG.getValue(message));
	}
}
