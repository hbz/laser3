package de.laser.mail

import de.laser.Org
import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import de.laser.survey.SurveyOrg
import grails.plugin.asyncmail.AsynchronousMailMessage

class MailReport {
    
    String subject
    String content

    String from
    String replyTo
    String to
    String ccReceiver
    String bccReceiver

    Date sentDate
    Date dateCreated
    Date lastUpdated

    boolean sentBySystem
    boolean modifedByOwner

    AsynchronousMailMessage asynchronousMailMessage

    Org receiverOrg
    Org owner

    @RefdataInfo(cat = RDConstants.MAIL_STATUS)
    RefdataValue status

    @RefdataInfo(cat = RDConstants.MAIL_TYPE)
    RefdataValue type

    SurveyOrg surveyOrg

    static constraints = {
        replyTo     (nullable:true, blank:true)
        ccReceiver     (nullable:true, blank:true)
        bccReceiver     (nullable:true, blank:true)
        surveyOrg     (nullable:true)
        receiverOrg (nullable:true)
    }

    static mapping = {
        id column: 'mr_id'
        version column: 'mr_version'

        subject column: 'mr_subject'
        content column: 'mr_content'
        from column: 'mr_from'
        replyTo column: 'mr_reply_to'
        to column: 'mr_to'
        ccReceiver column: 'mr_cc_receiver'
        bccReceiver column: 'mr_bcc_receiver'

        sentBySystem column: 'mr_sent_by_system'
        modifedByOwner column: 'mr_modifed_by_owner'

        asynchronousMailMessage column: 'mr_asynchronous_mail_message_fk'

        owner column: 'mr_owner_org_fk'
        receiverOrg column: 'mr_receiver_org_fk'

        surveyOrg column: 'mr_survey_org_fk'

        status column: 'mr_status_rv_fk'
        type column: 'mr_type_rv_fk'

        sentDate column: 'mr_sent_date'
        dateCreated column: 'mr_date_created'
        lastUpdated column: 'mr_last_updated'
    }
}
