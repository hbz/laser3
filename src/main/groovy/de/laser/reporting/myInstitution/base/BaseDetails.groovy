package de.laser.reporting.myInstitution.base

import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.SubscriptionProperty
import de.laser.reporting.ReportingCache

class BaseDetails {

    static Map<String, Object> getDetailsCache(String token) {
        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, token )
        rCache.readDetailsCache()
    }

    // ----- ----- -----

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

    static List<AbstractPropertyWithCalculatedLastUpdated> getPropertiesGeneric(Object obj, Long pdId, Org ctxOrg) {

        List<AbstractPropertyWithCalculatedLastUpdated> properties = []

        if (obj instanceof Subscription) {
            properties = SubscriptionProperty.executeQuery(
                    "select sp from SubscriptionProperty sp join sp.type pd where sp.owner = :sub and pd.id = :pdId " +
                            "and (sp.isPublic = true or sp.tenant = :ctxOrg) and pd.descr like '%Property' ",
                    [sub: obj, pdId: pdId, ctxOrg: ctxOrg]
            )
        }
        else if (obj instanceof License) {
            properties = LicenseProperty.executeQuery(
                    "select lp from LicenseProperty lp join lp.type pd where lp.owner = :lic and pd.id = :pdId " +
                            "and (lp.isPublic = true or lp.tenant = :ctxOrg) and pd.descr like '%Property' ",
                    [lic: obj, pdId: pdId, ctxOrg: ctxOrg]
            )
        }
        else if (obj instanceof Org) {
            properties = OrgProperty.executeQuery(
                    "select op from OrgProperty op join op.type pd where op.owner = :org and pd.id = :pdId " +
                            "and (op.isPublic = true or op.tenant = :ctxOrg) and pd.descr like '%Property' ",
                    [org: obj, pdId: pdId, ctxOrg: ctxOrg]
            )
        }
        properties
    }
}
