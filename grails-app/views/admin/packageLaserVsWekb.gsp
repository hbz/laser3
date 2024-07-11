<%@ page import="de.laser.helper.Icons; de.laser.utils.AppUtils; de.laser.convenience.Marker; de.laser.storage.RDConstants; de.laser.utils.DateUtils; de.laser.Org; de.laser.Package; de.laser.Platform; de.laser.RefdataValue; java.text.SimpleDateFormat" %>
<laser:htmlStart message="menu.admin.packageLaserVsWekb" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.admin.packageLaserVsWekb" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.packageLaserVsWekb" total="${recordsCount}" floated="true" />

<ui:messages data="${flash}"/>

<g:if test="${!error}">
    <laser:render template="/templates/filter/packageGokbFilter" model="[
            tmplConfigShow: filterConfig,
            curatoryGroupTypes: curatoryGroupTypes,
            automaticUpdates: automaticUpdates,
    ]"/>
</g:if>

<g:if test="${error}">
    <ui:msg class="error" showIcon="true" header="${message(code: 'message.attention')}">
        {error}
    </ui:msg>
</g:if>

<div class="twelve wide column la-clear-before">
    <div>
        <g:if test="${records}">
            <laser:render template="/templates/filter/packageGokbFilterTable" model="[
                    records: records,
                    tmplConfigShow: tableConfig
            ]"/>

            <ui:paginate action="packageLaserVsWekb" controller="admin" params="${params}" max="${max}" total="${recordsCount}"/>

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
