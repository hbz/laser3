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
    final static String[] AVAILABLE_NSTYPES = [
            NS_ORGANISATION,
            NS_LICENSE,
            NS_SUBSCRIPTION,
            NS_PACKAGE,
            NS_TITLE
    ]

    String ns
    String nsType
    Boolean hide
    String validationRegex
    String family
    Boolean nonUnique

    static mapping = {
        id column:'idns_id'
        ns column:'idns_ns'
        nsType column:'idns_type'
        hide column:'idns_hide'
        validationRegex column:'idns_val_regex'
        family column:'idns_family'
        nonUnique column:'idns_non_unique'
    }

    static constraints = {
        nsType          nullable:true, blank:false
        hide            nullable:true, blank:false
        validationRegex nullable:true, blank:false
        family          nullable:true, blank:false
        nonUnique       nullable:true, blank:false
    }
}
