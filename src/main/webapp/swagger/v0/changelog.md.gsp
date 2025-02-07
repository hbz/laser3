## LAS:eR - API

Source: ${de.laser.config.ConfigMapper.getGrailsServerURL()} - Version: ${de.laser.api.v0.ApiManager.VERSION}

#### 3.5 (preview)

- removed attribute: `EZBInstitutionStub.status`
- removed attribute: `OrganisationStub.status`
- removed attribute: `Org.links`
- removed new virtual object: `Link_Org`

#### 3.4.1 (ERMS-6223, ERMS-6224, ERMS-6238)

- internal refactoring: `EZBInstitutionStub.status`
- internal refactoring: `OrganisationStub.status`
- internal refactoring: `Org.links`

#### 3.4

- internal refactoring: `Org.type`

#### 3.3

- internal refactoring

#### 3.2

- internal refactoring

#### 3.1

- added new attribute: `SubscriptionStub.status`

#### 3.0

- removed attribute: `CostItem.subPkg`
- added new attribute: `CostItem.pkg`
- added new attribute: `IssueEntitlement.status`
- added new attribute: `License.altnames`
- added new attribute: `License.providers`
- added new attribute: `License.vendors`
- removed attribute: `Org.addresses` (split to public/privateAddresses)
- removed attribute: `Org.contacts`
- removed attribute: `Org.gokbId`
- added new attribute: `Org.publicAddresses`
- added new attribute: `Org.privateAddresses`
- removed attribute: `Package.organisations`
- added new attribute: `Package.provider`
- added new attribute: `Package.vendors`
- added new attribute: `Platform.provider`
- removed attribute: `Platform.serviceProvider`
- removed attribute: `Platform.softwareProvider`
- added new object: `Provider`
- added new object: `ProviderStub`
- added new object: `Vendor`
- added new object: `VendorStub`
- added new attribute: `Subscription.altnames`
- added new attribute: `Subscription.providers`
- added new attribute: `Subscription.vendors`

#### 2.15

- removed attribute: `Org.sector`

#### 2.14

- internal refactoring

#### 2.13

- internal refactoring

#### 2.12

- internal refactoring

#### 2.11

- internal refactoring

#### 2.10

- internal refactoring

#### 2.9

- /ezb/subscriptions: 404 handling fixed

#### 2.8

- new error code `503` introduced when too many database connections are open

#### 2.7

- internal refactoring

#### 2.6

- internal refactoring

#### 2.5

- internal refactoring

#### 2.4

- internal refactoring

#### 2.3

- internal refactoring

#### 2.2

- internal refactoring

#### 2.1

- internal refactoring

#### 2.0

- internal refactoring
- added new attribute: `Subscription.holdingSelection`
- added new attribute: `Subscription.referenceYear`
- added new attribute: `OrgAccessPoint.mailDomain`
- added new attribute: `OrganisationStub.sortname`
- removed attribute: `IssueEntitlement.name`
- removed attribute: `IssueEntitlement.status`
- removed attribute: `Org.shortname` (moved to OrganisationStub)
- removed attribute: `Org.name`
- removed attribute: `Person.addresses`
- removed attribute: `TitleInstancePackagePlatform.addresses`

#### 1.9

- internal refactoring

#### 1.8

- internal refactoring

#### 1.7

- internal refactoring
- bugfix for changedSince argument

#### 1.6

- added request parameter ezbOrgId for __/ezb/subscription/list/__

#### 1.5

- listing at __/ezb/subscription/list/__ is now restricted upon current subscriptions

#### 1.4

- internal refactoring

#### 1.3

- bugfix collection of ies

#### 1.2

- bugfix collection of ies

#### 1.1

- added request parameter `changedFrom` for __/ezb/subscription/__
- added new object: `EZBInstitutionList` (for documentation; no change on internal code)
- added new object: `EZBInstitutionStub` (for documentation; no change on internal code)
- removal of mandatory consortium permission for member subscriptions
- schema updated
- internal refactoring

#### 1.0

- first productive version of API with LAS:eR version 2.3

#### 0.138

- internal refactoring

#### 0.137

- added new attribute: `CostItemCollection.isVisibleForSubscriber`
- added new attribute: `License.openEnded`
- added new attribute: `PackageStub.status`
- added new attribute: `Platform.status`
- added new attribute: `PlatformStub.status`
- added new attribute: `Organisation.status`
- added new attribute: `OrganisationStub.status`
- modified attribute: `Package.packageStatus` to `Package.status`

