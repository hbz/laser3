package de.laser.api.v0

import de.laser.Doc
import de.laser.helper.Constants
import grails.converters.JSON
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Deprecated
@Slf4j
class ApiKbartConverter {

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
    @Deprecated
    static convertIssueEntitlements(JSON json) {

        // TODO .. incomplete Mapping
        def output

        List kbart = []
        kbart.add(KBART2_HEADER.join("\t"))

        def data = new JsonSlurper().parseText(json.toString())
        data.each{ ie ->
            ie.coverages.each { covStmt ->
                String[] row = new String[KBART2_HEADER.size()]

                row[ KBART2_HEADER.indexOf("publication_title") ]        = normValue( ie.tipp?.title?.title )
                row[ KBART2_HEADER.indexOf("print_identifier") ]         = normValue( ie.tipp?.title?.identifiers?.find{ it.namespace == "issn" }?.value )
                row[ KBART2_HEADER.indexOf("online_identifier") ]        = normValue( ie.tipp?.title?.identifiers?.find{ it.namespace == "eissn" }?.value )

                row[ KBART2_HEADER.indexOf("date_first_issue_online") ]  = normValue( ApiToolkit.formatInternalDate(covStmt.startDate) )
                row[ KBART2_HEADER.indexOf("num_first_vol_online") ]     = normValue( covStmt.startVolume )
                row[ KBART2_HEADER.indexOf("num_first_issue_online") ]   = normValue( covStmt.startIssue )
                row[ KBART2_HEADER.indexOf("date_last_issue_online") ]   = normValue( ApiToolkit.formatInternalDate(covStmt.endDate) )
                row[ KBART2_HEADER.indexOf("num_last_vol_online") ]      = normValue( covStmt.endVolume )
                row[ KBART2_HEADER.indexOf("num_last_issue_online") ]    = normValue( covStmt.endIssue )

                row[ KBART2_HEADER.indexOf("title_url") ]                = normValue( ie.tipp?.hostPlatformURL )
                row[ KBART2_HEADER.indexOf("first_author") ]             = ""
                row[ KBART2_HEADER.indexOf("title_id") ]                 = ""

                row[ KBART2_HEADER.indexOf("embargo_info") ]             = normValue( covStmt.embargo )
                row[ KBART2_HEADER.indexOf("coverage_depth") ]           = normValue( covStmt.coverageDepth )
                row[ KBART2_HEADER.indexOf("notes") ]                    = normValue( covStmt.coverageNote )

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

                row[ KBART2_HEADER.indexOf("access_start_date") ]        = normValue( ApiToolkit.formatInternalDate(ie.accessStartDate) )
                row[ KBART2_HEADER.indexOf("access_end_date") ]          = normValue( ApiToolkit.formatInternalDate(ie.accessEndDate) )
                row[ KBART2_HEADER.indexOf("access_status") ]            = normValue( ie.status ) // ?

                kbart.add(row.join("\t"))
            }
        }

        output = kbart.join("\n")
        output
    }

    @Deprecated
    static getAsDocument(def data) {
        def todo = new Doc(
                title:          "KBART-EXPORT",
                content:        data,
                contentType:    Doc.CONTENT_TYPE_STRING,
                mimeType:       Constants.MIME_TEXT_PLAIN
        )
        todo
    }

    @Deprecated
    static normValue(def value) {
        value ? value : ''
    }

    @Deprecated
    static normDate(def date) {
        // TODO in json
        date
    }
}
