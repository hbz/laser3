package com.k_int.kbplus.api.v0.converter

import com.k_int.kbplus.Doc
import com.k_int.kbplus.api.v0.base.OutService
import de.laser.domain.Constants
import grails.converters.JSON
import groovy.json.*
import groovy.util.logging.Log4j

@Log4j
class KbartService {

    OutService outService

    static final KBART2_HEADER = [
        "publication_title",
        "print_identifier",
        "online_identifier",
        "date_first_issue_online",
        "num_first_vol_online",
        "num_first_issue_online",
        "date_last_issue_online",
        "num_last_vol_online",
        "num_last_issue_online",
        "title_url",
        "first_author",
        "title_id",
        "embargo_info",
        "coverage_depth",
        "notes",
        "publisher_name",
        "publication_type",
        "date_monograph_published_print",
        "date_monograph_published_online",
        "monograph_volume",
        "monograph_edition",
        "first_editor",
        "parent_publication_title_id",
        "preceding_publication_title_id",
        "access_type",
        "DOI",
        "ISSNs",
        "eISSNs",
        "ISBNs",
        "eISBNs",
        "access_start_date",
        "access_end_date",
        "access_status"
    ]

    /**
     * @return
     */
    def convertIssueEntitlements(JSON json) {

        // TODO .. incomplete Mapping
        def output

        List kbart = []
        kbart.add(KBART2_HEADER.join("\t"))

        def data = new JsonSlurper().parseText(json.toString())
        data.each{ ie ->
            String[] row = new String[KBART2_HEADER.size()]

            row[ KBART2_HEADER.indexOf("publication_title") ]        = normValue( ie.tipp?.title?.title )
            row[ KBART2_HEADER.indexOf("print_identifier") ]         = normValue( ie.tipp?.title?.identifiers?.find{ it.namespace == "issn" }?.value )
            row[ KBART2_HEADER.indexOf("online_identifier") ]        = normValue( ie.tipp?.title?.identifiers?.find{ it.namespace == "eissn" }?.value )

            row[ KBART2_HEADER.indexOf("date_first_issue_online") ]  = normValue( ie.tipp?.startDate )
            row[ KBART2_HEADER.indexOf("num_first_vol_online") ]     = normValue( ie.tipp?.startVolume )
            row[ KBART2_HEADER.indexOf("num_first_issue_online") ]   = normValue( ie.tipp?.startIssue )
            row[ KBART2_HEADER.indexOf("date_last_issue_online") ]   = normValue( ie.tipp?.endDate )
            row[ KBART2_HEADER.indexOf("num_last_vol_online") ]      = normValue( ie.tipp?.endVolume )
            row[ KBART2_HEADER.indexOf("num_last_issue_online") ]    = normValue( ie.tipp?.endIssue )

            row[ KBART2_HEADER.indexOf("title_url") ]                = normValue( ie.tipp?.hostPlatformURL )
            row[ KBART2_HEADER.indexOf("first_author") ]             = ""
            row[ KBART2_HEADER.indexOf("title_id") ]                 = ""

            row[ KBART2_HEADER.indexOf("embargo_info") ]             = normValue( ie.embargo )
            row[ KBART2_HEADER.indexOf("coverage_depth") ]           = normValue( ie.coverageDepth )
            row[ KBART2_HEADER.indexOf("notes") ]                    = normValue( ie.coverageNote )

            row[ KBART2_HEADER.indexOf("publisher_name") ]                   = normValue( ie.tipp?.title?.publisher?.name )
            row[ KBART2_HEADER.indexOf("publication_type") ]                 = ""
            row[ KBART2_HEADER.indexOf("date_monograph_published_print") ]   = ""
            row[ KBART2_HEADER.indexOf("date_monograph_published_online") ]  = ""
            row[ KBART2_HEADER.indexOf("monograph_volume") ]                 = ""
            row[ KBART2_HEADER.indexOf("monograph_edition") ]                = ""
            row[ KBART2_HEADER.indexOf("first_editor") ]                     = ""
            row[ KBART2_HEADER.indexOf("parent_publication_title_id") ]      = ""
            row[ KBART2_HEADER.indexOf("preceding_publication_title_id") ]   = ""
            row[ KBART2_HEADER.indexOf("access_type") ]                      = ""

            row[ KBART2_HEADER.indexOf("DOI") ]      = normValue( ie.tipp?.title?.identifiers?.find{ it.namespace == "doi" }?.value )
            row[ KBART2_HEADER.indexOf("ISSNs") ]    = normValue( ie.tipp?.title?.identifiers?.findAll{ it.namespace == "issn" }?.value.join(", ") )
            row[ KBART2_HEADER.indexOf("eISSNs") ]   = normValue( ie.tipp?.title?.identifiers?.findAll{ it.namespace == "eissn" }?.value.join(", ") )
            row[ KBART2_HEADER.indexOf("ISBNs") ]    = normValue( ie.tipp?.title?.identifiers?.findAll{ it.namespace == "isbn" }?.value.join(", ") )
            row[ KBART2_HEADER.indexOf("eISBNs") ]   = normValue( ie.tipp?.title?.identifiers?.findAll{ it.namespace == "eisbn" }?.value.join(", ") )

            row[ KBART2_HEADER.indexOf("access_start_date") ]        = normValue( ie.accessStartDate )
            row[ KBART2_HEADER.indexOf("access_end_date") ]          = normValue( ie.accessEndDate )
            row[ KBART2_HEADER.indexOf("access_status") ]            = normValue( ie.status ) // ?

            kbart.add(row.join("\t"))
        }

        output = kbart.join("\n")
        output
    }

    def getAsDocument(def data) {
        def todo = new Doc(
                title:          "KBART-EXPORT",
                content:        data,
                contentType:    Doc.CONTENT_TYPE_STRING,
                mimeType:       Constants.MIME_TEXT_PLAIN
        )
        todo
    }

    def normValue(def value) {
        value ? value : ''
    }

    def normDate(def date) {
        // TODO in json
        date
    }
}
