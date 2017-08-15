<laser:subNav actionName="${actionName}">
    <laser:subNavItem controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" message="myinst.currentSubscriptions.label" />
    <!--
        <laser:subNavItem controller="myInstitutions" action="addSubscription" params="${[shortcode:params.shortcode]}" text="New Subscription (via Package)" />
    -->
    <laser:subNavItem controller="myInstitutions" action="emptySubscription" params="${[shortcode:params.shortcode]}" message="myinst.emptySubscription.label" />
</laser:subNav>
