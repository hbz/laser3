package de.laser.stats

import de.laser.base.AbstractCounterApiSource

/**
 * Is actually deprecated and should keep SUSHI sources until we:kb was prepared for that; now, it serves merely as container for journal reports
 */
class Counter4ApiSource extends AbstractCounterApiSource{

    static final String JOURNAL_REPORT_1        = "JR1"
    static final String JOURNAL_REPORT_1_GOA    = "JR1GOA"
    static final String JOURNAL_REPORT_2        = "JR2"
    //JR3-4 are optional
    static final String JOURNAL_REPORT_5        = "JR5"
    static final String DATABASE_REPORT_1       = "DR1"
    static final String DATABASE_REPORT_2       = "DR2"
    static final String PLATFORM_REPORT_1       = "PR1"
    static final String BOOK_REPORT_1           = "BR1"
    static final String BOOK_REPORT_2           = "BR2"
    //BR3-4 are optional
    static final String BOOK_REPORT_3           = "BR3"
    static final String BOOK_REPORT_4           = "BR4"
    static final String BOOK_REPORT_5           = "BR5"
    static List<String> COUNTER_4_REPORTS       = [JOURNAL_REPORT_1, JOURNAL_REPORT_1_GOA, JOURNAL_REPORT_2, JOURNAL_REPORT_5,
                                                   DATABASE_REPORT_1, DATABASE_REPORT_2,
                                                   PLATFORM_REPORT_1,
                                                   BOOK_REPORT_1, BOOK_REPORT_2, BOOK_REPORT_3, BOOK_REPORT_4, BOOK_REPORT_5
    ]

    static mapping = {
        id              column: "c4as_id"
        version         column: "c4as_version"
        baseUrl         column: "c4as_base_url"
        provider        column: "c4as_provider_fk"
        platform        column: "c4as_platform_fk"
        arguments       column: "c4as_arguments", type: 'text'
    }

    static constraints = {
        arguments       (nullable: true)
    }

}
