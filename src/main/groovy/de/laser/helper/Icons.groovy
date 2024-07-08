package de.laser.helper

import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icons {

    class Auth {

        public static String INST_USER              = 'user icon'
        public static String INST_EDITOR            = 'user edit icon'
        public static String INST_ADM               = 'user shield icon'

        public static String ORG_INST_BASIC         = 'user circle icon'    // grey
        public static String ORG_INST_PRO           = 'trophy icon'         // grey
        public static String ORG_CONSORTIUM_BASIC   = 'user circle icon'    // teal
        public static String ORG_CONSORTIUM_PRO     = 'trophy icon'         // teal
        public static String ORG_SUPPORT            = 'theater masks icon'  // red

        public static String ROLE_ADMIN             = 'tools icon'          // orange
        public static String ROLE_USER              = 'user icon'
        public static String ROLE_YODA              = 'star of life icon'   // red
    }


    // please do not touch during refactoring ..
    // domain classes / main objects

    public static String ANNOUNCEMENT               = 'flag icon'

    public static String COSTS                      = 'money bill icon'             // todo
    public static String COSTS_CONFIG               = 'money bill alternate icon'   // todo

    public static String DOCUMENT                   = 'file alternate icon' // todo: duplicate > tipp.coverageDepth > Icons.TIPP_COVERAGE_DEPTH

    public static String GASCO                      = 'layer group icon'

    public static String LICENSE                    = 'balance scale icon'

    public static String MARKER                     = 'bookmark icon'       // todo: duplicate > reporting.history/bookmarks

    public static String ORG                        = 'university icon'

    public static String PACKAGE                    = 'gift icon'

    public static String PLATFORM                   = 'cloud icon'          // todo: duplicate > url, flagContentGokb, flagContentElasticsearch

    public static String PROVIDER                   = 'handshake icon'      // todo: duplicate > org.legalInformation > Icons.ORG_LEGAL_INFORMATION

    public static String REPORTING                  = 'chartline icon'

    public static String STATS                      = 'chart bar icon'

    public static String SUBSCRIPTION               = 'clipboard icon'      // todo: duplicate license.licenseCategory, tipp.accessStartDate/tipp.accessEndDate

    public static String SURVEY                     = 'chart pie icon'

    public static String TASK                       = 'calendar check outline icon'

    public static String VENDOR                     = 'boxes icon'

    public static String WORKFLOW                   = 'tasks icon'

    // please do not touch during refactoring ..
    // domain class attributes

    public static String ADDRESS_PUBLIC             = 'address card icon'

    public static String ADDRESS_PRIVATE            = 'address card outline icon'

    public static String ORG_LEGAL_INFORMATION      = 'hands helping icon'

    public static String SUBSCRIPTION_IS_MULTIYEAR  = 'forward icon'

    public static String TIPP_COVERAGE_DEPTH        = 'file alternate icon'

    // please do not touch during refactoring ..
    // properties

    public static String PRIVATE_PROPERTY           = 'shield alternate icon'

    public static String PROPERTY_HARDDATA          = 'check circle icon green' // COLOR !!

    public static String PROPERTY_LOGIC             = 'cube icon red'           // COLOR !!

    public static String PROPERTY_MANDATORY         = 'star icon'               // todo: duplicate: survey/renew? > Icons.MY_OBJECT

    public static String PROPERTY_MULTIPLE          = 'redo icon orange'        // COLOR !! todo: duplicate: currentSubscriptionsTransfer

    public static String PROPERTY_USED              = 'info circle icon blue'   // COLOR !! todo: duplicate: Icons.INFO

    // please do not touch during refactoring ..
    // generic

    public static String DATE                       = 'calendar alternate outline icon'

    public static String EMAIL                      = 'envelope outline icon'

    public static String ERROR                      = 'exclamation triangle icon'

    public static String HELP                       = 'question icon'

    public static String HELP_TOOLTIP               = 'grey question circle icon'   // COLOR !!

    public static String IMPORTANT_TOOLTIP          = 'exclamation circle icon'     // TODO: merge
    public static String IMPORTANT_TOOLTIP2         = 'exclamation triangle icon'   // TODO: merge

    public static String INFO                       = 'info icon'

    public static String INFO_TOOLTIP               = 'info circle icon'            // todo: duplicate: Icons.PROPERTY_USED

    public static String MY_OBJECT                  = 'star icon'                   // todo: duplicate: survey/renew? > Icons.MANDATORY_PROPERTY

    public static String WARNING                    = 'exclamation icon'

    // please do not touch during refactoring ..
    // cmds, functions

    public static String CMD_COPY                   = 'copy icon'

    public static String CMD_DELETE                 = 'trash alternate outline icon'

    public static String CMD_DOWNLOAD               = 'download icon'

    public static String CMD_EDIT                   = 'write icon'

    public static String CMD_ERASE                  = 'eraser icon'

    public static String CMD_UNLINK                 = 'unlink icon'

    // please do not touch during refactoring ..
    // links

    public static String LINK_EXTERNAL              = 'external alternate icon'

    public static String LINK_FILTERED              = 'filter icon'

    public static String LINK_NEXT                  = 'arrow right icon'     // todo: duplicate: ui:anualRings, ui:statusWithRings

    public static String LINK_PREV                  = 'arrow left icon'      // todo: duplicate: ui:anualRings, ui:statusWithRings
}
