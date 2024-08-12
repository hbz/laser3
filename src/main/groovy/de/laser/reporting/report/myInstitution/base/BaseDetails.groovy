package de.laser.reporting.report.myInstitution.base

import de.laser.License
import de.laser.Org
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.wekb.Vendor
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.properties.PropertyDefinition
import de.laser.properties.OrgProperty
import de.laser.reporting.report.GenericHelper
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * This class contains general methods for retrieving base details valid for each report
 */
class BaseDetails {

    /**
     * Retrieves the property values for the given object and context institution
     * @param obj the object where the property has been defined
     * @param pdId the property ID whose value should be retrieved
     * @param ctxOrg the context institution ({@link Org}) whose property is being queried
     * @return a list of stringified property values
     * @see PropertyDefinition
     */
    static List<String> resolvePropertiesGeneric(Object obj, Long pdId, Org ctxOrg) {

        getPropertiesGeneric(obj, pdId, ctxOrg).collect { prop ->
            if (prop.getType().isRefdataValueType()) {
                if (prop.getRefValue()) {
                    prop.getRefValue()?.getI10n('value')
                }
            } else {
                prop.getValue()
            }
        }.sort()
    }

    /**
     * Gets the properties of the given type (property definition)
     * @param obj the object (one of {@link License}, {@link Org}, {@link Platform} or {@link Subscription})
     * @param pdId the ID of the {@link PropertyDefinition} of the properties being requested
     * @param ctxOrg the tenant institution of the (private) properties
     * @return a list of matching properties
     * @see PropertyDefinition
     */
    static List<AbstractPropertyWithCalculatedLastUpdated> getPropertiesGeneric(Object obj, Long pdId, Org ctxOrg) {

        List<AbstractPropertyWithCalculatedLastUpdated> properties = []
        String xp = ''

             if (obj instanceof License)        { xp = 'LicenseProperty' }
        else if (obj instanceof Org)            { xp = 'OrgProperty' }
        else if (obj instanceof Platform)       { xp = 'PlatformProperty' }
        else if (obj instanceof Provider)       { xp = 'ProviderProperty' }
        else if (obj instanceof Subscription)   { xp = 'SubscriptionProperty' }
        else if (obj instanceof Vendor)         { xp = 'VendorProperty' }

        if (xp) {
            properties = OrgProperty.executeQuery(
                    "select xp from " + xp + " xp join xp.type pd "
                        + "where xp.owner = :obj and (xp.isPublic = true or xp.tenant = :ctxOrg) "
                        + "and pd.id = :pdId and pd.descr like '%Property' ",
                    [obj: obj, pdId: pdId, ctxOrg: ctxOrg]
            )
        }
        properties
    }

    /**
     * Reverses the fields in the columns for the display
     * @param fields the map of fields to revers
     * @param columns the count of columns
     * @return the reversed map of fields
     */
    static List<Map<String, Object>> reorderFieldsInColumnsForUI(Map<String, Object> fields, int columns) {

        List<Map<String, Object>> result = []
        int cols = Math.round(fields.size() / columns) as int

        for (int i=0; i<columns; i++) {
            result.add( fields.take(cols) )
            fields = fields.drop(cols)
        }
        // println result

        result
    }

    /**
     * Gets the field label associated to the given column
     * @param key the column for which the label should be retrieved
     * @param field the field of the column
     * @return the associated label
     */
    static String getFieldLabelforColumn(String key, String field) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()
        Map<String, Map> esdConfig = BaseConfig.getCurrentConfigElasticsearchData(key)

        // println ' > BaseDetails.getFieldLabelforColumn() ' + key + ' + ' + field
        String label = field

        if (field.startsWith( key + '-' )) {
            label = g.message(code: esdConfig.get(field).get('label')) ?: ( field + ' *' )
        }
        else {
            label = GenericHelper.getFieldLabel( BaseConfig.getCurrentConfig( key ).base as Map, field)
            if (label == '?') {
                String code = key + '.' + field + '.label'
                label = g.message(code: code)

                if (label == code) {
                    if (field == '_dtField_lastUpdated') {
                        label = g.message(code: 'default.lastUpdated.label')
                    }
                    else if (field == '_dtField_wekb') {
                        label = g.message(code: 'wekb')
                    }
                    else if (field == '_dtField_currentTitles') {
                        label = g.message(code: 'package.show.nav.current')
                    }
                    else if (field == '_dtField_?_propertyLocal') {
                        label = g.message(code: 'reporting.details.property.value')
                        //label = BaseDetailsExport.getMessage('x-property')
                    }
                    else {
                        println 'label != code - [ ' + key + ' -<>- ' + field + ' ]'
                    }
                }
            }
        }
        label
    }
}
