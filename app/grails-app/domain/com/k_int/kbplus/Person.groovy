package com.k_int.kbplus

import de.laser.domain.BaseDomainComponent

import java.util.Date
import java.util.List

import javax.persistence.Transient
import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import com.sun.org.apache.xalan.internal.xsltc.compiler.Sort
import groovy.util.logging.*

@Log4j
class Person extends BaseDomainComponent {

    String       title
    String       first_name
    String       middle_name
    String       last_name
    RefdataValue gender         // RefdataCategory 'Gender'
    Org          tenant
    RefdataValue isPublic       // RefdataCategory 'YN'
    RefdataValue contactType    // RefdataCategory 'Person Contact Type'
    RefdataValue roleType       // RefdataCategory 'Person Position'

    static mapping = {
        id              column:'prs_id'
        globalUID       column:'prs_guid'
        version         column:'prs_version'
        title           column:'prs_title'
        first_name      column:'prs_first_name'
        middle_name     column:'prs_middle_name'
        last_name       column:'prs_last_name'
        gender          column:'prs_gender_rv_fk'
        tenant          column:'prs_tenant_fk'
        isPublic        column:'prs_is_public_rv_fk'
        contactType     column:'prs_contact_type_rv_fk'
        roleType        column:'prs_role_type_rv_fk'

        roleLinks   cascade: 'all'
        addresses   cascade: 'all', lazy: false
        contacts    cascade: 'all', lazy: false
        privateProperties   cascade: 'all'
    }
    
    static mappedBy = [
        roleLinks:          'prs',
        addresses:          'prs',
        contacts:           'prs',
        privateProperties:  'owner'
    ]
  
    static hasMany = [
        roleLinks: PersonRole,
        addresses: Address,
        contacts:  Contact,
        privateProperties: PersonPrivateProperty
    ]
    
    static constraints = {
        globalUID   (nullable:true,  blank:false, unique:true, maxSize:255)
        title       (nullable:true,  blank:false)
        first_name  (nullable:true,  blank:false)
        middle_name (nullable:true,  blank:false)
        last_name   (nullable:false, blank:false)
        gender      (nullable:true)
        tenant      (nullable:true)
        isPublic    (nullable:true)
        contactType (nullable:true)
        roleType    (nullable:true)
    }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    @Override
    String toString() {
        ((title ?: '') + ' ' + (last_name ?: ' ') + (first_name ? ', ' + first_name : '') + ' ' + (middle_name ?: '')).trim()
    }

    static def lookup(firstName, lastName, tenant, isPublic, contactType) {

        def person
        def prsList = Person.findAllWhere(
                first_name: firstName,
                last_name: lastName,
                contactType: contactType,
                isPublic: isPublic,
                tenant: tenant,
        )
        if ( prsList.size() > 0 ) {
            person = prsList.get(0)
        }
        person
    }

    static def lookup(firstName, lastName, tenant, isPublic, contactType, org, functionType) {

        def person
        def prsList = []

        Person.findAllWhere(
                first_name: firstName,
                last_name: lastName,
                contactType: contactType,
                isPublic: isPublic,
                tenant: tenant
        ).each{ p ->
            if (PersonRole.findWhere(prs: p, functionType: functionType, org: org)) {
                prsList << p
            }
        }
        if ( prsList.size() > 0 ) {
            person = prsList.get(0)
        }
        person
    }

    static def getPublicByOrgAndFunc(Org org, String func) {
        def result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic.value != 'No' and pr.org = ? and pr.functionType.value = ?",
                [org, func]
        )
        result
    }

    static def getPublicByOrgAndObjectResp(Org org, def obj, String resp) {
        def q = ''
        def p = ['org': org, 'resp': resp]

        if (obj instanceof License) {
            q = ' and pr.lic = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Cluster) {
            q = ' and pr.cluster = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Package) {
            q = ' and pr.pkg = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Subscription) {
            q = ' and pr.sub = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof TitleInstance) {
            q = ' and pr.title = :obj '
            p << ['obj': obj]
        }

        def result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic.value != 'No' and pr.org = :org and pr.responsibilityType.value = :resp " + q,
                p
        )
        result
    }

    static def getPrivateByOrgAndFuncFromAddressbook(Org org, String func, Org tenant) {
        def result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic.value = 'No' and pr.org = ? and pr.functionType.value = ? and p.tenant = ?",
                [org, func, tenant]
        )
        result
    }

    static def getPrivateByOrgAndObjectRespFromAddressbook(Org org, def obj, String resp, Org tenant) {
        def q = ''
        def p = ['org': org, 'resp': resp, 'tnt': tenant]

        if (obj instanceof License) {
            q = ' and pr.lic = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Cluster) {
            q = ' and pr.cluster = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Package) {
            q = ' and pr.pkg = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Subscription) {
            q = ' and pr.sub = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof TitleInstance) {
            q = ' and pr.title = :obj '
            p << ['obj': obj]
        }

        def result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic.value = 'No' and pr.org = :org and pr.responsibilityType.value = :resp and p.tenant = :tnt " + q,
                p
        )
        result
    }

    /**
     *
     * @param obj
     * @return list with two elements for building hql query
     */
    static List hqlHelper(obj){
        
        def result = []
        result.add(obj ? obj : '')
        result.add(obj ? '= ?' : 'is null')
        
        result
    }
    
    /*
    @Transient
    def getCustomPropByName(name){
      return privateProperties.find{it.type.name == name}
    }*/
}
