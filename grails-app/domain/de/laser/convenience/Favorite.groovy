package de.laser.convenience

import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.auth.User

class Favorite {

    static enum TYPE {
        WEKB_CHANGES ("WEKB_CHANGES"), UNKOWN ("UNKOWN")

        TYPE(String value) {
            this.value = value
        }
        public String value
    }

    TYPE type
    String note = ""

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            org:    Org,
            pkg:    Package,
            plt:    Platform,
            user:   User
    ]

    static mapping = {
        id          column:'fav_id'
        version     column:'fav_version'

        org         column:'fav_org_fk'
        pkg         column:'fav_pkg_fk'
        plt         column:'fav_plt_fk'

        user        column:'fav_user_fk', index: 'fav_user_idx'
        type        column:'fav_type_enum'
        note        column:'fav_note', type: 'text'

        dateCreated column: 'fav_date_created'
        lastUpdated column: 'fav_last_updated'
    }

    static constraints = {
        org         (nullable:true)
        pkg         (nullable:true)
        plt         (nullable:true)

        note        (nullable: true, blank: true)
    }
}
