package com.k_int.kbplus

class IdentifierGroup {

  Date dateCreated
  Date lastUpdated

  static hasMany = [ identifiers:Identifier]
  static mappedBy = [ identifiers:'ig']

  static mapping = {
    id column:'ig_id'

    dateCreated column: 'ig_date_created'
    lastUpdated column: 'ig_last_updated'
  }

  static constraints = {
    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true, blank: false)
    dateCreated (nullable: true, blank: false)

  }

}
