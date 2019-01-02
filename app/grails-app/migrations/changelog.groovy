databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

    // postgresql migration
	// include file: 'changelog-0.groovy'

	// v0.12
	include file: 'changelog-10.groovy'
}
