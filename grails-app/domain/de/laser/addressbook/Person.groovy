package de.laser.addressbook

import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.properties.PersonProperty
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import groovy.util.logging.Slf4j

/**
 * A person is a contact entity; it does not need to be a person in the strict sense but also a collective or a
 * functional entity. Contact is thus used synonymously to person in this context, despite the Contact domain class.
 * A person may be attached to one or more organisations, titles, packages, licenses or subscriptions. This link is
 * implemented by the {@link PersonRole} structure. As the contact details towards a person may vary from institution to
 * institution, contact details such as addresses are stored separately from the person instance. Moreover, contacts may
 * be public or private, i.e. visibility is restricted to the institution which set up the contact
 * @see de.laser.addressbook.Address
 * @see de.laser.addressbook.Contact
 * @see PersonRole
 */
@Slf4j
class Person extends AbstractBaseWithCalculatedLastUpdated {

    String       title
    String       first_name
    String       middle_name
    String       last_name
    Org          tenant

    @RefdataInfo(cat = RDConstants.GENDER)
    RefdataValue gender

    boolean isPublic = false

    @RefdataInfo(cat = RDConstants.PERSON_CONTACT_TYPE)
    RefdataValue contactType

    @Deprecated
    @RefdataInfo(cat = RDConstants.PERSON_POSITION)
    RefdataValue roleType // TODO remove !?

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static mapping = {
        id              column:'prs_id'
        globalUID       column:'prs_guid'
        version         column:'prs_version'
        title           column:'prs_title'
        first_name      column:'prs_first_name'
        middle_name     column:'prs_middle_name'
        last_name       column:'prs_last_name'
        gender          column:'prs_gender_rv_fk'
        tenant          column:'prs_tenant_fk'
        isPublic        column:'prs_is_public'
        contactType     column:'prs_contact_type_rv_fk'
        roleType        column:'prs_role_type_rv_fk'

        roleLinks           cascade: 'all', batchSize: 10
        contacts            cascade: 'all', lazy: false
        propertySet   cascade: 'all', batchSize: 10

        dateCreated column: 'prs_date_created'
        lastUpdated column: 'prs_last_updated'
        lastUpdatedCascading column: 'prs_last_updated_cascading'
    }
    
    static mappedBy = [
        roleLinks:          'prs',
        contacts:           'prs',
        propertySet:        'owner'
    ]
  
    static hasMany = [
            roleLinks: PersonRole,
            contacts:  Contact,
            propertySet: PersonProperty
    ]
    
