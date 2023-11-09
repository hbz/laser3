package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.*
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.finance.CostItem
import de.laser.interfaces.DeleteFlag
import de.laser.interfaces.MarkerSupport
import de.laser.oap.OrgAccessPoint
import de.laser.properties.OrgProperty
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils

/**
 * An organisation record.
 * Organisations may represent in LAS:eR:
 * <ol>
 *     <li>academic institutions</li>
 *     <li>commercial organisations: editors or publishing houses</li>
 * </ol>
 * Institutions may be: university or public libraries, research organisations, or other academic institutions.
 * Above that, organisations may be editors, providers, publishers, agencies. They are called by the super term "organisations".
 * The main difference between organisations and institutions is that institutions may have user accounts linked to them whereas publishers, agencies etc. are not supposed to have such.
 * There are above that several ways to distinguish technically an organisation from an institution:
 * <ul>
 *     <li>institutions have a customer type</li>
 *     <li>the organisational types are different, see {@link RDConstants#ORG_TYPE}</li>
 *     <li>a well maintained institution record has the sector set to 'Academic' while (other) organisations are usually set to 'Commercial'</li>
 *     <li>institutions are linked with different org role types to other objects than other organisations are (see the controlled list under {@link RDConstants#ORGANISATIONAL_ROLE} for the role types)</li>
 *     <li>institution metadata is maintained in LAS:eR directly whereas providers should curate themselves in we:kb; organisations thus have a we:kb-ID (stored as {@link #gokbId}, naming is legacy) which serves as synchronisation
 *     key between the data in the two webapps</li>
 * </ul>
 * @see OrgRole
 * @see OrgSetting
 */
