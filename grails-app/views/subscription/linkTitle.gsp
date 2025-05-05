<%@ page import="de.laser.RefdataValue;de.laser.storage.RDConstants" %>
<laser:htmlStart message="subscription.details.linkTitle.label.subscription" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.linkTitle.label.subscription')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${subscription.name}" />
<br/>
<br/>
<h2 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'subscription.details.linkTitle.heading.subscription')}</h2>

<ui:messages data="${flash}"/>

<laser:render template="/templates/filter/tipp_ieFilter"/>

<h3 class="ui icon header la-clear-before la-noMargin-top">
    <ui:bubble count="${num_tipp_rows}" grey="true"/> <g:message code="title.filter.result"/>
</h3>

<g:if test="${params.containsKey('filterSet')}">
    <div class="ui form">
        <div class="three wide fields">
            <div class="field">
                <laser:render template="/templates/titles/sorting_dropdown" model="${[sd_type: 2, sd_journalsOnly: journalsOnly, sd_sort: params.sort, sd_order: params.order]}" />
            </div>
        </div>
    </div>
    <div class="ui grid">
        <div class="row">
            <div class="column">
                <laser:render template="/templates/tipps/table_accordion" model="[tipps: titlesList, fixedSubscription: subscription, showPackageLinking: true, disableStatus: true]"/>
            </div>
        </div>
    </div>
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}" max="${max}" total="${num_tipp_rows}"/>
</g:if>
<g:else>
    <ui:msg class="info" showIcon="true" message="title.filter.notice"/>
</g:else>

<laser:htmlEnd />
