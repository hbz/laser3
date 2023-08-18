package de.laser.convenience

import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.auth.User

class Marker {

    static enum TYPE {
        WEKB_CHANGES ("WEKB_CHANGES"), UNKOWN ("UNKOWN")

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
            pkg:    Package,
            plt:    Platform,
            user:   User
    ]

    static mapping = {
        id          column:'mkr_id'
        version     column:'mkr_version'

        org         column:'mkr_org_fk'
        pkg         column:'mkr_pkg_fk'
        plt         column:'mkr_plt_fk'

        user        column:'mkr_user_fk', index: 'mkr_user_idx'
        type        column:'mkr_type_enum'

        dateCreated column: 'mkr_date_created'
        lastUpdated column: 'mkr_last_updated'
    }

    static constraints = {
        org         (nullable:true)
        pkg         (nullable:true)
        plt         (nullable:true)
    }
}
