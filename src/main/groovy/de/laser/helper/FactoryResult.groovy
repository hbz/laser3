package de.laser.helper

import grails.web.mvc.FlashScope
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder


public class FactoryResult {
    Object result = null
    List existingDuplicates = []
    List<String> status = []

    public static final String STATUS_OK = 'STATUS_OK'
    public static final String STATUS_ERR = 'STATUS_ERR'
    public static final String STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_REFERENCE_OBJ = 'STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_REFERENCE_OBJ'
    public static final String STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ = 'STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ'
    public static final String STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_SYSTEM = 'STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_SYSTEM'

//    public FactoryResult(def result, List existingDuplicates, String status){
//        this.result = result
//        this.existingDuplicates = existingDuplicates
//        this.status += status
//    }
//    public FactoryResult(def result){
//        new FactoryResult(result, null, STATUS_OK)
//    }

    void setFlashScopeByStatus(FlashScope flash) {
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleContextHolder.getLocale()

        Object[] args = [result?.ns?.ns, result?.value]

        flash.message = flash.message != null  ? flash.message : ""
        flash.error   = flash.error != null    ? flash.error : ""

        status.each {
            switch (it) {
                case FactoryResult.STATUS_OK:
                    flash.message += messageSource.getMessage('identifier.create.success', args, locale)
                    break;
                case FactoryResult.STATUS_ERR:
                    flash.error += messageSource.getMessage('identifier.create.err', args, locale)
                    break;
                case FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_REFERENCE_OBJ:
                    flash.error += messageSource.getMessage('identifier.create.err.alreadyExist', args, locale)
                    break;
                case FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_SEVERAL_EXIST_IN_REFERENCE_OBJ:
                    flash.error += messageSource.getMessage('identifier.create.warn.alreadyExistSeveralTimes', args, locale)
                    break;
                case FactoryResult.STATUS_ERR_UNIQUE_BUT_ALREADY_EXISTS_IN_SYSTEM:
                    flash.error += messageSource.getMessage('identifier.create.err.uniqueNs', args, locale)
                    break;
                default:
                    flash.error += status
            }
        }
    }
}
