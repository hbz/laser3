package com.k_int.kbplus
import com.k_int.ClassUtils
import de.laser.domain.I10nTranslatableAbstract
import de.laser.domain.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder
import javax.persistence.Transient

class RefdataValue extends I10nTranslatableAbstract {

    String value

    // N.B. This used to be ICON but in the 2.x series this was changed to be a css class which denotes an icon
    // Please stick with the change to store css classes in here and not explicit icons
    String icon
    //For cases were we want to present a specific group of values, eg License/Sub related
    String group

    // indicates this object is created via front-end
    boolean softData

    static belongsTo = [
        owner:RefdataCategory
    ]

  
    // We wish some refdata items to model a sharing of permission from the owner of an object to a particular
    // organisation. For example, an organisation taking out a license (Via an OrgRole link) needs to be editable by that org.
    // Therefore, we would like all OrgRole links of type "Licensee" to convey
    // permissions of "EDIT" and "VIEW" indicating that anyone who has the corresponding rights via their
    // connection to that org can perform the indicated action.
    // Object Side = Share Permission, User side == grant permission
    @Transient
    Set<OrgPermShare> sharedPermissions = []

    /*
    static hasMany = [
            sharedPermissions: com.k_int.kbplus.OrgPermShare
    ]
    static mappedBy = [
            sharedPermissions: 'rdv'
    ]
    */

    static mapping = {
                    id column: 'rdv_id'
               version column: 'rdv_version'
                 owner column: 'rdv_owner', index: 'rdv_entry_idx'
                 value column: 'rdv_value', index: 'rdv_entry_idx'
                  icon column: 'rdv_icon'
                 group column: 'rdv_group'
              softData column: 'rdv_soft_data'
    }

    static constraints = {
        icon     (nullable:true)
        group    (nullable:true,  blank:false)
        softData (nullable:false, blank:false, default:false)
    }

    /**
     * Create RefdataValue and matching I10nTranslation.
     * Create RefdataCategory, if needed.
     * Softdata flag will be removed, if RefdataValue is found.
     *
     * @param category_name
     * @param i10n
     * @return
     */
    static def loc(String category_name, Map i10n) {

        def cat = RefdataCategory.loc(category_name, [:])
        def rdvValue = i10n['key'] ?: i10n['en']

        def result = findByOwnerAndValueIlike(cat, rdvValue)
        if (! result) {
            result = new RefdataValue(owner: cat, value: rdvValue)
        }
        result.softData = false
        result.save(flush: true)

        I10nTranslation.createOrUpdateI10n(result, 'value', i10n)

        result
    }

    static def refdataFind(params) {
        def result = []
        def matches = I10nTranslation.refdataFindHelper(
                params.baseClass,
                'value',
                params.q,
                LocaleContextHolder.getLocale()
        )
        matches.each { it ->
            result.add([id: "${it.class.name}:${it.id}", text: "${it.getI10n('value')}"])
        }
        matches
    }

    static def refdataCreate(value) {
        // return new RefdataValue(value:value);
        return null;
    }

    static def getByValueAndCategory(value, category) {

        RefdataValue.findByValueAndOwner(value, RefdataCategory.findByDesc(category))
    }

    static def getByCategoryDescAndI10nValueDe(categoryName, value) {

        def data = RefdataValue.executeQuery("select rdv from RefdataValue as rdv, RefdataCategory as rdc, I10nTranslation as i10n "
                    + " where rdv.owner = rdc and rdc.desc = ? "
                    + " and i10n.referenceId = rdv.id and i10n.valueDe = ?"
                    + " and i10n.referenceClass = 'com.k_int.kbplus.RefdataValue' and i10n.referenceField = 'value'"
                    , ["${categoryName}", "${value}"] )

        if (data.size() > 0) {
            return data[0]
        }

        null
    }

    // still provide OLD mapping for string compares and such stuff
    public String toString() {
        value
    }

    /**
    * Equality should be decided like this, although we currently got duplicates
    * refdatavalue for same string value
    **/
    @Override
    public boolean equals (Object o) {
        def obj = ClassUtils.deproxy(o)
        if (obj != null) {
            if ( obj instanceof RefdataValue ) {
                return obj.id == id
            }
        }
        return false
    }

    def afterInsert() {
        I10nTranslation.createOrUpdateI10n(this, 'value', [de: this.value, en: this.value])
    }

    def afterDelete() {
        def rc = this.getClass().getName()
        def id = this.getId()
        I10nTranslation.where{referenceClass == rc && referenceId == id}.deleteAll()
    }

    def getSharedPermissions() {
        OrgPermShare.findAllByRdv(this)
    }
}
