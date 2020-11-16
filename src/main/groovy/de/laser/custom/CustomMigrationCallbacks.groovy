package de.laser.custom

import de.laser.helper.ConfigUtils
import liquibase.Liquibase
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database

class CustomMigrationCallbacks {

	def grailsApplication

	void beforeStartMigration(Database database) {
	}

	void onStartMigration(Database database, Liquibase liquibase, String changelogName) {

		List allIds = liquibase.getDatabaseChangeLog().getChangeSets().collect { ChangeSet it -> it.filePath + '::' + it.id + '::' + it.author }
		List ranIds = database.getRanChangeSetList().collect { RanChangeSet it -> it.changeLog + '::' + it.id + '::' + it.author }
		List diff   = allIds.minus(ranIds)

		if (! diff.empty) {
			println '--------------------------------------------------------------------------------'
			println '-     Database migration'
			println '-        ' + diff.size() + ' relevant changesets detected ..'
			println '-        dumping current database ..'

			def dataSource = grailsApplication.config.dataSource
			URI uri = new URI(dataSource.url.substring(5))

			String backupFile = ConfigUtils.getDeployBackupLocation() + "/laser-backup-${new Date().format('yyyy-MM-dd-HH:mm:ss')}.sql"

			Map<String, String> config = [
					dbname: "${uri.getScheme()}://${dataSource.username}:${dataSource.password}@${uri.getHost()}:${uri.getPort()}${uri.getRawPath()}",
					schema: "public",
					file  : "${backupFile}"
			]
			println '-           pg_dump: ' + ConfigUtils.getPgDumpPath()
			println '-            source: ' + database
			println '-            target: ' + backupFile

			try {
				String cmd = ConfigUtils.getPgDumpPath() + ' -x ' + (config.collect { '--' + it.key + '=' + it.value }).join(' ')

				cmd.execute().waitForProcessOutput(System.out, System.err)

			} catch (Exception e) {
				println '-           error: ' + e.getMessage()
				e.printStackTrace()
			}

			println '-        done ..'

			if (ConfigUtils.getSchemaSpyScripPath()) {

				println 'Executing post-migration scripts'

				try {
					String cmd = 'sh ' + ConfigUtils.getSchemaSpyScripPath()
					println '-        ' + cmd

					cmd.execute()

				} catch (Exception e) {
					println '-        error: ' + e.getMessage()
					e.printStackTrace()
				}
			}

			println '--------------------------------------------------------------------------------'
		}
	}

	void afterMigrations(Database Database) {
	}
}