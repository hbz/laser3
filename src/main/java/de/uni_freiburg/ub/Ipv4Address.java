package de.uni_freiburg.ub;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

public class Ipv4Address extends IpAddress {

	protected static final short MAX_CIDR_SUFFIX = 32;
	protected long bits;
	protected short max_cidr_suffix;

	public Ipv4Address() {
	}

	public Ipv4Address(long ipAddress) throws InvalidIpAddressException {
		this.max_cidr_suffix = 32;
		if (0 <= ipAddress && ipAddress <= 4294967295l) {
			this.bits = ipAddress;
		} else {
			throw new InvalidIpAddressException();
		}
	}

	public static IpAddress parseIpAddress(String str) throws InvalidIpAddressException, NumberFormatException {

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

	public String toString() {
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

	public String toHexString() {
		return String.format("%08x", bits);
	}
	
	public long longValue() {
		return this.bits;
	};

	public boolean isGreater(IpAddress ipAddr) {
		return this.longValue() > ((Ipv4Address) ipAddr).longValue();
	}

	public boolean isGreaterEqual(IpAddress ipAddr) {
		return this.longValue() >= ((Ipv4Address) ipAddr).longValue();
	}

	public boolean isLesser(IpAddress ipAddr) {
		return this.longValue() < ((Ipv4Address) ipAddr).longValue();
	}

	public boolean isLesserEqual(IpAddress ipAddr) {
		return this.longValue() <= ((Ipv4Address) ipAddr).longValue();
	}

	public Ipv4Address next() {
		return new Ipv4Address(bits + 1);
	}
	
	public Ipv4Address prev() {
		return new Ipv4Address(bits - 1);
	}

	public long highBits() {
		return bits;
	}

	public long lowBits() {
		return bits;
	}

	public IpAddress getUpperLimit(int cidrSuffix) {
		long lowBlockUpper = (bits | (~(-1l << MAX_CIDR_SUFFIX - cidrSuffix)));

		return new Ipv4Address(lowBlockUpper);
	}

	public IpAddress getLowerLimit(int cidrSuffix) {
		long lowBlockLower = (bits & (-1l << MAX_CIDR_SUFFIX - cidrSuffix));

		return new Ipv4Address(lowBlockLower);
	}

	public short parseCidrSuffix(String s) {
		short cidrSuffix = Short.parseShort(s);
		if (cidrSuffix < 0 || cidrSuffix > MAX_CIDR_SUFFIX) {
			throw new NumberFormatException();
		}
		return cidrSuffix;
	}
	
	@Override
	public int compareTo(IpAddress o) {
		Ipv4Address ipv4Addr = (Ipv4Address) o;
		return Long.signum(bits - ipv4Addr.bits);
	}
}
