<laser:serviceInjection/>

<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'generalProperties', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscriptionsManagement.generalProperties')}" tab="generalProperties"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'linkLicense', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscription.details.linkLicenseMembers.label')}" tab="linkLicense"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'linkPackages', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscription.details.linkPackagesMembers.label')}" tab="linkPackages"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                   params="${[id: params.id, tab: 'permanentTitles', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                   text="${message(code: 'subscriptionsManagement.permanentTitles')}" tab="permanentTitles"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'properties', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscriptionsManagement.properties')}" tab="properties"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'providerAgency', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscriptionsManagement.providerAgency')}" tab="providerAgency"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'documents', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscriptionsManagement.documents')}" tab="documents"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'notes', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscriptionsManagement.notes')}" tab="notes"/>

    <ui:subNavItem controller="${controllerName}" action="${actionName}"
                    params="${[id: params.id, tab: 'multiYear', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                    text="${message(code: 'subscription.isMultiYear.label')}" tab="multiYear"/>

    <g:if test="${controllerName == 'subscription' && !params.showMembersSubWithMultiYear}">
        <ui:subNavItem controller="${controllerName}" action="${actionName}"
                        params="${[id: params.id, tab: 'customerIdentifiers', isSiteReloaded:false, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]}"
                        text="${message(code: 'org.customerIdentifier.plural')}" tab="customerIdentifiers"/>
    </g:if>
</ui:subNav>