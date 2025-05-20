package de.laser.custom

import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.storage.BeanStore
import de.laser.utils.AppUtils
import grails.core.GrailsApplication
import liquibase.Liquibase
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database

import java.text.SimpleDateFormat

/**
 * Contains database migration cleanup methods. As methods are used mainly once,
 * they are not documented in detail
 */
class CustomMigrationCallbacks {

	GrailsApplication grailsApplication

	static final String VT = '--------------------------------------------------------------------------------'
	static final String V1 = '-     '
	static final String V2 = '-        '
	static final String V3 = '-           '

	static void _localChangelogMigration_2022_04() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getDataSource())

		int count1 = (sql.rows("select * from databasechangelog where filename like 'done/pre%'")).size()
		int count2 = (sql.rows("select * from databasechangelog where filename like 'changelog-2021-%'")).size()
		int count3 = (sql.rows("select * from databasechangelog where filename like 'changelog-2022-%'")).size()

		if (count1 || count2 || count3) {
			sql.withTransaction {
				println VT
				println V1 + 'Cleanup database migration table'
				println V2 + 'done/pre% -> ' + count1
				println V3 + 'updating pre1.0: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'done/pre1.0/', 'legacy/') where filename like 'done/pre1.0/%'")
				println V3 + 'updating pre2.0: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'done/pre2.0/', 'legacy/') where filename like 'done/pre2.0/%'")

				println V2 + 'changelog-2021-% -> ' + count2
				println V3 + 'updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelog-', 'legacy/changelog-') where filename like 'changelog-2021-%'")

				println V2 + 'changelog-2022-% -> ' + count3
				println V3 + 'updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelog-', 'changelogs/') where filename like 'changelog-2022-%'")

				sql.commit()
				println VT
			}
		}

		int count4 = (sql.rows("select * from databasechangelog where id = 'changelog' and filename = 'changelog.groovy'")).size()

		if (count4) {
			sql.withTransaction {
				println VT
				println V1 + 'Cleanup database migration table'
				println V2 + 'updating: ' + sql.executeUpdate("update databasechangelog set id = 'laser' where id = 'changelog' and filename = 'changelog.groovy'")

				sql.commit()
				println VT
			}
		}
	}

	static void _localChangelogMigration_2023_06() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getDataSource())

		String[] ranges = ['changelogs/2023-01-%', 'changelogs/2023-02-%', 'changelogs/2023-03-%', 'changelogs/2023-04-%', 'changelogs/2023-05-%']
		int[] count = [0, 0, 0, 0, 0]

		ranges.eachWithIndex { r, i ->
			String query = "select * from databasechangelog where filename like '${r}'"
			count[i] = (sql.rows(query).size())
		}

		if (count.sum()) {
			sql.withTransaction {
				println VT
				println V1 + 'Cleanup database migration table'
				ranges.each { r ->
					println V2 + '' + r
					String query = "update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like '${r}'"
					println V3 + 'updating: ' + sql.executeUpdate(query)
				}

				sql.commit()
				println VT
			}
		}
	}

	static void _localChangelogMigration_2024_06() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getDataSource())

		int count = (sql.rows("select * from databasechangelog where filename like 'changelogs/2023-%'")).size()

		if (count) {
			sql.withTransaction {
				println VT
				println V1 + 'Cleanup database migration table'
				println V2 + 'changelogs/2023-%'
				println V3 + 'updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like 'changelogs/2023-%'")

				sql.commit()
				println VT
			}
		}
	}

	static void _localChangelogMigration_2024_06_storage() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getStorageDataSource())

		int count = (sql.rows("select * from databasechangelog where filename like 'changelogs/2023-%'")).size()

		if (count) {
			sql.withTransaction {
				println VT
				println V1 + 'Cleanup storage migration table'
				println V2 + 'changelogs/2023-%'
				println V3 + 'updating: ' + sql.executeUpdate("update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like 'changelogs/2023-%'")

				sql.commit()
				println VT
			}
		}
	}

	static void _localChangelogMigration_2025_05() {
		groovy.sql.Sql sql = new groovy.sql.Sql(BeanStore.getDataSource())

		String[] ranges = ['changelogs/2024-%', 'changelogs/2025-01-%', 'changelogs/2025-02-%', 'changelogs/2025-03-%', 'changelogs/2025-04-%']
		int[] count = [0, 0, 0, 0, 0]

		ranges.eachWithIndex { r, i ->
			String query = "select * from databasechangelog where filename like '${r}'"
			count[i] = (sql.rows(query).size())
		}

		if (count.sum()) {
			sql.withTransaction {
				println VT
				println V1 + 'Cleanup database migration table'
				ranges.each { r ->
					println V2 + '' + r
					String query = "update databasechangelog set filename = replace(filename, 'changelogs/', 'legacy/') where filename like '${r}'"
					println V3 + 'updating: ' + sql.executeUpdate(query)
				}

				sql.commit()
				println VT
			}
		}
	}

	void beforeStartMigration(Database database) {
	}

	/**
	 * Executed upon database migration start; fetches the changesets for the given database and executes them, if not done already.
	 * A database dump is being created before executing changesets
	 * @param database the database on which the changes should be checked
	 * @param liquibase the database changelogs
	 * @param changelogName unused
	 */
	void onStartMigration(Database database, Liquibase liquibase, String changelogName) {
		boolean isStorage = (database.connection.getURL() == ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.url', String))

		List allIds = liquibase.getDatabaseChangeLog().getChangeSets().collect { ChangeSet it -> it.filePath + '::' + it.id + '::' + it.author }
		List ranIds = database.getRanChangeSetList().collect { RanChangeSet it -> it.changeLog + '::' + it.id + '::' + it.author }
		List diff   = allIds.minus(ranIds)

		if (AppUtils.getCurrentServer() == AppUtils.DEV) {
			if (! diff.empty) {
				println VT
				println V1 + 'Database migration'
				println V2 + diff.size() + ' relevant changeset/s found ..'
				println V2 + 'dump ignored, because server ' + AppUtils.getCurrentServer() + ' ..'
				println V2 + 'done ..'
				println VT
			}
		}
		else if (! diff.empty) {
			Map dataSource = ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT, Map) as Map
			String fileName = 'laser-backup'

			if (isStorage) {
				dataSource = ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE, Map) as Map
				fileName = 'laser-storage-backup'
			}

			println VT
			println V1 + 'Database migration'
			println V2 + diff.size() + ' relevant changeset/s found ..'
			println V2 + 'dumping current database ..'

			URI uri = new URI(dataSource.url.substring(5))

			String backupFile = (ConfigMapper.getDeployBackupLocation() ?: ConfigDefaults.DEPLOYBACKUP_LOCATION_FALLBACK) +
					"/${fileName}-${(new SimpleDateFormat('yyyy-MM-dd-HH:mm:ss')).format(new Date())}.sql"

			Map<String, String> config = [
					dbname: "${uri.getScheme()}://${dataSource.username}:${dataSource.password}@${uri.getHost()}:${uri.getPort()}${uri.getRawPath()}",
					schema: "public",
					file  : "${backupFile}"
			]
			String pgDump = ConfigMapper.getPgDumpPath()

			println V3 + 'pg_dump : ' + pgDump
			println V3 + ' source : ' + database
			println V3 + ' target : ' + backupFile

			try {
				if ( pgDump ) {
					String cmd = pgDump + ' -x ' + (config.collect { '--' + it.key + '=' + it.value }).join(' ')
					cmd.execute().waitForProcessOutput(System.out, System.err)
				}
				else {
					println V3 + 'Backup ignored, because no config for pg_dump'
				}

			} catch (Exception e) {
				println V3 + 'error: ' + e.getMessage()
				e.printStackTrace()
			}

			println V2 + 'done ..'
			println VT
		}

		if (isStorage) {
//			_localChangelogMigration_2024_06_storage()
		} else {
			_localChangelogMigration_2025_05()
		}
	}

	void afterMigrations(Database Database) {
	}
}
