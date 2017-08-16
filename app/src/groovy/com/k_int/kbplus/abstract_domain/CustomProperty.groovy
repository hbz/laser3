package com.k_int.kbplus.abstract_domain

/**
 * Created by ioannis on 26/06/2014.
 * Custom properties must always follow the naming convention: Owner + CustomProperty, where owner is the
 * name of owner class and be under com.k_int.kbplus . For example LicenseCustomProperty , SubscriptionCustomProperty.
 * Relevant code in PropertyDefinition, createCustomPropertyValue
 * For change notifications to work, the class containing the custom properties must have a hasMany named
 * customProperties. See PendingChangeController@120 (CustomPropertyChange)
 */

abstract class CustomProperty extends AbstractProperty {
}
