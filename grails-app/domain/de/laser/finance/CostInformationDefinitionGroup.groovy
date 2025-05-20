package de.laser.finance

import de.laser.Org

class CostInformationDefinitionGroup {

    Org tenant
    CostInformationDefinition costInformationDefinition

    static mapping = {
        id column: 'cifg_id'
        version column: 'cifg_version'
        tenant column: 'cifg_tenant_fk', index: 'cifg_tenant_idx'
        costInformationDefinition column: 'cifg_cost_information_definition_fk', index: 'cifg_cost_information_definition_idx'
    }

    static constraints = {
    }
}
