package com.k_int.kbplus

import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import groovy.util.logging.*

@Log4j
class Person {

    String       first_name
    String       middle_name
    String       last_name
    RefdataValue gender
    
    static mapping = {
        id          column:'prs_id'
        version     column:'prs_version'
        first_name  column:'prs_first_name'
        middle_name column:'prs_middle_name'
        last_name   column:'prs_last_name'
        gender      column:'prs_gender'
    }
    
    static mappedBy = [
        roleLinks: 'prs',
        contacts:  'prs'
    ]
  
    static hasMany = [
        roleLinks: PersonRole,
        contacts:  Contact
    ]
    
    static constraints = {
        first_name  (nullable:false, blank:false)
        middle_name (nullable:true,  blank:true)
        last_name   (nullable:false, blank:false)
        gender      (nullable:true)
    }
    
    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('Gender')
    }
    
    @Override
    String toString() {
        last_name + ', ' + first_name + ' ' + middle_name + ' (' + id + ')'
    }
    
    static def lookupOrCreate(firstName, middleName, lastName, gender) {
        
        def result = null
        
        firstName  = firstName  ? firstName  : ''
        middleName = middleName ? middleName : ''
        lastName   = lastName   ? lastName   : ''
        gender     = gender     ? gender : null // TODO
        
        def p
        if(middleName != ''){
            p = Person.executeQuery(
                "from Person p where lower(p.first_name) = ? and lower(p.middle_name) = ? and lower(p.last_name) = ?", 
                [firstName.toLowerCase(), middleName.toLowerCase(), lastName.toLowerCase()]
            )
        }
        else{
            p = Person.executeQuery(
                "from Person p where lower(p.first_name) = ? and lower(p.last_name) = ?",
                [firstName.toLowerCase(), lastName.toLowerCase()]
            )
        }
        if(!p){
            LogFactory.getLog(this).debug('trying to save new person')
            
            result = new Person(
                first_name:  firstName,
                middle_name: middleName,
                last_name:   lastName,
                gender:      gender
                )
                
            if(!result.save()){
                result.errors.each{ println it }
            }
        }
        else {
            result = p
        }
        
        result     
       
    }
}