    static constraints = {
        globalUID   (nullable:true,  blank:false, unique:true, maxSize:255)
        title       (nullable:true,  blank:false)
        first_name  (nullable:true,  blank:false)
        middle_name (nullable:true,  blank:false)
        last_name   (blank:false)
        gender      (nullable:true)
        tenant      (nullable:true)
        contactType (nullable:true)
        roleType    (nullable:true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    /**
     * Gets all reference values in the given category string; is a mirror of the method in {@link RefdataValue}
     * @param category the reference data category to look for
     * @return a {@link List} of {@link RefdataValue}s matching the given category
     * @see RefdataValue
     * @see de.laser.RefdataCategory#getAllRefdataValues(java.lang.String)
     */
    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    /**
     * Returns the person details as string, in the order (title) last name, first name, middle name
     * @return a concatenated string of the person details
     */
    @Override
    String toString() {
        ((title ?: '') + ' ' + (last_name ?: ' ') + (first_name ? ', ' + first_name : '') + ' ' + (middle_name ?: '')).trim()
    }

    /**
     * Retrieves all public persons attached to the given organisation and of the given function type
     * @param target the organisation or vendor whose contacts should be retrieved
     * @param func the function type string to get
     * @param tenant the tenant organisation whose maintains
     * @return a {@link List} of persons matching to the given function type and attached to the given {@link Org}
     */
    static List<Person> getPublicByOrgAndFunc(target, String func, Org tenant = null) {
        String instanceFilter, tenantFilter = ''
        Map<String, Object> params = [functionType: func]
        if(target instanceof Org) {
            params.org = target
            instanceFilter = 'pr.org = :org'
        }
        else if(target instanceof Provider) {
            params.provider = target
            instanceFilter = 'pr.provider = :provider'
        }
        else if(target instanceof Vendor) {
            params.vendor = target
            instanceFilter = 'pr.vendor = :vendor'
        }
        if(tenant) {
            tenantFilter = 'and p.tenant = :tenant'
            params.tenant = tenant
        }
        if(instanceFilter) {
            Person.executeQuery(
                    'select p from Person as p inner join p.roleLinks pr where p.isPublic = true and '+instanceFilter+' '+tenantFilter+' and pr.functionType.value = :functionType',
                    params
            )
        }
        else []
    }

    /**
     * Gets all email addresses maintained by the context institution and matching to the given function type, ordered by public/private and then the org the person is being attached to
     * @param func the requested function type as string (Functional Contact or Personal Contact)
     * @param contextOrg the institution whose contacts should be retrieved
     * @return a {@link Map} of structure
     * {
     *     publicContacts: {
     *         org1: [mail1, mail2, ..., mailn],
     *         org2: [mail1, mail2, ..., mailn],
     *         ...,
     *         orgn: [mail1, mail2, ..., mailn]
     *     },
     *     privateContacts: {
     *         org1: [mail1, mail2, ..., mailn],
     *         org2: [mail1, mail2, ..., mailn],
     *         ...,
     *         orgn: [mail1, mail2, ..., mailn]
     *     }
     * }
     */
    static Map getPublicAndPrivateEmailByFunc(String func,Org contextOrg) {
        List allPersons = executeQuery('select p,pr from Person as p join p.roleLinks pr join p.contacts c where pr.functionType.value = :functionType',[functionType: func])
        Map publicContactMap = [:], privateContactMap = [:]
        allPersons.each { row ->
            Person p = (Person) row[0]
            PersonRole pr = (PersonRole) row[1]
            if(p.isPublic) {
                p.contacts.each { Contact c ->
                    if(c.contentType == RDStore.CCT_EMAIL) {
                        if(pr.org) {
                            if(!publicContactMap.containsKey(pr.org))
                                publicContactMap[pr.org] = new HashSet()
                            publicContactMap[pr.org].add(c.content)
                        }
                        else if(pr.vendor) {
                            if(!publicContactMap.containsKey(pr.vendor))
                                publicContactMap[pr.vendor] = new HashSet()
                            publicContactMap[pr.vendor].add(c.content)
                        }
                    }
                }
            }
            else {
                p.contacts.each { Contact c ->
                    if(c.contentType == RDStore.CCT_EMAIL && p.tenant == contextOrg) {
                        if(pr.org) {
                            if(!privateContactMap.containsKey(pr.org))
                                privateContactMap[pr.org] = new HashSet()
                            privateContactMap[pr.org].add(c.content)
                        }
                        else if(pr.vendor) {
                            if(!privateContactMap.containsKey(pr.vendor))
                                privateContactMap[pr.vendor] = new HashSet()
                            privateContactMap[pr.vendor].add(c.content)
                        }
                    }
                }
            }
        }
        [publicContacts: publicContactMap, privateContacts: privateContactMap]
    }

    /**
     * Gets all public contacts attached to the given organisation and object, matching to the given responsibility.
     * If org is null, this method gets ALL public responsibilities attached to the given object; if the object is missing, too, get all public responsibilities
     * @param target the {@link Org} to which the contacts are attached to
     * @param obj the object (one of {@link de.laser.License}, {@link de.laser.wekb.Package} or {@link de.laser.Subscription}) for which the requested persons are responsible
     * @param resp the responsibility of the persons requested
     * @return a {@link List} of persons attached to the given organisation and object and matching to the given responsibility
     */
    static List<Person> getPublicByOrgAndObjectResp(def target, def obj, String resp) {
        String q = '', targetClause = ''
        Map<String, Object> p = ['resp': resp]
        if(target) {
            if(target instanceof Org) {
                p.org = target
                targetClause = 'and pr.org = :org '
            }
            else if(target instanceof Provider) {
                p.provider = target
                targetClause = 'and pr.provider = :provider '
            }
            else if(target instanceof Vendor) {
                p.vendor = target
                targetClause = 'and pr.vendor = :vendor '
            }
        }

        if (obj instanceof License) {
            q = ' and pr.lic = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Package) {
            q = ' and pr.pkg = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Subscription) {
            q = ' and pr.sub = :obj '
            p << ['obj': obj]
        }

        List<Person> result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic = true " +
                        targetClause +
                        "and pr.responsibilityType.value = :resp " + q,
                p
        )
        result
    }

    /**
     * Gets the private contact points attached to the given organisation/vendor, maintained by the given tenant institution and matching to the given function type
     * @param target the {@link Org} or {@link Vendor} to which the contacts are attached to
     * @param func the function type of the contacts to be retrieved (one of Functional Contact or Personal Contact)
     * @param tenant the tenant institution ({@link Org}) whose contacts should be retrieved
     * @return a {@link List} of persons of the given function type, attached to the given organisation and maintained by the given tenant
     */
    static List<Person> getPrivateByOrgAndFuncFromAddressbook(target, String func) {
        String targetClause
        Map<String, Object> queryParams = [functionType: func, tenant: BeanStore.getContextService().getOrg()]
        List<Person> result
        if(target instanceof Org) {
            targetClause = 'pr.org = :org'
            queryParams.org = target
        }
        else if(target instanceof Provider) {
            targetClause = 'pr.provider = :provider'
            queryParams.provider = target
        }
        else if(target instanceof Vendor) {
            targetClause = 'pr.vendor = :vendor'
            queryParams.vendor = target
        }
        if (targetClause) {
            result = Person.executeQuery(
                    'select p from Person as p inner join p.roleLinks pr where p.isPublic = false and '+targetClause+' and pr.functionType.value = :functionType and p.tenant = :tenant',
                    queryParams
            )
        }
        else result = []
        result
    }

    /**
     * Gets all private contacts attached to the given organisation/vendor and object, matching to the given responsibility and maintained by the given tenant institution
     * @param target the {@link Org} or {@link Vendor} to which the persons are attached to
     * @param obj the object (one of {@link License}, {@link Package} or {@link Subscription}) for which the requested persons are responsible
     * @param resp the responsibility which the requested persons have
     * @param tenant the tenant institution ({@link Org}) whose private contacts (= private addressbook) should be consulted
     * @return a {@link List} of persons matching to the given responsibility, attached to the given organisation and object and maintained by the given tenant institution
     */
    static List<Person> getPrivateByOrgAndObjectRespFromAddressbook(target, def obj, String resp) {
        String q = '', targetClause
        Map<String, Object> p = ['resp': resp, 'tnt': BeanStore.getContextService().getOrg()]

        if (target instanceof Org) {
            p << ['org': target]
            targetClause = 'and pr.org = :org'
        }
        else if (target instanceof Provider) {
            p << ['provider': target]
            targetClause = 'and pr.provider = :provider'
        }
        else if (target instanceof Vendor) {
            p << ['vendor': target]
            targetClause = 'and pr.vendor = :vendor'
        }

        if (obj instanceof License) {
            q = ' and pr.lic = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Package) {
            q = ' and pr.pkg = :obj '
            p << ['obj': obj]
        }
        if (obj instanceof Subscription) {
            q = ' and pr.sub = :obj '
            p << ['obj': obj]
        }

        if(targetClause) {
            List<Person> result = Person.executeQuery(
                    'select p from Person as p inner join p.roleLinks pr where p.isPublic = false '+targetClause+' and pr.responsibilityType.value = :resp and p.tenant = :tnt ' + q,
                    p
            )
            result
        }
        else []
    }

    /**
     * Retrieves all person-organisation links which point to the given organisation
     * @param org the {@link Org} to which the persons are linked to
     * @return a {@link Set} of {@link PersonRole} links pointing to the given {@link Org}
     */
    LinkedHashSet<PersonRole> getPersonRoleByOrg(Org org) {
        return roleLinks.findAll {it.org?.id == org.id}
    }

    /**
     * Retrieves all person-provider links which point to the given provider
     * @param provider the {@link Provider} to which the persons are linked to
     * @return a {@link Set} of {@link PersonRole} links pointing to the given {@link Provider}
     */
    LinkedHashSet<PersonRole> getPersonRoleByProvider(Provider provider) {
        return roleLinks.findAll {it.provider?.id == provider.id}
    }

    /**
     * Retrieves all person-vendor links which point to the given vendor
     * @param vendor the {@link Vendor} to which the persons are linked to
     * @return a {@link Set} of {@link PersonRole} links pointing to the given {@link Vendor}
     */
    LinkedHashSet<PersonRole> getPersonRoleByVendor(Vendor vendor) {
        return roleLinks.findAll {it.vendor?.id == vendor.id}
    }

    /**
     * Retrieves all person-target links which point to the given person
     * @param target the {@link Org}, {@link Person} or {@link Vendor}, defined as a map, to which the persons are linked to
     * @return a {@link Set} of {@link PersonRole} links pointing to the given target
     */
    Set<PersonRole> getPersonRoleByTarget(Map target) {
        Set result = []
        if(target.org != null)
            result = roleLinks.findAll { PersonRole pr -> pr.org?.id == target.org.id}
        else if(target.provider != null)
            result = roleLinks.findAll { PersonRole pr -> pr.provider?.id == target.provider.id}
        else if(target.vendor != null)
            result = roleLinks.findAll { PersonRole pr -> pr.vendor?.id == target.vendor.id}
        result
    }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Called from {@link de.laser.ajax.AjaxHtmlController#editPerson()}
     * Retrieves the organisation to which this person is linked to
     * @return the {@link Org} to which this person is linked to
     */
    Org getBelongsToOrg() {

        List<Org> orgs = PersonRole.executeQuery(
                "select distinct(pr.org) from PersonRole as pr where pr.prs = :person ", [person: this]
        )

        if(orgs.size() > 0)
        {

            return orgs[0]
        }else {
            return null
        }

    }
}
