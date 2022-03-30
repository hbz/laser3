package de.uni_freiburg.ub;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

class Ipv4Address extends IpAddress {

	// >--- 03-2022 ---> migrated from java source

	protected static final short MAX_CIDR_SUFFIX = 32;
	protected long bits;
	protected short max_cidr_suffix;

	/**
	 * Default constructor
	 */
	Ipv4Address() {
	}

	/**
	 * Constructor to instantiate an IPv4 address by the number of bits
	 * @param ipAddress the bit count of the IP address
	 * @throws InvalidIpAddressException if the bit number is not between 0 and 2^32
	 */
	Ipv4Address(long ipAddress) throws InvalidIpAddressException {
		this.max_cidr_suffix = 32;
		if (0 <= ipAddress && ipAddress <= 4294967295l) {
			this.bits = ipAddress;
		} else {
			throw new InvalidIpAddressException();
		}
	}

	/**
	 * Parses the given IPv4 address string; an exception is thrown if it is not in the format (X(XX).X(XX).X(XX).X(XX)) where 0 < X < 255
	 * @param str the input string
	 * @return the parsed address object
	 * @throws InvalidIpAddressException if there are not exactly four blocks in the address
	 * @throws NumberFormatException if the number in each block is not between 0 and 255
	 */
	static IpAddress parseIpAddress(String str) throws InvalidIpAddressException, NumberFormatException {

		String[] blocks = str.split("\\.");

		if (blocks.length != 4) {
			throw new InvalidIpAddressException();
		}

		Integer result = 0;
		for (int idx = 0; idx <= 3; idx++) {
			Integer block = Integer.parseInt(blocks[idx]);
			if (0 <= block && block <= 255) {
				result |= Integer.parseInt(blocks[idx]) << ((3 - idx) * 8);
			} else {
				throw new NumberFormatException();
			}
		}

		return new Ipv4Address(Integer.toUnsignedLong(result));
	}

	/**
	 * Converts the bit representation into four blocks of numbers between 0 and 255 (the known format of IPv4 addresses)
	 * @return the IPv4 address string
	 */
	String toString() {
		final short[] shorts = new short[4];
		for (int i = 0; i < 4; i++) {
			shorts[i] = (short) (((bits << i * 8) >>> 8 * (3)) & 0xFF);
		}

		final String[] strings = new String[shorts.length];
		for (int i = 0; i < shorts.length; i++) {
			strings[i] = String.valueOf(shorts[i]);
		}

		return String.join(".", strings);
	}

	/**
	 * Outputs the address bits as hexadecimal string
	 * @return the hexadecimal string represenation of the address
	 */
	String toHexString() {
		return String.format("%08x", bits);
	}

	/**
	 * Outputs the bit representation of the address
	 * @return the address as long
	 */
	long longValue() {
		return this.bits;
	};

	/**
	 * Checks whether this address is greater than the given IPv4 address
	 * @param ipAddr the address to check against
	 * @return is this address greater than the given one?
	 */
	boolean isGreater(IpAddress ipAddr) {
		return this.longValue() > ((Ipv4Address) ipAddr).longValue();
	}

	/**
	 * Checks whether this address is greater than or equal the given IPv4 address
	 * @param ipAddr the address to check against
	 * @return is this address greater than or equal the given one?
	 */
	boolean isGreaterEqual(IpAddress ipAddr) {
		return this.longValue() >= ((Ipv4Address) ipAddr).longValue();
	}

	/**
	 * Checks whether this address is less than the given IPv4 address
	 * @param ipAddr the address to check against
	 * @return is this address less than the given one?
	 */
	boolean isLesser(IpAddress ipAddr) {
		return this.longValue() < ((Ipv4Address) ipAddr).longValue();
	}

	/**
	 * Checks whether this address is less than or equal the given IPv4 address
	 * @param ipAddr the address to check against
	 * @return is this address less than or equal the given one?
	 */
	boolean isLesserEqual(IpAddress ipAddr) {
		return this.longValue() <= ((Ipv4Address) ipAddr).longValue();
	}

	/**
	 * Retrieves the following address
	 * @return the next address
	 */
	Ipv4Address next() {
		return new Ipv4Address(bits + 1);
	}

	/**
	 * Retrieves the previous address
	 * @return the previous address
	 */
	Ipv4Address prev() {
		return new Ipv4Address(bits - 1);
	}

	/**
	 * irrelevant for IPv4; does the same as {@link #longValue()}
	 * @return the bit value of the address
	 */
	long highBits() {
		return bits;
	}

	/**
	 * irrelevant for IPv4; does the same as {@link #longValue()}
	 * @return the bit value of the address
	 */
	long lowBits() {
		return bits;
	}

	/**
	 * Gets the upper limit of the range within which this address is located
	 * @param cidrSuffix the CIDR suffix to determine the range to match against
	 * @return the upper limit with the given CIDR suffix
	 */
	IpAddress getUpperLimit(int cidrSuffix) {
		long lowBlockUpper = (bits | (~(-1l << MAX_CIDR_SUFFIX - cidrSuffix)));

		return new Ipv4Address(lowBlockUpper);
	}

	/**
	 * Gets the lower limit of the range within which this address is located
	 * @param cidrSuffix the CIDR suffix to determine the range to match against
	 * @return the lower limit with the given CIDR suffix
	 */
	IpAddress getLowerLimit(int cidrSuffix) {
		long lowBlockLower = (bits & (-1l << MAX_CIDR_SUFFIX - cidrSuffix));

		return new Ipv4Address(lowBlockLower);
	}

	/**
	 * Parses the given CIDR suffix string
	 * @param s the CIDR suffix string to parse
	 * @return the parsed CIDR suffix
	 * @throws NumberFormatException if the suffix value is not between 0 and 32 (MAX_CIDR_SUFFIX)
	 */
	short parseCidrSuffix(String s) {
		short cidrSuffix = Short.parseShort(s);
		if (cidrSuffix < 0 || cidrSuffix > MAX_CIDR_SUFFIX) {
			throw new NumberFormatException();
		}
		return cidrSuffix;
	}

	/**
	 * Compares this address to the given IPv4 address
	 * @param o the address to compare against
	 * @return the comparison result (-1, 0, 1)
	 */
	@Override
	int compareTo(IpAddress o) {
		Ipv4Address ipv4Addr = (Ipv4Address) o;
		return Long.signum(bits - ipv4Addr.bits);
	}
}
