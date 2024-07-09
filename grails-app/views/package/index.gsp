<%@ page import="de.laser.helper.Icons; de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.storage.RDConstants; de.laser.utils.DateUtils; de.laser.Org; de.laser.Package; de.laser.Platform; de.laser.RefdataValue; java.text.SimpleDateFormat" %>
<laser:htmlStart message="package.show.all" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="package.show.all" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="package.show.all" total="${recordsCount}" floated="true" />

<ui:messages data="${flash}"/>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter" model="[
            tmplConfigShow: filterConfig,
            curatoryGroupTypes: curatoryGroupTypes,
            automaticUpdates: automaticUpdates,
    ]"/>
</g:if>

<g:if test="${error}">
    <div class="ui icon error message">
        <i class="${Icons.SYM.ERROR}"></i>
        <i class="close icon"></i>
        <div class="content">
            <div class="header">
                ${message(code: 'message.attention')}
            </div>
            <p>${error}</p>
        </div>
    </div>
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
