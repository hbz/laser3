<laser:serviceInjection />

<semui:subNav actionName="${actionName}">

    <semui:subNavItem  controller="subscription" action="linkLicenseConsortia"
                       params="${[id: params.id]}"
                       message="subscription.details.linkLicenseConsortium.label"/>

    <semui:subNavItem controller="subscription" action="linkPackagesConsortia"
                      params="${[id: params.id]}"
                      message="subscription.details.linkPackagesConsortium.label"/>

    <semui:subNavItem controller="subscription" action="propertiesConsortia"
                               params="${[id: params.id]}"
                               message="subscription.propertiesConsortia.label"/>

    <semui:subNavItem controller="subscription" action="subscriptionPropertiesConsortia"
                      params="${[id: params.id]}"
                      message="subscription.subscriptionPropertiesConsortia.label"/>

</semui:subNav>
