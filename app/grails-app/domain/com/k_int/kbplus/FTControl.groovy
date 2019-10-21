package com.k_int.kbplus

class FTControl {

  String domainClassName
  String activity
  Long lastTimestamp
  Integer esElements
  Integer dbElements

  Date dateCreated
  Date lastUpdated

  static constraints = {
    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true, blank: false)
    dateCreated (nullable: true, blank: false)
  }
}
