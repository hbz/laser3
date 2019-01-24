package com.k_int.kbplus

import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

class GenericOIDService {

  GrailsApplication grailsApplication

  def resolveOID(oid) {

    def result = null;

    if ( oid != null ) {
      def oid_components = oid.toString().split(':');
  
      GrailsClass domain_class
  
      if ( oid_components[0].startsWith("com.k_int") ) {
        domain_class = grailsApplication.getArtefact('Domain', oid_components[0])
      }
      else if ( oid_components[0].startsWith("de.laser") ) {
        domain_class = grailsApplication.getArtefact('Domain', oid_components[0])
      }
      else {
        domain_class = grailsApplication.getArtefact('Domain', "com.k_int.kbplus.${oid_components[0]}")
      }
  
      if ( domain_class ) {
        result = domain_class.getClazz().get(oid_components[1])
        // log.debug("oid ${oid} resolved to ${result}")
      }
      else {
        log.error("resolve OID failed to identify a domain class. Input was ${oid_components}");
      }
    }
    GrailsHibernateUtil.unwrapIfProxy(result)
  }

  String getOID(def object) {
    if (object) {
      return "${object.class.name}:${object.id}"
    }
    null
  }
}
