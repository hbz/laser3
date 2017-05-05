package com.k_int.kbplus

import java.util.Date
import java.util.List

import groovy.util.logging.Log4j

import org.apache.commons.logging.LogFactory

import groovy.util.logging.*

@Log4j
class Person {

    String       first_name
    String       middle_name
    String       last_name
    RefdataValue gender
    Org          owner
    RefdataValue isPublic // 'YN'
    
    static mapping = {
        id          column:'prs_id'
        version     column:'prs_version'
        first_name  column:'prs_first_name'
        middle_name column:'prs_middle_name'
        last_name   column:'prs_last_name'
        gender      column:'prs_gender'
        owner       column:'prs_owner_fk'
        isPublic    column:'prs_is_public_rdv_fk'
    }
    
    static mappedBy = [
        roleLinks: 'prs',
        addresses: 'prs',
        contacts:  'prs'
    ]
  
    static hasMany = [
        roleLinks: PersonRole,
        addresses: Address,
        contacts:  Contact
    ]
    
    static constraints = {
        first_name  (nullable:false, blank:false)
        middle_name (nullable:true,  blank:true)
        last_name   (nullable:false, blank:false)
        gender      (nullable:true)
        owner       (nullable:true)
        isPublic    (nullable:true)
    }
    
    static getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    @Override
    String toString() {
        last_name + ', ' + first_name + ' ' + middle_name + ' (' + id + ')'
    }
        
    static def lookupOrCreate(firstName, middleName, lastName, gender, org, roleType) {
        
        def resultPerson = null
        def resultPersonRole = null
        
        def hqlFirstName  = Person.hqlHelper(firstName)
        def hqlMiddleName = Person.hqlHelper(middleName)
        def hqlLastName   = Person.hqlHelper(lastName)
        def hqlGender     = Person.hqlHelper(gender)
        def hqlOrg        = Person.hqlHelper(org)
        def hqlRoleType   = Person.hqlHelper(roleType)
               
        def query = "select p from Person p, PersonRole pr where p = pr.prs "
        query = query + "and pr.org ${hqlOrg[1]} and pr.roleType ${hqlRoleType[1]} "
        query = query + "and lower(p.first_name) ${hqlFirstName[1]} and lower(p.middle_name) ${hqlMiddleName[1]} and lower(p.last_name) ${hqlLastName[1]}"

        def queryParams = [hqlOrg[0], hqlRoleType[0], hqlFirstName[0].toLowerCase(), hqlMiddleName[0].toLowerCase(), hqlLastName[0].toLowerCase()]
        queryParams.removeAll([null, ''])
        
        log.debug(query)
        log.debug('@ ' + queryParams)
        
        def p = Person.executeQuery(query, queryParams)
        
        if(!p){
            LogFactory.getLog(this).debug("trying to save new person: ${firstName} ${middleName} ${lastName}")
            
            resultPerson = new Person(
                first_name:  firstName,
                middle_name: middleName,
                last_name:   lastName,
                gender:      gender
                )
                
            if(!resultPerson.save()){
                resultPerson.errors.each{ println it }
            }
            
            if(resultPerson && org && roleType){
                LogFactory.getLog(this).debug("trying to save new personRole: ${resultPerson} - ${roleType} - ${org}")
                
                resultPersonRole = new PersonRole(
                    roleType:   roleType,
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
                    
                if(!resultPersonRole.save()){
                    resultPersonRole.errors.each{ println it }
                }
            }
        }
        else {
            // TODO catch multiple results
            if(p.size() > 0){
                resultPerson = p[0]
            }
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
}
