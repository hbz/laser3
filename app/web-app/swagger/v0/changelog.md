## LAS:eR - API

Source: ${grailsApplication.config.grails.serverURL} - Version: ${de.laser.api.v0.ApiManager.VERSION}

#### 0.101

- added new endpoint: __/platform__
- added new endpoint: __/platformList__
- added new attribute: `Platform.properties`
- added new attribute: `Platform.provider`
- removed attribute: `Platform.provenance`
- removed attribute: `Platform.serviceProvider`
- removed attribute: `Platform.softwareProvider`
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
