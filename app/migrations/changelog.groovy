databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {}

	// migration to postgresql

	include file: 'pre1.0/changelog-0.groovy'
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

	// new deployment strategy

	include file: 'changelog-2019-10-07.groovy'
	include file: 'changelog-2019-10-08.groovy'
	include file: 'changelog-2019-10-10.groovy'
	include file: 'changelog-2019-10-14.groovy'
	include file: 'changelog-2019-10-21.groovy'
	include file: 'changelog-2019-10-23.groovy'
	include file: 'changelog-2019-10-24.groovy'
	include file: 'changelog-2019-10-31.groovy'
	include file: 'changelog-2019-11-13.groovy'
	include file: 'changelog-2019-11-14.groovy'
	include file: 'changelog-2019-11-15.groovy'
	include file: 'changelog-2019-11-19.groovy'
	include file: 'changelog-2019-11-21.groovy'
	include file: 'changelog-2019-11-26.groovy'
	include file: 'changelog-2019-11-27.groovy'
	include file: 'changelog-2019-12-02.groovy'
	include file: 'changelog-2019-12-04.groovy'
	include file: 'changelog-2019-12-05.groovy'
	include file: 'changelog-2019-12-06.groovy'
	include file: 'changelog-2019-12-10.groovy'
	include file: 'changelog-2019-12-13.groovy'
	include file: 'changelog-2019-12-19.groovy'
	include file: 'changelog-2019-12-20.groovy'
	include file: 'changelog-2020-01-17.groovy'
	include file: 'changelog-2020-01-24.groovy'
	include file: 'changelog-2020-02-06.groovy'
	include file: 'changelog-2020-02-10.groovy'
	include file: 'changelog-2020-02-18.groovy'
	include file: 'changelog-2020-02-19.groovy'
	include file: 'changelog-2020-03-02.groovy'
	include file: 'changelog-2020-03-05.groovy'
	include file: 'changelog-2020-03-09.groovy'
	include file: 'changelog-2020-03-10.groovy'
	include file: 'changelog-2020-03-13.groovy'
	include file: 'changelog-2020-03-18.groovy'
	include file: 'changelog-2020-03-20.groovy'
	include file: 'changelog-2020-03-26.groovy'
	include file: 'changelog-2020-03-27.groovy'
	//include file: 'changelog-2020-03-30.groovy' chicken-and-egg problem due oo access
	include file: 'changelog-2020-04-16.groovy'
	include file: 'changelog-2020-04-27.groovy'
	include file: 'changelog-2020-05-03.groovy'
	include file: 'changelog-2020-05-04.groovy'
	include file: 'changelog-2020-05-07.groovy'
	include file: 'changelog-2020-05-12.groovy'
}
