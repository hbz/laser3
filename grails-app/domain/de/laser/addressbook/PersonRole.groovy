package de.laser.addressbook

import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import de.laser.wekb.Package
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor

/**
 * This class ensures connections between {@link de.laser.addressbook.Person}s and {@link Org}s. Moreover, a person role may be specified to an instance of certain other objects like {@link de.laser.OrgRole} does it for organisations.
 * Possible objects to restrict are:
 * <ul>
 *     <li>{@link License}</li>
 *     <li>{@link de.laser.wekb.Package}</li>
 *     <li>{@link Subscription}</li>
 *     <li>{@link de.laser.wekb.TitleInstancePackagePlatform}</li>
 * </ul>
 * Unlike in ${@link de.laser.OrgRole}, the link between the person and the object other than organisation is not typised; it serves rather as a specification for the connection between the person and the organisation, e.g.
 * person A is a general contact of organisation B about the subscription C.
 * The relation of the person and the organisation is typised by three distinctive ways which exclude each other. There are several types, using each different sets of reference values:
 * <ol>
 *     <li>function type</li>
 *     <li>position type</li>
 *     <li>responsibility type</li>
 * </ol>
 * 1. uses the reference category {@link RDConstants#PERSON_FUNCTION},
 * 2. the reference category {@link RDConstants#PERSON_POSITION} and
 * 3. the reference category {@link RDConstants#PERSON_RESPONSIBILITY}
 */
class PersonRole implements Comparable<PersonRole>{
    public static final String REFDATA_GENERAL_CONTACT_PRS = "General contact person"

    /**
     * The person has a certain position at the given organisation. Is exclusive with other types
     */
    @RefdataInfo(cat = RDConstants.PERSON_POSITION)
    RefdataValue    positionType

    /**
     * The person has a certain function at the given organisation. Is exclusive with other types
     */
    @RefdataInfo(cat = RDConstants.PERSON_FUNCTION)
    RefdataValue    functionType

    /**
     * The person has a certain responsibility at the given organisation, the responsibility concerns usually a certain object. Is exclusive with other types
     * @see #setReference(java.lang.Object)
     */
    @RefdataInfo(cat = RDConstants.PERSON_RESPONSIBILITY)
    RefdataValue    responsibilityType

    License                        lic
    Package                        pkg
    Subscription                   sub
    TitleInstancePackagePlatform   tipp
    Date                           start_date
    Date                           end_date

    Date dateCreated
    Date lastUpdated
    
    static belongsTo = [
        prs:        Person,
        org:        Org,
        provider:   Provider,
        vendor:     Vendor,
    ]

    static transients = ['reference'] // mark read-only accessor methods
    
    static mapping = {
        id          column:'pr_id'
        version     column:'pr_version'
        positionType            column:'pr_position_type_rv_fk'
        functionType            column:'pr_function_type_rv_fk'
        responsibilityType      column:'pr_responsibility_type_rv_fk'
        prs         column:'pr_prs_fk',     index: 'pr_prs_org_idx, pr_prs_ven_idx, pr_prs_prov_idx'
        lic         column:'pr_lic_fk'
        org         column:'pr_org_fk',     index: 'pr_prs_org_idx'
        pkg         column:'pr_pkg_fk'
        provider    column:'pr_provider_fk',  index: 'pr_prs_prov_idx'
        sub         column:'pr_sub_fk'
        tipp        column:'pr_tipp_fk'
        vendor      column:'pr_vendor_fk',  index: 'pr_prs_ven_idx'
        start_date  column:'pr_startdate'
        end_date    column:'pr_enddate'
        
        dateCreated column: 'pr_date_created'
        lastUpdated column: 'pr_last_updated'
    }
    
    static constraints = {
        positionType        (nullable:true)
        functionType        (nullable:true)
        responsibilityType  (nullable:true)
        lic         (nullable:true)
        org         (nullable:true)
        pkg         (nullable:true)
        provider    (nullable:true)
        sub         (nullable:true)
        tipp        (nullable:true)
        vendor      (nullable:true)
        start_date  (nullable:true)
        end_date    (nullable:true)
        lastUpdated (nullable: true)
    }

