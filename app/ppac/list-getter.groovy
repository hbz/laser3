// list getter must always return valid lists (null entries removed) and log npe warnings
// 2019-10-30

class Dummy {

	// bad code
	// return value is null or contains null entries
	static getList_deprecated(param) {
		if (param > 0.5) {
			return [1, 2, 3, null, null, 6, 7, null]
		}
		else {
			return null
		}
	}

	// good code
	// list as return value without null entries; warning is logged if there would be
	static List<Object> getList(param) {
		List<Object> result = []

		if (param > 0.5) {
			result = [1, 2, 3, null, null, 6, 7, null]
		}

		if (null in result) {
			println "NPE-WARNING (param: " + param + ")"
		}

		result.findResults {it}
	}
}

class Demo {

	// bad code
	// unnecessary testing due erroneously values
	static doSomething_deprecated(param) {
		def list = Dummy.getList_deprecated(param)
		int sum = 0

		list?.each{ it ->
			println "e.g. frontend: <a href=\"url?id=${it?.intValue()?.toString()}\">Link</a>"
			if (it) {
				sum += it
			}
		}
		sum
	}

	// good code
	// trustworthy values
	static int doSomething(param) {
		List<Object> list = Dummy.getList(param)
		int sum = 0

		list.each{ it ->
			println "e.g. frontend: <a href=\"url?id=${it.intValue().toString()}\">Link</a>"
			sum += it
		}
		sum
	}
}

double rnd = Math.random()

println '-- deprecated --'
println Demo.doSomething_deprecated(rnd)

println '-- good code --'
println Demo.doSomething(rnd)