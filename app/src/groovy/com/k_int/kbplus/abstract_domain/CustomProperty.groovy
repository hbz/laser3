package com.k_int.kbplus.abstract_domain

import de.laser.traits.AuditTrait

/**
 * Created by ioannis on 26/06/2014.
 * Custom properties must always follow the naming convention: Owner + CustomProperty, where owner is the
 * name of owner class and be under com.k_int.kbplus . For example LicenseCustomProperty , SubscriptionCustomProperty.
 * Relevant code in PropertyDefinition, createGenericProperty
 * For change notifications to work, the class containing the custom properties must have a hasMany named
 * customProperties. See PendingChangeController@120 (CustomPropertyChange)
 */

abstract class CustomProperty extends AbstractProperty /* implements AuditTrait */ {

    // AuditTrait
    // static controlledProperties = ['stringValue','intValue','decValue','refValue','note','dateValue']

}
