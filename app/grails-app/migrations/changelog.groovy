databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	// 0.1.1 - database snapshot @ laser-qa
	include file: 'changelog-0.groovy'

    // << grails prod dbm-changelog-sync

    // 0.2 - heave to release branch
    include file: 'changelog-100.groovy'
}
