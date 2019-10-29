import de.laser.helper.RDStore
import geb.spock.GebReportingSpec
import org.elasticsearch.common.joda.time.LocalDate
import pages.*
import spock.lang.Stepwise
import com.k_int.kbplus.*

@Stepwise
class LicenseSpec extends GebReportingSpec {


    //The following will setup everything required for this test case
    def setupSpec(){
        def org = new com.k_int.kbplus.Org(name:Data.Org_name,impId:Data.Org_impId,sector:RefdataValue.findByValue('Higher Education')).save()
        def user = com.k_int.kbplus.auth.User.findByUsername(Data.UserA_name)
        def userAdm = com.k_int.kbplus.auth.User.findByUsername(Data.UserD_name)
        def formal_role = com.k_int.kbplus.auth.Role.findByAuthority('INST_ADM')
        def userOrg = new com.k_int.kbplus.auth.UserOrg(dateRequested:System.currentTimeMillis(),
                status:com.k_int.kbplus.auth.UserOrg.STATUS_APPROVED,
                org:org,
                user:user,
                formalRole:formal_role).save()
        def userOrgAdmin = new com.k_int.kbplus.auth.UserOrg(dateRequested:System.currentTimeMillis(),
                status:com.k_int.kbplus.auth.UserOrg.STATUS_APPROVED,
                org:org,
                user:userAdm,
                formalRole:formal_role).save()

        def licensee_role_ref = RefdataValue.getByValueAndCategory('Licensee', 'Organisational Role')
        def license  = new com.k_int.kbplus.License(reference:"Test License").save()
        def licensee_role  = new com.k_int.kbplus.OrgRole(roleType:licensee_role_ref,lic:license,org:org).save()


    }


    def "First log in to the system"(){
        setup:
          to PublicPage
        when:
          loginLink()
          at LogInPage
          login(Data.UserD_name, Data.UserD_passwd)
        then:
          at DashboardPage
    }
    // def "Test License Search"(){
    //   setup:
    //   def license = License.findByReference("Test License")
    //   go "myInstitutions/${Data.Org_Url}/currentLicenses"
    //   when:
    //     $("input",name:"validOn").value = date
    //     $("input",type:"submit",value:"Search").click()
    //     Thread.sleep
    //   then:
    //     openLicense("Test License")

    // }

    def "Test CustomProperties"(){
        def license = License.findByReference("Test License")
        setup:
          go '/laser/license/index/'+license.id
          at LicensePage
        when:
          addCustomPropType("Alumni Access")
          setRefPropertyValue("Alumni Access","No")
          deleteCustomProp("Alumni Access")
        then:
          at LicensePage
    }


    def "add items to list and submit to compare a license (license properties)"() {
        setup: "Going to license comparison page..."
          def org = Org.findByNameAndImpId(Data.Org_name,Data.Org_impId)
          def licensee_role_ref = RefdataValue.getByValueAndCategory('Licensee','Organisational Role')
          def ed       = new LocalDate().now().plusMonths(6).toDate()
          def sd       = new LocalDate().now().minusMonths(6).toDate()
          def l_status = RefdataValue.getByValueAndCategory('Current', 'License Status')
          def license2 = new com.k_int.kbplus.License(reference:"Test License 2", startDate: sd, endDate: ed, status: l_status).save()
          def license3 = new com.k_int.kbplus.License(reference:"Test License 3", startDate: sd, endDate: ed, status: l_status).save()
          def licensee_role2 = new com.k_int.kbplus.OrgRole(roleType:licensee_role_ref,lic:license2,org:org).save()
          def licensee_role3 = new com.k_int.kbplus.OrgRole(roleType:licensee_role_ref,lic:license3,org:org).save()
          go '/laser/licenseCompare/index?shortcode='+Data.Org_Url
          at LicenseComparePage
        when:
          compare(license2.reference,license3.reference)
        then:
          header() == "Compare Licenses (KB+ License Properties)"
          tableCount() > 0
    }


    def "license export test"() {
        setup:
          go "myInstitutions/${Data.Org_Url}/currentLicenses"
          at LicensePage
        when:
          searchLicense("","test")
          exportLicense("Licensed Issue Entitlements (CSV)")
          exportLicense("Licensed Subscriptions/Packages (CSV)")
        then:
          at LicensePage
    }

    def "log back into the system so we can change the configuration"(){
        setup:
          to PublicPage
        when:
          loginLink()
        at LogInPage
          login(Data.UserD_name, Data.UserD_passwd)
        then:
          at DashboardPage
    }

    def "Change org to have public journal access "() {
        setup: "Custom property page"
          go '/laser/organisation/config/'+Org.findByName(Data.Org_name).id
          at LicensePage
        when: "Properties are listed, find Public Journal Access"
          addCustomInputProperty(Data.License_publicProp_journals, Data.License_public_journals)
        then: "Value of Public Journal Access should have changed"
          rowResults() == 1 //default custom property for Org
          Thread.sleep(500)
          propertyChangedCheck(Data.License_publicProp_journals) == Data.License_public_journals
    }

}
