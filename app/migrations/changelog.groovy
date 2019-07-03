databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	include file: 'changelog-0.groovy'		// migration to postgresql

	include file: 'changelog-10.groovy'		// v0.12

	include file: 'changelog-20.groovy'		// v0.13

	// include file: 'changelog-20-qa.groovy' 	// QA only
	// include file: 'changelog-20-prod.groovy'	// PROD only

	include file: 'changelog-30.groovy'		// v0.14

	include file: 'changelog-40.groovy'		// v0.15

	include file: 'changelog-50.groovy'		// v0.16

	// include file: 'changelog-50-qa.groovy' 	// QA only
	// include file: 'changelog-50-prod.groovy'	// PROD only

	include file: 'changelog-60.groovy'		// v0.17
	include file: 'changelog-65.groovy'

	include file: 'changelog-70.groovy'		// v0.18
	include file: 'changelog-75.groovy'
	// include file: 'changelog-76.groovy'
}
