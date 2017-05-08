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
    RefdataValue gender     // RefdataCategory 'Gender'
    Org          owner
    RefdataValue isPublic   // RefdataCategory 'YN'
    
    static mapping = {
        id          column:'prs_id'
        version     column:'prs_version'
        first_name  column:'prs_first_name'
        middle_name column:'prs_middle_name'
        last_name   column:'prs_last_name'
        gender      column:'prs_gender_rv_fk'
        owner       column:'prs_owner_fk'
        isPublic    column:'prs_is_public_rv_fk'
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
        
    // TODO implement existing check (lookup)
    // TODO implement responsibilityType
    static def customCreate(firstName, middleName, lastName, gender, owner, isPublic, org, functionType) {
        
        LogFactory.getLog(this).debug("trying to save new person: ${firstName} ${middleName} ${lastName}")
        def resultPerson = null
        def resultPersonRole = null
 
        resultPerson = new Person(
            first_name:  firstName,
            middle_name: middleName,
            last_name:   lastName,
            gender:      gender,
            owner:       owner,
            isPublic:    isPublic
            )
            
        if(!resultPerson.save()){
            resultPerson.errors.each{ println it }
        }
        
        if(resultPerson){
            LogFactory.getLog(this).debug("trying to save new personRole: ${resultPerson} - ${functionType} - ${org}")
            
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
                
            if(!resultPersonRole.save()){
                resultPersonRole.errors.each{ println it }
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
