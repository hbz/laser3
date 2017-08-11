package com.k_int.kbplus

import javax.persistence.Transient

class IdentifierOccurrence {

    static auditable = true

    Identifier identifier

    static belongsTo = [
            lic:    License,
            org:    Org,
            pkg:    Package,
            sub:    Subscription,
            ti:     TitleInstance,
            tipp:   TitleInstancePackagePlatform
    ]

    static mapping = {
        id  column:'io_id'
        identifier column:'io_canonical_id'
        lic     column:'io_lic_fk'
        org     column:'io_org_fk'
        pkg     column:'io_pkg_fk'
        sub     column:'io_sub_fk'
        ti      column:'io_ti_fk'
        tipp    column:'io_tipp_fk'
  }

  static constraints = {
        lic     (nullable:true)
        org     (nullable:true)
        pkg     (nullable:true)
        sub     (nullable:true)
        ti      (nullable:true)
        tipp    (nullable:true)
  }

    /**
     * Generic setter
     */
    def setReference(def owner) {
        lic  = owner instanceof License ? owner : lic
        org  = owner instanceof Org ? owner : org
        pkg  = owner instanceof Package ? owner : pkg
        sub  = owner instanceof Subscription ? owner : sub
        tipp = owner instanceof TitleInstancePackagePlatform ? owner : tipp
        ti   = owner instanceof TitleInstance ? owner : ti
    }

    static getAttributeName(def object) {
        def name

        name = object instanceof License ?  'lic' : name
        name = object instanceof Org ?      'org' : name
        name = object instanceof Package ?  'pkg' : name
        name = object instanceof Subscription ?                 'sub' :  name
        name = object instanceof TitleInstancePackagePlatform ? 'tipp' : name
        name = object instanceof TitleInstance ?                'ti' :   name

        name
    }

    String toString() {
        "IdentifierOccurrence(${id} - lic:${lic}, org:${org}, pkg:${pkg}, sub:${sub}, ti:${ti}, tipp:${tipp}"
    }

  @Transient
  def onSave = {
  }

  @Transient
  def onDelete = {
  }


}
