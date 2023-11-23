package de.laser

import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils

import java.text.SimpleDateFormat

/**
 * This class keeps track of permanently purchased titles. It holds
 * <ul>
 *     <li>the institution ({@link Org}) having subscribed the title</li>
 *     <li>the issue entitlement being purchased</li>
 *     <li>the underlying title instance of the sales unit</li>
 *     <li>the subscription by which the purchase has been concluded</li>
 * </ul>
 * Purpose of this linking table is the marking of titles which are already permanently in stock if the given institution
 * and thus a double purchase should be avoided
 * @see Subscription
 * @see IssueEntitlement
 * @see TitleInstancePackagePlatform
 */
class PermanentTitle {

    Org owner
    Subscription subscription
    IssueEntitlement issueEntitlement
    TitleInstancePackagePlatform tipp

    Date dateCreated
    Date lastUpdated

    static constraints = {
        owner(unique: ['tipp'])
    }

    static mapping = {
        id column: 'pt_id'
        version column: 'pt_version'

        dateCreated column: 'pt_date_created'
        lastUpdated column: 'pt_last_updated'

        owner column: 'pt_owner_fk', index: 'pt_owner_idx'
        subscription column: 'pt_subscription_fk', index: 'pt_subscription_idx'
        issueEntitlement column: 'pt_ie_fk', index: 'pt_ie_idx'
        tipp column: 'pt_tipp_fk', index: 'pt_tipp_idx'
    }

    String getPermanentTitleInfo(Org contextOrg){
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String period = subscription.startDate ? sdf.format(subscription.startDate)  : ''

        period = subscription.endDate ? period + ' - ' + sdf.format(subscription.endDate)  : ''

        period = period ? '('+period+')' : ''

        String statusString = subscription.status ? subscription.status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        Org consortia = subscription.getConsortia()

        if(consortia && consortia != contextOrg){
            return subscription.name + ' - ' + statusString + ' ' +period + ' - ' + " (${subscription.getConsortia()?.name})"

        } else {

            return subscription.name + ' - ' + statusString + ' ' +period
        }
    }
}
