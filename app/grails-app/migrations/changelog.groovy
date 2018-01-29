databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	// 0.1.1 - database snapshot @ laser-qa
	include file: 'changelog-0.groovy'

    // << grails prod dbm-changelog-sync

    // heave to release branch 0.2
    include file: 'changelog-100.groovy'

	// heave to release branch 0.3
	include file: 'changelog-110.groovy'
	include file: 'changelog-120.groovy'
}
