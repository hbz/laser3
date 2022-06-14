package de.uni_freiburg.ub;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
public class Ipv6Address extends IpAddress {

	protected static final short MAX_CIDR_SUFFIX = 128;
	
	protected long highBits;
	protected long lowBits;

	/**
	 * Constructor specifying the high and low bits of the address
	 * @param highBits highest order bits
	 * @param lowBits lowest order bits
	 */
	public Ipv6Address(long highBits, long lowBits) {
		this.highBits = highBits;
		this.lowBits = lowBits;
	}

	/**
	 * Normalises the given string, retrieving its host
	 * @param s the string to normalise
	 * @return the host address of the input string
	 * @throws UnknownHostException if the host is not known
	 */
	public static String normalize(String s) throws UnknownHostException {
		return InetAddress.getByName(s).getHostAddress();
	}

	/**
	 * Parses the given string as a IP v6 address
	 * @param s the string to parse
	 * @return the parsed entity
	 */
	public static IpAddress parseIpAddress(String s) {
		try {
			s = normalize(s);
		} catch (UnknownHostException e) {
			throw new NumberFormatException();
		}
		String[] blocks = s.split(":");

		long high = 0L;
		long low = 0L;

		for (int i = 0; i < 8; i++) {
			long longValue = 0l;
			if (! blocks[i].isEmpty()) {
				longValue = Long.parseLong(blocks[i], 16);
			}
			if (longValue < 0 || longValue > 65535) {
				throw new NumberFormatException();
			}
			
			if (0 <= i && i < 4) {
				high |= (longValue << ((4 - i - 1) * 16));
			} else {
				low |= (longValue << ((4 - i - 1) * 16));
			}
		}

		return new Ipv6Address(high, low);
	}

	/**
	 * Prepares the numeric blocks of this address
	 * @return the array of shorts constituting the address
	 */
	private short[] toShortArray() {
		int N_SHORTS = 8;
		final short[] shorts = new short[N_SHORTS];

		for (int i = 0; i < N_SHORTS; i++) {
			if (0 <= i && i < 4)
				shorts[i] = (short) (((highBits << i * 16) >>> 16 * (N_SHORTS - 1)) & 0xFFFF);
			else
				shorts[i] = (short) (((lowBits << i * 16) >>> 16 * (N_SHORTS - 1)) & 0xFFFF);
		}

		return shorts;
	}

	/**
	 * Converts the blocks into an array of strings
	 * @return the array of zero padded strings of shorts
	 */
	private String[] toArrayOfZeroPaddedstrings() {
		final short[] shorts = toShortArray();
		final String[] strings = new String[shorts.length];
		for (int i = 0; i < shorts.length; i++) {
			strings[i] = String.format("%04x", shorts[i]);
		}
		return strings;
	}

	/**
	 * Outputs the address as string, the array of shorts joined by colon
	 * @return the address string
	 */
	public String toString() {
		return String.join(":", toArrayOfZeroPaddedstrings());
	}

	/**
	 * Outputs the address as hexadecimal string
	 * @return the address in hexadecimal notation
	 */
	public String toHexString() {
		return String.format("%016x", highBits) + String.format("%016x", lowBits);
	}

	/**
	 * Checks whether this address is greater than the given {@link IpAddress}
	 * @param ipAddr the address to compare against
	 * @return is the given address greater than this?
	 */
	public boolean isGreater(IpAddress ipAddr) {
		Ipv6Address ipv6Addr = (Ipv6Address) ipAddr;

		if (this.highBits == ipv6Addr.highBits) {
			return (Long.compareUnsigned(this.lowBits, ipv6Addr.lowBits) > 0);
			//return this.lowBits > ipv6Addr.lowBits;
		} else {
			//return this.highBits > ipv6Addr.highBits;
			return (Long.compareUnsigned(this.highBits, ipv6Addr.highBits) > 0);
		}
	}

	/**
	 * Checks whether this address is greater or equal than the given {@link IpAddress}
	 * @param ipAddr the address to compare against
	 * @return is the given address greater than this or equal?
	 */
	public boolean isGreaterEqual(IpAddress ipAddr) {
		Ipv6Address ipv6Addr = (Ipv6Address) ipAddr;
		if (this.highBits == ipv6Addr.highBits) {
			//return this.lowBits >= ipv6Addr.lowBits;
			return (Long.compareUnsigned(this.lowBits, ipv6Addr.lowBits) >= 0);
		} else {
			//return this.highBits > ipv6Addr.highBits;
			return (Long.compareUnsigned(this.highBits, ipv6Addr.highBits) > 0);
		}
	}

