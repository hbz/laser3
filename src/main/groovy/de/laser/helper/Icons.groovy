package de.laser.helper

import de.laser.annotations.UnstableFeature

@UnstableFeature
class Icons {

    // please do not touch during refactoring ..
    // domain classes / main objects

    public static String ANNOUNCEMENT               = 'flag icon'

    public static String DOCUMENT                   = 'file alternate icon' // todo: duplicate > tipp.coverageDepth > Icons.TIPP_COVERAGE_DEPTH

    public static String LICENSE                    = 'balance scale icon'

    public static String MARKER                     = 'bookmark icon'       // todo: duplicate > reporting.history/bookmarks

    public static String ORG                        = 'university icon'

    public static String PACKAGE                    = 'gift icon'

    public static String PLATFORM                   = 'cloud icon'          // todo: duplicate > url, flagContentGokb, flagContentElasticsearch

    public static String PROVIDER                   = 'handshake icon'      // todo: duplicate > org.legalInformation > Icons.ORG_LEGAL_INFORMATION

    public static String REPORTING                  = 'chartline icon'

    public static String SUBSCRIPTION               = 'clipboard icon'      // todo: duplicate license.licenseCategory, tipp.accessStartDate/tipp.accessEndDate

    public static String SURVEY                     = 'chart pie icon'

    public static String TASK                       = 'calendar check outline icon'

    public static String VENDOR                     = 'shipping fast icon'  // todo: duplicate > subscription/license.isPublicForApi

    public static String WORKFLOW                   = 'tasks icon'

    // please do not touch during refactoring ..
    // domain class attributes

    public static String ORG_LEGAL_INFORMATION      = 'hands helping icon'

    public static String SUB_IS_MULTIYEAR           = 'forward icon'

    public static String TIPP_COVERAGE_DEPTH        = 'file alternate icon' // ? right

    // please do not touch during refactoring ..
    // properties

    public static String PRIVATE_PROPERTY           = 'shield alternate icon'

    public static String PROPERTY_HARDDATA          = 'check circle icon green'

    public static String PROPERTY_LOGIC             = 'cube icon red'

    public static String PROPERTY_MANDATORY         = 'star icon'               // todo: duplicate: survey/renew? > Icons.MY_OBJECT

    public static String PROPERTY_MULTIPLE          = 'redo icon orange'        // todo: duplicate: currentSubscriptionsTransfer

    public static String PROPERTY_USED              = 'info circle icon blue'   // todo: duplicate: discountScale.note

    // please do not touch during refactoring ..
    // generic

    public static String DATE                       = 'calendar alternate outline icon'

    public static String HELP_TOOLTIP               = 'grey question circle icon'

    public static String MY_OBJECT                  = 'star icon'           // todo: duplicate: survey/renew? > Icons.MANDATORY_PROPERTY

    // please do not touch during refactoring ..
    // cmds, functions

    public static String CMD_DELETE                 = 'trash alternate outline icon'

    public static String CMD_UNLINK                 = 'unlink icon'

    // please do not touch during refactoring ..
    // links

    public static String LINK_EXTERNAL              = 'external alternate icon'

    public static String LINK_FILTERED              = 'filter icon'

    public static String LINK_NEXT                  = 'arrow right icon'     // todo: duplicate: ui:anualRings, ui:statusWithRings

    public static String LINK_PREV                  = 'arrow left icon'      // todo: duplicate: ui:anualRings, ui:statusWithRings
}
