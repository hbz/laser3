package de.laser.stats

import de.laser.base.AbstractCounterApiSource

/**
 * Is actually deprecated and should keep SUSHI sources until we:kb was prepared for that; now, it serves merely as container for journal reports
 */
@Deprecated
class Counter5ApiSource extends AbstractCounterApiSource {

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
