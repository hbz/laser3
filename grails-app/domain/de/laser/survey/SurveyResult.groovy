package de.laser.survey

import de.laser.Org
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.finance.CostItem
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.properties.PropertyDefinition
import de.laser.storage.RDStore

/**
 * This class reflects - as a property like {@link de.laser.properties.SubscriptionProperty} - the survey participant's answer to a survey.
 * The survey is reflected by the {@link SurveyConfig} class.
 */
class SurveyResult extends AbstractPropertyWithCalculatedLastUpdated implements CalculatedLastUpdated {

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
        tenant        column: 'surre_tenant_fk' //never set, is for mapping of superclass
        owner column: 'surre_owner_fk', index: 'surre_owner_idx'
        participant column: 'surre_participant_fk', index: 'surre_participant_idx'
        isPublic    column: 'surre_is_public'
        type column: 'surre_type_fk', index: 'surre_type_idx'
        surveyConfig column: 'surre_survey_config_fk', index: 'surre_survey_config_idx'

        comment column: 'surre_comment', type: 'text'
        participantComment column: 'surre_participant_comment', type: 'text'
        ownerComment column: 'surre_owner_comment', type: 'text'

        isRequired column: 'surre_is_required'
    }

    /**
     * Checks if the value of this property (= survey question result) has been set
     * @return true if there is any kind of value (= answer on behalf of the participant), false otherwise
     */
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

    /**
     * Gets a {@link CostItem} belonging to the participant and the survey ({@link SurveyOrg} retrieved by the {@link SurveyConfig} and the participant {@link Org}) of this property
     * @return
     */
    CostItem getCostItem(){
        return CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant), RDStore.COST_ITEM_DELETED)
    }

    /**
     * Retrieves the value of this property, respective to its value type
     * @return the stringified value of the property
     * @see {@link AbstractPropertyWithCalculatedLastUpdated}
     */
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
            return dateValue ? dateValue.getDateString() : ""
        }
        else if (type.isURLType()) {
            return urlValue.toString()
        }
        else if (type.isRefdataValueType()) {
            return refValue ? refValue.getI10n('value') : ""
        }
    }

    /**
     * calls {@link #toString()}
     */
    String getValue() {
        return toString()
    }

    /**
     * Same as {@link #getResult()} just with different checks
     * @return the stringified value
     */
    @Override
    String toString(){
        if (stringValue)      { return stringValue }
        if (intValue != null) { return intValue.toString() }
        if (decValue != null) { return decValue.toString() }
        if (refValue)         { return refValue.toString() }
        if (dateValue)        { return dateValue.getDateString() }
        if (urlValue)         { return urlValue.toString() }
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

    /**
     * Retrieves the subscription linked to this survey and the participant
     * @return the {@link Subscription} object
     */
    Subscription getParticipantSubscription(){
        Subscription subscription
        if (surveyConfig.subscription){
            subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                    [parentSub  : surveyConfig.subscription,
                     participant: participant
                    ])[0]
        }

        return subscription
    }
}
