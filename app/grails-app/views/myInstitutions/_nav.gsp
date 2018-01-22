<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" message="myinst.currentSubscriptions.label" />
    <!--
        <semui:subNavItem controller="myInstitutions" action="addSubscription" params="${[shortcode:params.shortcode]}" text="New Subscription (via Package)" />
    -->
    <semui:subNavItem controller="myInstitutions" action="emptySubscription" params="${[shortcode:params.shortcode]}" message="myinst.emptySubscription.label" />
</semui:subNav>
