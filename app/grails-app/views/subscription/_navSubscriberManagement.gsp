<laser:serviceInjection />

<semui:subNav actionName="${actionName}">

    <semui:subNavItem  controller="subscription" action="linkLicenseMembers"
                       params="${[id: params.id]}"
                       message="subscription.details.linkLicenseMembers.label"/>

    <semui:subNavItem controller="subscription" action="linkPackagesMembers"
                      params="${[id: params.id]}"
                      message="subscription.details.linkPackagesMembers.label"/>

    <semui:subNavItem controller="subscription" action="propertiesMembers"
                               params="${[id: params.id]}"
                               message="subscription.propertiesMembers.label"/>

    <semui:subNavItem controller="subscription" action="subscriptionPropertiesMembers"
                      params="${[id: params.id]}"
                      message="subscription.subscriptionPropertiesMembers.label"/>

</semui:subNav>
