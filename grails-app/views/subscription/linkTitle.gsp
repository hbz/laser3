<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.wekb.Platform; de.laser.wekb.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
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
<br/>
<br/>

<h3 class="ui left floated aligned icon header la-clear-before">${message(code: 'package.plural')}
<ui:totalNumber total="${recordsCount}"/>
</h3>


<g:if test="${error}">
    <ui:msg class="error" noClose="true" text="${error}"/>
</g:if>
<g:else>
    <laser:render template="/templates/filter/packageGokbFilter" model="[tmplConfigShow: [
            ['singleTitle', 'pkgStatus'],
            ['provider', 'vendor', 'ddc', 'curatoryGroup'],
            ['curatoryGroupType', 'automaticUpdates']
    ]]"/>
</g:else>

<ui:messages data="${flash}"/>

<div class="ui icon message" id="durationAlert" style="display: none">
    <i class="notched circle loading icon"></i>

    <div class="content">
        <div class="header">
            <g:message code="globalDataSync.requestProcessing"/>
        </div>
        <g:message code="globalDataSync.requestProcessingInfo"/>

    </div>
</div>



<g:if test="${records}">
    <laser:render template="/templates/filter/packageGokbFilterTable"
                  model="[
                          tmplConfigShow: tmplConfigShow,
                          pkgs: pkgs,
                          bulkProcessRunning: bulkProcessRunning
                  ]"
    />
    <ui:paginate action="linkTitle" controller="subscription" params="${params}"
                    max="${max}" total="${recordsCount}"/>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "package.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="title.filter.notice" args="${[message(code: "package.plural")]}"/></strong>
    </g:else>
</g:else>

<laser:htmlEnd />
