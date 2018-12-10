package com.k_int.kbplus.abstract_domain

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import de.laser.helper.DateUtil
import org.codehaus.groovy.grails.validation.routines.UrlValidator
import org.codehaus.groovy.grails.web.util.StreamCharBuffer

import javax.persistence.Transient

abstract class AbstractProperty implements Serializable {

    String           stringValue
    Integer          intValue
    BigDecimal       decValue
    RefdataValue     refValue
    URL              urlValue
    String           note = ""
    Date             dateValue

    static mapping = {
        stringValue  type: 'text'
        note         type: 'text'
    }

    static constraints = {
        stringValue (nullable: true)
        intValue    (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)
    }

    @Transient
    def getValueType(){
        if(stringValue)
            return "stringValue"
        if(intValue)
            return "intValue"
        if(decValue)
            return "decValue"
        if(refValue)
            return "refValue"
        if(dateValue)
            return "dateValue"
        if(urlValue)
            return "urlValue"
    }

    public String getValue() {
        return toString()
    }

    @Override
    public String toString(){
        if(stringValue)
            return stringValue
        if(intValue != null)
            return intValue.toString()
        if(decValue != null)
            return decValue.toString()
        if(refValue)
            return refValue.toString()
        if(dateValue)
            return dateValue.getDateString()
        if(urlValue)
            return urlValue.toString()
    }

    def copyInto(AbstractProperty newProp){
        if(stringValue)
            newProp.stringValue = stringValue
        else if(intValue != null)
            newProp.intValue = intValue.toInteger()
        else if(decValue != null)
            newProp.decValue = decValue
        else if(refValue)
            newProp.refValue = refValue
        else if(dateValue)
            newProp.dateValue = dateValue
        else if(urlValue)
            newProp.urlValue = urlValue

        newProp.note = note
        newProp
    }

    def static parseValue(value, type){
        def result
        
        switch (type){
            case Integer.toString():
                result = Integer.parseInt(value)
                break
            case String.toString():
                result = value
                break
            case BigDecimal.toString():
                result = new BigDecimal(value)
                break
            case org.codehaus.groovy.runtime.NullObject.toString():
                result = null
                break
            case Date.toString():
                result = DateUtil.toDate_NoTime(value)
                break
            case URL.toString():
                result = new URL(value)
                break
            default:
                result = "AbstractProperty.parseValue failed"
        }
        return result
    }

    def setValue(value, type, rdc) {

        if (type == Integer.toString()) {
            intValue = parseValue(value, type)
        }
        else if (type == BigDecimal.toString()) {
            decValue = parseValue(value, type)
        }
        else if (type == String.toString()) {
            stringValue = parseValue(value, type)
        }
        else if (type == Date.toString()) {
            dateValue = parseValue(value, type)
        }
        else if (type == RefdataValue.toString()) {
            refValue = RefdataValue.findByOwnerAndValue(RefdataCategory.findByDesc(rdc), value.toString())
        }
            //TODO Validator einfügen
        else if (type == URL.toString()) {
//            UrlValidator validaor = new UrlValidator()
//            if (validaor.isValid(value)) {
                urlValue = parseValue(value, type)
//            } else {
//                throw new Exception("URL is invalid")
//            }
        }
    }
}
