package de.laser.dbm

import liquibase.Liquibase
import liquibase.database.Database

class MigrationCallbacks {

	def grailsApplication

	void beforeStartMigration(Database Database) {

		println '--------------------------------------------------------------------------------'
		println 'Database Migration'
		println '   new changesets detected ..'
		println '   dumping current database ..'

		def dataSource = grailsApplication.config.dataSource
		URI uri		   = new URI(dataSource.url.substring(5))

		String backupFile = grailsApplication.config.deployBackupLocation + "/laser-backup-${new Date().format('yyyy-MM-dd-HH:mm:ss')}.sql"

		Map<String, String> config = [
				dbname:	"${uri.getScheme()}://${dataSource.username}:${dataSource.password}@${uri.getHost()}:${uri.getPort()}${uri.getRawPath()}",
				schema: "public",
				file: 	"${backupFile}"
		]

		println '   source: ' + Database
		println '   target: ' + backupFile

		try {
			String cmd = "/usr/bin/pg_dump -x " + (config.collect{ '--' + it.key + '=' + it.value }).join(' ')

			cmd.execute().waitForProcessOutput(System.out, System.err)

		} catch (Exception e) {
			println '   error: ' + e.getMessage()
			e.printStackTrace()
		}
	}

	void onStartMigration(Database database, Liquibase liquibase, String changelogName) {

		println '  processing: ' + changelogName
	}

	void afterMigrations(Database Database) {

		println '  done ..'
		println '--------------------------------------------------------------------------------'
	}
}