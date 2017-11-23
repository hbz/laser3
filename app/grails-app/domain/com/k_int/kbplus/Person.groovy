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

    String       first_name
    String       middle_name
    String       last_name
    RefdataValue gender     // RefdataCategory 'Gender'
    Org          tenant
    RefdataValue isPublic       // RefdataCategory 'YN'
    RefdataValue contactType    // RefdataCategory 'Person Contact Type'
    RefdataValue roleType       // RefdataCategory 'Person Position'

    static mapping = {
        id              column:'prs_id'
        globalUID       column:'prs_guid'
        version         column:'prs_version'
        first_name      column:'prs_first_name'
        middle_name     column:'prs_middle_name'
        last_name       column:'prs_last_name'
        gender          column:'prs_gender_rv_fk'
        tenant          column:'prs_tenant_fk'
        isPublic        column:'prs_is_public_rv_fk'
        contactType     column:'prs_contact_type_rv_fk'
        roleType        column:'prs_role_type_rv_fk'
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
        first_name  (nullable:false, blank:false)
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
        last_name + ', ' + first_name + ' ' + middle_name + ' (' + id + ')'
    }
    
    // TODO implement existing check (lookup)
    // TODO implement responsibilityType
    static def lookupOrCreateWithPersonRole(firstName, middleName, lastName, gender, tenant, isPublic, org, functionType) {
        
        def info = "saving new person: ${firstName} ${middleName} ${lastName}"
        def resultPerson = null
        def resultPersonRole = null
 
        // TODO: ugly mapping fallback
        if (middleName=='')
            middleName = null
            
        def check = Person.findAllWhere(
            first_name:  firstName,
            middle_name: middleName,
            last_name:   lastName,
            gender:      gender,
            tenant:      tenant,
            isPublic:    isPublic, 
            ).sort({id: 'asc'})
            
        if (check.size()>0) {
            resultPerson = check.get(0)
            info += " > ignored/duplicate"
        }
        else{
            resultPerson = new Person(
                first_name:  firstName,
                middle_name: middleName,
                last_name:   lastName,
                gender:      gender,
                tenant:      tenant,
                isPublic:    isPublic
                )
                
            if (!resultPerson.save()) {
                resultPerson.errors.each{ println it }
            }
            else {
                info += " > ok"
            }
        }
        LogFactory.getLog(this).debug(info)
        
        if (resultPerson) {
            info = "saving new personRole: ${resultPerson} - ${functionType} - ${org}"
            
            check = PersonRole.findAllWhere(
                functionType:   functionType,
                prs:        resultPerson,
                lic:        null,
                org:        org,
                cluster:    null,
                pkg:        null,
                sub:        null,
                title:      null,
                start_date: null,
                end_date:   null
                ).sort({id: 'asc'})
                
            if (check.size()>0) {
                resultPersonRole = check.get(0)
                info += " > ignored/duplicate"
            }
            else {
                resultPersonRole = new PersonRole(
                    functionType:   functionType,
                    prs:        resultPerson,
                    lic:        null,
                    org:        org,
                    cluster:    null,
                    pkg:        null,
                    sub:        null,
                    title:      null,
                    start_date: null,
                    end_date:   null
                    )
                    
                if (!resultPersonRole.save()) {
                    resultPersonRole.errors.each{ println it }
                }
                else {
                    info += " > ok"
                }
            }
            LogFactory.getLog(this).debug(info)
        }
        resultPerson      
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
