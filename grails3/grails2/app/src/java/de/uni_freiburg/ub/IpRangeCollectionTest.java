package de.uni_freiburg.ub;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
//import org.junit.jupiter.api.Test;

public class IpRangeCollectionTest {

	@Test
	public void testAdd() throws Exception {
		IpRangeCollection ipRangeCollection = new IpRangeCollection();

		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.128-132.230.25.255"));
		assertArrayEquals(new String[] {"132.230.25.128/25"}, ipRangeCollection.toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.128-132.230.25.255"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.25.128/25"}, ipRangeCollection.toCidrStrings().toArray());
		
		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88ff/128"));
		assertArrayEquals(new String[] {"4001:4860:4860:0000:0000:0000:0000:88ff/128"}, ipRangeCollection.toCidrStrings().toArray());
		
		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88fe/127"));
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88ff/128"));
		assertArrayEquals(new String[] {"4001:4860:4860:0000:0000:0000:0000:88fe/127", "4001:4860:4860:0000:0000:0000:0000:88ff/128"}, ipRangeCollection.toCidrStrings().toArray());

	}

	@Test
	public void testCompact() throws Exception {
		IpRangeCollection ipRangeCollection = new IpRangeCollection();
		
		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88ff/128"));
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88fe/127"));
		assertArrayEquals(new String[] {"4001:4860:4860:0000:0000:0000:0000:88fe/127"}, ipRangeCollection.compact().toCidrStrings().toArray());
		
		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88ff/128"));
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:0:88fe/127"));
		ipRangeCollection.add(IpRange.parseIpRange("4001:4860:4860:0:0:0:7777:88fe/128"));
		assertArrayEquals(new String[] {"4001:4860:4860:0000:0000:0000:0000:88fe/127", "4001:4860:4860:0000:0000:0000:7777:88fe/128"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.128-132.230.25.255"));
		assertArrayEquals(new String[] {"132.230.25.0/24"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.0-132.230.26.127"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.26.0/25"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.0-132.230.26.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.*"));
		assertArrayEquals(new String[] {"132.230.25.0/24", "132.230.26.0/25"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.0-132.230.26.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.127"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.26.0/25", "132.230.30.0/25"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.128-132.230.26.255"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.127"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.26.128/25", "132.230.30.0/25"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.128-132.230.26.255"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.127"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.26.128/25", "132.230.30.0/25"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.128-132.230.26.255"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.128"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.26.128/25", "132.230.30.0/25", "132.230.30.128/32"}, ipRangeCollection.compact().toCidrStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.128-132.230.26.255"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.130"));
		assertArrayEquals(new String[] {"132.230.25.0/25", "132.230.26.128/25", "132.230.30.0/25", "132.230.30.128/31", "132.230.30.130/32"}, ipRangeCollection.compact().toCidrStrings().toArray());
	}

	@Test
	public void testToRangeString() throws Exception {
		IpRangeCollection ipRangeCollection = new IpRangeCollection();
		
		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		assertArrayEquals(new String[] {"132.230.25.0-132.230.25.127"}, ipRangeCollection.compact().toRangeStrings().toArray());
		
		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.26.128-132.230.26.255"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.130"));
		assertArrayEquals(new String[] {"132.230.25.0-132.230.25.127", "132.230.26.128-132.230.26.255", "132.230.30.0-132.230.30.130"}, ipRangeCollection.compact().toRangeStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.128-132.230.25.255"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.130"));
		assertArrayEquals(new String[] {"132.230.25.0-132.230.25.255", "132.230.30.0-132.230.30.130"}, ipRangeCollection.compact().toRangeStrings().toArray());

		ipRangeCollection = new IpRangeCollection();
		ipRangeCollection.add(IpRange.parseIpRange("132.230.30.0-132.230.30.130"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.0-132.230.25.127"));
		ipRangeCollection.add(IpRange.parseIpRange("132.230.25.128-132.230.25.255"));
		assertArrayEquals(new String[] {"132.230.25.0-132.230.25.255", "132.230.30.0-132.230.30.130"}, ipRangeCollection.compact().toRangeStrings().toArray());
	}

}
