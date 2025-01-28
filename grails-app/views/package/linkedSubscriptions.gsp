<%@ page import="de.laser.CustomerTypeService" %>

<laser:htmlStart message="package.show.nav.linkedSubscriptions" />

<ui:breadcrumbs>
    <ui:crumb controller="package" action="index" message="package.show.all"/>
    <ui:crumb class="active" message="package.show.nav.linkedSubscriptions"/>
</ui:breadcrumbs>


<ui:h1HeaderWithIcon type="package">
    ${packageInstance.name}
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyPkg: isMyPkg]}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>

<ui:objectStatus object="${packageInstance}" />

<ui:messages data="${flash}"/>


<h2>
    <g:message code="package.show.nav.linkedSubscriptions"/> <ui:totalNumber total="${num_sub_rows}" class="floated"/>
</h2>

<laser:render template="${customerTypeService.getSubscriptionFilterTemplatePath()}"/>

<laser:render template="/templates/subscription/subscriptionTable"/>

<laser:htmlEnd/>
