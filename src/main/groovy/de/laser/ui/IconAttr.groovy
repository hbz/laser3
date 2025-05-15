package de.laser.ui

import de.laser.annotations.UIDoc
import de.laser.annotations.UnstableFeature

@UnstableFeature
class IconAttr {
    // please do not touch during refactoring ..

    class ATTR {
        // spec. domain class attributes/datas

        @UIDoc(usage = 'Only for Document attribute: confidentiality')
        public static String DOCUMENT_CONFIDENTIALITY   = 'lock icon'

        @UIDoc(usage = 'Only for License attribute: licenseCategory')
        public static String LICENSE_CATEGORY           = 'stamp icon'
        @UIDoc(usage = 'Only for License processing')
        public static String LICENSE_PROCESSING         = 'file signature icon'

        @UIDoc(usage = 'Only for Org: customer type')
        public static String ORG_CUSTOMER_TYPE          = 'trophy icon'
        @UIDoc(usage = 'Only for Org attribute: isBetaTester')
        public static String ORG_IS_BETA_TESTER         = 'bug icon'
        @UIDoc(usage = 'Only for Org attributes: createdBy / legallyObligedBy')
        public static String ORG_LEGAL_INFORMATION      = 'key icon'

        @UIDoc(usage = 'Only for Org attributes: createdBy=true / legallyObligedBy=true (COLORED)')
        public static String ORG_LEGAL_INFORMATION_11   = 'green check circle icon'
        @UIDoc(usage = 'Only for Org attributes: createdBy=true  / legallyObligedBy (COLORED)')
        public static String ORG_LEGAL_INFORMATION_10   = 'grey outline circle icon'
        @UIDoc(usage = 'Only for Org attributes: createdBy / legallyObligedBy=true (COLORED)')
        public static String ORG_LEGAL_INFORMATION_01   = 'red question mark icon'

        @UIDoc(usage = 'Only for Subscription transfer: discount scale')
        public static String SUBSCRIPTION_DISCOUNT_SCALE    = 'percentage icon'

        @UIDoc(usage = 'Only for Subscription attribute: form')
        public static String SUBSCRIPTION_FORM              = 'dice icon'

        @UIDoc(usage = 'Only for Subscription attribute: hasPerpetualAccess')
        public static String SUBSCRIPTION_HAS_PERPETUAL_ACCESS  = 'flag outline icon'
        @UIDoc(usage = 'Only for Subscription attribute: hasPublishComponent')
        public static String SUBSCRIPTION_HAS_PUBLISH_COMPONENT = 'quote right icon'
        @UIDoc(usage = 'Only for Subscription attribute: holdingSelection')
        public static String SUBSCRIPTION_HOLDING_SELECTION     = 'cut icon'            // TODO

        @UIDoc(usage = 'Only for Subscription invoice processing')
        public static String SUBSCRIPTION_INVOICE_PROCESSING    = 'file signature icon'
        @UIDoc(usage = 'Only for Subscription attribute: isMultiYear')
        public static String SUBSCRIPTION_IS_MULTIYEAR      = 'forward icon'
        @UIDoc(usage = 'Only for Subscription attribute: kind')
        public static String SUBSCRIPTION_KIND              = 'dice five icon'          // TODO
        @UIDoc(usage = 'Only for Subscription attribute: resource')
        public static String SUBSCRIPTION_RESOURCE          = 'expand icon'             // TODO

        @UIDoc(usage = 'Only for Subscription transfer: survey cancellation')
        public static String SURVEY_CANCELLATION   = 'times circle icon'
        @UIDoc(usage = 'Only for Subscription transfer: survey evaluation')
        public static String SURVEY_EVALUTAION     = 'vote yea icon'

        @UIDoc(usage = 'Only for Survey: Participants')
        public static String SURVEY_PARTICIPANTS            = 'users icon'
        @UIDoc(usage = 'Only for Survey: All results processed by org (COLORED)')
        public static String SURVEY_RESULTS_PROCESSED       = 'edit green icon'
        @UIDoc(usage = 'Only for Survey: Not all results processed by org (COLORED)')
        public static String SURVEY_RESULTS_NOT_PROCESSED   = 'edit red icon'

        @UIDoc(usage = 'Only for Survey: Not all results processed by org (COLORED)')
        public static String SURVEY_ORG_TRANSFERRED   = 'exchange green icon'

        @UIDoc(usage = 'Only for TASK: creator')
        public static String TASK_CREATOR           = 'user icon'
        @UIDoc(usage = 'Only for TASK: status done')
        public static String TASK_STATUS_DONE       = 'check circle outline icon'   // TODO
        @UIDoc(usage = 'Only for TASK: status open')
        public static String TASK_STATUS_OPEN       = 'la-open icon'               // TODO
        @UIDoc(usage = 'Only for TASK: status deferred')
        public static String TASK_STATUS_DEFERRED   = 'pause circle outline icon'   // TODO

        @UIDoc(usage = 'Only for TIPP: access date (COLORED)')
        public static String TIPP_ACCESS_DATE       = 'clipboard check icon grey'   // TODO
        @UIDoc(usage = 'Only for TIPP: various access types (hybrid, delayed) (COLORED)')
        public static String TIPP_ACCESS_TYPE       = 'lock open icon grey'         // TODO
        @UIDoc(usage = 'Only for TIPP: various coverage data (COLORED)')
        public static String TIPP_COVERAGE          = 'la-books icon grey'          // TODO
        @UIDoc(usage = 'Only for TIPP: coverageDepth (COLORED)')
        public static String TIPP_COVERAGE_DEPTH    = 'file alternate icon grey'    // TODO
        @UIDoc(usage = 'Only for TIPP: coverageNote (COLORED)')
        public static String TIPP_COVERAGE_NOTE     = 'quote right icon grey'       // TODO
        @UIDoc(usage = 'Only for TIPP: embargo (COLORED)')
        public static String TIPP_EMBARGO           = 'hand paper icon grey'        // TODO
        @UIDoc(usage = 'Only for TIPP: firstAuthor (COLORED)')
        public static String TIPP_FIRST_AUTHOR      = 'user circle icon grey'       // TODO
        @UIDoc(usage = 'Only for TIPP: firstEditor (COLORED)')
        public static String TIPP_FIRST_EDITOR      = 'industry circle icon grey'   // TODO
        @UIDoc(usage = 'Only for TIPP: issue (COLORED)')
        public static String TIPP_ISSUE             = 'la-notebook icon grey'       // TODO
        @UIDoc(usage = 'Only for TIPP: medium (COLORED)')
        public static String TIPP_MEDIUM            = 'medium icon grey'            // TODO
        @UIDoc(usage = 'Only for TIPP: status (COLORED)')
        public static String TIPP_STATUS            = 'key icon grey'               // TODO
        @UIDoc(usage = 'Only for TIPP: summaryOfContent (COLORED)')
        public static String TIPP_SUMMARY_OF_CONTENT    = 'desktop icon grey'       // TODO

        @UIDoc(usage = 'Only for Workflow: checkpoint')
        public static String WORKFLOW_CHECKPOINT    = 'circle icon'
    }
}
