<%@ page import="de.laser.domain.I10nTranslation; com.k_int.properties.PropertyDefinition;com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue"  %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.user.properties', default: 'Properties and Refdatas')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.user.properties" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header"><semui:headerIcon />${message(code: 'menu.user.properties', default: 'Properties and Refdatas')}</h1>


<h3>${message(code: 'propertyDefinition.plural', default: 'Properties')}</h3>
<div class="ui styled fluid accordion">
    <g:each in="${propertyDefinitions}" var="entry">
        <g:if test="${entry.key != "System Config"}">
        <div class="title">
            <i class="dropdown icon"></i>
            <g:message code="propertyDefinition.${entry.key}.label" default="${entry.key}" />
        </div>
        <div class="content">
            <table class="ui celled la-table la-table-small table">
                <thead>
                <tr>
                    <th>DE</th>
                    <th>EN</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                    <g:each in="${entry.value}" var="pd">
                        <g:set var="pdI10nName" value="${I10nTranslation.createI10nOnTheFly(pd, 'name')}" />
                        <tr>
                            <td>${pdI10nName.valueDe}</td>
                            <td>${pdI10nName.valueEn}</td>
                            <td>
                                <g:if test="${! usedPdList?.contains(pd.id)}">
                                    <span data-position="top right" data-tooltip="Dieser Wert wird bisher nicht verwendet (ID:${pd.id})">
                                        <i class="info circle icon grey"></i>
                                    </span>
                                </g:if>

                                <g:if test="${pd.softData}">
                                    <span data-position="top right" data-tooltip="${message(code:'default.softData.tooltip')}">
                                        <i class="tint icon teal"></i>
                                    </span>
                                </g:if>
                                <g:if test="${pd.multipleOccurrence}">
                                    <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                                        <i class="list icon grey"></i>
                                    </span>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </g:if>
    </g:each>
</div>

<h3>${message(code: 'refdata.plural', default: 'Refdatas')}</h3>

<div class="ui styled fluid accordion">
    <g:each in="${rdCategories}" var="rdc">
        <g:set var="rdcI10n" value="${I10nTranslation.createI10nOnTheFly(rdc, 'desc')}" />

        <div class="title">
            <i class="dropdown icon"></i>
            ${fieldValue(bean: rdc, field: "desc")}
        </div>
        <div class="content">

            <table class="ui celled la-table la-table-small table">
                <thead>
                <tr>
                    <th>DE</th>
                    <th>EN</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        <strong>${rdcI10n.valueDe}</strong>
                    </td>
                    <td>
                        <strong>${rdcI10n.valueEn}</strong>
                    </td>
                    <td>
                        <g:if test="${rdc.softData}">
                            <span data-position="top right" data-tooltip="${message(code:'default.softData.tooltip')}">
                                <i class="tint icon teal"></i>
                            </span>
                        </g:if>
                    </td>
                </tr>

                <g:each in="${RefdataValue.findAllByOwner(rdc, [sort: 'value'])}" var="rdv">
                    <tr>
                        <td>
                            ${I10nTranslation.createI10nOnTheFly(rdv, 'value').valueDe}
                        </td>
                        <td>
                            ${I10nTranslation.createI10nOnTheFly(rdv, 'value').valueEn}
                        </td>
                        <td>
                            <g:if test="${! usedRdvList?.contains(rdv.id)}">
                                <span data-position="top right" data-tooltip="Dieser Wert wird bisher nicht verwendet (ID:${rdv.id})">
                                    <i class="info circle icon grey"></i>
                                </span>
                            </g:if>

                            <g:if test="${rdv.softData}">
                                <span data-position="top right" data-tooltip="${message(code:'default.softData.tooltip')}">
                                    <i class="tint icon teal"></i>
                                </span>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

    </g:each>
</div>

</body>
</html>