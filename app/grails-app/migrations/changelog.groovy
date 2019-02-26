databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

    // postgresql migration
	include file: 'changelog-0.groovy'

	include file: 'changelog-10.groovy'		// v0.12

	include file: 'changelog-20.groovy'		// v0.13

	// include file: 'changelog-20-qa.groovy' 	// v0.13 @ QA only

	// include file: 'changelog-20-prod.groovy'	// v0.13 @ PROD only

	include file: 'changelog-30.groovy'		// v0.14
}
