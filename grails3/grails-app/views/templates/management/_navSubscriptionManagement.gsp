<laser:serviceInjection/>

<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'generalProperties']}"
                    text="${message(code: 'subscriptionsManagement.generalProperties')}" tab="generalProperties"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'linkLicense']}"
                    text="${message(code: 'subscription.details.linkLicenseMembers.label')}" tab="linkLicense"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'linkPackages']}"
                    text="${message(code: 'subscription.details.linkPackagesMembers.label')}" tab="linkPackages"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'properties']}"
                    text="${message(code: 'subscriptionsManagement.properties')}" tab="properties"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'providerAgency']}"
                    text="${message(code: 'subscriptionsManagement.providerAgency')}" tab="providerAgency"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'documents']}"
                    text="${message(code: 'subscriptionsManagement.documents')}" tab="documents"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'notes']}"
                    text="${message(code: 'subscriptionsManagement.notes')}" tab="notes"/>

    <semui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'multiYear']}"
                    text="${message(code: 'subscription.isMultiYear.label')}" tab="multiYear"/>


    <g:if test="${controllerName == 'subscription'}">
        <semui:subNavItem controller="${controllerName}" action="${actionName}"
                        params="${[id: params.id, tab: 'customerIdentifiers']}"
                        text="${message(code: 'org.customerIdentifier.plural')}" tab="customerIdentifiers"/>
    </g:if>
</semui:subNav>