@Slf4j
class Org extends AbstractBaseWithCalculatedLastUpdated
        implements DeleteFlag, MarkerSupport, Comparable<Org> {

    String name
    String shortcode            // Used to generate friendly semantic URLs
    String sortname
    String legalPatronName
    String url
    String urlGov
    String linkResolverBaseURL
    SortedSet subjectGroup

    String importSource         // "nationallizenzen.de", "edb des hbz"
    Date lastImportDate

    String gokbId
    String comment
    String ipRange
    String scope

    Org createdBy
    Org legallyObligedBy
    String categoryId

    boolean eInvoice = false

    Date retirementDate
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    @RefdataInfo(cat = RDConstants.ORG_SECTOR)
    RefdataValue sector

    @RefdataInfo(cat = RDConstants.ORG_STATUS)
    RefdataValue status

    @RefdataInfo(cat = RDConstants.COUNTRY, i18n = 'org.country.label')
    RefdataValue country

    @RefdataInfo(cat = '?', i18n = 'org.region.label')
    RefdataValue region

    @RefdataInfo(cat = RDConstants.LIBRARY_NETWORK, i18n = 'org.libraryNetwork.label')
    RefdataValue libraryNetwork

    @RefdataInfo(cat = RDConstants.FUNDER_TYPE, i18n = 'org.funderType.label')
    RefdataValue funderType

    @RefdataInfo(cat = RDConstants.FUNDER_HSK_TYPE, i18n = 'org.funderHSK.label')
    RefdataValue funderHskType

    @RefdataInfo(cat = RDConstants.LIBRARY_TYPE, i18n = 'org.libraryType.label')
    RefdataValue libraryType

    @RefdataInfo(cat = RDConstants.COST_CONFIGURATION)
    RefdataValue costConfigurationPreset

    @RefdataInfo(cat = RDConstants.E_INVOICE_PORTAL)
    RefdataValue eInvoicePortal

    SortedSet ids
    SortedSet altnames

    static transients = [
            'deleted', 'customerType', 'customerTypeI10n', 'designation',
            'calculatedPropDefGroups', 'empty', 'consortiaMember',
            'customerType_Basic', 'customerType_Pro', 'customerType_Inst', 'customerType_Consortium',
    ] // mark read-only accessor methods

    static mappedBy = [
        ids:                'org',
        outgoingCombos:     'fromOrg',
        incomingCombos:     'toOrg',
        links:              'org',
        prsLinks:           'org',
        contacts:           'org',
        addresses:          'org',
        propertySet:        'owner',
        altnames:           'org',
        documents:          'org',
        hasCreated:         'createdBy',
        hasLegallyObliged:  'legallyObligedBy'
    ]

    static hasMany = [
        ids:                Identifier,
        subjectGroup:       OrgSubjectGroup,
        outgoingCombos:     Combo,
        incomingCombos:     Combo,
        links:              OrgRole,
        prsLinks:           PersonRole,
        contacts:           Contact,
        addresses:          Address,
        propertySet:        OrgProperty,
        altnames:           AlternativeName,
        discoverySystemFrontends: DiscoverySystemFrontend,
        discoverySystemIndices: DiscoverySystemIndex,
        orgType:            RefdataValue,
        documents:          DocContext,
        platforms:          Platform,
        hasCreated:         Org,
        hasLegallyObliged:  Org,
        accessPoints:   OrgAccessPoint
    ]

    static mapping = {
                cache true
                sort 'sortname'
                id          column:'org_id'
           version          column:'org_version'
         globalUID          column:'org_guid'
              name          column:'org_name',      index:'org_name_idx'
          sortname          column:'org_sortname',  index:'org_sortname_idx'
   legalPatronName          column:'org_legal_patronname'
               url          column:'org_url'
            urlGov          column:'org_url_gov'
      linkResolverBaseURL   column:'org_link_resolver_base_url', type: 'text'
           comment          column:'org_comment'
           ipRange          column:'org_ip_range'
         shortcode          column:'org_shortcode', index:'org_shortcode_idx'
             scope          column:'org_scope'
        categoryId          column:'org_cat'
        eInvoice            column:'org_e_invoice'
        eInvoicePortal      column:'org_e_invoice_portal_fk', lazy: false
        gokbId              column:'org_gokb_id', type:'text'
            sector          column:'org_sector_rv_fk', lazy: false
            status          column:'org_status_rv_fk'
    retirementDate          column:'org_retirement_date'
           country          column:'org_country_rv_fk'
            region          column:'org_region_rv_fk'
    libraryNetwork          column:'org_library_network_rv_fk'
        funderType          column:'org_funder_type_rv_fk'
     funderHskType          column:'org_funder_hsk_type_rv_fk'
       libraryType          column:'org_library_type_rv_fk'
      importSource          column:'org_import_source'
    lastImportDate          column:'org_last_import_date'
       dateCreated          column:'org_date_created'
       lastUpdated          column:'org_last_updated'
        createdBy           column:'org_created_by_fk'
        legallyObligedBy    column:'org_legally_obliged_by_fk'
    costConfigurationPreset column:'org_config_preset_rv_fk'
       lastUpdatedCascading column:'org_last_updated_cascading'

        orgType             joinTable: [
                name:   'org_type',
                key:    'org_id',
                column: 'refdata_value_id', type:   'BIGINT'
        ], lazy: false

        ids                 sort: 'ns', batchSize: 10
        outgoingCombos      batchSize: 10
        incomingCombos      batchSize: 10
        links               batchSize: 10
        prsLinks            batchSize: 10
        propertySet    batchSize: 10
        documents           batchSize: 10
        platforms           sort:'name', order:'asc', batchSize: 10
        hasCreated          batchSize: 10
        hasLegallyObliged   batchSize: 10
    }

    static constraints = {
           globalUID(nullable:true, blank:false, unique:true, maxSize:255)
                name(blank:false, maxSize:255)
            sortname(nullable:true, blank:true, maxSize:255)
     legalPatronName(nullable:true, blank:true, maxSize:255)
                 url(nullable:true, blank:true, maxSize:512)
              urlGov(nullable:true, blank:true, maxSize:512)
 linkResolverBaseURL(nullable:true, blank:false)
      retirementDate(nullable:true)
             comment(nullable:true, blank:true, maxSize:2048)
             ipRange(nullable:true, blank:true, maxSize:1024)
              sector(nullable:true)
           shortcode(nullable:true, blank:true, maxSize:128)
               scope(nullable:true, blank:true, maxSize:128)
          categoryId(nullable:true, blank:true, maxSize:128)
             country(nullable:true)
              region(nullable:true)
            eInvoicePortal(nullable:true)
//        , validator: {RefdataValue val, Org obj, errors ->
//                  if ( ! val.owner.desc.endsWith(obj.country.toString().toLowerCase())){
//                      errors.rejectValue('region', 'regionDoesNotBelongToSelectedCountry')
//                      return false
//                  }
//              })
      libraryNetwork(nullable:true)
          funderType(nullable:true)
       funderHskType(nullable:true)
         libraryType(nullable:true)
        importSource(nullable:true, blank:true)
      lastImportDate(nullable:true)
           createdBy(nullable:true)
    legallyObligedBy(nullable:true)
      costConfigurationPreset(nullable:true)
             orgType(nullable:true)
             gokbId (nullable:true, blank:true)
        lastUpdatedCascading (nullable: true)
    }

    static final Set<String> WEKB_PROPERTIES = ['homepage', 'metadataDownloaderURL', 'kbartDownloaderURL', 'roles']

    /**
     * Checks if the organisation is marked as deleted
     * @return true if the status is deleted, false otherwise
     */
    @Override
    boolean isDeleted() {
        return RDStore.ORG_STATUS_DELETED.id == status?.id
    }

    /**
     * Generates a shortcode for the new organisation record and sets the institution whose member created the entry.
     * This serves as reference for institutions which do not have a client access (yet); if there are issues with the
     * contact details, one can turn towards the creating institution for further information
     * This is retrieved by the context org; but if the organisation is inserted by the cronjob-triggered synchronisation script, i.e. the
     * new organisation comes from we:kb, there is of course no context organisation because no request context is given. If the
     * context service is called in any way while no request context is given, the method crashes
     */
    @Override
    def beforeInsert() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name)
        }

        //ugliest HOTFIX ever #2
        if(!Thread.currentThread().name.contains("Sync")) {
            if (BeanStore.getContextService().getOrg()) {
                createdBy = BeanStore.getContextService().getOrg()
            }
        }

        super.beforeInsertHandler()
    }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        BeanStore.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }

    /**
     * Sets for an institution the default customer type, that is ORG_INST_BASIC for consortium members with a basic set of permissions
     * @return true if the setup was successful, false otherwise
     */
    boolean setDefaultCustomerType() {
        def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)

        if (oss == OrgSetting.SETTING_NOT_FOUND) {
            log.debug ('Setting default customer type for org: ' + this.id)
            OrgSetting.add(this, OrgSetting.KEYS.CUSTOMER_TYPE, Role.findByAuthorityAndRoleType(CustomerTypeService.ORG_INST_BASIC, 'org'))
            return true
        }
        false
    }

    /**
     * Gets the customer type of this institution
     * @return the customer type string
     */
    String getCustomerType() {
        String result
        def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSetting.SETTING_NOT_FOUND) {
            result = oss.roleValue?.authority
        }
        result
    }

    /**
     * Gets the internationalised value of the customer type of this institution
     * @return the localised value string of the customer type for display
     */
    String getCustomerTypeI10n() {
        String result
        def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSetting.SETTING_NOT_FOUND) {
            result = oss.roleValue?.getI10n('authority')
        }
        result
    }

    /**
     * Checks if the given organisation is an institution or consortium with customer type BASIC
     * @see CustomerTypeService
     * @return true if the customer type, if set at all, is one of {@link CustomerTypeService#ORG_INST_BASIC} or {@link CustomerTypeService#ORG_CONSORTIUM_BASIC}, false otherwise
     */
    boolean isCustomerType_Basic() {
        this.getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_CONSORTIUM_BASIC ]
    }

    /**
     * Checks if the given organisation is an institution or consortium with customer type PRO
     * @see CustomerTypeService
     * @return true if the customer type, if set at all, is one of {@link CustomerTypeService#ORG_INST_PRO} or {@link CustomerTypeService#ORG_CONSORTIUM_PRO}, false otherwise
     */
    boolean isCustomerType_Pro() {
        this.getCustomerType() in [CustomerTypeService.ORG_INST_PRO, CustomerTypeService.ORG_CONSORTIUM_PRO ]
    }

    /**
     * Checks if the given organisation is an institution, either BASIC or PRO
     * @see CustomerTypeService
     * @return true if the customer type, if set at all, is one of {@link CustomerTypeService#ORG_INST_BASIC} or {@link CustomerTypeService#ORG_INST_PRO}, false otherwise
     */
    boolean isCustomerType_Inst() {
        this.getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO ]
    }

    /**
     * Checks if the given organisation is a consortium, either BASIC or PRO
     * @see CustomerTypeService
     * @return true if the customer type, if set at all, is one of {@link CustomerTypeService#ORG_CONSORTIUM_BASIC} or {@link CustomerTypeService#ORG_CONSORTIUM_PRO}, false otherwise
     */
    boolean isCustomerType_Consortium() {
        this.getCustomerType() in [ CustomerTypeService.ORG_CONSORTIUM_BASIC, CustomerTypeService.ORG_CONSORTIUM_PRO ]
    }

