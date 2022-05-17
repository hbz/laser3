package de.laser.api.v0

import de.laser.storage.Constants

/**
 * This class is an enum which formats are supported per endpoints. It contains no methods
 */
class ApiReader {

    static SUPPORTED_FORMATS = [
            'costItem':                 [Constants.MIME_APPLICATION_JSON],
            'costItemList':             [Constants.MIME_APPLICATION_JSON],
            'document':                 [Constants.MIME_ALL],
            'ezb/subscription':         [Constants.MIME_TEXT_TSV],
            'ezb/subscription/list':    [Constants.MIME_APPLICATION_JSON],
            'ezb/license/illIndicators':    [Constants.MIME_APPLICATION_JSON],
            'issueEntitlements':        [Constants.MIME_TEXT_PLAIN, Constants.MIME_APPLICATION_JSON],
            'license':                  [Constants.MIME_APPLICATION_JSON],
            'licenseList':              [Constants.MIME_APPLICATION_JSON],
            'onixpl':                   [Constants.MIME_APPLICATION_XML],
            'oamonitor/organisations/list': [Constants.MIME_APPLICATION_JSON],
            'oamonitor/organisations':      [Constants.MIME_APPLICATION_JSON],
            'oamonitor/subscriptions':      [Constants.MIME_APPLICATION_JSON],
            'orgAccessPoint':             [Constants.MIME_APPLICATION_JSON],
            'organisation':             [Constants.MIME_APPLICATION_JSON],
            'package':                  [Constants.MIME_APPLICATION_JSON],
            'platform':                 [Constants.MIME_APPLICATION_JSON],
            'platformList':             [Constants.MIME_APPLICATION_JSON],
            'propertyList':             [Constants.MIME_APPLICATION_JSON],
            'refdataList':              [Constants.MIME_APPLICATION_JSON],
            'statistic/packages/list':      [Constants.MIME_APPLICATION_JSON],
            'statistic/packages':           [Constants.MIME_APPLICATION_JSON],
            'subscription':             [Constants.MIME_APPLICATION_JSON],
            'subscriptionList':         [Constants.MIME_APPLICATION_JSON]
    ]

    static SIMPLE_QUERIES = [
            'oamonitor/organisations/list',
            'ezb/subscription/list',
            'refdataList',
            'platformList',
            'propertyList',
            'statistic/packages/list'
    ]


    // ##### CONSTANTS #####

    final static NO_CONSTRAINT          = "NO_CONSTRAINT"
    final static LICENSE_STUB           = "LICENSE_STUB"

    // type of stub to return
    final static PACKAGE_STUB           = "PACKAGE_STUB"
    final static SUBSCRIPTION_STUB      = "SUBSCRIPTION_STUB"

    // ignoring relations
    final static IGNORE_ALL             = "IGNORE_ALL"  // cutter for nested objects
    final static IGNORE_NONE            = "IGNORE_NONE" // placeholder, if needed
    final static IGNORE_CLUSTER         = "IGNORE_CLUSTER"

    final static IGNORE_LICENSE         = "IGNORE_LICENSE"
    final static IGNORE_ORGANISATION    = "IGNORE_ORGANISATION"
    final static IGNORE_PACKAGE         = "IGNORE_PACKAGE"
    final static IGNORE_SUBSCRIPTION    = "IGNORE_SUBSCRIPTION"
    final static IGNORE_TITLE           = "IGNORE_TITLE"
    final static IGNORE_TIPP            = "IGNORE_TIPP"

    final static IGNORE_SUBSCRIPTION_AND_PACKAGE = "IGNORE_SUBSCRIPTION_AND_PACKAGE"

    final static IGNORE_CUSTOM_PROPERTIES        = "IGNORE_CUSTOM_PROPERTIES"
    final static IGNORE_PRIVATE_PROPERTIES       = "IGNORE_PRIVATE_PROPERTIES"
}
