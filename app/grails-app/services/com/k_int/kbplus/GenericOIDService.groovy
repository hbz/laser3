package com.k_int.kbplus

import de.laser.helper.AppUtils
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClass
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class GenericOIDService {

  GrailsApplication grailsApplication

  def resolveOID(oid) {

    def result = null;

    if ( oid != null ) {
      def oid_components = oid.toString().split(':');
  
      GrailsClass domain_class = AppUtils.getDomainClass( oid_components[0] )

      if (! domain_class) {
        domain_class = AppUtils.getDomainClassGeneric( oid_components[0] )
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

  static String getOID(def object) {
    if (object) {
      return "${object.class.name}:${object.id}"
    }
    null
  }
}
