package de.laser.ui

import de.laser.annotations.UIDoc
import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icon extends IconAttr {
    // please do not touch during refactoring ..

    class AUTH {
        // please do not touch during refactoring ..
        // roles / perms / customer types

        @UIDoc(usage = 'Only for Inst Role: User')
        public static String INST_USER      = 'user icon'
        @UIDoc(usage = 'Only for Inst Role: Editor')
        public static String INST_EDITOR    = 'user edit icon'
        @UIDoc(usage = 'Only for Inst Role: Adm')
        public static String INST_ADM       = 'user shield icon'

        @UIDoc(usage = 'Only for Customer Type: Institution')
        public static String ORG_INST           = 'walking icon'
        @UIDoc(usage = 'Only for Customer Type: Consortium')
        public static String ORG_CONSORTIUM     = 'landmark icon'
        @UIDoc(usage = 'Only for Customer Type: Support (COLORED)')
        public static String ORG_SUPPORT        = 'theater masks icon red'

        @UIDoc(usage = 'Only for Customer Type: Institution Basic (COLORED)')
        public static String ORG_INST_BASIC         = 'walking icon grey'
        @UIDoc(usage = 'Only for Customer Type: Institution Pro (COLORED)')
        public static String ORG_INST_PRO           = 'walking icon teal'
        @UIDoc(usage = 'Only for Customer Type: Consortium Basic (COLORED)')
        public static String ORG_CONSORTIUM_BASIC   = 'landmark icon grey'
        @UIDoc(usage = 'Only for Customer Type: Consortium Pro (COLORED)')
        public static String ORG_CONSORTIUM_PRO     = 'landmark icon teal'

        @UIDoc(usage = 'Only for Role: User')
        public static String ROLE_USER      = 'user alternate icon'
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
        @UIDoc(usage = 'Only for Buttons/Links with command: Edit element (see also READ)')
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
        public static String READ       = 'search icon'                                                     // TODO
        @UIDoc(usage = 'Only for Buttons/Links with command: Read element (see also EDIT)')
        public static String REMOVE     = 'minus icon'
        @UIDoc(usage = 'Only for Buttons/Links with command: Replace element')
        public static String REPLACE    = 'sync icon'
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
        @UIDoc(usage = 'Only for Links to google maps')
        public static String GOOGLE_MAPS    = 'map marker alternate icon'
        @UIDoc(usage = 'Only for Links to send emails')
        public static String MAIL_TO        = 'paper plane outline icon'
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
        public static String IN_USE     = 'check circle icon olive'

        @UIDoc(usage = 'Only for Property attribute: isHardData (COLORED)')
        public static String HARDDATA       = 'database icon green'
        @UIDoc(usage = 'Only for Property attribute: !isHardData (COLORED)')
        public static String HARDDATA_NOT   = 'times icon red'
        @UIDoc(usage = 'Only for Property attribute: isUsedForLogic (COLORED)')
        public static String LOGIC          = 'cube icon red'
        @UIDoc(usage = 'Only for Property attribute: mandatory (COLORED)')
        public static String MANDATORY      = 'star icon yellow'               // todo: duplicate: survey/renew? > Icon.SIG.MY_OBJECT
        @UIDoc(usage = 'Only for Property attribute: not mandatory (COLORED)')
        public static String MANDATORY_NOT  = 'la-star slash icon yellow'
        @UIDoc(usage = 'Only for Property attribute: multipleOccurrence (COLORED)')
        public static String MULTIPLE       = 'check double icon teal'
        @UIDoc(usage = 'Only for Property attribute: not multipleOccurrence (COLORED)')
        public static String MULTIPLE_NOT   = 'la-check-double slash icon teal'
    }

    class FNC {
        // please do not touch during refactoring ..
        // finance, costs

        @UIDoc(usage = 'Only for Cost items')
        public static String COST           = 'euro sign icon'
        @UIDoc(usage = 'Only for Cost item configurations')
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

    class MATH {
        // please do not touch during refactoring ..

        @UIDoc(usage = 'Mathematical symbol for: is equal to')
        public static String EQUAL              = 'la-equals icon'
        @UIDoc(usage = 'Mathematical symbol for: is less than')
        public static String LESS               = 'la-less-than icon'
        @UIDoc(usage = 'Mathematical symbol for: is greater than')
        public static String GREATER            = 'la-greater-than icon'
        @UIDoc(usage = 'Mathematical symbol for: is less than or equal to')
        public static String LESS_OR_EQUAL      = 'la-less-than-equal icon'
        @UIDoc(usage = 'Mathematical symbol for: is greater than or equal to')
        public static String GREATER_OR_EQUAL   = 'la-greater-than-equal icon'
    }

    class SIG {
        // please do not touch during refactoring ..
        // assignment

        @UIDoc(usage = 'Assignment symbol for: Inheritance')
        public static String INHERITANCE        = 'thumbtack icon'
        @UIDoc(usage = 'Assignment symbol for: Inheritance set auto (COLORED)')
        public static String INHERITANCE_AUTO   = 'la-thumbtack-regular grey icon'
        @UIDoc(usage = 'Assignment symbol for: Inheritance - OFF')
        public static String INHERITANCE_OFF    = 'la-thumbtack slash icon'

//        @UIDoc(usage = 'Assignment symbol for: Marked object')
//        public static String MARKER         = 'bookmark icon'       // todo: duplicate > reporting.history/bookmarks
//        @UIDoc(usage = 'Assignment symbol for: Marked object - OFF')
//        public static String MARKER_OFF     = 'la-bookmark slash icon'

        @UIDoc(usage = 'Assignment symbol for: Is my object')
        public static String MY_OBJECT      = 'star icon'               // todo: duplicate: survey/renew? > Icon.PROP.MANDATORY

        @UIDoc(usage = 'Assignment symbol for: New object')
        public static String NEW_OBJECT     = 'certificate icon'

        @UIDoc(usage = 'Assignment symbol for: Shared object')          // todo: merge with SHARED_OBJECT_ON
        public static String SHARED_OBJECT      = 'share alternate icon'

        @UIDoc(usage = 'Assignment symbol for: Shared object - ON')
        public static String SHARED_OBJECT_ON   = 'la-share icon'
        @UIDoc(usage = 'Assignment symbol for: Shared object - OFF')
        public static String SHARED_OBJECT_OFF  = 'la-share slash icon'

        @UIDoc(usage = 'Assignment symbol for: Object is visible - ON/YES')
        public static String VISIBLE_ON     = 'eye outline icon'
        @UIDoc(usage = 'Assignment symbol for: Object is visible - OFF/NO')
        public static String VISIBLE_OFF    = 'eye outline slash icon'
    }

    class SYM {
        // please do not touch during refactoring ..
        // convenient symbols

        @UIDoc(usage = 'Generic symbol for: Yes/Accept/Done (see also NO)')
        public static String YES    = 'check icon'
        @UIDoc(usage = 'Generic symbol for: No/Reject/Incomplete (see also YES)')
        public static String NO     = 'times icon'
        @UIDoc(usage = 'Generic symbol for: Unkown')
        public static String UNKOWN         = 'question icon'

        @UIDoc(usage = 'Generic symbol for: Checkbox/Option false (see also CHECKBOX_CHECKED)')
        public static String CHECKBOX           = 'square outline icon'
        @UIDoc(usage = 'Generic symbol for: Checkbox/Option true (see also CHECKBOX)')
        public static String CHECKBOX_CHECKED   = 'square outline check icon'

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
        @UIDoc(usage = 'Generic symbol for: Language')
        public static String LANGUAGE       = 'language icon'

        @UIDoc(usage = 'Generic symbol for: Status')
        public static String STATUS         = 'traffic light icon'
        @UIDoc(usage = 'Generic symbol for: Alternative name')
        public static String ALTNAME        = 'tag icon'
        @UIDoc(usage = 'Generic symbol for: Public for API/Data transfer')
        public static String IS_PUBLIC      = 'lock open icon'

        @UIDoc(usage = 'Generic symbol for: Note')
        public static String NOTE           = 'sticky note outline icon'
        @UIDoc(usage = 'Generic symbol for: Properties')
        public static String PROPERTIES     = 'tags icon'
        @UIDoc(usage = 'Generic symbol for: Linked objects')
        public static String LINKED_OBJECTS = 'linkify icon'

        @UIDoc(usage = 'Generic symbol for: Search')
        public static String SEARCH         = 'search icon'
        @UIDoc(usage = 'Generic symbol for: Options')
        public static String OPTIONS        = 'sliders horizontal icon'

        @UIDoc(usage = 'Generic icon / Caution: free to use')
        public static String CIRCLE         = 'circle icon'
        @UIDoc(usage = 'Generic icon / Caution: free to use')
        public static String SQUARE         = 'square icon'
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
    }

    class TOOLTIP {
        // please do not touch during refactoring ..
        // tooltips

        @UIDoc(usage = 'Only for tooltip trigger: Help (COLORED)')
        public static String HELP       = 'grey question circle icon'
        @UIDoc(usage = 'Only for tooltip trigger: Important, possibly warning')
        public static String IMPORTANT  = 'exclamation circle icon'
        @UIDoc(usage = 'Only for tooltip trigger: Info')
        public static String INFO       = 'info circle icon'            // todo: duplicate: Icon.PROP.IN_USE
        @UIDoc(usage = 'Only for tooltip trigger: Error')
        public static String ERROR    = 'exclamation triangle icon'
    }

    // please do not touch during refactoring ..
    // domain classes / top level objects

    @UIDoc(usage = 'Symbol for public Address/Contact/Person')
    public static String ACP_PUBLIC                 = 'address card icon'
    @UIDoc(usage = 'Symbol for private Address/Contact/Person')
    public static String ACP_PRIVATE                = 'address card outline icon'

    public static String ADDRESS                    = 'map marker alternate icon'

    public static String ANNOUNCEMENT               = 'flag icon'

    public static String DOCUMENT                   = 'file alternate icon'

    @UIDoc(usage = 'Symbol for data dashboard')
    public static String DATA_DASHBOARD             = 'chartline icon'

    public static String DUE_DATE                   = 'bell icon'

    public static String FINANCE                    = 'euro sign icon'

    public static String GASCO                      = 'layer group icon'

    public static String IDENTIFIER                 = 'barcode icon'

    public static String IE_GROUP                   = 'object ungroup icon'

    public static String LASER                      = 'la-laser icon'

    public static String LICENSE                    = 'balance scale icon'

    public static String MARKER                     = 'bookmark icon'       // todo: duplicate > reporting.history/bookmarks

    public static String ORG                        = 'university icon'

    public static String PACKAGE                    = 'gift icon'

    public static String PLATFORM                   = 'cloud icon'          // todo: duplicate > url, flagContentGokb, flagContentElasticsearch

    public static String PROVIDER                   = 'broadcast tower icon'    // TODO

    public static String REPORTING                  = 'chart pie icon'

    public static String STATS                      = 'chart bar icon'

    public static String SUBSCRIPTION               = 'clipboard icon'      // todo: duplicate license.licenseCategory, tipp.accessStartDate/tipp.accessEndDate

    public static String SURVEY                     = 'poll icon'

    public static String TASK                       = 'calendar check outline icon'

    public static String TIPP                       = 'book icon'

    public static String VENDOR                     = 'handshake icon'

    public static String WEKB                       = 'la-wekb icon'

    public static String WORKFLOW                   = 'tasks icon'

}
