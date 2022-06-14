package de.laser


import de.laser.properties.PersonProperty
import de.laser.titles.TitleInstance
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation
import groovy.util.logging.Slf4j
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * A person is a contact entity; it does not need to be a person in the strict sense but also a collective or a
 * functional entity. Contact is thus used synonymously to person in this context, despite the Contact domain class.
 * A person may be attached to one or more organisations, titles, packages, licenses or subscriptions. This link is
 * implemented by the {@link PersonRole} structure. As the contact details towards a person may vary from institution to
 * institution, contact details such as addresses are stored separately from the person instance. Moreover, contacts may
 * be public or private, i.e. visibility is restricted to the institution which set up the contact
 * @see Address
 * @see Contact
 * @see PersonRole
 */
@Slf4j
class Person extends AbstractBaseWithCalculatedLastUpdated {

    static Log static_logger = LogFactory.getLog(Person)

    String       title
    String       first_name
    String       middle_name
    String       last_name
    Org          tenant

    @RefdataAnnotation(cat = RDConstants.GENDER)
    RefdataValue gender

    boolean isPublic = false

    @RefdataAnnotation(cat = RDConstants.PERSON_CONTACT_TYPE)
    RefdataValue contactType

    @Deprecated
    @RefdataAnnotation(cat = RDConstants.PERSON_POSITION)
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
        addresses           cascade: 'all', lazy: false
        contacts            cascade: 'all', lazy: false
        propertySet   cascade: 'all', batchSize: 10

        dateCreated column: 'prs_date_created'
        lastUpdated column: 'prs_last_updated'
        lastUpdatedCascading column: 'prs_last_updated_cascading'
    }
    
    static mappedBy = [
        roleLinks:          'prs',
        addresses:          'prs',
        contacts:           'prs',
        propertySet:        'owner'
    ]
  
    static hasMany = [
            roleLinks: PersonRole,
            addresses: Address,
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

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    /**
     * Gets all reference values in the given category string; is a mirror of the method in {@link RefdataValue}
     * @param category the reference data category to look for
     * @return a {@link List} of {@link RefdataValue}s matching the given category
     * @see RefdataValue
     * @see RefdataCategory#getAllRefdataValues(java.lang.String)
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
     * Gets a person with the following parameters:
     * @param firstName first name of the person
     * @param lastName last name of the person or name of the collective
     * @param tenant the institution ({@link Org}) who created the person
     * @param isPublic the flag whether the contact point is public (= visible to everyone)
     * @param contactType the distinction between a real person and a collective contact point; one of Functional Contact or Personal Contact
     * @param org the organisation to which the contact is attached to
     * @param functionType the type of function the person has, one of the {@link RDConstants#PERSON_FUNCTION} reference values
     * @return a {@link List} of persons/contact points matching the given arguments, an empty list if nothing is found
     */
    static Person lookup(String firstName, String lastName, Org tenant, boolean isPublic, RefdataValue contactType, Org org, RefdataValue functionType) {

        Person person
        List<Person> prsList = []

        Person.findAllWhere(
                first_name: firstName,
                last_name: lastName,
                contactType: contactType,
                isPublic: isPublic,
                tenant: tenant
        ).each{ Person p ->
            if (PersonRole.findWhere(prs: p, functionType: functionType, org: org)) {
                prsList << p
            }
        }
        if ( prsList.size() > 0 ) {
            person = prsList.get(0)
        }
        person
    }

    /**
     * Retrieves all public persons attached to the given organisation and of the given function type
     * @param org the organisation whose contacts should be retrieved
     * @param func the function type string to get
     * @return a {@link List} of persons matching to the given function type and attached to the given {@link Org}
     */
    static List<Person> getPublicByOrgAndFunc(Org org, String func) {
        Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org and pr.functionType.value = :functionType",
                [org: org, functionType: func]
        )
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
                        if(publicContactMap[pr.org])
                            publicContactMap[pr.org].add(c.content)
                        else {
                            publicContactMap[pr.org] = new HashSet()
                            publicContactMap[pr.org].add(c.content)
                        }
                    }
                }
            }
            else {
                p.contacts.each { Contact c ->
                    if(c.contentType == RDStore.CCT_EMAIL && p.tenant == contextOrg) {
                        if(privateContactMap[pr.org])
                            privateContactMap[pr.org].add(c.content)
                        else {
                            privateContactMap[pr.org] = new HashSet()
                            privateContactMap[pr.org].add(c.content)
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
     * @param org the {@link Org} to which the contacts are attached to
     * @param obj the object (one of {@link License}, {@link Package} or {@link Subscription}) for which the requested persons are responsible
     * @param resp the responsibility of the persons requested
     * @return a {@link List} of persons attached to the given organisation and object and matching to the given responsibility
     */
    static List<Person> getPublicByOrgAndObjectResp(Org org, def obj, String resp) {
        String q = ''
        def p = org ? ['org': org, 'resp': resp] : ['resp': resp]

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
        if (obj instanceof TitleInstance) {
            q = ' and pr.title = :obj '
            p << ['obj': obj]
        }

        List<Person> result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic = true " +
                        (org ? "and pr.org = :org " : "" ) +
                        "and pr.responsibilityType.value = :resp " + q,
                p
        )
        result
    }

    /**
     * Gets the private contact points attached to the given organisation, maintained by the given tenant institution and matching to the given function type
     * @param org the {@link Org} to which the contacts are attached to
     * @param func the function type of the contacts to be retrieved (one of Functional Contact or Personal Contact)
     * @param tenant the tenant institution ({@link Org}) whose contacts should be retrieved
     * @return a {@link List} of persons of the given function type, attached to the given organisation and maintained by the given tenant
     */
    static List<Person> getPrivateByOrgAndFuncFromAddressbook(Org org, String func, Org tenant) {
        List<Person> result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic = false and pr.org = :org and pr.functionType.value = :functionType and p.tenant = :tenant",
                [org: org, functionType: func, tenant: tenant]
        )
        result
    }

    /**
     * Gets all private contacts attached to the given organisation and object, matching to the given responsibility and maintained by the given tenant institution
     * @param org the {@link Org} to which the persons are attached to
     * @param obj the object (one of {@link License}, {@link Package} or {@link Subscription}) for which the requested persons are responsible
     * @param resp the responsibility which the requested persons have
     * @param tenant the tenant institution ({@link Org}) whose private contacts (= private addressbook) should be consulted
     * @return a {@link List} of persons matching to the given responsibility, attached to the given organisation and object and maintained by the given tenant institution
     */
    static List<Person> getPrivateByOrgAndObjectRespFromAddressbook(Org org, def obj, String resp, Org tenant) {
        String q = ''
        def p = ['org': org, 'resp': resp, 'tnt': tenant]

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
        if (obj instanceof TitleInstance) {
            q = ' and pr.title = :obj '
            p << ['obj': obj]
        }

        List<Person> result = Person.executeQuery(
                "select p from Person as p inner join p.roleLinks pr where p.isPublic = false and pr.org = :org and pr.responsibilityType.value = :resp and p.tenant = :tnt " + q,
                p
        )
        result
    }

    /**
     * Retrieves all person-organisation links which point to the given organisation
     * @param org the {@link Org} to which the persons are linked to
     * @return a {@link Set} of {@link PersonRole} links pointing to the given {@link Org}
     */
    LinkedHashSet<PersonRole> getPersonRoleByOrg(Org org) {
        return roleLinks.findAll {it.org?.id == org.id}
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
