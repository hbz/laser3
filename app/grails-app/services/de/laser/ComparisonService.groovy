package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import grails.transaction.Transactional

@Transactional
class ComparisonService {

  GenericOIDService genericOIDService

  /**
   * Builds into the grouped properties return map the given group key and binding for the given object.
   *
   * @param groupedProperties - the return map groupedProperties. Please check if it is really necessary to reassign again and again the whole map.
   * @param groupKey
   * @param groupBinding
   * @param cmpObject
   * @return
   */
  Map getGroupedPropertyTrees(Map groupedProperties, PropertyDefinitionGroup groupKey, PropertyDefinitionGroupBinding groupBinding, cmpObject) {
    //get the current properties within each group for each object
    ArrayList<AbstractPropertyWithCalculatedLastUpdated> licenseProps = groupKey.getCurrentProperties(cmpObject)
    LinkedHashMap group = (LinkedHashMap) groupedProperties.get(groupKey)
    if(licenseProps.size() > 0) {
      if(group) {
        group.groupTree = buildComparisonTree(group.groupTree,cmpObject,licenseProps)
        group.binding.put(cmpObject,groupBinding)
      }
      else if(!group) {
        TreeMap groupTree = new TreeMap()
        LinkedHashMap binding = new LinkedHashMap()
        binding.put(cmpObject,groupBinding)
        group = [groupTree:buildComparisonTree(groupTree,cmpObject,licenseProps),binding:binding]
      }
    }
    group
  }

  /**
   * Builds for the given list of properties a comparison tree for the given object.
   * As the method is being called in a loop where a structure remapping is being done, the result map is being handed as parameter as well.
   *
   * Archivkopie {
   *   Archivkopie: Kosten{51: null, 57: Free},
   *   Archivkopie: Form{51: null, 57: Data},
   *   Archivkopie: Recht{51: null, 57: Yes}
   *   }, binding: ?
   * Geristand {
   *   Signed{51: Yes, 57: Yes},
   *   Anzuwendes Recht{51: Dt. Recht, 57: null},
   *   Gerichtsstand{51: Berlin: null}
   *  }, binding: ?
   *
   * @param result - the map being filled or updated
   * @return the updated map
   */
    Map buildComparisonTree(Map result,cmpObject,Collection<AbstractPropertyWithCalculatedLastUpdated> props) {
      props.each { prop ->

        //property level - check if the group contains already a mapping for the current property
        def propertyMap = result.get(genericOIDService.getOID(prop.type))
        if(propertyMap == null) {
          propertyMap = [:]
        }
        List propertyList = propertyMap.get(cmpObject)
        if(propertyList == null) {
          propertyList = [prop]
        }
        else {
          propertyList.add(prop)
        }

        propertyMap.put(cmpObject,propertyList)
        result.put(genericOIDService.getOID(prop.type),propertyMap)
      }
      result
    }

  Map buildComparisonTreeIEs(Map result,cmpObject,Collection<IssueEntitlement> props) {
    props.each { issueEntitlement ->

      //property level - check if the group contains already a mapping for the current property
      def propertyMap = result.get(genericOIDService.getOID(issueEntitlement.tipp))
      if(propertyMap == null) {
        propertyMap = [:]
      }
      List propertyList = propertyMap.get(cmpObject)
      if(propertyList == null) {
        propertyList = [issueEntitlement]
      }
      else {
        propertyList.add(issueEntitlement)
      }

      propertyMap.put(cmpObject,propertyList)
      result.put(genericOIDService.getOID(issueEntitlement.tipp),propertyMap)
    }
    result
  }

  Map buildComparisonTreePropertyDefintion(Map result,cmpObject,Collection<PropertyDefinition> props) {
    props.each { prop ->

      //property level - check if the group contains already a mapping for the current property
      def propertyMap = result.get(genericOIDService.getOID(prop))
      if(propertyMap == null) {
        propertyMap = [:]
      }
      List propertyList = propertyMap.get(cmpObject)
      if(propertyList == null) {
        propertyList = [prop]
      }
      else {
        propertyList.add(prop)
      }

      propertyMap.put(cmpObject,propertyList)
      result.put(genericOIDService.getOID(prop),propertyMap)
    }
    result
  }

  Map comparePropertiesWithAudit(Collection<AbstractPropertyWithCalculatedLastUpdated> props, boolean compareValue, boolean compareNote) {

    Map result = [:]

    props.sort{it.type.getI10n('name')}.each { prop ->

      //Vererbung
      if(AuditConfig.getConfig(prop)) {

        List propertyList = result.get(genericOIDService.getOID(prop.type))
        if (propertyList == null) {
          propertyList = [prop]
        } else {
          propertyList.add(prop)
        }
        result.put(genericOIDService.getOID(prop.type), propertyList)
        if (propertyList.size() == 2){
          if((compareValue && propertyList[0].getValue() != propertyList[1].getValue()) || (compareNote && propertyList[0].note != propertyList[1].note) ) {
          }else{
            result.remove(genericOIDService.getOID(prop.type))
          }
        }
      }
    }
    result
  }

  /**
   * Builds from a given {@link List} a {@link Map} of {@link TitleInstancePackagePlatform}s to compare the {@link Subscription}s of each {@link IssueEntitlement}
   *
   * @param lists - the unified list of {@link IssueEntitlement}s
   * @return the {@link Map} containing each {@link TitleInstancePackagePlatform} with the {@link Subscription}s containing the entitlements
   */
    Map buildTIPPComparisonMap(List<IssueEntitlement> lists) {
      Map<TitleInstancePackagePlatform, Set<Long>> result = [:]
      lists.each { ie ->
        Set<Subscription> subscriptionsContaining = result.get(ie.tipp)
        if(!subscriptionsContaining) {
          subscriptionsContaining = []
        }
        subscriptionsContaining << ie.subscription.id
        result[ie.tipp] = subscriptionsContaining
      }
      result
    }

  /**
   * COPY from ComparisonService with small changes
   * Builds into the grouped properties return map the given group key and binding for the given object.
   *
   * @param groupedProperties - the return map groupedProperties. Please check if it is really necessary to reassign again and again the whole map.
   * @param groupKey
   * @param groupBinding
   * @param cmpObject
   * @return
   */
  Map getGroupedPropertyTreesSortedAndAllowed(Map groupedProperties, PropertyDefinitionGroup groupKey, PropertyDefinitionGroupBinding groupBinding, cmpObject, Org contextOrg) {
    //get the current properties within each group for each object
    ArrayList<AbstractPropertyWithCalculatedLastUpdated> properties = groupKey.getCurrentProperties(cmpObject)
    LinkedHashMap group = (LinkedHashMap) groupedProperties.get(groupKey)
    if(properties.size() > 0) {
      List allowedProperties = properties.findAll {prop -> (prop.tenant?.id == contextOrg.id || !prop.tenant) || prop.isPublic || (prop.hasProperty('instanceOf') && prop.instanceOf && AuditConfig.getConfig(prop.instanceOf))}
      if(group) {
        group.groupTree = buildComparisonTree(group.groupTree, cmpObject, allowedProperties)
        group.binding.put(cmpObject, groupBinding)
      }
      else if(!group) {
        TreeMap groupTree = new TreeMap()
        LinkedHashMap binding = new LinkedHashMap()
        binding.put(cmpObject,groupBinding)
        group = [groupTree: buildComparisonTree(groupTree, cmpObject, allowedProperties), binding:binding]
      }
      group.groupTree = group.groupTree.sort {genericOIDService.resolveOID(it.key).getI10n('name')}
    }
    group
  }
}
