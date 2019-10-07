package com.k_int.kbplus

class OrgTitleStats {

  long lastRetrievedTimestamp

  TitleInstance title
  Org org

  Date dateCreated
  Date lastUpdated

  static constraints = {
    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true, blank: false)
    dateCreated (nullable: true, blank: false)
  }

  static mapping = {
    lastUpdated column: 'ots_last_updated'
    dateCreated column: 'ots_date_created'
  }
}
