package de.laser.interfaces

interface DeleteFlag {

    // boolean isDeleted = false
    //
    // static mapping = {
    //    isDeleted column: 'xyz_is_deleted'
    // }
    //
    // static constraints = {
    //    isDeleted(nullable: true, blank: false, default: false)
    // }
    //
    // static hibernateFilters = {
    //    isDeletedFilter(condition: 'xyz_is_deleted != true', default: true)
    // }

    boolean isDeleted()
}
