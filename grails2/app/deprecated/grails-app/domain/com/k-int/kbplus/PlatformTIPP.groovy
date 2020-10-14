package com.k_int.kbplus

@Deprecated
class PlatformTIPP {

    //TitleInstancePackagePlatform tipp
    //Platform platform
    String titleUrl
    String rel

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            tipp    : TitleInstancePackagePlatform,
            platform: Platform
    ]

    static mapping = {
        lastUpdated column: 'ptipp_last_updated'
        dateCreated column: 'ptipp_date_created'
    }

    static constraints = {
        titleUrl    (nullable: true, blank: true)
        rel         (nullable: true, blank: true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
}
