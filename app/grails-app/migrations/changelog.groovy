databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	// 0.1.1 - database snapshot - laser-qa
	include file: 'changelog-0.groovy'

	// 0.1.1 - code snapshot - hbz-master
	include file: 'changelog-1.groovy'

	// 0.1.1 - manual fixes to sync db with gorm
	include file: 'changelog-2.groovy'

}
