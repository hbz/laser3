package de.laser

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import grails.transaction.Transactional

@Transactional
class ComparisonService {

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
    ArrayList<AbstractProperty> licenseProps = groupKey.getCurrentProperties(cmpObject)
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
   * @param result - the map being filled or updated
   * @return the updated map
   */
    TreeMap buildComparisonTree(TreeMap result,cmpObject,Collection<AbstractProperty> props) {
      props.each { prop ->
        //property level - check if the group contains already a mapping for the current property
        def propertyMap = result.get(prop.type.class.name+":"+prop.type.id)
        if(propertyMap == null)
          propertyMap = [:]
        List propertyList = propertyMap.get(cmpObject)
        if(propertyList == null)
          propertyList = [prop]
        else propertyList.add(prop)
        propertyMap.put(cmpObject,propertyList)
        result.put(prop.type.class.name+":"+prop.type.id,propertyMap)
      }
      result
    }
}
