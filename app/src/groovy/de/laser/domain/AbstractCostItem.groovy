package de.laser.domain

import com.k_int.kbplus.RefdataValue
import de.laser.helper.RefdataAnnotation

abstract class AbstractCostItem extends AbstractBaseDomain {

    @RefdataAnnotation(cat = 'Currency')
    RefdataValue billingCurrency

    Double costInBillingCurrency   //The actual amount - new cost ex tax
    Double costInLocalCurrency     //local amount entered

}
