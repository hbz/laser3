package com.k_int.kbplus

import de.laser.helper.AppUtils
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class GenericOIDService {

  static String getOID(def object) {
    (object && DomainClassArtefactHandler.isDomainClass(object.class)) ? "${object.class.name}:${object.id}" : null
  }

  Object resolveOID(String oid) {
    def result

    if (oid) {
      String[] parts = oid.split(':')

      GrailsClass dc = AppUtils.getDomainClass(parts[0])
      if (! dc) {
        dc = AppUtils.getDomainClassGeneric(parts[0])
      }
      if (dc)  {
        result = dc.getClazz().get(parts[1])
      }
      else {
        log.error("failed to resolveOID() for: ${oid}")
      }
    }
    GrailsHibernateUtil.unwrapIfProxy(result)
  }

  List<Map<String, Object>> getOIDMapList(List<Object> objList, String property) {
    List<Map<String, Object>> result = []

    if (!objList.isEmpty()) {
      Object tmp = objList.get(0)
      if (! DomainClassArtefactHandler.isDomainClass(tmp.class)) {
        println "WARNING: GenericOIDService.getOIDMapList() -> ${tmp.class.name} is not a domain class"
      }
      else if (! tmp.metaClass.hasProperty(property)) {
        println "WARNING: GenericOIDService.getOIDMapList() -> ${tmp.class.name} has no property '${property}'"
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
