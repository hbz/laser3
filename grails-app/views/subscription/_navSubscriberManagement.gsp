<laser:serviceInjection />

<semui:subNav showInTabular="true" actionName="${actionName}">

    <semui:subNavItem  controller="subscription" action="linkLicenseMembers"
                       params="${[id: params.id]}"
                       text="${message(code:'subscription.details.linkLicenseMembers.label')}"/>

    <semui:subNavItem controller="subscription" action="linkPackagesMembers"
                      params="${[id: params.id]}"
                      text="${message(code:'subscription.details.linkPackagesMembers.label')}"/>

    <semui:subNavItem controller="subscription" action="propertiesMembers"
                               params="${[id: params.id]}"
                               text="${message(code:'subscription.propertiesMembers.label')}"/>

    <semui:subNavItem controller="subscription" action="subscriptionPropertiesMembers"
                      params="${[id: params.id]}"
                      text="${message(code:'subscription.subscriptionPropertiesMembers.label')}"/>

    <semui:subNavItem controller="subscription" action="customerIdentifiersMembers"
                      params="${[id: params.id]}"
                      text="${message(code:'org.customerIdentifier.plural')}"/>

</semui:subNav>
