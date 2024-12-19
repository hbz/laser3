package de.laser.convenience

import de.laser.Org
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.auth.User

/**
 * A class to bookmark objects coming from the we:kb knowledge base in order to monitor the changes performed there
 */
class Marker {

    /**
     * The enum containing types of markers. Currently, {@link #WEKB_CHANGES} is supported, along with {@link #UNKOWN}
     * as dummy value
     */
    static enum TYPE {
        WEKB_CHANGES ("WEKB_CHANGES"), TIPP_CHANGES ("TIPP_CHANGES"), UNKOWN ("UNKOWN")

        TYPE(String value) {
            this.value = value
        }
        public String value

        static TYPE get(String value) {
            for (TYPE t : TYPE.values()) {
                if (t.value.equalsIgnoreCase(value)) {
                    return t
                }
            }
            return null
        }
    }

    TYPE type

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            org:    Org,
            plt:    Platform,
            pkg:    Package,
            prov:   Provider,
            ven:    Vendor,
            tipp:   TitleInstancePackagePlatform,
            user:   User
    ]

    static mapping = {
        id          column:'mkr_id'
        version     column:'mkr_version'

        org         column:'mkr_org_fk'
        plt         column:'mkr_plt_fk'
        pkg         column:'mkr_pkg_fk'
        prov        column:'mkr_prov_fk'
        ven         column:'mkr_ven_fk'
        tipp        column:'mkr_tipp_fk'

        user        column:'mkr_user_fk', index: 'mkr_user_idx'
        type        column:'mkr_type_enum'

        dateCreated column: 'mkr_date_created'
        lastUpdated column: 'mkr_last_updated'
    }

    static constraints = {
        org         (nullable:true)
        plt         (nullable:true)
        pkg         (nullable:true)
        prov        (nullable:true)
        ven         (nullable:true)
        tipp        (nullable:true)
    }
}
