package de.laser

import com.k_int.kbplus.IssueEntitlement
import de.laser.helper.RDStore
import grails.transaction.Transactional

import java.text.SimpleDateFormat

@Transactional
class TitleStreamService {

    def contextService
    def messageSource
    SimpleDateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd')

    /**
     * Generates a title stream export list according to the KBart II-standard but enriched with additional identifiers such as ZDB-ID
     * The standard is as defined on {@see <a href="https://www.uksg.org/kbart/s5/guidelines/data_fields">KBart definition</a>}
     *
     * @param entitlementData - a {@link List} containing the actual data
     * @return a {@link Map} containing lists for the title row and the column data
     */
    Map<String,List> generateTitleExportList(List entitlementData) {
        Map<String,List> export = [titleRow:[
                'publication_title',
                'print_identifier',
                'online_identifier',
                'date_first_issue_online',
                'num_first_vol_online',
                'num_first_issue_online',
                'date_last_issue_online',
                'num_last_vol_online',
                'num_last_issue_online',
                'title_url',
                'first_author',
                'title_id',
                'embargo_info',
                'coverage_depth',
                'notes',
                'publisher_name',
                'publication_type',
                'date_monograph_published_print',
                'date_monograph_published_online',
                'monograph_volume',
                'monograph_edition',
                'first_editor',
                'parent_publication_title_id',
                'preceding_publication_title_id',
                'access_type',
                'access_start_date',
                'access_end_date',
                'zdb_id',
                'zdb_ppn',
                'DOI',
                'ISSNs',
                'eISSNs',
                'pISBNs',
                'ISBNs'
        ],columnData:[]]
        entitlementData.each { ieObj ->
            IssueEntitlement entitlement = (IssueEntitlement) ieObj
            List row = []
            log.debug("processing ${entitlement.tipp.title}")
            //publication_title
            row.add("${entitlement.tipp.title.title} (${entitlement.subscription.dropdownNamingConvention(contextService.org)})")
            log.debug("add main identifiers")
            //print_identifier - namespace pISBN is proprietary for LAS:eR because no eISBN is existing and ISBN is used for eBooks as well
            if(entitlement.tipp.title.getIdentifierValue('pISBN'))
                row.add(entitlement.tipp.title.getIdentifierValue('pISBN'))
            else if(entitlement.tipp.title.getIdentifierValue('ISSN'))
                row.add(entitlement.tipp.title.getIdentifierValue('ISSN'))
            else row.add(' ')
            //online_identifier
            if(entitlement.tipp.title.getIdentifierValue('ISBN'))
                row.add(entitlement.tipp.title.getIdentifierValue('ISBN'))
            else if(entitlement.tipp.title.getIdentifierValue('eISSN'))
                row.add(entitlement.tipp.title.getIdentifierValue('eISSN'))
            else row.add(' ')
            log.debug("process package start and end")
            //date_first_issue_online
            row.add(entitlement.startDate ? dateFormat.format(entitlement.startDate) : ' ')
            //num_first_volume_online
            row.add(entitlement.startVolume ?: ' ')
            //num_first_issue_online
            row.add(entitlement.startIssue ?: ' ')
            //date_last_issue_online
            row.add(entitlement.endDate ? dateFormat.format(entitlement.endDate) : ' ')
            //num_last_volume_online
            row.add(entitlement.endVolume ?: ' ')
            //num_last_issue_online
            row.add(entitlement.endIssue ?: ' ')
            log.debug("add title url")
            //title_url
            row.add(entitlement.tipp.hostPlatformURL ?: ' ')
            //first_author (no value?)
            row.add(' ')
            //title_id (no value?)
            row.add(' ')
            //embargo_information
            row.add(entitlement.embargo ?: ' ')
            //coverage_depth
            row.add(entitlement.coverageDepth ?: ' ')
            //notes
            row.add(entitlement.coverageNote ?: ' ')
            //publisher_name (no value?)
            row.add(' ')
            //publication_type
            switch(entitlement.tipp.title.type) {
                case RDStore.TITLE_TYPE_JOURNAL: row.add('serial')
                    break
                case RDStore.TITLE_TYPE_EBOOK: row.add('monograph')
                    break
                default: row.add(' ')
                    break
            }
            //date_monograph_published_print (no value?)
            row.add(' ')
            //date_monograph_published_online (no value?)
            row.add(' ')
            //monograph_volume (no value?)
            row.add(' ')
            //monograph_edition (no value?)
            row.add(' ')
            //first_editor (no value?)
            row.add(' ')
            //parent_publication_title_id (no value?)
            row.add(' ')
            //preceding_publication_title_id (no value?)
            row.add(' ')
            //access_type
            switch(entitlement.tipp.payment) {
                case RDStore.TIPP_PAYMENT_OA: row.add('F')
                    break
                case RDStore.TIPP_PAYMENT_PAID: row.add('P')
                    break
                default: row.add(' ')
                    break
            }
            //access_start_date
            row.add(entitlement.derivedAccessStartDate ? dateFormat.format(entitlement.derivedAccessStartDate) : ' ')
            //access_end_date
            row.add(entitlement.derivedAccessEndDate ? dateFormat.format(entitlement.derivedAccessEndDate) : ' ')
            log.debug("processing identifiers")
            //zdb_id
            row.add(entitlement.tipp.title.joinIdentfiers('zdb',','))
            //zdb_ppn
            row.add(entitlement.tipp.title.joinIdentfiers('zdb_ppn',','))
            //DOI
            row.add(entitlement.tipp.title.joinIdentfiers('doi',','))
            //ISSNs
            row.add(entitlement.tipp.title.joinIdentfiers('issn',','))
            //eISSNs
            row.add(entitlement.tipp.title.joinIdentfiers('eissn',','))
            //pISBNs
            row.add(entitlement.tipp.title.joinIdentfiers('pisbn',','))
            //ISBNs
            row.add(entitlement.tipp.title.joinIdentfiers('isbn',','))
            export.columnData.add(row)
        }
        export
    }

}
