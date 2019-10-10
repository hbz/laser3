databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	include file: 'pre1.0/changelog-0.groovy'		// migration to postgresql

	include file: 'pre1.0/changelog-10.groovy'		// v0.12
	include file: 'pre1.0/changelog-20.groovy'		// v0.13
	// include file: 'pre1.0/changelog-20-qa.groovy' 	// QA only
	// include file: 'pre1.0/changelog-20-prod.groovy'	// PROD only

	include file: 'pre1.0/changelog-30.groovy'		// v0.14
	include file: 'pre1.0/changelog-40.groovy'		// v0.15
	include file: 'pre1.0/changelog-50.groovy'		// v0.16
	// include file: 'pre1.0/changelog-50-qa.groovy' 	// QA only
	// include file: 'pre1.0/changelog-50-prod.groovy'	// PROD only

	include file: 'pre1.0/changelog-60.groovy'		// v0.17
	include file: 'pre1.0/changelog-65.groovy'
	include file: 'pre1.0/changelog-70.groovy'		// v0.18
	include file: 'pre1.0/changelog-75.groovy'
	include file: 'pre1.0/changelog-76.groovy'
	include file: 'pre1.0/changelog-80.groovy'		// v0.19
	include file: 'pre1.0/changelog-90.groovy'		// v0.20
	include file: 'pre1.0/changelog-95.groovy'
	include file: 'pre1.0/changelog-96.groovy'
	include file: 'pre1.0/changelog-97.groovy'
	include file: 'pre1.0/changelog-98.groovy'
	include file: 'pre1.0/changelog-100.groovy'		// v1.0
	include file: 'pre1.0/changelog-101.groovy'

	include file: 'changelog-2019-10-07.groovy'		// new deployment strategy
	include file: 'changelog-2019-10-08.groovy'
	include file: 'changelog-2019-10-10.groovy'
}
