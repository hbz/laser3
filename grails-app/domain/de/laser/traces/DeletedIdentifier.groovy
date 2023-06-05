package de.laser.traces

import de.laser.Identifier
import de.laser.exceptions.CreationException

class DeletedIdentifier {

    Long oldDatabaseID
    String oldValue
    String oldNamespace
    String oldGlobalUIDPointer
    String oldDatabaseIDPointer //if no globalUID can be found
    String oldReferenceObjectType
    Date dateCreated
    Date lastUpdated
    Date oldDateCreated
    Date oldLastUpdated

    static mapping = {
        datasource                    'storage'
        id                                  column: 'di_id'
        version                             column: 'di_version'
        oldDatabaseID                       column: 'di_old_database_id', index: 'di_old_database_id'
        oldValue                            column: 'di_old_value', index: 'di_old_value_idx, di_old_value_ns_idx'
        oldNamespace                        column: 'di_old_namespace', index: 'di_old_namespace_idx, di_old_value_ns_idx'
        oldGlobalUIDPointer                 column: 'di_old_global_uid', index: 'di_old_global_idx'
        oldDatabaseIDPointer                column: 'di_old_database_id_pointer', index: 'di_old_database_id_pointer, di_old_db_id_pointer_idx'
        oldReferenceObjectType              column: 'di_old_reference_object_type', index: 'di_old_reference_object_type_idx, di_old_db_id_ref_obj_idx'
        dateCreated                         column: 'di_date_created'
        oldDateCreated                      column: 'di_old_date_created'
        lastUpdated                         column: 'di_last_updated'
        oldLastUpdated                      column: 'di_old_last_updated'
    }

    static constraints = {
        oldGlobalUIDPointer (nullable: true)
    }

    static DeletedIdentifier construct(Identifier delId) throws CreationException {
        def reference = delId.getReference()
        DeletedIdentifier trace = new DeletedIdentifier(oldDatabaseID: delId.id, oldValue: delId.value, oldNamespace: delId.ns.ns, oldDatabaseIDPointer: reference.id, oldReferenceObjectType: reference.class.name, oldDateCreated: delId.dateCreated, oldLastUpdated: delId.lastUpdated)
        if(reference.hasProperty('globalUID'))
            trace.oldGlobalUIDPointer = reference.globalUID

        if(trace.save())
            trace
        else throw new CreationException(trace.getErrors().getAllErrors())
    }
}
