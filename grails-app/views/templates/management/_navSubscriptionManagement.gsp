<laser:serviceInjection/>

<semui:tabs actionName="${actionName}">

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'generalProperties']}"
                    text="${message(code: 'subscriptionsManagement.generalProperties')}" tab="generalProperties"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'providerAgency']}"
                    text="${message(code: 'subscriptionsManagement.providerAgency')}" tab="providerAgency"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'documents']}"
                    text="${message(code: 'subscriptionsManagement.documents')}" tab="documents"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'notes']}"
                    text="${message(code: 'subscriptionsManagement.notes')}" tab="notes"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'multiYear']}"
                    text="${message(code: 'subscription.isMultiYear.label')}" tab="multiYear"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'linkLicense']}"
                    text="${message(code: 'subscription.details.linkLicenseMembers.label')}" tab="linkLicense"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'linkPackages']}"
                    text="${message(code: 'subscription.details.linkPackagesMembers.label')}" tab="linkPackages"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'properties']}"
                    text="${message(code: 'subscriptionsManagement.properties')}" tab="properties"/>

    <semui:tabsItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'customerIdentifiers']}"
                    text="${message(code: 'org.customerIdentifier.plural')}" tab="customerIdentifiers"/>
</semui:tabs>