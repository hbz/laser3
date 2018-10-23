package de.laser.helper

import com.k_int.kbplus.RefdataValue

class RDStore {

    static final OR_LICENSING_CONSORTIUM = RefdataValue.getByValueAndCategory('Licensing Consortium', 'Organisational Role')

    static final OR_LICENSEE = RefdataValue.getByValueAndCategory('Licensee','Organisational Role')

    static final OR_LICENSEE_CONS = RefdataValue.getByValueAndCategory('Licensee_Consortial','Organisational Role')


    static final OR_SUBSCRIPTION_CONSORTIA = RefdataValue.getByValueAndCategory('Subscription Consortia','Organisational Role')

    static final OR_SUBSCRIBER = RefdataValue.getByValueAndCategory('Subscriber','Organisational Role')

    static final OR_SUBSCRIBER_CONS = RefdataValue.getByValueAndCategory('Subscriber_Consortial','Organisational Role')
}
