package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.web.servlet.mvc.GrailsParameterMap

import javax.persistence.Transient
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * This class reflects a title package which may be subscribed as a whole or partially (e.g. pick-and-choose).
 * This is a reflection of the we:kb-implementation of the package class (see <a href="https://github.com/hbz/wekb/blob/wekb-dev/server/gokbg3/grails-app/domain/org/gokb/cred/Package.groovy">here</a>).
 * If a package is being subscribed, the link between subscription and package is being represented by a {@link SubscriptionPackage} connection
 * @see TitleInstancePackagePlatform
 * @see Platform
 * @see SubscriptionPackage
 */
class Package extends AbstractBaseWithCalculatedLastUpdated {

    String name
    String sortname
    String gokbId
    String vendorURL
    String cancellationAllowances

    @RefdataInfo(cat = RDConstants.PACKAGE_CONTENT_TYPE)
    RefdataValue contentType

    @RefdataInfo(cat = RDConstants.PACKAGE_STATUS)
    RefdataValue packageStatus

    @RefdataInfo(cat = RDConstants.PACKAGE_BREAKABLE)
    RefdataValue breakable

    @RefdataInfo(cat = RDConstants.PACKAGE_CONSISTENT)
    RefdataValue consistent

    @RefdataInfo(cat = RDConstants.PACKAGE_FILE)
    RefdataValue file

    @RefdataInfo(cat = RDConstants.PACKAGE_SCOPE)
    RefdataValue scope

    boolean isPublic = false

    Platform nominalPlatform
    Date startDate
    Date endDate
    Set pendingChanges
    SortedSet ids
    SortedSet ddcs
    SortedSet languages
    SortedSet altnames
    Boolean autoAccept = false

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

static hasMany = [  tipps:     TitleInstancePackagePlatform,
                    orgs:      OrgRole,
                    prsLinks:  PersonRole,
                    documents: DocContext,
                    subscriptions:  SubscriptionPackage,
                    pendingChanges: PendingChange,
                    ids: Identifier,
                    ddcs: DeweyDecimalClassification,
                    languages: Language,
                    altnames: AlternativeName]

  static mappedBy = [tipps:     'pkg',
                     orgs:      'pkg',
                     prsLinks:  'pkg',
                     documents: 'pkg',
                     subscriptions: 'pkg',
                     pendingChanges: 'pkg',
                     ids:       'pkg',
                     ddcs:      'pkg',
                     languages: 'pkg',
                     altnames:  'pkg']


  static mapping = {
                    sort sortname: 'asc'
                      id column:'pkg_id'
                 version column:'pkg_version'
               globalUID column:'pkg_guid'
                    name column:'pkg_name'
                sortname column:'pkg_sort_name'
                  gokbId column:'pkg_gokb_id'
             contentType column:'pkg_content_type_rv_fk'
           packageStatus column:'pkg_status_rv_fk'
                    file column:'pkg_file_rv_fk'
               breakable column:'pkg_breakable_rv_fk'
              consistent column:'pkg_consistent_rv_fk'
         nominalPlatform column:'pkg_nominal_platform_fk'
               startDate column:'pkg_start_date',   index:'pkg_dates_idx'
                 endDate column:'pkg_end_date',     index:'pkg_dates_idx'
                isPublic column:'pkg_is_public'
            scope column:'pkg_scope_rv_fk'
               vendorURL column:'pkg_vendor_url'
  cancellationAllowances column:'pkg_cancellation_allowances', type:'text'
                     tipps batchSize: 10
            pendingChanges sort:'ts', order: 'asc', batchSize: 10
                  autoAccept column: 'pkg_auto_accept'
                 dateCreated column: 'pkg_date_created'
                 lastUpdated column: 'pkg_last_updated'
        lastUpdatedCascading column: 'pkg_last_updated_cascading'

            orgs            batchSize: 10
            prsLinks        batchSize: 10
            documents       batchSize: 10
            subscriptions   batchSize: 10
            ids             sort: 'ns', batchSize: 10
            ddcs            batchSize: 10
            languages       batchSize: 10
  }

