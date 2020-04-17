package de.uni_freiburg.ub;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddress implements Comparable<IpAddress>{
	
	protected short max_cidr_suffix;
	
	public final IpAddress ipAddress;
	
	public IpAddress() {
		ipAddress = null;
	}
	
	public static IpAddress parseIpAddress(long bits) throws InvalidIpAddressException {
		return new Ipv4Address(bits);
	}
	
	public static IpAddress parseIpAddress(long highBits, long lowBits) {
		return new Ipv6Address(highBits, lowBits);
	}

	public static IpAddress parseIpAddress(String str) throws InvalidIpAddressException {
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
	
	public IpAddress getUpperLimit(int cidrSuffix) {
		return this.getUpperLimit(cidrSuffix);
	}
	
	public IpAddress getLowerLimit(int cidrSuffix) {
		return this.getLowerLimit(cidrSuffix);
	}
	
	public String toString() {
		return ipAddress.toString();
	}

	public boolean isGreater(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isGreater(ipAddr);
	}
	
	public boolean isGreaterEqual(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isGreaterEqual(ipAddr);
	}
	
	public boolean isLesser(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isLesser(ipAddr);
	}
	
	public boolean isLesserEqual(IpAddress ipAddr) throws InvalidIpAddressException {
		if (this.getClass() != ipAddr.getClass()) {
			throw new InvalidIpAddressException();
		}
		
		return this.isLesserEqual(ipAddr);
	}
	
	public IpAddress next() {
		return this.next();
	}
	
	public IpAddress prev() {
		return this.next();
	}
	
	public short parseCidrSuffix(String s) {
		return parseCidrSuffix(s);
	}
	
	public String toHexString() {
		return this.toHexString();
	}
	
	@Override
	public int compareTo(IpAddress o) {
		return this.compareTo(o);
	}
}

