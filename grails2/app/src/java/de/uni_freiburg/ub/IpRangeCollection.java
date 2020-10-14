package de.uni_freiburg.ub;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import de.uni_freiburg.ub.Exception.InvalidIpAddressException;
import de.uni_freiburg.ub.Exception.InvalidRangeException;

public class IpRangeCollection {

	List<IpRange> ipRangeCollection;

	public IpRangeCollection() {
		this.ipRangeCollection = new LinkedList<IpRange>();
	}

	public IpRangeCollection add(IpRange ipRange) throws InvalidIpAddressException {
		this.ipRangeCollection.add(ipRange);
		return this;
	}

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

	public List<String> toCidrStrings() {
		List<String> cidr = new LinkedList<String>();

		for (IpRange ipRange : ipRangeCollection) {
			cidr.addAll(ipRange.toCidr());
		}
		return cidr;
	}
	
	public List<String> toRangeStrings() {
		List<String> ranges = new LinkedList<String>();
		
		for (IpRange ipRange : ipRangeCollection) {
			ranges.add(ipRange.lowerLimit.toString() + "-" + ipRange.upperLimit.toString());
		}
		return ranges;
	}
	
	public List<String> toInputStrings() {
		List<String> ranges = new LinkedList<String>();
		
		for (IpRange ipRange : ipRangeCollection) {
			ranges.add(ipRange.toInputString());
		}
		return ranges;
	}
}
