package de.laser


import de.laser.base.AbstractI10n
import de.laser.helper.LocaleHelper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * A reference data category represents a controlled list which contains one or more reference data values. Like the entries, the list name can be translated itself.
 * Unlike the entries or property definitions, reference data categories can only be hard-coded; the source file is located under /src/main/webapp/setup/RefdataCategory.csv.
 * The key of the reference data category is located under {@link #desc}
 * @see RefdataValue
 * @see de.laser.properties.PropertyDefinition
 */
class RefdataCategory extends AbstractI10n {

    static Log static_logger = LogFactory.getLog(RefdataCategory)

    String desc
    String desc_de
    String desc_en

    // indicates this object is created via current bootstrap
    boolean isHardData = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
            cache   true
              id column: 'rdc_id'
         version column: 'rdc_version'
            desc column: 'rdc_description', index:'rdc_description_idx'
         desc_de column: 'rdc_description_de', index:'rdc_description_de_idx'
         desc_en column: 'rdc_description_en', index:'rdc_description_en_idx'
        isHardData column: 'rdc_is_hard_data'
        dateCreated column: 'rdc_date_created'
        lastUpdated column: 'rdc_last_updated'
    }

    static constraints = {
        // Nullable is true, because values are already in the database
        desc_de (nullable: true, blank: false)
        desc_en (nullable: true, blank: false)
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    /**
     * Constructs a new reference data category with the given parameter map; if there is no reference data category with
     * the specified key, it will be created
     * @param map the parameter map containing the updated reference category data
     * @return the new or updated reference category instance
     */
    static RefdataCategory construct(Map<String, Object> map) {
        withTransaction {
            String token = map.get('token')
            boolean hardData = new Boolean(map.get('hardData'))
            Map i10n = map.get('i10n')

            RefdataCategory rdc = RefdataCategory.findByDescIlike(token) // todo: case sensitive token

            if (!rdc) {
                static_logger.debug("INFO: no match found; creating new refdata category for ( ${token}, ${i10n} )")
                rdc = new RefdataCategory(desc: token) // todo: token
            }

            rdc.desc_de = i10n.get('desc_de') ?: null
            rdc.desc_en = i10n.get('desc_en') ?: null

            rdc.isHardData = hardData
            rdc.save()

            rdc
        }
    }

    /**
     * Retrieves all reference data categories matching to the given query (sub-)string
     * @param params the parameter map containing the search query
     * @return a {@link List} of {@link Map}s of structure [id: oid, text: localised description string (name)] matching the given query string
     */
    static def refdataFind(GrailsParameterMap params) {
        List<Map<String, Object>> result = []
        List<RefdataCategory> matches = []

        if(! params.q) {
            matches = RefdataCategory.findAll()
        }
        else {
            String q = "%${params.q.trim().toLowerCase()}%"

            switch (LocaleHelper.getCurrentLang()) {
                case 'en':
                    matches = RefdataCategory.executeQuery("select rc from RefdataCategory rc where lower(rc.desc_en) like :q", [q: q])
                    break
                case 'de':
                    matches = RefdataCategory.executeQuery("select rc from RefdataCategory rc where lower(rc.desc_de) like :q", [q: q])
                    break
            }
        }

        matches.each { it ->
            result.add([id: "${it.id}", text: "${it.getI10n('desc')}"])
        }
        result
    }

    /**
     * Performs a fuzzy query and retrieves a reference data category matching at least partially to the given query string
     * @param desc the category name to look for
     * @return the reference data category matching to the string, null otherwise
     */
    static RefdataCategory getByDesc(String desc) {
        RefdataCategory.findByDescIlike(desc)
    }

  /**
   * Returns a list containing category depending reference data values
   * @param category_name the name of the reference category whose reference values should be retrieved
   * @return a {@link List} of all reference data values belonging to the given reference data category
   */
    static List<RefdataValue> getAllRefdataValues(String category_name) {
        if (! category_name) {
            return []
        }
        String i10nAttr = LocaleHelper.getLocalizedAttributeName('value')
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and lower(rdc.desc) = :category order by rdv.${i10nAttr}"

        RefdataValue.executeQuery( query, [category: category_name.toLowerCase()] )
    }

    /**
     * Returns all reference data values belonging to the given list of category names, the values are ordered by the localised value string
     * @param category_names a {@link List} of category names whose values should be retrieved
     * @return a {@link List} of {@link RefdataValue}s belonging to the given categories
     */
    static List<RefdataValue> getAllRefdataValues(List category_names) {
        if (! category_names) {
            return []
        }
        String i10nAttr = LocaleHelper.getLocalizedAttributeName('value')
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and lower(rdc.desc) in (:categories) order by rdv.${i10nAttr}"

        RefdataValue.executeQuery( query, [categories: category_names.collect{it.toLowerCase()}] )
    }

    /**
     * Returns all reference data values belonging to the given list of category names, the values are ordered by the order number defined in {@link RefdataReorderService#reorderRefdata()}
     * @param category_name the name of the category whose reference data values should be retrieved
     * @return a {@link List} of {@link RefdataValue}s belonding to the given reference data category
     */
    static List<RefdataValue> getAllRefdataValuesWithOrder(String category_name) {
        if (! category_name) {
            return []
        }
        String query = "select rdv from RefdataValue as rdv, RefdataCategory as rdc where rdv.owner = rdc and lower(rdc.desc) = :category order by rdv.order asc, rdv.value asc"

        RefdataValue.executeQuery( query, [category: category_name.toLowerCase()] )
    }

    /**
     * Returns all reference data values with their explanation strings, ordered by the given sort parameter map
     * @param category_name the reference category name whose reference values should be retrieved
     * @param sort the parameter map according which the values are being sorted, in the structure: [sort: field, order: 'asc'/'desc']
     * @return a {@link List} of {@link Map} entries in the structure [id: id, value: localised string, expl: localised explnanation of the reference value]
     */
    static getAllRefdataValuesWithI10nExplanation(String category_name, Map sort) {
        List<RefdataValue> refdatas = RefdataValue.findAllByOwner(RefdataCategory.findByDescIlike(category_name), sort)

        List result = []
        refdatas.each { rd ->
            result.add(id:rd.id, value:rd.getI10n('value'), expl:rd.getI10n('expl'))
        }
        result
    }
}