//    boolean isCustomerType_Consortium_or_Support() {
//        this.getCustomerType() in [ CustomerTypeService.ORG_CONSORTIUM_BASIC, CustomerTypeService.ORG_CONSORTIUM_PRO, CustomerTypeService.ORG_SUPPORT ] // hasPerm(ORG_CONSORTIUM_BASIC)
//    }

    boolean isCustomerType_Support() {
        this.getCustomerType() == CustomerTypeService.ORG_SUPPORT
    }

    /**
     * Checks if the given organisation is an institution of type BASIC
     * @see CustomerTypeService
     * @return true if the customer type is {@link CustomerTypeService#ORG_INST_BASIC}, false otherwise
     */
    boolean isCustomerType_Inst_Basic() {
        this.getCustomerType() == CustomerTypeService.ORG_INST_BASIC
    }

    /**
     * Checks if the given organisation is an institution of type PRO
     * @see CustomerTypeService
     * @return true if the customer type is {@link CustomerTypeService#ORG_INST_PRO}, false otherwise
     */
    boolean isCustomerType_Inst_Pro() {
        this.getCustomerType() == CustomerTypeService.ORG_INST_PRO
    }

    /**
     * Checks if the given organisation is a consortium of type BASIC
     * @see CustomerTypeService
     * @return true if the customer type is {@link CustomerTypeService#ORG_CONSORTIUM_BASIC}, false otherwise
     */
    boolean isCustomerType_Consortium_Basic() {
        this.getCustomerType() == CustomerTypeService.ORG_CONSORTIUM_BASIC
    }

    /**
     * Checks if the given organisation is a consortium of type PRO
     * @see CustomerTypeService
     * @return true if the customer type is {@link CustomerTypeService#ORG_CONSORTIUM_PRO}, false otherwise
     */
    boolean isCustomerType_Consortium_Pro() {
        this.getCustomerType() == CustomerTypeService.ORG_CONSORTIUM_PRO
    }

    /**
     * Gets the given OrgSetting enum key, creating new one (with the given default value) if not existing
     * @param key the enum key to look for
     * @param defaultValue the value to insert if the key does not exist
     * @return the org setting
     */
    OrgSetting getSetting(OrgSetting.KEYS key, def defaultValue) {
        def os = OrgSetting.get(this, key)
        (os == OrgSetting.SETTING_NOT_FOUND) ? OrgSetting.add(this, key, defaultValue) : (OrgSetting) os
    }

    /**
     * Gets the VALUE of the given OrgSetting enum key, creating new OrgSetting (with the given default value) if not existing
     * @param key the enum key to look for
     * @param defaultValue
     * @return the org setting value
     */
    def getSettingsValue(OrgSetting.KEYS key, def defaultValue) {
        OrgSetting setting = getSetting(key, defaultValue)
        setting.getValue()
    }

    /**
     * Gets the VALUE of given OrgSetting enum key, creating new OrgSetting (without value) if not existing
     * @param key the enum key to look for
     * @return the org setting value
     */
    def getSettingsValue(OrgSetting.KEYS key) {
        getSettingsValue(key, null)
    }

    @Override
    def beforeUpdate() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name)
        }
        super.beforeUpdateHandler()
    }

    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * One of the functions to generate a shortcode from the organisation name; replaces blanks with underscores and strips everything beyond 128 characters
     * @param name the organisation's name
     * @return the prepared string
     */
    static String generateShortcodeFunction(String name) {
        return StringUtils.left(name.trim().replaceAll(" ","_"), 128) // FIX
    }

    static Set<Org> getAllByCustomerType(String customerType) {
        BeanStore.getCustomerTypeService().getAllOrgsByCustomerType(customerType).toSet()
    }

    /**
     * Generates a shortcode for the given organisation's name
     * @param name the name to prepare
     * @return the shortened and, if necessary, postfixed shortcode
     */
    @Deprecated
    String generateShortcode(String name) {
        String candidate = Org.generateShortcodeFunction(name)
        incUntilUnique(candidate)
    }

    @Deprecated
    String incUntilUnique(String name) {
        String result = name
        if ( Org.findByShortcode(result) ) {
            // There is already a shortcode for that identfier
            int i = 2
            while ( Org.findByShortcode("${name}_${i}") ) {
                i++
            }
            result = "${name}_${i}"
        }
        result
    }

    /**
     * Gets the property definition groups defined by the given institution for the organisation to be viewed
     * @param contextOrg the institution whose property definition groups should be loaded
     * @return a {@link Map} of property definition groups, ordered by sorted, global, local and orphaned property definitions
     * @see de.laser.properties.PropertyDefinition
     * @see de.laser.properties.PropertyDefinitionGroup
     */
    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        BeanStore.getPropertyService().getCalculatedPropDefGroups(this, contextOrg)
    }

    /**
     * Gets the identifier for the given namespace string; if there are multiple occurrences, the FIRST one in the list
     * is being retrieved (which may vary)
     * @param idtype the namespace string to which the requested identifier belongs to
     * @return the {@link Identifier} if found, null otherwise
     */
    Identifier getIdentifierByType(String idtype) {
        Identifier result

        List<Identifier> test = getIdentifiersByType(idtype)
        if (test.size() > 0) {
            result = test.get(0)  // TODO refactoring: multiple occurrences
        }
        result
    }

    /**
     * Gets all identifiers of this institution belonging to the given namespace
     * @param idtype the namespace string to which the requested identifiers belong
     * @return a {@link List} of {@link Identifier}s belonging to the given namespace
     */
    List<Identifier> getIdentifiersByType(String idtype) {

        Identifier.executeQuery(
                'select id from Identifier id join id.ns ns where id.org = :org and lower(ns.ns) = :idtype',
                [org: this, idtype: idtype.toLowerCase()]
        )
    }

    /**
     * Gets all organisations matching at least partially to the given query string
     * @param params the parameter map containing the query string
     * @return a {@link Map} of query results in the structure [id: oid, text: org.name]
     */
    static def refdataFind(GrailsParameterMap params) {
        GenericOIDService genericOIDService = BeanStore.getGenericOIDService()

        genericOIDService.getOIDMapList( Org.findAllByNameIlike("%${params.q}%", params), 'name' )
    }

    /**
     * Creates a new organisation record with the given name
     * @param value the name of the new organisation
     * @return the new organisation instance
     */
    // called from AjaxController.resolveOID2()
  static Org refdataCreate(String value) {
    return new Org(name:value)
  }

    /**
     * Gets the display string for this organisation; the following cascade is being checked. If one field is not set, the following is being returned:
     * <ol>
     *     <li>sortname</li>
     *     <li>name</li>
     *     <li>globalUID</li>
     *     <li>database id</id>
     * </ol>
     * @return one of the fields listed above
     */
    String getDesignation() {
        sortname ?: (name ?: (globalUID ?: id))
    }

    /**
     * Checks if there are objects attached to the given organisation
     * @return true if no {@link CostItem}s, {@link Subscription}s or {@link User}s are linked to this organisation, false otherwise
     */
    boolean isEmpty() {
        Map deptParams = [org:this,current:RDStore.SUBSCRIPTION_CURRENT]
        //verification a: check if org has cost items
        List costItems = CostItem.executeQuery('select ci from CostItem ci join ci.sub sub join sub.orgRelations orgRoles where orgRoles.org = :org and sub.status = :current',deptParams)
        if(costItems)
            return false
        //verification b: check if org has current subscriptions
        List currentSubscriptions = Subscription.executeQuery('select s from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and s.status = :current',deptParams)
        if(currentSubscriptions)
            return false
        //verification c: check if org has active users
        if (User.findAllByFormalOrg(this))
            return false
        return true
    }

    /**
     * Is the toString() implementation; returns the name of this organisation
     * @return the name of this organisation
     */
    @Override
    String toString() {
        name
    }

    /**
     * Retrieves the general contact persons of this organisation
     * @param onlyPublic should only the public contacts being retieved?
     * @return a {@link List} of {@link Person}s marked as general contacts of this organisation
     */
    List<Person> getGeneralContactPersons(boolean onlyPublic) {
        if (onlyPublic) {
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org and pr.functionType = :gcp",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
            )
        }
        else {
            Org ctxOrg = BeanStore.getContextService().getOrg()
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where pr.org = :org and pr.functionType = :gcp " +
                    " and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, ctx: ctxOrg]
            )
        }
    }

    /**
     * Retrieves e-mails of the general contact persons of this organisation
     * @param onlyPublic should only the public contacts being retieved?
     * @return a {@link List} of {@link String}s marked as general contacts  e-mails of this organisation
     */
    List<String> getMailsOfGeneralContactPersons(boolean onlyPublic) {
        if (onlyPublic) {
            Person.executeQuery(
                    "select distinct c.content from Contact c inner join c.prs as p inner join p.roleLinks pr where c.contentType = :contentType and p.isPublic = true and pr.org = :org and pr.functionType = :gcp",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, contentType: RDStore.CCT_EMAIL]
            )
        }
        else {
            Org ctxOrg = BeanStore.getContextService().getOrg()
            Person.executeQuery(
                    "select distinct c.content from Contact c inner join c.prs as p inner join p.roleLinks pr where c.contentType = :contentType and pr.org = :org and pr.functionType = :gcp " +
                            " and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, ctx: ctxOrg, contentType: RDStore.CCT_EMAIL]
            )
        }
    }

    /**
     * Gets all public contact persons of this organisation
     * @return a {@link List} of public {@link Person}s
     */
    List<Person> getPublicPersons() {
        Person.executeQuery(
                "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org",
                [org: this]
        )
    }

    /**
     * Gets the contact persons; optionally, a function type may be given as filter. Moreover, the request may be limited to public contacts only
     * @param onlyPublic retrieve only public contacts?
     * @param functionType the function type of the contacts to be requested
     * @return a {@link List} of {@link Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(boolean onlyPublic, RefdataValue functionType = null, boolean exWekb = false) {
        Map<String, Object> queryParams = [org: this]
        String functionTypeFilter = ''
        if(functionType) {
            functionTypeFilter = 'and pr.functionType = :functionType'
            queryParams.functionType = functionType
        }
        if (onlyPublic) {
            if(exWekb) {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.org = :org '+functionTypeFilter+' and p.tenant = :org',
                        queryParams
                )
            }
            else {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org '+functionTypeFilter,
                        queryParams
                )
            }
        }
        else {
            queryParams.ctx = BeanStore.getContextService().getOrg()
            Person.executeQuery(
                    'select distinct p from Person as p inner join p.roleLinks pr where pr.org = :org ' + functionTypeFilter +
                            ' and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )',
                    queryParams
            )
        }
    }

    /**
     * Gets all type reference values attributed to this organisation
     * @return a {@link List} of {@link RefdataValue}s assigned to this organisation
     */
    List<RefdataValue> getAllOrgTypes() {
        RefdataValue.executeQuery("select ot from Org org join org.orgType ot where org = :org", [org: this])
    }

    /**
     * Gets all type reference value ids attributed to this organisation
     * @return a {@link List} of reference data IDs assigned to this organisation
     */
    List getAllOrgTypeIds() {
        RefdataValue.executeQuery("select ot.id from Org org join org.orgType ot where org = :org", [org: this])
    }

    /**
     * Checks if this institution is linked to any other institution by the given combo link type
     * @param comboType the type of link to check
     * @return true if there are any links from this institution to any other institution, false otherwise
     */
    boolean isInComboOfType(RefdataValue comboType) {
        if(Combo.findByFromOrgAndType(this, comboType))
            return true
        return false
    }

    /**
     * Checks if this institution is member of any consortium
     * @return true if this institution is linked to any consortium, false otherwise
     */
    boolean isConsortiaMember() {
        isInComboOfType(RDStore.COMBO_TYPE_CONSORTIUM)
    }

    /**
     * Called from {@link OrganisationController#ids()} and {@link OrganisationController#show()}.
     * Sets up for this institution the set of core identifiers that every institution should curate.
     * The namespaces of those core identifiers are defined at {@link IdentifierNamespace#CORE_ORG_NS}
     */
    void createCoreIdentifiersIfNotExist(){
        if(!(RDStore.OT_PROVIDER.id in this.getAllOrgTypeIds())){

            boolean isChanged = false
            IdentifierNamespace.CORE_ORG_NS.each{ coreNs ->
                if ( ! ids.find {it.ns.ns == coreNs}){
                    addOnlySpecialIdentifiers(coreNs, IdentifierNamespace.UNKNOWN)
                    isChanged = true
                }
            }
            if (isChanged) refresh()
        }
    }

    /**
     * Adds the ISIL, EZB and WIBID {@link Identifier} stubs (see {@link IdentifierNamespace#CORE_ORG_NS} for those namespaces) to this institution
     * @param ns the namespace string to be added
     * @param value the value to look up or to set if the identifier instance does not exist
     */
    void addOnlySpecialIdentifiers(String ns, String value) {
        boolean found = false
        this.ids.each {
            if ( it?.ns?.ns == ns && it.value == value ) {
                found = true
            }
        }

        if ( !found && value != '') {
            value = value?.trim()
            ns = ns?.trim()
            Identifier ident = Identifier.construct([value: value, reference: this, namespace: ns, nsType: Org.class.name])
            log.debug("Create new identifier: ${ident.getId()} ns:${ns} value:${value}")
        }
    }

    /**
     * Checks if the given institution has at least one enabled administrator. That enables the institution that it
     * administrates itself autonomously, without external intervention on behalf of a consortium or a superadmin
     * @return true if there is at least one enabled {@link User} belonging to this institution who has the {@link Role} INST_ADM, false otherwise
     */
    boolean hasInstAdminEnabled() {
        List<Long> admins = User.executeQuery(
                'select u.id from User u where u.formalOrg = :fo and u.formalRole = :fr and u.enabled = true',
                [fo: this, fr: Role.findByAuthority('INST_ADM')])
        admins.size() > 0
    }

    /**
     * Checks if there is an institutional administrator registered to this institution
     * @return true if there is at least one user registered as institutional administrator, false otherwise
     */
    boolean hasInstAdmin(){
        if (User.findAllByFormalOrgAndFormalRole(this, Role.findByAuthority('INST_ADM'))) {
            return true
        }
        else {
            return false
        }
    }

    /**
     * Lists all users affiliated to this institution
     * @return a {@link List} of {@link User}s associated to this institution; grouped by administrators, editors and users (with reading permissions only)
     */
    Map<String, Object> getUserMap(){
        Map<String, Object> result = [
                instAdms:       User.findAllByFormalOrgAndFormalRole(this, Role.findByAuthority('INST_ADM')),
                instEditors:    User.findAllByFormalOrgAndFormalRole(this, Role.findByAuthority('INST_EDITOR')),
                instUsers:      User.findAllByFormalOrgAndFormalRole(this, Role.findByAuthority('INST_USER'))
        ]

        return result
    }

    /**
     * Substitution caller for {@link #dropdownNamingConvention(de.laser.Org)}; substitutes with the context institution
     * @return this organisation's name according to the dropdown naming convention (<a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">see here</a>)
     */
    String dropdownNamingConvention() {
        return dropdownNamingConvention(BeanStore.getContextService().getOrg())
    }

    /**
     * Displays this organisation's name according to the dropdown naming convention as specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @param contextOrg the institution whose perspective should be taken
     * @return this organisation's name according to the dropdown naming convention
     */
    String dropdownNamingConvention(Org contextOrg){
        String result = ''
        if (contextOrg.isCustomerType_Inst()){
            if (name) {
                result += name
            }
        } else {
            if (sortname) {
                result += sortname
            }
            if (name) {
                result += ' (' + name + ')'
            }
        }
        result
    }

    /**
     * Gets the Leitweg-ID for this institution; the Leitweg-ID is necessary for the North-Rhine Westphalia billing system.
     * See <a href="https://www.land.nrw/de/e-rechnung-nrw">the pages of the NRW billing system (page in German)</a>
     * @return the {@link Identifier} of the {@link IdentifierNamespace#LEIT_ID}
     */
    Identifier getLeitID() {
        return Identifier.findByOrgAndNs(this, IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_ID))
    }

    /**
     * Checks if the organisation is being marked for the given user with the given marker type
     * @param user the {@link User} whose watchlist should be checked
     * @param type the {@link Marker.TYPE} of the marker to check
     * @return true if the organisation is marked, false otherwise
     */
    @Override
    boolean isMarked(User user, Marker.TYPE type) {
        Marker.findByOrgAndUserAndType(this, user, type) ? true : false
    }

    /**
     * Sets the marker for the organisation for given user of the given type
     * @param user the {@link User} for which the organisation should be marked
     * @param type the {@link Marker.TYPE} of marker to record
     */
    @Override
    void setMarker(User user, Marker.TYPE type) {
        if (!isMarked(user, type)) {
            Marker m = new Marker(org: this, user: user, type: type)
            m.save()
        }
    }

    /**
     * Removes the given marker with the given type for the organisation from the user's watchlist
     * @param user the {@link User} from whose watchlist the organisation marker should be removed
     * @param type the {@link Marker.TYPE} of marker to remove
     */
    @Override
    void removeMarker(User user, Marker.TYPE type) {
        withTransaction {
            Marker.findByOrgAndUserAndType(this, user, type).delete(flush:true)
        }
    }

    @Override
    int compareTo(Org o) {
        int result = sortname <=> o.sortname
        if(!result)
            name <=> o.name
        if(!result)
            id <=> o.id
        result
    }
}
