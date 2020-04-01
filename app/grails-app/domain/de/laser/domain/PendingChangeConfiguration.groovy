package de.laser.domain

import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SubscriptionPackage
import de.laser.exceptions.CreationException
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation
import org.springframework.context.MessageSource

import javax.persistence.Transient

class PendingChangeConfiguration {

    @Transient
    MessageSource messageSource

    static final String NEW_TITLE = "pendingChange.message_TP01"
    static final String TITLE_UPDATED = "pendingChange.message_TP02"
    static final String TITLE_DELETED = "pendingChange.message_TP03"
    static final String COVERAGE_UPDATED = "pendingChange.message_TC01"
    static final String NEW_COVERAGE = "pendingChange.message_TC02"
    static final String COVERAGE_DELETED = "pendingChange.message_TC03"
    static final String PACKAGE_PROP = "pendingChange.message_PK01"
    static final String PACKAGE_DELETED = "pendingChange.message_PK02"
    static final Set<String> settingKeys = [NEW_TITLE,TITLE_UPDATED,TITLE_DELETED,NEW_COVERAGE,COVERAGE_UPDATED,COVERAGE_DELETED,PACKAGE_PROP,PACKAGE_DELETED]

    String settingKey
    @RefdataAnnotation(cat = RDConstants.PENDING_CHANGE_CONFIG_SETTING)
    RefdataValue settingValue
    boolean withNotification = false

    static belongsTo = [subscriptionPackage: SubscriptionPackage]

    static mapping = {
        subscriptionPackage     column: 'pcc_sp_fk'
        settingKey              column: 'pcc_setting_key_enum'
        settingValue            column: 'pcc_setting_value_rv_fk'
        withNotification        column: 'pcc_with_information'
    }

    static constraints = {
        settingValue(nullable:true,blank:false)
    }

    static PendingChangeConfiguration construct(Map<String,Object> configMap) throws CreationException {
        if(configMap.subscriptionPackage instanceof SubscriptionPackage) {
            PendingChangeConfiguration pcc = findBySubscriptionPackageAndSettingKey((SubscriptionPackage) configMap.subscriptionPackage,configMap.settingKey)
            if(!pcc)
                pcc =  new PendingChangeConfiguration(subscriptionPackage: (SubscriptionPackage) configMap.subscriptionPackage,settingKey: configMap.settingKey)
            pcc.settingValue = configMap.settingValue
            pcc.withNotification = configMap.withNotification
            if(pcc.save())
                pcc
            else throw new CreationException("Error on saving pending change configuration: ${pcc.errors}")
        }
        else {
            throw new CreationException("Invalid subscription package object given: ${configMap.subscriptionPackage}")
        }
    }

}
