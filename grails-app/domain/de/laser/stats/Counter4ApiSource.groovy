package de.laser.stats

import de.laser.base.AbstractCounterApiSource

/**
 * Is actually deprecated and should keep SUSHI sources until we:kb was prepared for that; now, it serves merely as container for journal reports
 */
@Deprecated
class Counter4ApiSource extends AbstractCounterApiSource{

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
