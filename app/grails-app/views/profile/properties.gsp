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

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'menu.user.properties', default: 'Properties and Refdatas')}</h1>


<h3>${message(code: 'propertyDefinition.plural', default: 'Properties')}</h3>

<div class="ui styled fluid accordion">
    <g:each in="${propertyDefinitions}" var="entry">
        <g:if test="${entry.key != "System Config"}">
        <div class="title">
            <i class="dropdown icon"></i>
            <g:message code="propertyDefinitions.${entry.key}.label" default="${entry.key}" />
        </div>
        <div class="content">
            <table class="ui celled la-table la-table-small table">
                <thead>
                <tr>
                    <th>DE</th>
                    <th>EN</th>
                    <th>Erklärung</th>
                    <th>Explanation</th>
                    <th></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                    <g:each in="${entry.value}" var="pd">
                        <g:set var="pdI10nName" value="${I10nTranslation.createI10nOnTheFly(pd, 'name')}" />
                        <g:set var="pdI10nExpl" value="${I10nTranslation.createI10nOnTheFly(pd, 'expl')}" />
                        <tr>
                            <td>${pdI10nName.valueDe}</td>
                            <td>${pdI10nName.valueEn}</td>
                            <td>${pdI10nExpl?.valueDe}</td>
                            <td>${pdI10nExpl?.valueEn}</td>
                            <td>
                                <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                <g:if test="${pd?.type == 'class com.k_int.kbplus.RefdataValue'}">

                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(pd.refdataCategory)}" var="refdataValue">
                                        <g:set var="refdataValues" value="${refdataValues + refdataValue?.getI10n('value')}"/>
                                    </g:each>

                                    <span data-position="top right" data-tooltip="${refdataValues.join('/')}">
                                        <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                    </span>
                                </g:if>
                                <g:else>
                                    <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${usedPdList?.contains(pd.id)}">
                                    <span data-position="top right" data-tooltip="${message(code:'default.dataIsUsed.tooltip', args:[pd.id])}">
                                        <i class="info circle icon blue"></i>
                                    </span>
                                </g:if>
                                <g:if test="${pd.multipleOccurrence}">
                                    <span data-position="top right" data-tooltip="${message(code:'default.multipleOccurrence.tooltip')}">
                                        <i class="redo icon orange"></i>
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
                    </td>
                </tr>

                <g:each in="${RefdataValue.findAllByOwner(rdc).toSorted()}" var="rdv">
                    <tr>
                        <td>
                            ${I10nTranslation.createI10nOnTheFly(rdv, 'value').valueDe}
                        </td>
                        <td>
                            ${I10nTranslation.createI10nOnTheFly(rdv, 'value').valueEn}
                        </td>
                        <td>
                            <g:if test="${usedRdvList?.contains(rdv.id)}">
                                <span data-position="top right" data-tooltip="${message(code:'default.dataIsUsed.tooltip', args:[rdv.id])}">
                                    <i class="info circle icon blue"></i>
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