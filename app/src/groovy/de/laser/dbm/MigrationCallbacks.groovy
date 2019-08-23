package de.laser.dbm

import liquibase.Liquibase
import liquibase.database.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MigrationCallbacks {

	protected static Logger LOG = LoggerFactory.getLogger(this)

	void beforeStartMigration(Database Database) {
		LOG.info('DBM beforeStartMigration')
		println 'DBM beforeStartMigration'
	}

	void onStartMigration(Database database, Liquibase liquibase, String changelogName) {
		LOG.info('DBM onStartMigration')
		println 'DBM onStartMigration'
	}

	void afterMigrations(Database Database) {
		LOG.info('DBM afterMigrations')
		println 'DBM afterMigrations'
	}
}