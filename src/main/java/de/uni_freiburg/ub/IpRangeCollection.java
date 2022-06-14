package de.uni_freiburg.ub;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;
import de.uni_freiburg.ub.Exception.InvalidRangeException;

/**
 * Represents a collection of IP(v4/v6) ranges
 * @see IpRange
 * @see Ipv4Address
 * @see Ipv6Address
 */
public class IpRangeCollection {

	List<IpRange> ipRangeCollection;

	/**
	 * Constructor to initialise the empty collection of ranges
	 */
	public IpRangeCollection() {
		this.ipRangeCollection = new LinkedList<IpRange>();
	}

	/**
	 * Adds an IP range to the collection
	 * @param ipRange the range to add
	 * @return the collection with the added range
	 * @throws InvalidIpAddressException if the object is not valid
	 */
	public IpRangeCollection add(IpRange ipRange) throws InvalidIpAddressException {
		this.ipRangeCollection.add(ipRange);
		return this;
	}

	/**
	 * Outputs the ranges in the collection
	 * @return the collection of IP ranges
	 * @throws InvalidIpAddressException if an address is invalid
	 * @throws InvalidRangeException if the range is invalid
	 * @see IpAddress
	 * @see IpRange
	 */
	public IpRangeCollection compact() throws InvalidIpAddressException, InvalidRangeException {
		IpRangeCollection result = new IpRangeCollection();
		IpRange currentRange;

		ipRangeCollection.sort(new Comparator<IpRange>() {
			@Override
			public int compare(IpRange o1, IpRange o2) {
				return o1.getLowerLimit().compareTo(o2.lowerLimit);
			}
		});

		if (ipRangeCollection.isEmpty()) {
			return this;
		} else {
			IpRange lowestRange = ipRangeCollection.get(0);
			currentRange = lowestRange;
		}

		// the ranges in ipRangeCollection are sorted by their lowerLimits !!
		for (IpRange range : ipRangeCollection) {
			if (currentRange.upperLimit.next().isGreaterEqual(range.lowerLimit)) {
				// the ranges are overlapping or touching each other
				if (range.upperLimit.isLesserEqual(currentRange.upperLimit)) {
					// range is a real subrange of currentRange
				} else {
					// range extends currentRange
					currentRange.upperLimit = range.upperLimit;
				}
			} else {
				// there is a gap between range and current range
				// => currentRange has max size
				result.add(new IpRange(currentRange.lowerLimit, currentRange.upperLimit));
				// walk on with range as new current range
				currentRange = range;
			}
		}

		result.add(new IpRange(currentRange.lowerLimit, currentRange.upperLimit));

		return result;
	}

	/**
	 * Outputs all ranges in the collection in their CIDR representations
	 * @return a {@link List} of CIDR representation strings
	 */
	public List<String> toCidrStrings() {
		List<String> cidr = new LinkedList<String>();

		for (IpRange ipRange : ipRangeCollection) {
			cidr.addAll(ipRange.toCidr());
		}
		return cidr;
	}

	/**
	 * Outputs all ranges in the collection in range strings
	 * @return a {@link List} of IP range strings
	 */
	public List<String> toRangeStrings() {
		List<String> ranges = new LinkedList<String>();
		
		for (IpRange ipRange : ipRangeCollection) {
			ranges.add(ipRange.lowerLimit.toString() + "-" + ipRange.upperLimit.toString());
		}
		return ranges;
	}

	/**
	 * Output all input strings in this collection of ranges
	 * @return a {@link List} of (raw) input strings
	 */
	public List<String> toInputStrings() {
		List<String> ranges = new LinkedList<String>();
		
		for (IpRange ipRange : ipRangeCollection) {
			ranges.add(ipRange.toInputString());
		}
		return ranges;
	}
}
