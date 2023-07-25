package de.laser.custom

import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.storage.BeanStore
import grails.core.GrailsApplication
import liquibase.Liquibase
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database

import java.text.SimpleDateFormat

class CustomMigrationCallbacks {

	GrailsApplication grailsApplication

	static void _localChangelogMigration_april22() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getDataSource())

		int count1 = (sql.rows("select * from databasechangelog where filename like 'done/pre%'")).size()
		int count2 = (sql.rows("select * from databasechangelog where filename like 'changelog-2021-%'")).size()
		int count3 = (sql.rows("select * from databasechangelog where filename like 'changelog-2022-%'")).size()

		if (count1 || count2 || count3) {
			sql.withTransaction {
				println '--------------------------------------------------------------------------------'
				println '-     Cleanup database migration table'
				println '-        done/pre% -> ' + count1
				println '-           updating pre1.0: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'done/pre1.0/', 'legacy/') where filename like 'done/pre1.0/%'")
				println '-           updating pre2.0: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'done/pre2.0/', 'legacy/') where filename like 'done/pre2.0/%'")

				println '-        changelog-2021-% -> ' + count2
				println '-           updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelog-', 'legacy/changelog-') where filename like 'changelog-2021-%'")

				println '-        changelog-2022-% -> ' + count3
				println '-           updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelog-', 'changelogs/') where filename like 'changelog-2022-%'")

				sql.commit()
				println '--------------------------------------------------------------------------------'
			}
		}

		int count4 = (sql.rows("select * from databasechangelog where id = 'changelog' and filename = 'changelog.groovy'")).size()

		if (count4) {
			sql.withTransaction {
				println '--------------------------------------------------------------------------------'
				println '-     Cleanup database migration table'
				println '-        updating: ' + sql.executeUpdate("update databasechangelog set id = 'laser' where id = 'changelog' and filename = 'changelog.groovy'")

				sql.commit()
				println '--------------------------------------------------------------------------------'
			}
		}
	}

	static void _localChangelogMigration_july24() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getDataSource())

		String[] ranges = ['changelogs/2023-01-%', 'changelogs/2023-02-%', 'changelogs/2023-03-%', 'changelogs/2023-04-%', 'changelogs/2023-05-%']
		int[] count = [0, 0, 0, 0, 0]

		ranges.eachWithIndex { r, i ->
			String query = "select * from databasechangelog where filename like '${r}'"
			count[i] = (sql.rows(query).size())
		}

		if (count.sum()) {
			sql.withTransaction {
				println '--------------------------------------------------------------------------------'
				println '-     Cleanup database migration table'
				ranges.each { r ->
					println '-        ' + r
					String query = "update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like '${r}'"
					println '-           updating: ' + sql.executeUpdate(query)
				}

				sql.commit()
				println '--------------------------------------------------------------------------------'
			}
		}
	}

	static void _localChangelogMigration_july24_storage() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getStorageDataSource())

		int count = (sql.rows("select * from databasechangelog where filename like 'changelogs/2022-%'")).size()

		if (count) {
			sql.withTransaction {
				println '--------------------------------------------------------------------------------'
				println '-     Cleanup database migration table'
				println '-        changelogs/2022-%'
				println '-           updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like 'changelogs/2022-%'")
				println '-        changelogs/2023-02-%'
				println '-           updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like 'changelogs/2023-02-%'")

				sql.commit()
				println '--------------------------------------------------------------------------------'
			}
		}
	}

	void beforeStartMigration(Database database) {
	}

	void onStartMigration(Database database, Liquibase liquibase, String changelogName) {

		_localChangelogMigration_july24()
		_localChangelogMigration_july24_storage()

		List allIds = liquibase.getDatabaseChangeLog().getChangeSets().collect { ChangeSet it -> it.filePath + '::' + it.id + '::' + it.author }
		List ranIds = database.getRanChangeSetList().collect { RanChangeSet it -> it.changeLog + '::' + it.id + '::' + it.author }
		List diff   = allIds.minus(ranIds)

		if (! diff.empty) {
			Map dataSource = ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT, Map) as Map
			String fileName = 'laser-backup'

			if (database.connection.getURL() == ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.url', String)) {
				dataSource = ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE, Map) as Map
				fileName = 'laser-storage-backup'
			}

			println '--------------------------------------------------------------------------------'
			println '-     Database migration'
			println '-        ' + diff.size() + ' relevant changeset/s found ..'
			println '-        dumping current database ..'

			URI uri = new URI(dataSource.url.substring(5))

			String backupFile = (ConfigMapper.getDeployBackupLocation() ?: ConfigDefaults.DEPLOYBACKUP_LOCATION_FALLBACK) +
					"/${fileName}-${(new SimpleDateFormat('yyyy-MM-dd-HH:mm:ss')).format(new Date())}.sql"

			Map<String, String> config = [
					dbname: "${uri.getScheme()}://${dataSource.username}:${dataSource.password}@${uri.getHost()}:${uri.getPort()}${uri.getRawPath()}",
					schema: "public",
					file  : "${backupFile}"
			]
			String pgDump = ConfigMapper.getPgDumpPath()

			println '-           pg_dump : ' + pgDump
			println '-            source : ' + database
			println '-            target : ' + backupFile

			try {
				if ( pgDump ) {
					String cmd = pgDump + ' -x ' + (config.collect { '--' + it.key + '=' + it.value }).join(' ')
					cmd.execute().waitForProcessOutput(System.out, System.err)
				}
				else {
					println '-           Backup ignored, because no config for pg_dump'
				}

			} catch (Exception e) {
				println '-           error: ' + e.getMessage()
				e.printStackTrace()
			}

			println '-        done ..'
			println '--------------------------------------------------------------------------------'
		}
	}

	void afterMigrations(Database Database) {
	}
}
