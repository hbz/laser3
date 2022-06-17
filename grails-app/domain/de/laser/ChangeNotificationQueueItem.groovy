package de.laser

@Deprecated
class ChangeNotificationQueueItem {

  String oid
  String changeDocument
  Date ts

  Date dateCreated
  Date lastUpdated

  static mapping = {
                id column:'cnqi_id'
           version column:'cnqi_version'
               oid column:'cnqi_oid'
    changeDocument column:'cnqi_change_document', type:'text'
                ts column:'cnqi_ts'

    dateCreated column: 'cnqi_date_created'
    lastUpdated column: 'cnqi_last_updated'
  }

  static constraints = {
    oid           (blank:false)
    changeDocument(blank:false)

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true)
    dateCreated (nullable: true)
  }

}
