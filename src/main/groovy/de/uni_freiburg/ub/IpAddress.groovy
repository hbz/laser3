package de.uni_freiburg.ub;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

/**
 * Abstract class to represent an IP{v4/v6} address
 * @see Ipv4Address
 * @see Ipv6Address
 */
class IpAddress implements Comparable<IpAddress>{

	// >--- 03-2022 ---> migrated from java source

	protected short max_cidr_suffix;
	
	final IpAddress ipAddress;

	/**
	 * Default constructor to initialise class
	 */
	IpAddress() {
		ipAddress = null;
	}

	@Deprecated
	static IpAddress parseIpAddress(long bits) throws InvalidIpAddressException {
		return new Ipv4Address(bits);
	}

	@Deprecated
	static IpAddress parseIpAddress(long highBits, long lowBits) {
		return new Ipv6Address(highBits, lowBits);
	}

	/**
	 * @param str the input to parse
	 * @return depending on the address syntax, either a new {@link Ipv4Address} or a new {@link Ipv6Address} instance
	 * @throws InvalidIpAddressException
	 */
	static IpAddress parseIpAddress(String str) throws InvalidIpAddressException {
		// if str contains only dots's we have a possible ipv4 address
		boolean isPossibleIpv4Addr = str.contains(".") & !str.contains(":");

		// if str contains a colons's we have a possible ipv6 address
		boolean isPossilbeIpv6Addr = str.contains(":") & !str.contains(".");

		// if str contains dots AND colons we have a possible ipv4-mapped or
		// ipv4-compatible ipv6 address
		boolean isPossilbeEmbeddedIpv4Addr = str.contains(":") & str.contains(".");

		if (isPossibleIpv4Addr) {
			return Ipv4Address.parseIpAddress(str);
		}

		if (isPossilbeEmbeddedIpv4Addr) {
			return new Ipv6Address(Long.valueOf("x0FFF"), 0l); // this can not work!? TODO check
		}

		if (isPossilbeIpv6Addr) {
			return Ipv6Address.parseIpAddress(str);
		}
		
		throw new InvalidIpAddressException();
	}

	/**
	 * Derives the upper limit from the given CIDR suffix
	 * @param cidrSuffix the CIDR suffix from which the upper limit should be derived
	 * @return the upper limit of the given address
	 */
	IpAddress getUpperLimit(int cidrSuffix) {
		return this.getUpperLimit(cidrSuffix);
	}

	/**
	 * Derives the lower limit from the given CIDR suffix
	 * @param cidrSuffix the CIDR suffix from which the lower limit should be derived
	 * @return the lower limit of the given address
	 */
	IpAddress getLowerLimit(int cidrSuffix) {
		return this.getLowerLimit(cidrSuffix);
	}

	/**
	 * Returns the string representation of the given address
	 * @return the IP address string
	 */
	String toString() {
		return ipAddress.toString();
	}

	/**
	 * Checks if the this address instance is higher than the given one
	 * @param ipAddr the other instance to compare with
	 * @return true if this instance is greater than the other one
	 * @throws InvalidIpAddressException
	 */
	boolean isGreater(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isGreater(ipAddr);
	}

	/**
	 * Checks if the this address instance is higher or equal than the given one
	 * @param ipAddr the other instance to compare with
	 * @return true if this instance is greater than or equal the other one
	 * @throws InvalidIpAddressException
	 */
	boolean isGreaterEqual(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isGreaterEqual(ipAddr);
	}

	/**
	 * Checks if the this address instance is lower than the given one
	 * @param ipAddr the other instance to compare with
	 * @return true if this instance is lesser than the other one
	 * @throws InvalidIpAddressException
	 */
	boolean isLesser(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isLesser(ipAddr);
	}

	/**
	 * Checks if the this address instance is lower or equal than the given one
	 * @param ipAddr the other instance to compare with
	 * @return true if this instance is lesser than the other one
	 * @throws InvalidIpAddressException
	 */
	boolean isLesserEqual(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isLesserEqual(ipAddr);
	}

	/**
	 * Returns the following address to this instance
	 * @return the matching IP address
	 */
	IpAddress next() {
		return this.next();
	}

	/**
	 * Returns the preceding address to this instance.
	 * A dummy method; see implementing classes for implementation
	 * @return the matching IP address
	 */
	IpAddress prev() {
		return this.next();
	}

	/**
	 * Parses the given CIDR suffix string
	 * @param s the CIDR suffix string to parse
	 * @return the parsed CIDR suffix
	 */
	short parseCidrSuffix(String s) {
		return parseCidrSuffix(s);
	}

	/**
	 * Outputs the address bits as hexadecimal string
	 * @return the hexadecimal string represenation of the address
	 */
	String toHexString() {
		return this.toHexString();
	}

	/**
	 * Compares this address to the given IP address
	 * @param o the address to compare against
	 * @return the comparison result (-1, 0, 1)
	 */
	@Override
	int compareTo(IpAddress o) {
		return this.compareTo(o);
	}
}

