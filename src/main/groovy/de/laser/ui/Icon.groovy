package de.laser.ui

import de.laser.annotations.IconUsage
import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icon {

    class AUTH {
        // please do not touch during refactoring ..
        // roles / perms / customer types

        @IconUsage(usage = 'Only for Inst Role: User')
        public static String INST_USER      = 'user icon'
        @IconUsage(usage = 'Only for Inst Role: Editor')
        public static String INST_EDITOR    = 'user edit icon'
        @IconUsage(usage = 'Only for Inst Role: Adm')
        public static String INST_ADM       = 'user shield icon'

        @IconUsage(usage = 'Only for Customer Type: Institution Basic')
        public static String ORG_INST_BASIC         = 'user circle icon grey'   // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Customer Type: Institution Pro')
        public static String ORG_INST_PRO           = 'trophy icon grey'        // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Customer Type: Consortium Basic')
        public static String ORG_CONSORTIUM_BASIC   = 'user circle icon teal'   // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Customer Type: Consortium Pro')
        public static String ORG_CONSORTIUM_PRO     = 'trophy icon teal'        // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Customer Type: Support')
        public static String ORG_SUPPORT            = 'theater masks icon red'  // COLOR INCLUDED !!

        @IconUsage(usage = 'Only for Role: User')
        public static String ROLE_USER      = 'user icon'
        @IconUsage(usage = 'Only for Role: Admin / Default color: orange')
        public static String ROLE_ADMIN     = 'tools icon'
        @IconUsage(usage = 'Only for Role: Yoda / Default color: red')
        public static String ROLE_YODA      = 'star of life icon'
    }

    class CMD {
        // please do not touch during refactoring ..
        // cmds, functions

        @IconUsage(usage = 'Only for Buttons/Links with command: Add element')
        public static String ADD        = 'plus icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Attach file')
        public static String ATTACHMENT = 'attach icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Copy element')
        public static String COPY       = 'copy icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Delete element')
        public static String DELETE     = 'trash alternate outline icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Download file')
        public static String DOWNLOAD   = 'download icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Edit element')
        public static String EDIT       = 'write icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Erase element')
        public static String ERASE      = 'eraser icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Move element up')
        public static String MOVE_UP    = 'arrow up icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Move element down')
        public static String MOVE_DOWN  = 'arrow down icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Remove element')
        public static String REMOVE     = 'minus icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Replace element')
        public static String REPLACE    = 'retweet icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Show more / Open accordion')
        public static String SHOW_MORE  = 'angle double down icon'
        @IconUsage(usage = 'Only for Buttons/Links with command: Unlink element / Do not delete')
        public static String UNLINK     = 'unlink icon'
    }

    class LNK {
        // please do not touch during refactoring ..
        // links

        @IconUsage(usage = 'Only for Links to external resources')
        public static String EXTERNAL   = 'external alternate icon'
        @IconUsage(usage = 'Only for Links to views with preset filters')
        public static String FILTERED   = 'filter icon'
        @IconUsage(usage = 'Only for Links to views for successors of the current object')
        public static String NEXT       = 'arrow right icon'     // todo: duplicate: ui:anualRings, ui:statusWithRings
        @IconUsage(usage = 'Only for Links to views for predecessors of the current object')
        public static String PREV       = 'arrow left icon'      // todo: duplicate: ui:anualRings, ui:statusWithRings
    }

    class PROP {
        // please do not touch during refactoring ..
        // properties only
        @IconUsage(usage = 'Only for Properties: formalOrg is tenant = private')
        public static String IS_PRIVATE = 'shield alternate icon'
        @IconUsage(usage = 'Only for Properties: currently in use')
        public static String IN_USE     = 'info circle icon blue'   // COLOR INCLUDED !! todo: duplicate: Icon.UI.INFO

        @IconUsage(usage = 'Only for Property attribute: isHardData')
        public static String HARDDATA   = 'check circle icon green' // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Property attribute: isUsedForLogic')
        public static String LOGIC      = 'cube icon red'           // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Property attribute: mandatory')
        public static String MANDATORY  = 'star icon'               // todo: duplicate: survey/renew? > Icon.UI.MY_OBJECT
        @IconUsage(usage = 'Only for Property attribute: multipleOccurrence')
        public static String MULTIPLE   = 'redo icon orange'        // COLOR INCLUDED !! todo: duplicate: currentSubscriptionsTransfer
    }

    class FNC {
        // please do not touch during refactoring ..
        // finance, costs

        @IconUsage(usage = 'Only for Cost items')
        public static String COST           = 'money bill icon'
        @IconUsage(usage = 'Only for cost item configurations')
        public static String COST_CONFIG    = 'money bill alternate icon'

        @IconUsage(usage = 'Only for Cost items: Configuration signed positive')
        public static String COST_POSITIVE  = 'circle plus icon green'      // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Cost items: Configuration signed negative')
        public static String COST_NEGATIVE  = 'circle minus icon red'       // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for Cost items: Configuration signed neutral')
        public static String COST_NEUTRAL   = 'circle icon yellow'          // COLOR INCLUDED !!

        @IconUsage(usage = 'Only for Cost items: Configuration not set')
        public static String COST_NOT_SET   = 'grey question circle icon'   // COLOR INCLUDED !!
    }

    class SYM {
        // please do not touch during refactoring ..
        // convenient symbols

        @IconUsage(usage = 'Generic symbol for: Date')
        public static String DATE           = 'calendar alternate outline icon'
        @IconUsage(usage = 'Generic symbol for: Email')
        public static String EMAIL          = 'envelope outline icon'
        @IconUsage(usage = 'Generic symbol for: Fax')
        public static String FAX            = 'fax icon'
        @IconUsage(usage = 'Generic symbol for: Mobile phone')
        public static String MOBILE         = 'mobile alternate icon'
        @IconUsage(usage = 'Generic symbol for: Phone')
        public static String PHONE          = 'phone icon'
        @IconUsage(usage = 'Generic symbol for: External Website')
        public static String URL            = 'globe icon'

        @IconUsage(usage = 'Generic symbol for: Search')
        public static String SEARCH         = 'search icon'
        @IconUsage(usage = 'Generic symbol for: Properties')
        public static String PROPERTIES     = 'tags icon'
        @IconUsage(usage = 'Generic symbol for: Unkown')
        public static String UNKOWN         = 'question icon'


        @IconUsage(usage = 'Only for Org attributes: createdBy/legallyObligedBy')
        public static String ORG_LEGAL_INFORMATION      = 'hands helping icon'

        @IconUsage(usage = 'Only for Subscription attribute: isMultiYear')
        public static String SUBSCRIPTION_IS_MULTIYEAR  = 'forward icon'

        @IconUsage(usage = 'Only for TIPP: coverageDepth')
        public static String TIPP_COVERAGE_DEPTH        = 'file alternate icon'
    }

    class UI {
        // please do not touch during refactoring ..
        // information / dialogs / messages

        // e.g. <ui:msg />
        @IconUsage(usage = 'Mandatory symbol for: Help')
        public static String HELP       = 'question icon'
        @IconUsage(usage = 'Mandatory symbol for: Info')
        public static String INFO       = 'info icon'
        @IconUsage(usage = 'Mandatory symbol for: Warning')
        public static String WARNING    = 'exclamation icon'
        @IconUsage(usage = 'Mandatory symbol for: Success')
        public static String SUCCESS    = 'check icon'
        @IconUsage(usage = 'Mandatory symbol for: Error')
        public static String ERROR      = 'exclamation triangle icon'

        @IconUsage(usage = 'Mandatory symbol for Address/Contact/Person: Is public')
        public static String ACP_PUBLIC     = 'address card icon'
        @IconUsage(usage = 'Mandatory symbol for Address/Contact/Person: Is private')
        public static String ACP_PRIVATE    = 'address card outline icon'

        @IconUsage(usage = 'Mandatory symbol for: Is my object')
        public static String MY_OBJECT  = 'star icon'                   // todo: duplicate: survey/renew? > Icon.PROP.MANDATORY
    }

    class TOOLTIP {
        // please do not touch during refactoring ..
        // tooltips

        @IconUsage(usage = 'Only for tooltip trigger: Help')
        public static String HELP       = 'grey question circle icon'   // COLOR INCLUDED !!
        @IconUsage(usage = 'Only for tooltip trigger: Important, possibly warning')
        public static String IMPORTANT  = 'exclamation circle icon'     // TODO: merge with Icon.TOOLTIP.SERIOUS
        @IconUsage(usage = 'Only for tooltip trigger: Info')
        public static String INFO       = 'info circle icon'            // todo: duplicate: Icon.PROP.IN_USE
        @IconUsage(usage = 'Only for tooltip trigger: Serious, possibly error')
        public static String SERIOUS    = 'exclamation triangle icon'   // TODO: merge with Icon.TOOLTIP.IMPORTANT
    }

    class UNC {
        // please do not touch during refactoring ..
        // uncategorized => none/lower semantics

        @IconUsage(usage = 'Layout helper / mostly wrapped with class="hidden"')
        public static String PLACEHOLDER    = 'coffee icon'
        @IconUsage(usage = 'Uncategorized icon / free to use')
        public static String CIRCLE         = 'circle icon'
        @IconUsage(usage = 'Uncategorized icon / free to use')
        public static String SQUARE         = 'square icon'
    }

    // please do not touch during refactoring ..
    // domain classes / top level objects

    public static String ANNOUNCEMENT               = 'flag icon'

    public static String DOCUMENT                   = 'file alternate icon'

    public static String GASCO                      = 'layer group icon'

    public static String LICENSE                    = 'balance scale icon'

    public static String MARKER                     = 'bookmark icon'       // todo: duplicate > reporting.history/bookmarks

    public static String ORG                        = 'university icon'

    public static String PACKAGE                    = 'gift icon'

    public static String PLATFORM                   = 'cloud icon'          // todo: duplicate > url, flagContentGokb, flagContentElasticsearch

    public static String PROVIDER                   = 'handshake icon'

    public static String REPORTING                  = 'chartline icon'

    public static String STATS                      = 'chart bar icon'

    public static String SUBSCRIPTION               = 'clipboard icon'      // todo: duplicate license.licenseCategory, tipp.accessStartDate/tipp.accessEndDate

    public static String SURVEY                     = 'chart pie icon'

    public static String TASK                       = 'calendar check outline icon'

    public static String TIPP                       = 'book icon'

    public static String VENDOR                     = 'boxes icon'

    public static String WEKB                       = 'la-gokb icon'

    public static String WORKFLOW                   = 'tasks icon'

}
