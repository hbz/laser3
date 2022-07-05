<%@ page import="de.laser.RefdataCategory; de.laser.I10nTranslation; de.laser.properties.PropertyDefinition; de.laser.RefdataValue"  %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code: 'menu.user.properties')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.user.properties" class="active"/>
</semui:breadcrumbs>

<semui:h1HeaderWithIcon message="menu.user.properties" />

<h2 class="la-clear-before">${message(code: 'propertyDefinition.plural')}</h2>

<div class="ui styled fluid accordion">
    <g:each in="${propertyDefinitions}" var="entry">
        <g:if test="${entry.key != "System Config"}">
        <div class="title">
            <i class="dropdown icon"></i>
            <g:message code="propertyDefinition.${entry.key}.label" default="${entry.key}" />
        </div>
        <div class="content">
            <table class="ui celled la-js-responsive-table la-table compact table">
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
                        <tr>
                            <td>${pd.getI10n('name', 'de')}</td>
                            <td>${pd.getI10n('name', 'en')}</td>
                            <td>${pd.getI10n('expl', 'de')}</td>
                            <td>${pd.getI10n('expl', 'en')}</td>
                            <td>
                                <g:set var="pdRdc" value="${pd.type?.split('\\.').last()}"/>
                                <g:if test="${pd?.isRefdataValueType()}">

                                    <g:set var="refdataValues" value="${[]}"/>
                                    <g:each in="${RefdataCategory.getAllRefdataValues(pd.refdataCategory)}" var="refdataValue">
                                        <g:if test="${refdataValue.getI10n('value')}">
                                            <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                                        </g:if>
                                    </g:each>

                                    <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${refdataValues.join('/')}">
                                        <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                    </span>
                                </g:if>
                                <g:else>
                                    <small>${PropertyDefinition.getLocalizedValue(pd.type)}</small>
                                </g:else>
                            </td>
                            <td>
                                <g:if test="${usedPdList?.contains(pd.id)}">
                                    <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'default.dataIsUsed.tooltip', args:[pd.id])}">
                                        <i class="info circle icon blue"></i>
                                    </span>
                                </g:if>
                                <g:if test="${pd.multipleOccurrence}">
                                    <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'default.multipleOccurrence.tooltip')}">
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

<h3 class="ui header">${message(code: 'refdata.plural')}</h3>

<div class="ui styled fluid accordion">
    <g:each in="${rdCategories}" var="rdc">

        <div class="title">
            <i class="dropdown icon"></i>
            ${rdc.getI10n('desc')}
        </div>
        <div class="content">

            <table class="ui celled la-js-responsive-table la-table compact table">
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
                        <strong>${rdc.getI10n('desc', 'de')}</strong>
                    </td>
                    <td>
                        <strong>${rdc.getI10n('desc', 'en')}</strong>
                    </td>
                    <td>
                    </td>
                </tr>

                <g:each in="${RefdataCategory.getAllRefdataValues(rdc.desc)}" var="rdv">
                    <tr>
                        <td>
                            ${rdv.getI10n('value', 'de')}
                        </td>
                        <td>
                            ${rdv.getI10n('value', 'en')}
                        </td>
                        <td>
                            <g:if test="${usedRdvList?.contains(rdv.id)}">
                                <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'default.dataIsUsed.tooltip', args:[rdv.id])}">
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