#### 0.136

- added new endpoint: __/ezb/license/illIndicators__
- added new object: `PropertySet`

#### 0.135

- added new attribute: `License.predecessors`
- added new attribute: `License.successors`
- modified attribute: `Subscription.predecessor` to `Subscription.predecessors` (object stub changed to array)
- modified attribute: `Subscription.successors` to `Subscription.successor` (object stub changed to array)

#### 0.134

- updating outdated schemata
- added new virtual object: `DeweyDecimalClassification`
- added new virtual object: `Link_License`
- added new virtual object: `Link_Org`
- added new virtual object: `Link_Subscription`
- added new virtual object: `Package_in_CostItem`
- added new virtual object: `PriceItemCollection`
- added new virtual object: `TitleGroup`
- added new attribute: `Contact.language`
- added new attribute: `CostItem.billingSumRounding`
- added new attribute: `CostItem.subPkg`
- added new attribute: `CostItem.titleGroups`
- added new attribute: `CostItemCollection.billingSumRounding`
- added new attribute: `CostItemCollection.finalCostRounding`
- added new attribute: `IssueEntitlement_in_CostItem.priceItems`
- added new attribute: `IssueEntitlement_in_CostItem.status`
- added new attribute: `License.linkedLicenses`
- added new attribute: `LicenseStub.startDate`
- added new attribute: `LicenseStub.endDate`
- added new attribute: `Organisation.altNames`
- added new attribute: `Organisation.legalPatronName`
- added new attribute: `Organisation.linkResolverBaseURL`
- added new attribute: `Organisation.links`
- added new attribute: `Organisation.retirementDate`
- added new attribute: `Organisation.url`
- added new attribute: `Organisation.urlGov`
- added new attribute: `Package.altnames`
- added new attribute: `Package_in_Subscription.altnames`
- added new attribute: `Subscription.hasPublishComponent`
- added new attribute: `Subscription.isAutomaticRenewAnnually`
- added new attribute: `Subscription.linkedSubscriptions`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.accessType`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.altnames`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.dateFirstInPrint`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.dateFirstOnline`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.ddcs`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.editionStatement`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.firstAuthor`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.firstEditor`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.languages`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.openAccess`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.priceItems`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.publisherName`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.seriesName`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.subjectReference`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.titleType`
- added new attribute: `TitleInstancePackagePlatform_in_Subscription.volume`
- added new attribute: `TitleInstancePackagePlatform_in_Package.accessStartDate`
- added new attribute: `TitleInstancePackagePlatform_in_Package.accessEndDate`
- added new attribute: `TitleInstancePackagePlatform_in_Package.accessType`
- added new attribute: `TitleInstancePackagePlatform_in_Package.altnames`
- added new attribute: `TitleInstancePackagePlatform_in_Package.dateFirstInPrint`
- added new attribute: `TitleInstancePackagePlatform_in_Package.dateFirstOnline`
- added new attribute: `TitleInstancePackagePlatform_in_Package.ddcs`
- added new attribute: `TitleInstancePackagePlatform_in_Package.editionStatement`
- added new attribute: `TitleInstancePackagePlatform_in_Package.firstAuthor`
- added new attribute: `TitleInstancePackagePlatform_in_Package.firstEditor`
- added new attribute: `TitleInstancePackagePlatform_in_Package.languages`
- added new attribute: `TitleInstancePackagePlatform_in_Package.openAccess`
- added new attribute: `TitleInstancePackagePlatform_in_Package.priceItems`
- added new attribute: `TitleInstancePackagePlatform_in_Package.publisherName`
- added new attribute: `TitleInstancePackagePlatform_in_Package.seriesName`
- added new attribute: `TitleInstancePackagePlatform_in_Package.subjectReference`
- added new attribute: `TitleInstancePackagePlatform_in_Package.titleType`
- added new attribute: `TitleInstancePackagePlatform_in_Package.volume`
- modified attribute: `CostItem.financialYear`: removed overhead structure
- modified attribute: `CostItem.invoice` changed to `CostItem.invoiceNumber` (removed overhead structure)
- modified attribute: `CostItem.order` changed to `CostItem.orderNumber` (removed overhead structure)
- modified attribute: `TitleInstancePackagePlatform_in_Package.publishers` changed to `TitleInstancePackagePlatform_in_Package.providers`
- modified attribute: `TitleInstancePackagePlatform_in_Subscription.publishers` changed to `TitleInstancePackagePlatform_in_Subscription.providers`
- removed virtual object: `Invoice`
- removed virtual object: `Order`
- removed virtual object: `OA2020_Virtual`
- removed stub: `TitleStub`
- removed attribute: `Contact.lastUpdated`
- removed attribute: `Contact.type`
- removed attribute: `CostItem.costItemCategory`
- removed attribute: `Invoice.dateOfInvoice`
- removed attribute: `Invoice.dateOfPayment`
- removed attribute: `Invoice.datePassedToFinance`
- removed attribute: `Invoice.endDate`
- removed attribute: `Invoice.id`
- removed attribute: `Invoice.startDate`
- removed attribute: `IssueEntitlement.coreStatusStart`
- removed attribute: `IssueEntitlement.coreStatusEnd`
- removed attribute: `IssueEntitlement.coreStatus`
- removed attribute: `IssueEntitlement.ieReason`
- removed attribute: `License.normReference`
- removed attribute: `Order.id`
- removed attribute: `Organisation.comment`
- removed attribute: `Organisation.scope`
- removed attribute: `Package.autoAccept`
- removed attribute: `Package.cancellationAllowances`
- removed attribute: `Package.consistent`
- removed attribute: `Package.endDate`
- removed attribute: `Package.isPublic`
- removed attribute: `Package.sortName`
- removed attribute: `Package.startDate`
- removed attribute: `Package.vendorURL`
- removed attribute: `Package_in_Subscription.vendorURL`
- removed attribute: `Platform.normname`
- removed attribute: `Platform.serviceProvider`
- removed attribute: `Platform.softwareProvider`
- removed attribute: `Subscription.cancellationAllowances`
- removed attribute: `Subscription.isSlaved`
- removed attribute: `Subscription.manualRenewalDate`
- removed attribute: `Subscription.noticePeriod`
- removed attribute: `Subscription.type`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.delayedOA`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.hybridOA`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.option`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.payment`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.statusReason`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.subscription`
- removed attribute: `TitleInstancePackagePlatform_in_Subscription.title`
- removed attribute: `TitleInstancePackagePlatform_in_Package.delayedOA`
- removed attribute: `TitleInstancePackagePlatform_in_Package.hybridOA`
- removed attribute: `TitleInstancePackagePlatform_in_Package.option`
- removed attribute: `TitleInstancePackagePlatform_in_Package.payment`
- removed attribute: `TitleInstancePackagePlatform_in_Package.statusReason`
- removed attribute: `TitleInstancePackagePlatform_in_Package.subscription`
- removed attribute: `TitleInstancePackagePlatform_in_Package.title`
- internal refactoring
- identifier value "Unknown" not rendered (= considered as empty)

#### 0.133

- removed duplicate column access_type from __/ezb/subscription__
- removed attribute: `Package.license`

#### 0.132

- bugfix for __/subscription/list__

#### 0.131

- added new attribute: `Subscription.members` for __/ezb/subscription/list__
- internal refactoring

#### 0.130

- added new endpoint: __/ezb/subscription/list__
- added new parameter changedFrom

#### 0.129

- added new endpoint: __/ezb/subscription__
- added new API level: API_LEVEL_EZB

#### 0.128

- added new attribute: `IssueEntitlement.perpetualAccessBySub`

#### 0.127

- internal refactoring

#### 0.126

- removed attribute: `License.type`
- updated schema:
  - `License.calculatedType`
  - `Subscription.calculatedType`

#### 0.125

- added new attribute: `Package.scope`
- added new attribute: `Package.file`
- removed attribute: `Package.fixed`
- removed attribute: `Package.packageScope`
- removed attribute: `Package.listVerifiedDate`
- removed attribute: `Package.listStatus`

#### 0.124

- internal refactoring

#### 0.123

- fixed attribute: `License.properties`
- fixed attribute: `Org.properties`
- fixed attribute: `Platform.properties`
- fixed attribute: `Subscription.properties`

#### 0.122

- added new attribute: `Org.eInvoicePortal`
- added new attribute: `Org.eInvoice`

#### 0.121

- modified attribute: `Address.type`

#### 0.120

- internal refactoring

#### 0.119

- internal refactoring

#### 0.118

- internal refactoring

#### 0.117

- internal refactoring

#### 0.116

- internal refactoring

#### 0.115

- added new endpoint: __/OrgAccessPoint__
- added new attribute: `Org.orgAccessPoints`
- added new attribute: `Subscription.orgAccessPoints`

#### 0.114

- internal refactoring

#### 0.113

- internal refactoring

#### 0.112

- internal refactoring

#### 0.111

- internal refactoring

#### 0.110

- added new attribute: `CostItem.isVisibleForSubscriber`
- modified attribute: `CostItem.finalCostRounding`

#### 0.109

- added new attribute: `Subscription.kind` for __/statistic/packages__

#### 0.108

- (re)added new attribute: `License.status`

#### 0.107

- fixed __/subscription__

#### 0.106

- reworked access to __/oamonitor/*__
- reworked access to __/statistic/*__

#### 0.105

- renamed endpoint: __/oaMonitorList__ to  __/oamonitor/organisations/list__
- renamed endpoint: __/oaMonitor__ to  __/oamonitor/organisations__
- renamed endpoint: __/oaMonitorSubscription__ to  __/oamonitor/subscriptions__
- renamed endpoint: __/statisticList__ to  __/statistic/packages/list__
- renamed endpoint: __/statisticPackage__ to  __/statistic/packages__

#### 0.104

- added `costItems` for __/oaMonitorSubscription__

#### 0.103

- changed calculation of: `CostItem.calculatedType`
- higher access restrictions: `Subscription.costItems`

#### 0.102

- added new attribute: `License.licenseCategory`
- removed attribute: `License.licenseType`
- removed attribute: `License.status`

#### 0.101

- added new endpoint: __/platform__
- added new endpoint: __/platformList__
- added new attribute: `Platform.properties`
- added new attribute: `Platform.provider`
- removed attribute: `Platform.provenance`
- removed attribute: `Platform.status`
- removed attribute: `Platform.type`
- updated schema:
    - `format: date-time` to `format: "yyyy-MM-dd'T'HH:mm:ss"`
    - `format: date` to `format: "yyyy-MM-dd'T'HH:mm:ss"`

#### 0.100

- modifying nested objects of type `Identifier` affects now:
    - `License.lastUpdated`
    - `Organisation.lastUpdated`
    - `Package.lastUpdated`
    - `Subscription.lastUpdated`
- modifying nested objects of type `Property` affects now:
    - `License.lastUpdated`
    - `Organisation.lastUpdated`
    - `Person.lastUpdated`
    - `Platform.lastUpdated`
    - `Subscription.lastUpdated`
    - `Organisation.lastUpdated`
- removed attribute: `Property[isPublic=No].dateCreated`
- removed attribute: `Property[isPublic=No].lastUpdated`

#### 0.99

- removed attribute: `License.onixplLicense`
- removed object: `OnixplLicense`

#### 0.98

- added new attribute: `Address.region`
- added new attribute: `Org.region`
- removed attribute: `Address.state`
- removed attribute: `Org.federalState`

#### 0.97

- added new endpoint: __/oaMonitorSubscription__
- added new attribute: `OrganisationStub.type`
- removed unused attribute: `Organisation.roleType`

#### 0.96

- restricted access to __/document__ (for owners only)
- changed request header `accept` to `*/*` for __/document__
- changed HTTP status code for __/document__ to `404` if there are only deleted contexts

#### 0.95

- reworked access to __/costItem__

#### 0.94

- changed HTTP status code for __/oaMonitorList__  to `404` if result is empty
- changed HTTP status code for __/statisticList__ to `404` if result is empty

#### 0.93

- added new attribute: `Package.contentType`
- added new attribute: `TitleStub.medium`
- removed attribute: `Package.packageType`
- removed attribute: `TitleStub.type`

#### 0.92

- added new HTTP status codes for __/oaMonitor*__ and __/statistic*__

#### 0.91

- fixed HTTP status codes for erroneous requests
- internal refactoring

#### 0.90

- added request header `x-debug` for __/oaMonitor__ and __/statistic__
- internal refactoring

#### 0.89

- added new attribute: `Property.type`
- added new attribute: `Property.refdataCategory`
- renamed attribute: `Property.name` to `Property.token`
- renamed attribute: `Property.description` to `Property.scope`
- added request header `x-debug` for __/propertyList__ and __/refdataList__

#### 0.88

- added new changelog: `/api/${apiVersion}/changelog.md`
- renamed specs url: `/api/${apiVersion}/specs.yaml`
- added new response header: `Laser-Api-Version`
- renamed debug response header to: `Laser-Api-Debug-foo`

#### 0.87

- added new request header `x-debug` for __/licenseList__ and __/subscriptionList__
