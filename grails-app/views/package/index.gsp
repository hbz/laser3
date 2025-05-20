<%@ page import="de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.storage.RDConstants; de.laser.utils.DateUtils; de.laser.Org; de.laser.wekb.Package; de.laser.wekb.Platform; de.laser.RefdataValue; java.text.SimpleDateFormat" %>
<laser:htmlStart message="package.show.all" />

<ui:breadcrumbs>
    <ui:crumb message="package.show.all" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="package.show.all" total="${recordsCount}" floated="true" />

<ui:messages data="${flash}"/>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter" model="[
            filterConfig: filterConfig,
            curatoryGroupTypes: curatoryGroupTypes,
            automaticUpdates: automaticUpdates,
    ]"/>
</g:if>

<g:if test="${error}">
    <ui:msg class="error" showIcon="true" header="${message(code: 'message.attention')}" text="${error}" />
</g:if>

<div class="twelve wide column la-clear-before">
    <div>
        <g:if test="${records}">
            <laser:render template="/templates/filter/packageGokbFilterTable" model="[
                    records: records,
                    currentPackageIdSet: currentPackageIdSet,
                    tmplConfigShow: tableConfig
            ]"/>
            <ui:paginate action="index" controller="package" params="${params}" max="${max}" total="${recordsCount}"/>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br/><strong><g:message code="filter.result.empty.object"
                                        args="${[message(code: "package.plural")]}"/></strong>
            </g:if>
            <g:elseif test="${!error}">
                <br/><strong><g:message code="result.empty.object"
                                        args="${[message(code: "package.plural")]}"/></strong>
            </g:elseif>
        </g:else>
    </div>
</div>

<laser:htmlEnd />
