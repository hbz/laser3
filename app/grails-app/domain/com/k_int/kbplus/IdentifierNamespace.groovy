package com.k_int.kbplus

import javax.persistence.Transient

class IdentifierNamespace {

    @Transient
    public static final NS_ORGANISATION = "com.k_int.kbplus.Org"
    @Transient
    public static final NS_LICENSE      = "com.k_int.kbplus.License"
    @Transient
    public static final NS_SUBSCRIPTION = "com.k_int.kbplus.Subscription"
    @Transient
    public static final NS_PACKAGE      = "com.k_int.kbplus.Package"
    @Transient
    public static final NS_TITLE        = "com.k_int.kbplus.TitleInstance"
    @Transient
    public static final NS_CREATOR        = "com.k_int.kbplus.Creator"


    @Transient
    final static String[] AVAILABLE_NSTYPES = [
            NS_ORGANISATION,
            NS_LICENSE,
            NS_SUBSCRIPTION,
            NS_PACKAGE,
            NS_TITLE,
            NS_CREATOR
    ]

    String ns
    String nsType
    Boolean isHidden
    String validationRegex
    String family
    Boolean isUnique

    static mapping = {
        id column:'idns_id'
        ns column:'idns_ns'
        nsType column:'idns_type'
        isHidden column:'idns_hide'
        validationRegex column:'idns_val_regex'
        family column:'idns_family'
        isUnique column:'idns_unique'
    }

    static constraints = {
        nsType          nullable:true, blank:false
        isHidden        nullable:true, blank:false
        validationRegex nullable:true, blank:false
        family          nullable:true, blank:false
        isUnique        nullable:false, blank:false, default: true
    }
}
