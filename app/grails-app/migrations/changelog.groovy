databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "changelog") {
		// TODO add changes and preconditions here
	}

	// postgreSQL migration
    include file: 'changelog-0.groovy'
}
