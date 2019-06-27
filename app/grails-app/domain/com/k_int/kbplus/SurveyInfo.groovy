package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class SurveyInfo {

    String name
    Date startDate
    Date endDate
    String comment

    Org owner

    @RefdataAnnotation(cat = 'Survey Type')
    RefdataValue type

    @RefdataAnnotation(cat = 'Survey Status')
    RefdataValue status

    Date dateCreated
    Date lastUpdated

    //List surveyConfigs

    static hasMany = [
            surveyConfigs: SurveyConfig
    ]

    static constraints = {
        startDate (nullable:true, blank:false)
        endDate (nullable:true, blank:false)
        surveyConfigs (nullable:true, blank:false)
        comment (nullable:true, blank:true)

    }

    static mapping = {
        id column: 'surin_id'
        version column: 'surin_version'

        name column: 'surin_name'
        startDate column: 'surin_start_date'
        endDate column: 'surin_end_date'
        comment column: 'surin_comment', type: 'text'

        dateCreated column: 'surin_date_created'
        lastUpdated column: 'surin_last_updated'

        owner column: 'surin_owner_org_fk'
        type column: 'surin_type_rv_fk'
        status column: 'surin_status_rv_fk'



    }


    def checkOpenSurvey()
    {
        boolean check = this.surveyConfigs.size() > 0 ? true : false

        this.surveyConfigs.each {

            if(it?.subscription && !(it?.surveyProperties?.size() > 0))
            {
                check = false
            }

            if(!(it?.orgs.org?.size > 0)){
                check = false
            }
        }

        return check
    }

    def checkSurveyInfoFinishByOrg(Org org) {
        def result = [:]

        def count = 1
        surveyConfigs.each {
            if(result."${it.checkResultsFinishByOrg(org)}"){

                result."${it.checkResultsFinishByOrg(org)}" = result."${it.checkResultsFinishByOrg(org)}"+1


            }else {
                result."${it.checkResultsFinishByOrg(org)}" = 1
            }
            count++
        }

        result.sort{it.value}

        print(result)
        println(result[0])
        println(result[0]?.getValue())

        result = (count == result[0]?.getAt(result[0]?.getKey()))  ? result[0]?.getKey() : null

        println(result)
        result

    }
}
