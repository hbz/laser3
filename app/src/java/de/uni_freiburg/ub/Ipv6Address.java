package de.uni_freiburg.ub;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

public class Ipv6Address extends IpAddress {

	protected static final short MAX_CIDR_SUFFIX = 128;
	
	protected long highBits;
	protected long lowBits;
	
	public Ipv6Address(long highBits, long lowBits) {
		this.highBits = highBits;
		this.lowBits = lowBits;
	}

	public static IpAddress parseIpAddress(String s) {

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

	private String[] toArrayOfZeroPaddedstrings() {
		final short[] shorts = toShortArray();
		final String[] strings = new String[shorts.length];
		for (int i = 0; i < shorts.length; i++) {
			strings[i] = String.format("%04x", shorts[i]);
		}
		return strings;
	}

	public String toString() {
		return String.join(":", toArrayOfZeroPaddedstrings());
	}
	
	public String toHexString() {
		return String.format("%016x", highBits) + String.format("%016x", lowBits);
	}

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
	
	public boolean isLesser(IpAddress ipAddr) {
		return ! ipAddr.isGreaterEqual(this);
	}
	
	public boolean isLesserEqual(IpAddress ipAddr) {
		return ! ipAddr.isGreater(this);
	}
	

	public long highBits() {
		return highBits;
	}

	public long lowBits() {
		return lowBits;
	}

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
	
	public short parseCidrSuffix(String s) {
		short cidrSuffix = Short.parseShort(s);
		if (cidrSuffix < 0 || cidrSuffix > MAX_CIDR_SUFFIX) {
			throw new NumberFormatException();
		}
		return cidrSuffix;
	}
	
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
