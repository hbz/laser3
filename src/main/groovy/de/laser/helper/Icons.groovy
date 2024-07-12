package de.laser.helper

import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icons {

    class DFT {
        // please do not touch during refactoring ..
        // basic / placeholders

        public static String PLACEHOLDER    = 'coffee icon'

        public static String CIRCLE         = 'circle icon'
        public static String SQUARE         = 'square icon'
    }

    class AUTH {
        // please do not touch during refactoring ..
        // roles / perms / customer types

        public static String INST_USER              = 'user icon'
        public static String INST_EDITOR            = 'user edit icon'
        public static String INST_ADM               = 'user shield icon'

        public static String ORG_INST_BASIC         = 'user circle icon grey'   // COLOR INCLUDED !!
        public static String ORG_INST_PRO           = 'trophy icon grey'        // COLOR INCLUDED !!
        public static String ORG_CONSORTIUM_BASIC   = 'user circle icon teal'   // COLOR INCLUDED !!
        public static String ORG_CONSORTIUM_PRO     = 'trophy icon teal'        // COLOR INCLUDED !!
        public static String ORG_SUPPORT            = 'theater masks icon red'  // COLOR INCLUDED !!

        public static String ROLE_ADMIN             = 'tools icon'              // default: orange
        public static String ROLE_USER              = 'user icon'
        public static String ROLE_YODA              = 'star of life icon'       // default: red
    }

    class CMD {
        // please do not touch during refactoring ..
        // cmds, functions

        public static String ATTACHMENT     = 'attach icon'
        public static String COPY           = 'copy icon'
        public static String DELETE         = 'trash alternate outline icon'
        public static String DOWNLOAD       = 'download icon'
        public static String EDIT           = 'write icon'
        public static String ERASE          = 'eraser icon'
        public static String MOVE_UP        = 'arrow up icon'
        public static String MOVE_DOWN      = 'arrow down icon'
        public static String REPLACE        = 'retweet icon'
        public static String SHOW_MORE      = 'angle double down icon'
        public static String UNLINK         = 'unlink icon'
    }

    class FNC {
        // please do not touch during refactoring ..
        // finance, costs

        public static String COST           = 'money bill icon'
        public static String COST_CONFIG    = 'money bill alternate icon'

        public static String COST_POSITIVE  = 'circle plus icon green'  // COLOR INCLUDED !!
        public static String COST_NEGATIVE  = 'circle minus icon red'   // COLOR INCLUDED !!
        public static String COST_NEUTRAL   = 'circle icon yellow'      // COLOR INCLUDED !!
    }

    class LNK {
        // please do not touch during refactoring ..
        // links

        public static String EXTERNAL       = 'external alternate icon'
        public static String FILTERED       = 'filter icon'
        public static String NEXT           = 'arrow right icon'     // todo: duplicate: ui:anualRings, ui:statusWithRings
        public static String PREV           = 'arrow left icon'      // todo: duplicate: ui:anualRings, ui:statusWithRings
        public static String WEKB           = 'la-gokb icon'
    }

    class PROP {
        // please do not touch during refactoring ..
        // properties

        public static String HARDDATA       = 'check circle icon green' // COLOR INCLUDED !!
        public static String IN_USE         = 'info circle icon blue'   // COLOR INCLUDED !! todo: duplicate: Icons.UI.INFO
        public static String IS_PRIVATE     = 'shield alternate icon'
        public static String LOGIC          = 'cube icon red'           // COLOR INCLUDED !!
        public static String MANDATORY      = 'star icon'               // todo: duplicate: survey/renew? > Icons.MY_OBJECT
        public static String MULTIPLE       = 'redo icon orange'        // COLOR INCLUDED !! todo: duplicate: currentSubscriptionsTransfer
    }

    class SYM {
        // please do not touch during refactoring ..
        // convenient symbols

        public static String DATE           = 'calendar alternate outline icon'
        public static String EMAIL          = 'envelope outline icon'
    }

    class UI {
        // please do not touch during refactoring ..
        // dialogs / unique symbols

        public static String ERROR          = 'exclamation triangle icon'
        public static String INFO           = 'info icon'
        public static String SUCCESS        = 'check icon'
        public static String WARNING        = 'exclamation icon'

        public static String HELP           = 'question icon'
    }

    // please do not touch during refactoring ..
    // domain classes / main objects

    public static String ANNOUNCEMENT               = 'flag icon'

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

    public static String UNKOWN                     = 'question icon'

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
    // generic

    public static String DATE                       = 'calendar alternate outline icon'

    public static String EMAIL                      = 'envelope outline icon'

    public static String HELP                       = 'question icon'

    public static String HELP_TOOLTIP               = 'grey question circle icon'   // COLOR INCLUDED !!

    public static String IMPORTANT_TOOLTIP          = 'exclamation circle icon'     // TODO: merge
    public static String IMPORTANT_TOOLTIP2         = 'exclamation triangle icon'   // TODO: merge

    public static String INFO_TOOLTIP               = 'info circle icon'            // todo: duplicate: Icons.PROP.IN_USE

    public static String MY_OBJECT                  = 'star icon'                   // todo: duplicate: survey/renew? > Icons.PROP.MANDATORY

}