  static constraints = {
                 globalUID(nullable:true, blank:false, unique:true, maxSize:255)
               contentType (nullable:true)
             packageStatus (nullable:true)
           nominalPlatform (nullable:true)
                 breakable (nullable:true)
                consistent (nullable:true)
                 startDate (nullable:true)
                   endDate (nullable:true)
                     scope (nullable:true)
                      file (nullable:true)
                    gokbId(blank:false, unique: true, maxSize: 511)
                 vendorURL(nullable:true, blank:false)
    cancellationAllowances(nullable:true, blank:false)
                  sortname(nullable:true, blank:false)
      lastUpdatedCascading (nullable:true)
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

    /**
     * Gets the content provider of this package
     * @return the {@link Org} linked to this package by {@link OrgRole} of type Content Provider or Provider
     */
  @Transient
  Org getContentProvider() {
    Org result
    orgs.each { OrgRole or ->
      if ( or.roleType in [RDStore.OR_CONTENT_PROVIDER, RDStore.OR_PROVIDER] )
        result = or.org
    }
    result
  }

    /**
     * Outputs this package's name and core data for labelling
     * @return the concatenated label of this package
     */
    String getLabel() {
        name + (nominalPlatform ? ' (' + nominalPlatform.name + ')' : '')
    }

    /**
     * Returns a string representation of this package
     * @return the name and id of this package
     */
  String toString() {
    name ? "${name}" : "Package ${id}"
  }

    /**
     * Returns a dropdown-formatted list of packages, filtered by the given config map.
     * @param params the parameter map to filter the entries against
     * @return a {@link List} of {@link Map}s, in format [id: {id}, text: {text}]
     */
  @Transient
  static def refdataFind(GrailsParameterMap params) {
    List<Map<String, Object>> result = []

    String hqlString = "select pkg from Package pkg where lower(pkg.name) like ? "
    def hqlParams = [((params.q ? params.q.toLowerCase() : '' ) + "%")]
      SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMdd()

    if(params.hasDate ){
        Date startDate = params.startDate.length() > 1 ? sdf.parse(params.startDate) : null
        Date endDate =  params.endDate.length() > 1 ? sdf.parse(params.endDate)  : null

      if(startDate) {
        hqlString += " AND pkg.startDate >= ?"
        hqlParams += startDate
      }
      if(endDate) {
        hqlString += " AND pkg.endDate <= ?"
        hqlParams += endDate
      }
    }

    if(params.hideDeleted == 'true'){
        hqlString += " AND pkg.packageStatus != ?"
        hqlParams += RDStore.PACKAGE_STATUS_DELETED
    }

    List queryResults = Package.executeQuery(hqlString, hqlParams)

    queryResults?.each { t ->
      def resultText = t.name
      String date = t.startDate? " (${sdf.format(t.startDate)})" :""
      resultText = params.inclPkgStartDate == "true" ? resultText + date : resultText
      resultText = params.hideIdent == "true" ? resultText : resultText+" (${t.identifier})"
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result
  }

    @Override
    def beforeInsert() {
        if ( name != null ) {
            sortname = generateSortName(name)
        }

        super.beforeInsertHandler()
    }

    @Override
    def beforeUpdate() {
        if ( name != null ) {
            sortname = generateSortName(name)
        }
        super.beforeUpdateHandler()
    }

    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Generates a sortable title string from the package's name, i.e. removes stopwords, performs normalising etc.
     * @param input_title the name to normalise
     * @return the normalised name string
     * @see Normalizer.Form#NFKD
     */
  static String generateSortName(String input_title) {
    if (!input_title) return null
    String s1 = Normalizer.normalize(input_title, Normalizer.Form.NFKD).trim().toLowerCase()
      Pattern punctuation = Pattern.compile('\\p{P}')
      s1 = s1.replaceAll(punctuation, '')
    s1 = s1.replaceFirst('^copy of ','')
    s1 = s1.replaceFirst('^the ','')
    s1 = s1.replaceFirst('^a ','')
    s1 = s1.replaceFirst('^an ','')
    s1 = s1.replaceFirst('^der ','')
    s1 = s1.replaceFirst('^die ','')
    s1 = s1.replaceFirst('^das ','')
    s1 = s1.replaceFirst('^l\'','')
    s1 = s1.replaceFirst('^le ','')
    s1 = s1.replaceFirst('^la ','')
    s1 = s1.replaceFirst('^les ','')
    s1 = s1.replaceFirst('^des ','')
    s1 = s1.replaceAll('°', ' ') //no trailing or leading °, so trim() is not needed
    return s1.trim()

  }

    /**
     * Retrieves the identifier of this package responding to the given namespace
     * If multiple identifiers respond to the same namespace, the LAST instance is being retrieved.
     * This method is thus subject of refactoring
     * @param idtype the namespace string to find
     * @return the or the last {@link Identifier} in the set of identifiers, responding to the identifier namespace
     */
    Identifier getIdentifierByType(String idtype) {
        Identifier result
        ids.each { ident ->
            if ( ident.ns.ns.equalsIgnoreCase(idtype) ) {
                result = ident
            }
        }
        result
    }

    /**
     * Retrieves all titles in this packages which are marked as current
     * @return a {@link Set} of {@link TitleInstancePackagePlatform}s which are marked as current
     */
    Set<TitleInstancePackagePlatform> getCurrentTipps() {
        Set<TitleInstancePackagePlatform> result = []
        if (this.tipps) {
            result = this.tipps?.findAll{it?.status?.id == RDStore.TIPP_STATUS_CURRENT.id}
        }
        result
    }

    /**
     * Called from linkPackages.gsp
     * Outputs the name of this package and how many titles are marked as current
     * @return a concatenated string of this package's name and the number of {@link TitleInstancePackagePlatform}s marked as current
     */
    String getPackageNameWithCurrentTippsCount() {
        return this.name + ' ('+ TitleInstancePackagePlatform.countByPkgAndStatus(this, RDStore.TIPP_STATUS_CURRENT) +')'
    }

}
