package de.laser

import de.laser.base.AbstractI10n
import de.laser.helper.LocaleUtils
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.text.SimpleDateFormat

/**
 * A controlled list entry. Reference data values are composed as follows:
 * <ul>
 *     <li>a category to which they belong ({@link #owner})</li>
 *     <li>an independent key string ({@link #value})</li>
 *     <li>localised translation strings of the list denominators ({@link #value_de} and {@link #value_en})</li>
 *     <li>localised translation strings of the explanations given to the list entry ({@link #expl_de} and {@link #expl_en})</li>
 * </ul>
 * Reference data values may be defined per frontend; but they are not hard-coded, i.e. they do not persist database changes and resets nor are they instance-independent. To ensure this,
 * define a new controlled list entry (reference data value) in /src/main/webapp/setup/RefdataValue.csv. Then, the entry will be inserted or updated upon startup of the webapp -
 * the procedure is the same as with property definitions or reference data categories
 * @see RefdataCategory
 * @see AbstractI10n
 * @see de.laser.properties.PropertyDefinition
 */
@Slf4j
class RefdataValue extends AbstractI10n implements Comparable<RefdataValue> {

    final static String CLASS = RefdataValue.class // introduced for refactoring -> RefdataValue.class.name

    String value
    String value_de
    String value_en

    String expl_de
    String expl_en

    //For cases were we want to present a specific group of values, eg License/Sub related
    String group

    // indicates this object is created via current bootstrap
    boolean isHardData = false

