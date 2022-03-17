package de.uni_freiburg.ub;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import de.uni_freiburg.ub.Exception.InvalidBlockException;
import de.uni_freiburg.ub.Exception.InvalidIpAddressException;
import de.uni_freiburg.ub.Exception.InvalidRangeException;

/**
 * Represents a range of IP addresses. IPv4 and IPv6 are supported. This IP range is used for IP-based access configurations where access control is regulated with IP ranges
 * @see IpAddress
 */
public class IpRange {

	protected IpAddress upperLimit;
	protected IpAddress lowerLimit;
	protected Integer cidrSuffix = null;
	protected String inputString = "";

	/**
	 * Constructor for an IP range between the given lower and upper limit addresses
	 * @param lowerLimit the lower IP address of the range
	 * @param upperLimit the upper IP address of the range
	 * @see IpAddress
	 * @throws InvalidRangeException if the lower limit is above the upper limit
	 */
	public IpRange(IpAddress lowerLimit, IpAddress upperLimit) {
		if (lowerLimit.isGreater(upperLimit)) {
			throw new InvalidRangeException();
		}

		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	/**
	 * Constructor for an IP range between the given lower and upper limit addresses; the IP range itself is also given as raw input string
	 * @param lowerLimit the lower IP address of the range
	 * @param upperLimit the upper IP address of the range
	 * @param input the raw input string
	 * @see IpAddress
	 * @throws InvalidRangeException if the lower limit is above the upper limit
	 */
	public IpRange(IpAddress lowerLimit, IpAddress upperLimit, String input) {
		this(lowerLimit, upperLimit);
		inputString = input;
	}

	/**
	 * Constructor for an IP range between the given lower and upper limit addresses with the given CIDR suffix
	 * @param lowerLimit the lower IP address of the range
	 * @param upperLimit the upper IP address of the range
	 * @param cidrSuffix the CIDR suffix, specifying the number of addresses in the range
	 * @see IpAddress
	 * @throws InvalidRangeException if the lower limit is above the upper limit
	 */
	public IpRange(IpAddress lowerLimit, IpAddress upperLimit, int cidrSuffix) {
		this(lowerLimit, upperLimit);
		this.cidrSuffix = cidrSuffix;
	}

	/**
	 * Parses the given string to a valid IpRange
	 * @param s the input string to parse
	 * @return the parsed {@link IpRange}
	 * @throws InvalidRangeException if the string is not valid
	 */
	public static IpRange parseIpRange(String s) throws InvalidRangeException {
		// remove all whitespace characters
		s = s.replaceAll("\\s", ""); //s = StringUtils.removeAll(s, "\\s");

		// handle cidr notation
		String[] parts = s.split("/");
		if (parts.length == 2) {
			IpAddress ipAddr = IpAddress.parseIpAddress(parts[0]);
			short cidrSuffix = ipAddr.parseCidrSuffix(parts[1]);
			return new IpRange(ipAddr.getLowerLimit(cidrSuffix), ipAddr.getUpperLimit(cidrSuffix), s);
		}

		// handle formats like: 132.230.250.234 - 132.230.250.255
		String[] limits = s.split("-");
		if (limits.length == 2) {
			try {
				IpAddress lowerLimit = IpAddress.parseIpAddress(limits[0]);
				IpAddress upperLimit = IpAddress.parseIpAddress(limits[1]);
				return new IpRange(lowerLimit, upperLimit, s);
			} catch (InvalidIpAddressException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

		try {
			IpRange result = getRange(s);
			return result;
		} catch (InvalidBlockException e) {
			throw new InvalidRangeException();
		}
	}

	/**
	 * Retrieves the blocks of IP addresses in the given (partial/wildcarded) address
	 * @param ipAddr the input string
	 * @return the array of blocks of IP addresses covered by wildcards
	 * @throws InvalidBlockException if the block is improperly defined
	 * @throws InvalidRangeException if the range specified is ivalid
	 */
	protected static String[] getBlocks(String ipAddr) throws InvalidBlockException, InvalidRangeException {
		String[] blocks = ipAddr.split("\\.");
 
		String blockA = "";
		String blockB = "";
		String blockC = "";
		String blockD = "";

		switch (blocks.length) {
		case 3:
			if (ipAddr.endsWith(".*")) {
				blockA = blocks[0];
				blockB = blocks[1];
				blockC = blocks[2];
				blockD = "*";
			} else {
				throw new InvalidBlockException();
			}
			break;
		case 4:
			if (!ipAddr.endsWith(".")) {
				blockA = blocks[0];
				blockB = blocks[1];
				blockC = blocks[2];
				blockD = blocks[3];
			}
			break;
		default:
			throw new InvalidRangeException();
		}

		short value = Short.parseShort(blockA);
		if (value < 0 || 255 < value) {
			throw new NumberFormatException();
		}

		value = Short.parseShort(blockB);
		if (value < 0 || 255 < value) {
			throw new NumberFormatException();
		}

		boolean moreChecksNeeded = true;
		String[] parts = blockC.split("-");
		if (parts.length == 2) {
			short highC = Short.parseShort(parts[1]);
			short lowC = Short.parseShort(parts[0]);
			if (!(0 <= lowC && lowC < highC && highC <= 255)) {
				throw new NumberFormatException();
			}
			moreChecksNeeded = false;
		}

		if (moreChecksNeeded && blockC.equals("*")) {
			moreChecksNeeded = false;
		}

		if (moreChecksNeeded) {
			value = Short.parseShort(blockC);
			if (value < 0 || 255 < value) {
				throw new NumberFormatException();
			}
		}

		moreChecksNeeded = true;
		parts = blockD.split("-");
		if (parts.length == 2) {
			short highD = Short.parseShort(parts[1]);
			short lowD = Short.parseShort(parts[0]);
			if (!(0 <= lowD && lowD < highD && highD <= 255)) {
				throw new NumberFormatException();
			}
			moreChecksNeeded = false;
		}

		if (moreChecksNeeded && blockD.equals("*")) {
			moreChecksNeeded = false;
		}

		if (moreChecksNeeded) {
			value = Short.parseShort(blockD);
			if (value < 0 || 255 < value) {
				throw new NumberFormatException();
			}
		}

		return new String[] { blockA, blockB, blockC, blockD };
	}

	/**
	 * Itemises the given input string into an IP range
	 * @param s the input string
	 * @return the parsed range
	 * @throws InvalidBlockException if the block is improperly defined
	 * @throws InvalidRangeException if the range is incorrectly defined
	 * @see IpRange
	 */
	protected static IpRange getRange(String s) throws InvalidBlockException, InvalidRangeException {

		if (s.contains(":")) {
			IpAddress ipAddr = IpAddress.parseIpAddress(s);
			return new IpRange(ipAddr, ipAddr, s);
		}
		
		String[] blocks = getBlocks(s);
		short blockA = Short.parseShort(blocks[0]);
		short blockB = Short.parseShort(blocks[1]);
		
		// allowed formats for blockC:
		// * number between 0 and 255,
		// Examples: 213, 234, 1, 99
		// * wildcard *
		// Examples: *
		// * two numbers between 0 and 255 separated by "-", first number must be
		// smaller or equal than the second number
		// Examples: 132-232, 232-255, 1-58

		Short blockC = null;
		try {
			blockC = Short.parseShort(blocks[2]);
			if (blockC < 0 || 255 < blockC) {
				throw new NumberFormatException();
			}
		} catch (Exception e) {
		}

		Short highC = null;
		Short lowC = null;
		if (blocks[2].equals("*")) {
			highC = 255;
			lowC = 0;
		}

		try {
			String[] parts = blocks[2].split("-");
			if (parts.length == 2) {
				highC = Short.parseShort(parts[1]);
				lowC = Short.parseShort(parts[0]);
				if (!(0 <= lowC && lowC < highC && highC <= 255)) {
					throw new NumberFormatException();
				}
			}
		} catch (Exception e) {
		}

		// handle block D
		Short blockD = null;
		try {
			blockD = Short.parseShort(blocks[3]);
			if (0 < blockD || blockC > 255) {
				throw new NumberFormatException();
			}
		} catch (Exception e) {
		}

		Short highD = null;
		Short lowD = null;
		if (blocks[3].equals("*")) {
			highD = 255;
			lowD = 0;
		}

		try {
			String[] parts = blocks[3].split("-");
			if (parts.length == 2) {
				highD = Short.parseShort(parts[1]);
				lowD = Short.parseShort(parts[0]);
				if (!(0 <= lowD && lowD < highD && highD <= 255)) {
					throw new NumberFormatException();
				}
			}
		} catch (Exception e) {
		}

		String resA = String.valueOf(blockA);
		String resB = String.valueOf(blockB);
		String resHighC = "";
		String resLowC = "";
		String resHighD = "";
		String resLowD = "";

		if (blockC == null) {
			if (blocks[3].equals("*")) {
				resHighC = String.valueOf(highC);
				resLowC = String.valueOf(lowC);
				resHighD = "255";
				resLowD = "0";
			} else
				throw new InvalidBlockException();
		}

		if (blockC != null) {
			resHighC = String.valueOf(blockC);
			resLowC = String.valueOf(blockC);
			if (blockD == null) {
				resHighD = String.valueOf(highD);
				resLowD = String.valueOf(lowD);
			} else {
				resHighD = String.valueOf(blockD);
				resLowD = String.valueOf(blockD);
			}
		}

		String end = String.valueOf(resA) + "." + String.valueOf(resB) + "." + String.valueOf(resHighC) + "."
				+ String.valueOf(resHighD);

		String start = String.valueOf(resA) + "." + String.valueOf(resB) + "." + String.valueOf(resLowC) + "."
				+ String.valueOf(resLowD);

		try {
			IpAddress lower = IpAddress.parseIpAddress(start);
			IpAddress upper = IpAddress.parseIpAddress(end);
			return new IpRange(lower, upper, s);
		} catch (InvalidIpAddressException e) {
			throw new InvalidRangeException();
		}
	}

	/**
	 * Outputs the range from lower to upper limit as string
	 * @return the range as string
	 * @throws InvalidIpAddressException if one of the addresses is not correctly defined
	 */
	public String toRangeString() throws InvalidIpAddressException {
		String lower = lowerLimit.toString();
		String upper = upperLimit.toString();
		String ipRange = lower + "-" + upper;

		return ipRange;
	}

	/**
	 * Gets the lower limit of the range
	 * @return the lower {@link IpAddress}
	 * @throws InvalidIpAddressException if the address is incorrect
	 */
	public IpAddress getLowerLimit() throws InvalidIpAddressException {
		return this.lowerLimit;
	}

	/**
	 * Gets the upper limit of the range
	 * @return the upper {@link IpAddress}
	 * @throws InvalidIpAddressException if the address is incorrect
	 */
	public IpAddress getUpperLimit() throws InvalidIpAddressException {
		return this.upperLimit;
	}

	/**
	 * Outputs the range as CIDR strings
	 * @return the {@link List} of {@link IpAddress}es in CIDR representation
	 */
	public List<String> toCidr() {

		List<String> result = new LinkedList<String>();

		Map<IpAddress, IpRange> cidrRanges = getCidr(lowerLimit, this.upperLimit);

		for (IpRange cidrRange : cidrRanges.values()) {
			try {
				result.add(cidrRange.getLowerLimit().toString() + "/" + (cidrRange.cidrSuffix+1));
			} catch (InvalidIpAddressException e) {

			}
		}

		return result;
	}

	/**
	 * Outputs the addresses between lower and upper limits in a {@link Map} of {@link IpAddress} and {@link IpRange}
	 * @param lowerAddr the lower {@link IpAddress}
	 * @param upperAddr the upper {@link IpAddress}
	 * @return a {@link Map} containing the ranges (in CIDR notation) for each address in the range
	 */
	private static Map<IpAddress, IpRange> getCidr(IpAddress lowerAddr, IpAddress upperAddr) {
		return getCidr(0, lowerAddr, upperAddr, new TreeMap<IpAddress, IpRange>());
	}

	/**
	 * Outputs the addresses between lower and upper limits in a {@link Map} of {@link IpAddress} and {@link IpRange}, starting from the given suffix, into the given output map
	 * @param n the starting CIDR suffix
	 * @param lower the lower {@link IpAddress}
	 * @param upper the upper {@link IpAddress}
	 * @param allRanges the output map where the ranges are being filled
	 * @return the input map allRanged filled with values
	 */
	private static Map<IpAddress, IpRange> getCidr(int n, IpAddress lower, IpAddress upper,
			Map<IpAddress, IpRange> allRanges) {
		if (lower.isGreater(upper)) {
			return allRanges;
		}
		
		IpAddress highBlockLower = (upper.getLowerLimit(n));
		IpAddress highBlockUpper = (highBlockLower.getUpperLimit(n+1));
		
		IpAddress midBlockUpper = (lower.getUpperLimit(n));
		IpAddress midBlockLower = (midBlockUpper.getLowerLimit(n+1));

		IpAddress lowBlockLower = (lower.getLowerLimit(n));
		IpAddress lowBlockUpper = (lowBlockLower.getUpperLimit(n+1));

		IpAddress resultUpperLimit = null;
		IpAddress resultLowerLimit = null;
		if (upper.isGreaterEqual(highBlockUpper) && highBlockLower.isGreaterEqual(lower)) {
			resultLowerLimit = highBlockLower;
			resultUpperLimit = highBlockUpper;
		}

		if (upper.isGreaterEqual(midBlockUpper) && midBlockLower.isGreaterEqual(lower)) {
			resultLowerLimit = midBlockLower;
			resultUpperLimit = midBlockUpper;
		}
		
		if (upper.isGreaterEqual(lowBlockUpper) && lowBlockLower.isGreaterEqual(lower)) {
			resultLowerLimit = lowBlockLower;
			resultUpperLimit = lowBlockUpper;
		}

		if (resultUpperLimit == null & resultLowerLimit == null) {
			allRanges = IpRange.getCidr(n + 1, lower, upper, allRanges);
		} else {
			try {
				allRanges = IpRange.getCidr(n, lower, resultLowerLimit.prev(), allRanges);
				allRanges = IpRange.getCidr(n, resultUpperLimit.next(), upper, allRanges);
			} catch (InvalidIpAddressException e) {
				// noting to do
			}
			IpRange cidrRange = new IpRange(resultLowerLimit, resultUpperLimit, n);
			allRanges.put(resultLowerLimit, cidrRange);
		}

		return allRanges;
	}

	/**
	 * Outputs the range between lower and upper as string
	 * @return the range string
	 */
	public String toString() {
		return lowerLimit.toString() +"-"+upperLimit.toString();
	}

	/**
	 * Gets the raw input string of the range
	 * @return the raw input string
	 */
	public String toInputString() {
		return inputString;
	}

	/**
	 * Gets the version of the IP range
	 * @return the IP revision version (v4/v6)
	 */
	public String getIpVersion() {
		if (upperLimit instanceof Ipv4Address) {
			return "v4";
		} 
		if (upperLimit instanceof Ipv6Address) {
			return "v6";
		}
		return null;
	} 
}
