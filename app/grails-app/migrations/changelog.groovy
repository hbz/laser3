databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	// 0.1.1 - database snapshot @ laser-qa
	include file: 'changelog-0.groovy'

    // << grails prod dbm-changelog-sync

    // release 0.2
    include file: 'changelog-100.groovy'

	// release 0.3
	include file: 'changelog-110.groovy'
	include file: 'changelog-120.groovy'

	// release 0.3.1
	include file: 'changelog-130.groovy'

	// release 0.4
	include file: 'changelog-140.groovy'
	include file: 'changelog-150.groovy'

	// release 0.4.5 & 0.5
	include file: 'changelog-160.groovy'
	include file: 'changelog-165.groovy'

	// release 0.6
	include file: 'changelog-170.groovy'

	// release 0.7
	include file: 'changelog-180.groovy'
	include file: 'changelog-185.groovy'
	include file: 'changelog-190.groovy'

	// release 0.8
	include file: 'changelog-200.groovy'
}
