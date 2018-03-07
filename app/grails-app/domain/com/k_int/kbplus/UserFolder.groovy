package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import javax.persistence.Transient

class UserFolder {

  String shortcode
  String name
  List items=[]
    Date dateCreated
    Date lastUpdated

  static belongsTo = [
    user:User
  ]

  static hasMany = [
    items:FolderItem
  ]

  static mapping = {
          id column:'uf_id'
     version column:'uf_version'
        user column:'uf_owner_id'
   shortcode column:'uf_shortcode'
        name column:'uf_name'
       items cascade: 'all-delete-orphan'
      dateCreated column: 'uf_dateCreated'
      lastUpdated column: 'uf_lastUpdated'
  }

  static constraints = {
    shortcode(nullable:true, blank:true)
    name(nullable:true, blank:true)
  }


  @Transient
  def addIfNotPresent(oid) {
    def present = false;
    items.each { 
      if ( it.referencedOid && ( it.referencedOid == oid ) ) {
        present = true
      }
    }

    if ( !present ) {
      items.add(new FolderItem(folder:this,referencedOid:oid))
    }
  }

    @Transient
    def removeItem(oid) {

        def folderitem = FolderItem.findByFolderAndReferencedOid(this, oid)
        if (folderitem) {
            this.items.remove(folderitem)
            while (this.items.remove(null));
        }

    }

    def removePackageItems() {

        def itemstoremove = FolderItem.findAllByFolder(this)
        //Remove only Package Items
        itemstoremove.each {

            def oid_components = it.referencedOid?.toString().split(':');
            if (oid_components[0].startsWith("com.k_int.kbplus")) {
                def components = oid_components[0].toString().split("[.]");
                if (components[3].contains('Package')) {
                    def folderitem = FolderItem.findByFolderAndReferencedOid(this, it.referencedOid)
                    if (folderitem) {
                        folderitem.delete()
                    }
                }
            }
        }

    }


}
