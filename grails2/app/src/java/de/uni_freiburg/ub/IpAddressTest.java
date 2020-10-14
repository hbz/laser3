package de.uni_freiburg.ub;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Assert;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

public class IpAddressTest {

	@Test
	public void testParseIpAddress() throws Exception {

		assertEquals("2001:4860:4860:0000:0000:0000:0000:8888",
				IpAddress.parseIpAddress("2001:4860:4860::8888").toString());

		assertEquals("2a00:a200:0000:0000:0000:0000:0000:0000",
				IpAddress.parseIpAddress("2a00:a200::").toString());

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress(2229672342l),
				IpAddress.parseIpAddress("132.230.25.150")));

		assertEquals(2229672342l, ((Ipv4Address) IpAddress.parseIpAddress("132.230.25.150")).longValue());

		assertEquals(3232236042l, ((Ipv4Address) IpAddress.parseIpAddress("192.168.2.10")).longValue());
		assertEquals(3232236042l, ((Ipv4Address) IpAddress.parseIpAddress("192.168.002.10")).longValue());
		assertEquals(4294967295l, ((Ipv4Address) IpAddress.parseIpAddress("255.255.255.255")).longValue());

		assertNumberFormatExceptionIsThrown("192.168.2.510");
		assertNumberFormatExceptionIsThrown("192.168.2.a");
		assertNumberFormatExceptionIsThrown("192..2.510");

		assertInvalidIpAddressExceptionIsThrown("132.230.2.510.10");
		assertInvalidIpAddressExceptionIsThrown("132.230..510.10");
		assertInvalidIpAddressExceptionIsThrown("132.230");

		assertNumberFormatExceptionIsThrown("132.230.*.30");
		assertNumberFormatExceptionIsThrown("132.230.*.30");

		// IPv6 Tests
		assertEquals("2001:4860:4860:0000:0000:0000:0000:8888",
				new Ipv6Address(2306204062558715904l, 34952l).toString());

		Ipv6Address ipv6Addr = (Ipv6Address) IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888");
		assertEquals(2306204062558715904l, ipv6Addr.highBits());
		assertEquals(34952l, ipv6Addr.lowBits());

		ipv6Addr = (Ipv6Address) IpAddress.parseIpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
		assertEquals(2306139570357600256l, ipv6Addr.highBits());
		assertEquals(151930230829876l, ipv6Addr.lowBits());

		assertEquals("2001:4860:4860:0000:0000:0000:0000:8888",
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").toString());

		assertEquals("2001:4860:4860:0000:0000:0000:0000:8888",
				IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8888").toString());

		assertNumberFormatExceptionIsThrown("2001:4860:4860:0:0:zzz:0:8888");
		assertNumberFormatExceptionIsThrown("2001:4860:4860:0:0:*:0:8888");
		assertNumberFormatExceptionIsThrown("2001:4860:4860:0:0:333333:0:8888");
	}

	@Test
	public void testToString() throws Exception {
		assertEquals("0.0.0.222", new Ipv4Address(222l).toString());
		assertEquals("0.0.1.0", new Ipv4Address(256l).toString());
		assertEquals("132.230.25.150", new Ipv4Address(2229672342l).toString());
		assertEquals("255.255.255.255", new Ipv4Address(4294967295l).toString());

		assertInvalidIpAddressExceptionIsThrown(4294967296l);
		assertInvalidIpAddressExceptionIsThrown(-10l);
	}

	@Test
	public void testGetUpperLimit() throws Exception {

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:7fff:ffff:ffff:ffff"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(65)));
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0000:ffff:ffff:ffff:ffff"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(64)));
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0001:ffff:ffff:ffff:ffff"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(63)));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8888"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(128)));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8889"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(127)));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("132.230.25.0"),
				IpAddress.parseIpAddress("132.230.25.150").getLowerLimit(24)));
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("132.230.25.128"),
				IpAddress.parseIpAddress("132.230.25.150").getLowerLimit(25)));

		assertTrue(EqualsBuilder.reflectionEquals(new Ipv6Address(2306204062558715904l, 34952l),
				new Ipv6Address(2306204062558715904l, 34952l).getUpperLimit(128)));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:88ff"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(120)));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:88bf"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getUpperLimit(122)));

	}

	@Test
	public void testGetLowerLimit() throws Exception {
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("132.230.25.255"),
				IpAddress.parseIpAddress("132.230.25.150").getUpperLimit(24)));
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("132.230.25.255"),
				IpAddress.parseIpAddress("132.230.25.150").getUpperLimit(25)));

		assertTrue(EqualsBuilder.reflectionEquals(new Ipv6Address(2306204062558715904l, 34952l),
				new Ipv6Address(2306204062558715904l, 34952l).getLowerLimit(128)));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8800"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getLowerLimit(120)));
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8880"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").getLowerLimit(122)));
	}

	@Test
	public void testCompareTo() throws Exception {
		assertEquals(0,
				IpAddress.parseIpAddress("132.230.25.150").compareTo(IpAddress.parseIpAddress("132.230.25.150")));
		assertEquals(-1,
				IpAddress.parseIpAddress("132.230.25.100").compareTo(IpAddress.parseIpAddress("132.230.25.150")));
		assertEquals(1,
				IpAddress.parseIpAddress("132.230.25.150").compareTo(IpAddress.parseIpAddress("132.230.25.100")));

		assertEquals(0, IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888")
				.compareTo(IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888")));

		assertEquals(-1, IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8880")
				.compareTo(IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888")));

		assertEquals(1, IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888")
				.compareTo(IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8880")));

		assertEquals(-1, IpAddress.parseIpAddress("2000:4860:4860:0000:0000:0000:0000:8888")
				.compareTo(IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888")));

		assertEquals(1, IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888")
				.compareTo(IpAddress.parseIpAddress("2000:4860:4860:0000:0000:0000:0000:8888")));
	}

	@Test
	public void testNext() throws Exception {
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8889"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").next()));

		// 2001:4860:4860:0001:7fff:ffff:ffff:ffff ==
		// 2306204062558715905l,9223372036854775807l
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0001:8000:0000:0000:0000"),
				new Ipv6Address(2306204062558715905l, 9223372036854775807l).next()));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0002:0000:0000:0000:0000"),
				IpAddress.parseIpAddress("2001:4860:4860:0001:ffff:ffff:ffff:ffff").next()));
	}

	@Test
	public void testPrev() throws Exception {
		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:4860:0:0:0:0:8887"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").prev()));

		assertTrue(EqualsBuilder.reflectionEquals(IpAddress.parseIpAddress("2001:4860:485f:ffff:ffff:ffff:ffff:ffff"),
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:0000").prev()));

	}

	@Test
	public void testToHexString() throws Exception {
		assertEquals("20014860486000000000000000008888",
				IpAddress.parseIpAddress("2001:4860:4860:0000:0000:0000:0000:8888").toHexString());
		assertEquals("84e61969", IpAddress.parseIpAddress("132.230.25.105").toHexString());
		assertEquals("01000000", IpAddress.parseIpAddress("1.0.0.0").toHexString());
		assertEquals("000a0002", IpAddress.parseIpAddress("0.10.0.2").toHexString());
	}

//	workaround for the following method to work with junit4
//	assertThrows(NumberFormatException.class, () -> {
//		IpAddress.parseIpAddress("192.168.2.510");
//	});
	private void assertNumberFormatExceptionIsThrown(String ip) {
		try {
			IpAddress.parseIpAddress(ip);
			Assert.fail("NumberFormatException should be thrown");
		} catch (NumberFormatException e) {
		}
	}

//	workaround for the following method to work with junit4
//	assertThrows(InvalidIpAddressException.class, () -> {
//		IpAddress.parseIpAddress("132.230..510.10");
//	});
	private void assertInvalidIpAddressExceptionIsThrown(String ip) {
		try {
			IpAddress.parseIpAddress(ip);
			Assert.fail("InvalidIpAddressException should be thrown");
		} catch (InvalidIpAddressException e) {
		}
	}

//	workaround for the following method to work with junit4
//	assertThrows(InvalidIpAddressException.class, () -> {
//		new Ipv4Address(-10l).toString();
//	});
	private void assertInvalidIpAddressExceptionIsThrown(long ip) {
		try {
			new Ipv4Address(ip).toString();
			Assert.fail("InvalidIpAddressException should be thrown");
		} catch (InvalidIpAddressException e) {
		}
	}

}
