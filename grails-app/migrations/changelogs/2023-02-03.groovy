package changelogs

import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupItem


databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1675405845878-1") {
        grailsChange {
            change {
                List<PropertyDefinition> pdList = PropertyDefinition.executeQuery(
                        "select pd from PropertyDefinition pd where pd.tenant is null and pd.descr = :descr", [descr: PropertyDefinition.ORG_PROP]
                ) as List<PropertyDefinition>

                List<PropertyDefinitionGroupItem> pdgiList = pdList.collect {
                    PropertyDefinitionGroupItem.findAllByPropDef(it as PropertyDefinition)
                }.flatten() as List<PropertyDefinitionGroupItem>

                // List<PropertyDefinitionGroup> pdgList = pdgiList.collect{ it.propDefGroup }.unique()
                List<PropertyDefinitionGroup> pdgList = PropertyDefinitionGroup.findAllByOwnerType('de.laser.Org')

                List<OrgProperty> propertyList = OrgProperty.findAllByTypeInList(pdList)

                println 'propertyList > ' + propertyList.id
                println 'pdgiList     > ' + pdgiList.id
                println 'pdgList      > ' + pdgList.id
                println 'pdList       > ' + pdList.id

                propertyList.each   { it.delete() }
                pdgiList.each       { it.delete() }
                pdgList.each        { it.delete() }
                pdList.each         { it.delete() }

            }
            rollback {}
        }
    }
}
