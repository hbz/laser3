package de.laser.helper

import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icons {

    // please do not touch during refactoring ..
    // domain classes / main objects

    public static String ANNOUNCEMENT               = 'flag icon'           // OK

    public static String DOCUMENT                   = 'file alternate icon' // OK - todo: duplicate > tipp.coverageDepth > Icons.TIPP_COVERAGE_DEPTH

    public static String LICENSE                    = 'balance scale icon'  // OK

    public static String MARKER                     = 'bookmark icon'       // OK - todo: duplicate > reporting.history/bookmarks

    public static String ORG                        = 'university icon'     // OK

    public static String PACKAGE                    = 'gift icon'           // OK

    public static String PLATFORM                   = 'cloud icon'          // OK - todo: duplicate > url, flagContentGokb, flagContentElasticsearch

    public static String PROVIDER                   = 'handshake icon'      // OK - todo: duplicate > org.legalInformation > Icons.ORG_LEGAL_INFORMATION

    public static String REPORTING                  = 'chartline icon'      // OK

    public static String SUBSCRIPTION               = 'clipboard icon'      // OK - todo: duplicate license.licenseCategory, tipp.accessStartDate/tipp.accessEndDate

    public static String SURVEY                     = 'chart pie icon'      // OK

    public static String TASK                       = 'calendar check outline icon' // OK

    public static String VENDOR                     = 'shipping fast icon'  // OK - todo: duplicate > subscription/license.isPublicForApi

    public static String WORKFLOW                   = 'tasks icon'          // OK

    // please do not touch during refactoring ..
    // domain class attributes

    public static String ORG_LEGAL_INFORMATION      = 'hands helping'

    public static String SUB_IS_MULTIYEAR           = 'forward'

    public static String TIPP_COVERAGE_DEPTH        = 'file alternate' // ? right

    // please do not touch during refactoring ..
    // properties

    public static String PRIVATE_PROPERTY           = 'shield alternate'

    // please do not touch during refactoring ..
    // generic

    public static String HELP_TOOLTIP               = 'grey question circle'

    // please do not touch during refactoring ..
    // cmds, functions

    public static String CMD_DELETE                 = 'trash alternate outline'

    public static String CMD_UNLINK                 = 'unlink'

    // please do not touch during refactoring ..
    // links

    public static String LINK_EXTERNAL              = 'external alternate'

    public static String LINK_FILTERED              = 'filter'

    public static String LINK_NEXT                  = 'arrow right'     // todo: duplicate: ui:anualRings, ui:statusWithRings

    public static String LINK_PREV                  = 'arrow left'      // todo: duplicate: ui:anualRings, ui:statusWithRings
}
