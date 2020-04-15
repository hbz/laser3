## LAS:eR - API

Source: ${grailsApplication.config.grails.serverURL} - Version: ${de.laser.api.v0.ApiManager.VERSION}

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


