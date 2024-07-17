package de.laser.ui

import de.laser.annotations.UIDoc
import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icon {

    class AUTH {
        // please do not touch during refactoring ..
        // roles / perms / customer types

        @UIDoc(usage = 'Only for Inst Role: User')
        public static String INST_USER      = 'user icon'
        @UIDoc(usage = 'Only for Inst Role: Editor')
        public static String INST_EDITOR    = 'user edit icon'
        @UIDoc(usage = 'Only for Inst Role: Adm')
        public static String INST_ADM       = 'user shield icon'

        @UIDoc(usage = 'Only for Customer Type: Institution Basic (COLORED)')
        public static String ORG_INST_BASIC         = 'user circle icon grey'
        @UIDoc(usage = 'Only for Customer Type: Institution Pro (COLORED)')
        public static String ORG_INST_PRO           = 'trophy icon grey'
        @UIDoc(usage = 'Only for Customer Type: Consortium Basic (COLORED)')
        public static String ORG_CONSORTIUM_BASIC   = 'user circle icon teal'
        @UIDoc(usage = 'Only for Customer Type: Consortium Pro (COLORED)')
        public static String ORG_CONSORTIUM_PRO     = 'trophy icon teal'
        @UIDoc(usage = 'Only for Customer Type: Support (COLORED)')
        public static String ORG_SUPPORT            = 'theater masks icon red'

        @UIDoc(usage = 'Only for Role: User')
        public static String ROLE_USER      = 'user icon'
        @UIDoc(usage = 'Only for Role: Admin (Default color: orange)')
        public static String ROLE_ADMIN     = 'tools icon'
        @UIDoc(usage = 'Only for Role: Yoda (Default color: red)')
        public static String ROLE_YODA      = 'star of life icon'
    }

    class CMD {
        // please do not touch during refactoring ..
        // cmds, functions

        @UIDoc(usage = 'Only for Buttons/Links with command: Add element (see also REMOVE)')
        public static String ADD        = 'plus icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Attach file')
        public static String ATTACHMENT = 'attach icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Copy element')
        public static String COPY       = 'copy icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Delete element')
        public static String DELETE     = 'trash alternate outline icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Download file')
        public static String DOWNLOAD   = 'download icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Edit element')
        public static String EDIT       = 'write icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Erase element')
        public static String ERASE      = 'eraser icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Linkify element (see also UNLINK)')
        public static String LINKIFY    = 'linkify icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Move element up (see also MOVE_DOWN)')
        public static String MOVE_UP    = 'arrow up icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Move element down (see also MOVE_UP)')
        public static String MOVE_DOWN  = 'arrow down icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Remove element (see also ADD)')
        public static String REMOVE     = 'minus icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Replace element')
        public static String REPLACE    = 'retweet icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Show more / Open accordion')
        public static String SHOW_MORE  = 'angle double down icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Unlink element / Do not delete (see also LINKIFY)')
        public static String UNLINK     = 'unlink icon'
    }

    class LNK {
        // please do not touch during refactoring ..
        // links

        @UIDoc(usage = 'Only for Links to external resources')
        public static String EXTERNAL   = 'external alternate icon'
        @UIDoc(usage = 'Only for Links to views with preset filters')
        public static String FILTERED   = 'filter icon'
        @UIDoc(usage = 'Only for Links to views for successors of the current object (see also PREV)')
        public static String NEXT       = 'arrow right icon'     // todo: duplicate: ui:anualRings, ui:statusWithRings
        @UIDoc(usage = 'Only for Links to views for predecessors of the current object (see also NEXT)')
        public static String PREV       = 'arrow left icon'      // todo: duplicate: ui:anualRings, ui:statusWithRings
    }

    class PROP {
        // please do not touch during refactoring ..
        // properties only
        @UIDoc(usage = 'Only for Properties: formalOrg is tenant = private')
        public static String IS_PRIVATE = 'shield alternate icon'
        @UIDoc(usage = 'Only for Properties: currently in use (COLORED)')
        public static String IN_USE     = 'info circle icon blue'   // todo: duplicate: Icon.UI.INFO

        @UIDoc(usage = 'Only for Property attribute: isHardData (COLORED)')
        public static String HARDDATA   = 'check circle icon green'
        @UIDoc(usage = 'Only for Property attribute: isUsedForLogic (COLORED)')
        public static String LOGIC      = 'cube icon red'
        @UIDoc(usage = 'Only for Property attribute: mandatory (Default color: yellow)')
        public static String MANDATORY  = 'star icon'               // todo: duplicate: survey/renew? > Icon.UI.MY_OBJECT
        @UIDoc(usage = 'Only for Property attribute: multipleOccurrence (COLORED)')
        public static String MULTIPLE   = 'redo icon orange'        // todo: duplicate: currentSubscriptionsTransfer
    }

    class FNC {
        // please do not touch during refactoring ..
        // finance, costs

        @UIDoc(usage = 'Only for Cost items')
        public static String COST           = 'money bill icon'
        @UIDoc(usage = 'Only for cost item configurations')
        public static String COST_CONFIG    = 'money bill alternate icon'

        @UIDoc(usage = 'Only for Cost items: Configuration signed positive (COLORED)')
        public static String COST_POSITIVE  = 'circle plus icon green'
        @UIDoc(usage = 'Only for Cost items: Configuration signed negative (COLORED)')
        public static String COST_NEGATIVE  = 'circle minus icon red'
        @UIDoc(usage = 'Only for Cost items: Configuration signed neutral (COLORED)')
        public static String COST_NEUTRAL   = 'circle icon yellow'

        @UIDoc(usage = 'Only for Cost items: Configuration not set (COLORED)')
        public static String COST_NOT_SET   = 'grey question circle icon'
    }

    class SYM {
        // please do not touch during refactoring ..
        // convenient symbols

        @UIDoc(usage = 'Generic symbol for: Date')
        public static String DATE           = 'calendar alternate outline icon'
        @UIDoc(usage = 'Generic symbol for: Email')
        public static String EMAIL          = 'envelope outline icon'
        @UIDoc(usage = 'Generic symbol for: Fax')
        public static String FAX            = 'fax icon'
        @UIDoc(usage = 'Generic symbol for: Mobile phone')
        public static String MOBILE         = 'mobile alternate icon'
        @UIDoc(usage = 'Generic symbol for: Phone')
        public static String PHONE          = 'phone icon'
        @UIDoc(usage = 'Generic symbol for: External Website')
        public static String URL            = 'globe icon'

        @UIDoc(usage = 'Generic symbol for: Search')
        public static String SEARCH         = 'search icon'
        @UIDoc(usage = 'Generic symbol for: Properties')
        public static String PROPERTIES     = 'tags icon'
        @UIDoc(usage = 'Generic symbol for: Unkown')
        public static String UNKOWN         = 'question icon'


        @UIDoc(usage = 'Only for Org attributes: createdBy / legallyObligedBy')
        public static String ORG_LEGAL_INFORMATION      = 'hands helping icon'

        @UIDoc(usage = 'Only for Subscription attribute: isMultiYear')
        public static String SUBSCRIPTION_IS_MULTIYEAR  = 'forward icon'

        @UIDoc(usage = 'Only for TIPP: coverageDepth')
        public static String TIPP_COVERAGE_DEPTH        = 'file alternate icon'
    }

    class UI {
        // please do not touch during refactoring ..
        // information / dialogs / messages

        // e.g. <ui:msg />
        @UIDoc(usage = 'Mandatory symbol for: Help')
        public static String HELP       = 'question icon'
        @UIDoc(usage = 'Mandatory symbol for: Info')
        public static String INFO       = 'info icon'
        @UIDoc(usage = 'Mandatory symbol for: Warning')
        public static String WARNING    = 'exclamation icon'
        @UIDoc(usage = 'Mandatory symbol for: Success')
        public static String SUCCESS    = 'check icon'
        @UIDoc(usage = 'Mandatory symbol for: Error')
        public static String ERROR      = 'exclamation triangle icon'

        @UIDoc(usage = 'Mandatory symbol for Address/Contact/Person: Is public')
        public static String ACP_PUBLIC     = 'address card icon'
        @UIDoc(usage = 'Mandatory symbol for Address/Contact/Person: Is private')
        public static String ACP_PRIVATE    = 'address card outline icon'

        @UIDoc(usage = 'Mandatory symbol for: Is my object')
        public static String MY_OBJECT  = 'star icon'                   // todo: duplicate: survey/renew? > Icon.PROP.MANDATORY
    }

    class TOOLTIP {
        // please do not touch during refactoring ..
        // tooltips

        @UIDoc(usage = 'Only for tooltip trigger: Help')
        public static String HELP       = 'grey question circle icon'   // COLOR INCLUDED !!
        @UIDoc(usage = 'Only for tooltip trigger: Important, possibly warning')
        public static String IMPORTANT  = 'exclamation circle icon'     // TODO: merge with Icon.TOOLTIP.SERIOUS
        @UIDoc(usage = 'Only for tooltip trigger: Info')
        public static String INFO       = 'info circle icon'            // todo: duplicate: Icon.PROP.IN_USE
        @UIDoc(usage = 'Only for tooltip trigger: Serious, possibly error')
        public static String SERIOUS    = 'exclamation triangle icon'   // TODO: merge with Icon.TOOLTIP.IMPORTANT
    }

    class UNC {
        // please do not touch during refactoring ..
        // uncategorized => none/lower semantics

        @UIDoc(usage = 'Layout helper / mostly wrapped with class="hidden"')
        public static String PLACEHOLDER    = 'coffee icon'
        @UIDoc(usage = 'Uncategorized icon / free to use')
        public static String CIRCLE         = 'circle icon'
        @UIDoc(usage = 'Uncategorized icon / free to use')
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