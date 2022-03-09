## LAS:eR - API

Source: ${grailsApplication.config.grails.serverURL} - Version: ${de.laser.api.v0.ApiManager.VERSION}

#### 0.130

- added new endpoint: __/ezb/subscription/list__
- removed parameter timestamp
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
