package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.finance.CostItem
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.properties.PropertyDefinition
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class SurveyResult extends AbstractPropertyWithCalculatedLastUpdated implements CalculatedLastUpdated {

    static Log static_logger = LogFactory.getLog(SurveyResult)

    PropertyDefinition type
    boolean isPublic = false
    boolean isRequired = false

    String           stringValue
    Integer          intValue
    BigDecimal       decValue
    RefdataValue     refValue
    URL              urlValue
    String           note = ""
    Date             dateValue
    Org              tenant

    Org owner
    Org participant

    String comment
    String participantComment
    String ownerComment

    Date finishDate
    Date startDate
    Date endDate

    SurveyConfig surveyConfig

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static constraints = {
        stringValue (nullable: true)
        intValue    (nullable: true)
        decValue    (nullable: true)
        refValue    (nullable: true)
        urlValue    (nullable: true)
        note        (nullable: true)
        dateValue   (nullable: true)
        tenant      (nullable: true)
        finishDate  (nullable:true)
        comment (nullable:true, blank:false)
        startDate   (nullable:true)
        endDate     (nullable:true)
        participantComment (nullable:true, blank:false)
        ownerComment (nullable:true, blank:false)

        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static transients = ['resultProcessed', 'costItem', 'result'] // mark read-only accessor methods

    static mapping = {

        id column: 'surre_id'
        version column: 'surre_version'

        dateCreated column: 'surre_date_created'
        lastUpdated column: 'surre_last_updated'
        lastUpdatedCascading column: 'surre_last_updated_cascading'
        stringValue column: 'surre_string_value', type: 'text'
        intValue    column: 'surre_int_value'
        decValue    column: 'surre_dec_value'
        refValue    column: 'surre_ref_value_rv_fk'
        urlValue    column: 'surre_url_value'
        note        column: 'surre_note', type: 'text'
        dateValue   column: 'surre_date_value'

        startDate column: 'surre_start_date'
        endDate column: 'surre_end_date'
        finishDate column: 'surre_finish_date'
        tenant        column: 'surre_tenant_fk' //never set, is for mapping of superclass
        owner column: 'surre_owner_fk'
        participant column: 'surre_participant_fk'
        isPublic    column: 'surre_is_public'
        type column: 'surre_type_fk'
        surveyConfig column: 'surre_survey_config_fk'

        comment column: 'surre_comment', type: 'text'
        participantComment column: 'surre_participant_comment', type: 'text'
        ownerComment column: 'surre_owner_comment', type: 'text'

        isRequired column: 'surre_is_required'
    }

    boolean isResultProcessed() {
        if (type.isIntegerType()) {
            return intValue ? true : false
        }
        else if (type.isStringType()) {
            return stringValue ? true : false
        }
        else if (type.isBigDecimalType()) {
            return decValue ? true : false
        }
        else if (type.isDateType()) {
            return dateValue ? true : false
        }
        else if (type.isURLType()) {
            return urlValue ? true : false
        }
        else if (type.isRefdataValueType()) {
            return refValue ? true : false
        }
    }

    CostItem getCostItem(){
        return CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant), RDStore.COST_ITEM_DELETED)
    }

    def getResult() {
        if (type.isIntegerType()) {
            return intValue.toString()
        }
        else if (type.isStringType()) {
            return stringValue
        }
        else if (type.isBigDecimalType()) {
            return decValue.toString()
        }
        else if (type.isDateType()) {
            return dateValue.getDateString()
        }
        else if (type.isURLType()) {
            return urlValue.toString()
        }
        else if (type.isRefdataValueType()) {
            return refValue ? refValue?.getI10n('value') : ""
        }
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def beforeUpdate(){
        super.beforeUpdateHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }
    @Override
    def afterDelete() {
        super.afterDeleteHandler()
    }
}
