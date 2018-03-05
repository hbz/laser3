package com.k_int.kbplus

class FolderItem {

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
    folder:UserFolder
  ]

  String referencedOid

  static mapping = {
                 id column:'fi_id'
            version column:'fi_version'
               user column:'fi_owner_id'
      referencedOid column:'fi_ref_oid'
      dateCreated column: 'fi_dateCreated'
      lastUpdated column: 'fi_lastUpdated'
      //folder column: 'fi_userfolder_fk'
  }

  static constraints = {
  }

  public boolean equals(Object o) {
    if ( o.id == this.id ) {
      return true;
    }
  }
}
