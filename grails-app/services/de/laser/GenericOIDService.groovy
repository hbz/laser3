package de.laser

import de.laser.utils.CodeUtils
import grails.gorm.transactions.Transactional
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This service is for resolving objects from their OID key
 */
@Transactional
class GenericOIDService {

  /**
   * Gets the OID representation of the given object
   * @param object the object whose OID should be generated
   * @return the OID key of the object
   */
  String getOID(def object) {
    (object && DomainClassArtefactHandler.isDomainClass(object.class)) ? "${object.class.name}:${object.id}" : null
  }

  /**
   * Resolves the object behind the given OID key
   * @param oid the OID key which should be resolved
   * @return the object behind the OID
   */
  Object resolveOID(def oid) {
    def result

    if (oid) {
      String[] parts = oid.toString().split(':')
      String domainClass = parts[0].trim()
      Class cls = CodeUtils.getDomainClass(domainClass) ?: CodeUtils.getDomainClassFallback(domainClass)
      if (cls)  {
        result = cls.get(parts[1].trim())
      }
      else {
        log.error("failed to resolveOID() for: ${oid}")
      }
    }
    GrailsHibernateUtil.unwrapIfProxy(result)
  }

  /**
   * Gets a list of oid-text maps for dropdown display
   * @param objList the objects which should figure in the dropdown
   * @param property the property to be used as label
   * @return a map of structure {id: oid, text: display label} for use in dropdowns
   */
  List<Map<String, Object>> getOIDMapList(List<Object> objList, String property) {
    List<Map<String, Object>> result = []

    if (!objList.isEmpty()) {
      Object tmp = objList.get(0)
      if (! DomainClassArtefactHandler.isDomainClass(tmp.class)) {
        log.warn("WARNING: GenericOIDService.getOIDMapList() -> ${tmp.class.name} is not a domain class")
      }
      else if (! tmp.metaClass.hasProperty(property)) {
        log.warn("WARNING: GenericOIDService.getOIDMapList() -> ${tmp.class.name} has no property '${property}'")
      }
      else {
        objList.each { obj ->
          result.add([id: "${obj.class.name}:${obj.id}", text: "${obj.getProperty(property)}"])
        }
      }
    }
    result
  }
}