    /**
     * Generic setter method; indicating the reference objects which may be attached to the {@link Person} to be linked
     */
    void setReference(def owner) {
        org      = owner instanceof Org ? owner : org
        lic      = owner instanceof License ? owner : lic
        pkg      = owner instanceof Package ? owner : pkg
        provider = owner instanceof Provider ? owner : provider
        sub      = owner instanceof Subscription ? owner : sub
        tipp     = owner instanceof TitleInstancePackagePlatform ? owner : tipp
        vendor   = owner instanceof Vendor ? owner : vendor
    }

    /**
     * Gets the reference object which specifies the connection between the {@link Person} and the {@link Org}
     * @return the reference object; one of {@link License}, {@link Package}, {@link Subscription} or {@link TitleInstancePackagePlatform}
     */
    String getReference() {
        if (lic)        return 'lic:' + lic.id
        if (pkg)        return 'pkg:' + pkg.id
        if (sub)        return 'sub:' + sub.id
        if (tipp)       return 'title:' + tipp.id
    }

    /**
     * Gets the role type of this link
     * @return one of {@link #functionType}, {@link #positionType} or {@link #responsibilityType} {@link RefdataValue}s
     */
    RefdataValue getRoleType() {
        if(functionType)
            functionType
        else if(positionType)
            positionType
        else if(responsibilityType)
            responsibilityType
        else null
    }

    /**
     * A mirror of {@link de.laser.RefdataCategory#getAllRefdataValues(java.lang.String)}; gets all reference values of the given reference category string
     * @param category the reference value category to retrieve
     * @return a {@link List} of {@link RefdataValue}s matching the given category
     */
    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)//.sort {it.getI10n("value")}
    }

    /**
     * Gets the first person-org link between the given {@link Person} and {@link Org}/{@link Provider}/{@link Vendor} matching the given responsibility type
     * @param prs the {@link Person} from which the link points to
     * @param org the {@link Org}, {@link Provider} or {@link Vendor} to which the link points to
     * @param resp the responsibility type (one of the {@link RDConstants#PERSON_RESPONSIBILITY} reference value strings) which exists between the person and the organisation
     * @return a {@link PersonRole} matching the given responsibility type and linking the given person with the organisation
     */
    static PersonRole getByPersonAndOrgAndRespValue(Person prs, def org, String resp) {
        Map<String, Object> configMap = [prs: prs, responsibilityType: RefdataValue.getByValueAndCategory(resp, RDConstants.PERSON_RESPONSIBILITY)]
        if(org instanceof Org) {
            configMap.org = org
        }
        else if(org instanceof Provider) {
            configMap.provider = org
        }
        else if(org instanceof Vendor) {
            configMap.vendor = org
        }
        List<PersonRole> result = PersonRole.findAllWhere(configMap)

        result.first()
    }

    static Set<PersonRole> getAllRolesByOwner(Person p, owner) {
        Set<PersonRole> result = []
        if(owner instanceof Org)
            result.addAll(PersonRole.findAllByPrsAndOrgAndFunctionTypeIsNotNull(p, owner as Org))
        else if(owner instanceof Provider)
            result.addAll(PersonRole.findAllByPrsAndProviderAndFunctionTypeIsNotNull(p, owner as Provider))
        else if(owner instanceof Vendor)
            result.addAll(PersonRole.findAllByPrsAndVendorAndFunctionTypeIsNotNull(p, owner as Vendor))
        result
    }

    /**
     * Comparator method between two person role links; compared are the function types; if they are equal, the person last, then first names are being compared with each other
     * @param that the object to compare with
     * @return the comparison result (-1, 0 or 1)
     */
    @Override
    int compareTo(PersonRole that) {
        String this_FunctionType = this?.functionType?.value
        String that_FunctionType = that?.functionType?.value
        int result

        if  (REFDATA_GENERAL_CONTACT_PRS == this_FunctionType){
            if (REFDATA_GENERAL_CONTACT_PRS == that_FunctionType) {
                String this_Name = (this?.prs?.last_name + this?.prs?.first_name)?:""
                String that_Name = (that?.prs?.last_name + that?.prs?.first_name)?:""
                result = (this_Name)?.compareTo(that_Name)
            } else {
                result = -1
            }
        } else {
            if (REFDATA_GENERAL_CONTACT_PRS == that_FunctionType) {
                result = 1
            } else {
                String this_fkType = (this?.functionType?.getI10n('value'))?:""
                String that_fkType = (that?.functionType?.getI10n('value'))?:""
                result = this_fkType?.compareTo(that_fkType)
            }
        }
        result
    }

}
