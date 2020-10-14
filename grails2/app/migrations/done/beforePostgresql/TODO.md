
# PostgreSQL Migration

- copy current MySQL database
- apply lastest dbm changes


- use dataCleanupMySQL.sql
- use exportFilesMySQL.sql


- create PostgreSQL database (set search_path to public)
- init database tables (starting app)
- migrate data with pgLoader using migrate2postgresql.load


- init database migration


    //grails prod dbm-create-changelog --defaultSchema=public
    grails prod dbm-changelog-sync --defaultSchema=public
    grails prod dbm-gorm-diff --defaultSchema=public --add changelog-0.groovy
    grails prod dbm-update

### TODO

#### Hibernate 4:
org.hibernate.event.internal.DefaultFlushEventListener.onFlush() @ 28
org.hibernate.engine.internal.SessionEventListenerManagerImpl

	public void flushEnd(int numberOfEntities, int numberOfCollections) { 
		if ( listenerList == null ) { 
			return; 
		} 
 
		for ( SessionEventListener listener : listenerList ) { 
			listener.flushEnd( numberOfEntities, numberOfCollections ); 
		} 
	}