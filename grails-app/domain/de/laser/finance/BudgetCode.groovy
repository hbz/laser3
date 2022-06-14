package de.laser.finance

import de.laser.Org

/**
 * This class represents a grouping unit for cost items. The grouping itself is done in the {@link CostItemGroup} class which links budget codes to {@link CostItem}s
 */
class BudgetCode {

    Org owner
    String value
    String descr

    Date dateCreated
    Date lastUpdated

    static mapping = {
         id column:'bc_id'
    version column:'bc_version'
      owner column:'bc_owner_fk', index:'bc_owner_idx'
      value column:'bc_value'
      descr column:'bc_description'
      dateCreated column: 'bc_date_created'
      lastUpdated column: 'bc_last_updated'
    }

    static constraints = {
        value (blank:false, unique: 'owner')
        descr (nullable:true,  blank:true, maxSize:2048)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}
