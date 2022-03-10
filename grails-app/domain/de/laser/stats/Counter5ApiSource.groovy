package de.laser.stats

import de.laser.base.AbstractCounterApiSource

/**
 * Is actually deprecated and should keep SUSHI sources until we:kb was prepared for that; now, it serves merely as container for journal reports
 */
class Counter5ApiSource extends AbstractCounterApiSource {

    static final String PLATFORM_MASTER_REPORT          = "pr"
    static final String PLATFORM_USAGE                  = "pr_p1"
    static final String DATABASE_MASTER_REPORT          = "dr"
    static final String DATABASE_SEARCH_AND_ITEM_USAGE  = "dr_d1"
    static final String DATABASE_ACCESS_DENIED          = "dr_d2"
    static final String TITLE_MASTER_REPORT             = "tr"
    static final String BOOK_REQUESTS                   = "tr_b1"
    static final String BOOK_ACCESS_DENIED              = "tr_b2"
    static final String BOOK_USAGE_BY_ACCESS_TYPE       = "tr_b3"
    static final String JOURNAL_REQUESTS                = "tr_j1"
    static final String JOURNAL_ACCESS_DENIED           = "tr_j2"
    static final String JOURNAL_USAGE_BY_ACCESS_TYPE    = "tr_j3"
    static final String JOURNAL_REQUESTS_BY_YOP         = "tr_j4"
    static final String ITEM_MASTER_REPORT              = "ir"
    static final String JOURNAL_ARTICLE_REQUESTS        = "ir_a1"
    static final String MULTIMEDIA_ITEM_REQUESTS        = "ir_m1"
    static List<String> COUNTER_5_REPORTS               = [PLATFORM_MASTER_REPORT, PLATFORM_USAGE,
                                                           DATABASE_MASTER_REPORT, DATABASE_SEARCH_AND_ITEM_USAGE, DATABASE_ACCESS_DENIED,
                                                           TITLE_MASTER_REPORT, BOOK_REQUESTS, BOOK_ACCESS_DENIED, BOOK_USAGE_BY_ACCESS_TYPE, JOURNAL_REQUESTS, JOURNAL_ACCESS_DENIED, JOURNAL_USAGE_BY_ACCESS_TYPE, JOURNAL_REQUESTS_BY_YOP,
                                                           ITEM_MASTER_REPORT, JOURNAL_ARTICLE_REQUESTS, MULTIMEDIA_ITEM_REQUESTS]

    static mapping = {
        id                       column: "c5as_id"
        version                  column: "c5as_version"
        baseUrl                  column: "c5as_base_url"
        provider                 column: "c5as_provider_fk"
        platform                 column: "c5as_platform_fk"
        arguments                column: "c5as_arguments", type: 'text'
    }

    static constraints = {
        arguments               (nullable: true)
    }
}