    // if manual ordering is wanted
    Long order

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        owner:RefdataCategory
    ]

    static mapping = {
            cache   true
                    id column: 'rdv_id'
               version column: 'rdv_version'
                 owner column: 'rdv_owner', index: 'rdv_owner_idx, rdv_owner_value_idx'
                 value column: 'rdv_value', index: 'rdv_value_idx, rdv_owner_value_idx'
              value_de column: 'rdv_value_de', index:'rdv_value_de_idx'
              value_en column: 'rdv_value_en', index:'rdv_value_en_idx'
               expl_de column: 'rdv_explanation_de', type: 'text'
               expl_en column: 'rdv_explanation_en', type: 'text'
                 group column: 'rdv_group'
              isHardData column: 'rdv_is_hard_data'
              order    column: 'rdv_order'

        dateCreated column: 'rdv_date_created'
        lastUpdated column: 'rdv_last_updated'

    }

    static constraints = {
        group    (nullable: true,  blank:false)
        order    (nullable: true)
        value_de (nullable: true, blank: false)
        value_en (nullable: true, blank: false)
        expl_de  (nullable: true, blank: false)
        expl_en  (nullable: true, blank: false)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    /**
     * Updates a reference data value with the given configuration map. If no reference data is found with the given value key, it will be created
     * @param map the parameter map containing the updated reference data
     * @return the new reference data instance
     */
    static RefdataValue construct(Map<String, Object> map) {

        withTransaction {
            String token    = map.get('token')
            String rdc      = map.get('rdc')

            boolean hardData    = Boolean.parseBoolean(map.get('hardData') as String)
            Map i10n            = map.get('i10n') as Map

            RefdataCategory cat = RefdataCategory.findByDescIlike(rdc)
            if (!cat) {
                cat = RefdataCategory.construct([
                        token   : rdc,
                        hardData: false,
                        i10n    : [desc_de: rdc, desc_en: rdc],
                ])
            }

            RefdataValue rdv = RefdataValue.findByOwnerAndValueIlike(cat, token)

            if (!rdv) {
                log.debug("INFO: no match found; creating new refdata value for ( ${token} @ ${rdc}, ${i10n} )")
                rdv = new RefdataValue(owner: cat, value: token)
            }

            rdv.value_de = i10n.get('value_de') ?: null
            rdv.value_en = i10n.get('value_en') ?: null

            rdv.expl_de = i10n.get('expl_de') ?: null
            rdv.expl_en = i10n.get('expl_en') ?: null

            rdv.isHardData = hardData
            rdv.save()

            rdv
        }
    }

    /**
     * Finds reference data values matching the given query string. Queried is among the values in the current server locale
     * @param params the parameter map containing the search query
     * @return a {@link Map} of the structure [id: oid, text: localised reference value string] for dropdown display
     */
    static def refdataFind(GrailsParameterMap params) {
        List<Map<String, Object>> result = []
        List<RefdataValue> matches = []

        if(! params.q) {
            matches = RefdataValue.findAll()
        }
        else {
            String q = "%${params.q.trim().toLowerCase()}%"

            switch (LocaleUtils.getCurrentLang()) {
                case 'en':
                    matches = RefdataValue.executeQuery("select rdv from RefdataValue rdv where lower(rdv.value_en) like :q", [q: q])
                    break
                case 'de':
                    matches = RefdataValue.executeQuery("select rdv from RefdataValue rdv where lower(rdv.value_de) like :q", [q: q])
                    break
            }
        }

        matches.each { it ->
            result.add([id: "${it.class.name}:${it.id}", text: "${it.getI10n('value')}"])
        }

        result
    }

    // called from AjaxController.resolveOID2()
    @Deprecated
    static def refdataCreate(value) {
        // return new RefdataValue(value:value);
        return null;
    }

    /**
     * Performs a fuzzy search with the given key string
     * @param value the (sub-)string of the reference key to be searched
     * @return the reference data value matching the given key string
     */
    static RefdataValue getByValue(String value) {
        RefdataValue.findByValueIlike(value)
    }

    /**
     * Gets a reference data value by the given value key and belonging to the given category name
     * @param value the key to search for
     * @param category the category constant the requested reference data value is belonging to
     * @return the reference data value matching to the given key and category
     */
    static RefdataValue getByValueAndCategory(String value, String category) {
        RefdataValue.findByValueIlikeAndOwner(value, RefdataCategory.findByDescIlike(category))
    }

    /**
     * This method gets a reference data value by the given category name and German value string
     * @param categoryName the category to which the queried reference data value belongs to
     * @param value the German value string to search for
     * @return the reference data value matching the queried value and belonging to the given category name
     */
    static RefdataValue getByCategoryDescAndI10nValueDe(String categoryName, String value) {
        if (!categoryName || !value) {
            return null
        }
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and rdc.desc = :category and rdv.value_de = :value_de"
        List<RefdataValue> data = RefdataValue.executeQuery( query, [category: categoryName, value_de: value] )

        return (data.size() > 0) ? data[0] : null
    }

    /**
     * This method gets a reference data value by the given list of category names and German value string
     * @param categoryNames a {@link List} of category names to look
     * @param value the German value string to look for
     * @return the reference data value matching to the given query string and belonging to one of the given categories - if multiple results are found, the first result is being returned
     */
    static RefdataValue getByCategoriesDescAndI10nValueDe(List categoryNames, String value) {
        if (!categoryNames || !value) {
            return null
        }
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and rdc.desc in (:categories) and rdv.value_de = :value_de"
        List<RefdataValue> data = RefdataValue.executeQuery( query, [categories: categoryNames, value_de: value] )

        return (data.size() > 0) ? data[0] : null
    }

    /**
     * Helper method: Determines for the reader number entry form the current semester and returns the appropriate reference data value
     * @return the reference data value representing the current semester
     */
    static RefdataValue getCurrentSemester() {
        Calendar now = GregorianCalendar.getInstance(), adjacentYear = GregorianCalendar.getInstance()
        SimpleDateFormat sdf = new SimpleDateFormat('yy')
        //Month is zero-based, April-September is summer term
        String semesterKey
        if(now.get(Calendar.MONTH) < 3) {
            adjacentYear.add(Calendar.YEAR, -1)
            //semesterKey = "w${adjacentYear.getTime().format("yy")}/${now.getTime().format("yy")}"
            semesterKey = "w${sdf.format(adjacentYear.getTime())}/${sdf.format(now.getTime())}"
        }
        else if(now.get(Calendar.MONTH) >= 9) {
            adjacentYear.add(Calendar.YEAR, 1)
            //semesterKey = "w${now.getTime().format("yy")}/${adjacentYear.getTime().format("yy")}"
            semesterKey = "w${sdf.format(now.getTime())}/${sdf.format(adjacentYear.getTime())}"
        }
        else {
            semesterKey = "s${sdf.format(now.getTime())}"
        }
        RefdataValue.getByValue(semesterKey)
    }

    /**
     * Compares this reference data value to another, based on the order; if the order is equal, the internationalised value strings are being compared.
     * The user-defined order is defined in {@link RefdataReorderService#reorderRefdata()}
     * @param rdv the other reference data value to compare with
     * @return the comparison result (-1, 0, 1)
     */
    int compareTo(RefdataValue rdv) {

        long a = rdv.order  ?: 0
        long b = this.order ?: 0

        if (a && b) {
            return a <=> b
        }
        else if (a && !b) {
            return 1
        }
        else if (!a && b) {
            return -1
        }
        else {
            return this.getI10n('value')?.compareTo(rdv.getI10n('value'))
        }
    }

    /**
     * Returns the value key of the reference data value
     * @return the value key
     */
    String toString() {
        // still provide OLD mapping for string compares and such stuff
        value
    }

    /**
     * Checks if the other reference data value instance has the same database id as this one
     * @param o the other object to compare this instance with
     * @return true if the database ids match, false otherwise
     */
    @Override
    boolean equals (Object o) {
        //def obj = ClassUtils.deproxy(o)
        def obj = GrailsHibernateUtil.unwrapIfProxy(o)
        if (obj != null) {
            if ( obj instanceof RefdataValue ) {
                return obj.id == id
            }
        }
        return false
    }

    /**
     * Prepares this reference value entry for the ElasticSearch index mapping
     * @return a {@link Map} represendung the ElasticSearch document structure for reference data values
     */
    Map getMapForES(){
            return ['id':    this.id,
                    'value':    this.value,
                    'value_de': this.value_de,
                    'value_en': this.value_en]
    }
}
