databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	// postgresql migration; first diff
	include file: 'changelog-0.groovy'
}