	/**
	 * Checks whether this address is less than the given {@link IpAddress}
	 * @param ipAddr the address to compare against
	 * @return is the given address less than this?
	 */
	public boolean isLesser(IpAddress ipAddr) {
		return ! ipAddr.isGreaterEqual(this);
	}

	/**
	 * Checks whether this address is less or equal than the given {@link IpAddress}
	 * @param ipAddr the address to compare against
	 * @return is the given address less than this or equal?
	 */
	public boolean isLesserEqual(IpAddress ipAddr) {
		return ! ipAddr.isGreater(this);
	}

	/**
	 * Returns the high bits of the address
	 * @return the highest 64 bits
	 */
	public long highBits() {
		return highBits;
	}

	/**
	 * Returns the low bits of the address
	 * @return the loest 64 bits
	 */
	public long lowBits() {
		return lowBits;
	}

	/**
	 * Gets the upper limit of the address with the given CIDR suffix
	 * @param cidrSuffix the CIDR suffix
	 * @return the upper limit of the corresponding range
	 */
	public IpAddress getUpperLimit(int cidrSuffix) {
		long lowBitsUpper = -1l;
		long highBitsUpper = highBits;

		long bitmask = -1l;
		if (cidrSuffix != 64 && cidrSuffix != 0) {
			bitmask = (~(-1l << 64 - cidrSuffix));
		} 
		
		if (cidrSuffix > 63) {
			lowBitsUpper  = (lowBits  | bitmask);
		} else {
			highBitsUpper = (highBits | bitmask);
		}

		return new Ipv6Address(highBitsUpper, lowBitsUpper);
	}

	/**
	 * Gets the lower limit of the address with the given CIDR suffix
	 * @param cidrSuffix the CIDR suffix
	 * @return the lower limit of the corresponding range
	 */
	public IpAddress getLowerLimit(int cidrSuffix) {
		long lowBitsLower = 0l;
		long highBitsLower = highBits;
		
		long bitmask = 0l;
		if (cidrSuffix != 64 && cidrSuffix != 0) {
			bitmask = (-1l << 64 - cidrSuffix);
		} 
		
		if (cidrSuffix > 63) {
			lowBitsLower = (lowBits & bitmask);
		} else {
			highBitsLower = (highBits & bitmask);
		}

		return new Ipv6Address(highBitsLower, lowBitsLower);
	}

	/**
	 * Parses the given CIDR suffix string
	 * @param s the suffix string to parse
	 * @return the parsed CIDR suffix
	 * @throws NumberFormatException if the short is not between 0 and MAX_CIDR_SUFFIX
	 */
	public short parseCidrSuffix(String s) {
		short cidrSuffix = Short.parseShort(s);
		if (cidrSuffix < 0 || cidrSuffix > MAX_CIDR_SUFFIX) {
			throw new NumberFormatException();
		}
		return cidrSuffix;
	}

	/**
	 * Retrieves the following IP address
	 * @return the next IP address
	 */
	public Ipv6Address next() {
		if (highBits == -1l && lowBits == -1l) {
			throw new InvalidIpAddressException();
		} 
		
		if (lowBits == -1l) {
			return new Ipv6Address(highBits+1, lowBits+1);
		} else {
			return new Ipv6Address(highBits, lowBits+1);
		}
	}

	/**
	 * Retrieves the previous IP address
	 * @return the previous IP address
	 */
	public Ipv6Address prev() {
		if (lowBits == 0l & highBits == 0l) {
			throw new InvalidIpAddressException();
		}
		
		if (lowBits == 0l) {
			return new Ipv6Address(highBits-1, lowBits-1);
		} else {
			return new Ipv6Address(highBits, lowBits-1);
		}
	}

	/**
	 * Compares this address to the given IPv6 address
	 * @param o the address to compare against
	 * @return the comparison result (-1, 0, 1)
	 */
	@Override
	public int compareTo(IpAddress o) {
		Ipv6Address ipv6Addr = (Ipv6Address) o;
		
		if (highBits == ipv6Addr.highBits) {
			return Long.signum(lowBits - ipv6Addr.lowBits);
		} else {
			return Long.signum(highBits - ipv6Addr.highBits);
		} 
	}
}
