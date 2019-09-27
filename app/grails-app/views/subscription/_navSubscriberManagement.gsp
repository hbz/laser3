<laser:serviceInjection />

<semui:subNav actionName="${actionName}">

    <semui:subNavItem  controller="subscription" action="linkLicenseMembers"
                       params="${[id: params.id]}"
                       text="${message(code:'subscription.details.linkLicenseMembers.label',args:args.memberTypeGenitive)}"/>

    <semui:subNavItem controller="subscription" action="linkPackagesMembers"
                      params="${[id: params.id]}"
                      text="${message(code:'subscription.details.linkPackagesMembers.label',args:args.memberTypeGenitive)}"/>

    <semui:subNavItem controller="subscription" action="propertiesMembers"
                               params="${[id: params.id]}"
                               text="${message(code:'subscription.propertiesMembers.label',args:args.memberTypeGenitive)}"/>

    <semui:subNavItem controller="subscription" action="subscriptionPropertiesMembers"
                      params="${[id: params.id]}"
                      text="${message(code:'subscription.subscriptionPropertiesMembers.label',args:args.memberTypeGenitive)}"/>

</semui:subNav>
