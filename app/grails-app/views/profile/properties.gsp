<%@ page import="de.laser.domain.I10nTranslation; com.k_int.properties.PropertyDefinition;com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.RefdataValue"  %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.user.properties', default: 'Properties and Refdatas')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.help" class="active"/>
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
                    <th>${message(code:'propertyDefinition.name.label', default:'Name')}</th>
                    <th>Name (DE)</th>
                    <th>Name (EN)</th>
                    <!--<th>DE: Description</th>
                            <th>EN: Description</th>-->
                </tr>
                </thead>
                <tbody>
                <g:each in="${entry.value}" var="pd">
                    <g:set var="pdI10nName"  value="${I10nTranslation.createI10nOnTheFly(pd, 'name')}" />
                    <!--<g:set var="pdI10nDescr" value="${I10nTranslation.createI10nOnTheFly(pd, 'descr')}" />-->
                    <tr>
                        <td>
                        <!-- ${pd.id} -->
                            ${fieldValue(bean: pd, field: "name")}
                            <g:if test="${pd.softData}">
                                <span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
                            </g:if>
                            <g:if test="${pd.multipleOccurrence}">
                                <span class="badge badge-info" title="${message(code:'default.multipleOccurrence.tooltip')}"> &#9733; </span>
                            </g:if>
                        </td>
                        <td>${pdI10nName.valueDe}</td>
                        <td>${pdI10nName.valueEn}</td>
                        <!--<td>${pdI10nDescr.valueDe}</td>
                                    <td>${pdI10nDescr.valueEn}" field="valueEn" /></td>-->
                    </tr>
                </g:each>

                </tbody>
            </table>
        </div>
    </g:if>
    </g:each>
</div>
<hr>
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
                    <th>Category (Key)</th>
                    <th>Value (Key)</th>
                    <th>DE</th>
                    <th>EN</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>
                        ${fieldValue(bean: rdc, field: "desc")}
                        <g:if test="${rdc.softData}">
                            <span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
                        </g:if>
                    </td>
                    <td></td>
                    <td>
                        <strong>${rdcI10n.valueDe}</strong>
                    </td>
                    <td>
                        <strong>${rdcI10n.valueEn}</strong>
                    </td>
                </tr>

                <g:each in="${RefdataValue.findAllByOwner(rdc, [sort: 'value'])}" var="rdv">
                    <tr>
                        <td></td>
                        <td>
                            <g:if test="${rdvList?.contains(rdv.id)}">
                                ${rdv.value}
                            </g:if>
                            <g:else>
                                <span data-position="top left" data-tooltip="Dieser Wert wird bisher nicht verwendet (ID:${rdv.id})"
                                      style="font-style:italic; color:lightsteelblue;">${rdv.value}</span>
                            </g:else>

                            <g:if test="${rdv.softData}">
                                <span class="badge" title="${message(code:'default.softData.tooltip')}"> &#8623; </span>
                            </g:if>
                        </td>
                        <td>
                            ${I10nTranslation.createI10nOnTheFly(rdv, 'value').valueDe}
                        </td>
                        <td>
                            ${I10nTranslation.createI10nOnTheFly(rdv, 'value').valueEn}
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