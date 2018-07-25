<%@ page import="com.k_int.kbplus.OrgRole; com.k_int.kbplus.RefdataValue" %>
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'gasco.title')}</title>
</head>

<body>

    <br />
    <br />

    <h2 class="ui title">${subscription}</h2>

    <table class="ui celled la-table table">
        <thead>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'issueEntitlement.label')}</th>
            <th>${message(code:'default.identifiers.label')}</th>
        </thead>
        <tbody>

            <g:each in="${pakkage.tipps}" var="tipp" status="counter">
                <tr>
                    <td>${counter + 1}</td>
                    <td>
                        <semui:listIcon type="${tipp.title.type.getI10n('value')}"/>
                        <strong>${tipp.title.title}</strong>
                        <br />

                        <g:if test="${tipp.hostPlatformURL}">
                            <a href="${tipp.hostPlatformURL}" title="${tipp.hostPlatformURL}" target="_blank">
                                ${tipp.hostPlatformURL}
                                <i class="ui icon share square"></i>
                            </a>
                        </g:if>
                        <br />

                        ${message(code:'tipp.platform', default:'Platform')}:
                        <g:if test="${tipp.platform.name}">
                            ${tipp.platform.name}
                        </g:if>
                        <g:else>
                            ${message(code:'default.unknown')}
                        </g:else>
                        <br />

                        ${message(code:'tipp.package', default:'Package')}:
                        <g:if test="${tipp.pkg}"><!-- TODO: show all packages -->
                            ${tipp.pkg}
                        </g:if>
                        <g:else>
                            ${message(code:'default.unknown')}
                        </g:else>
                        <br />
                    </td>

                    <td>
                        <g:each in="${tipp.title?.ids.sort{it.identifier.ns.ns}}" var="title_id">
                            <g:if test="${title_id.identifier.ns.ns.toLowerCase() != 'originediturl'}">
                                ${title_id.identifier.ns.ns}: <strong>${title_id.identifier.value}</strong>
                                <br />
                            </g:if>
                        </g:each>
                    </td>
                </tr>
            </g:each>

        </tbody>
    </table>

<%--
<table class="ui celled la-table table">
    <thead>
    <th>${message(code:'sidewide.number')}</th>
    <th>${message(code:'issueEntitlement.label')}</th>
    <th>${message(code:'default.identifiers.label')}</th>
    </thead>
    <tbody>

    <g:each in="${subscription.issueEntitlements}" var="ie" status="counter">
        <tr>
            <td>${counter + 1}</td>
            <td>
                <semui:listIcon type="${ie.tipp?.title.type.getI10n('value')}"/>
                <strong>${ie.tipp.title.title}</strong>
                <br />

                <g:if test="${ie.tipp?.hostPlatformURL}">
                    <a href="${ie.tipp?.hostPlatformURL}" title="${ie.tipp?.hostPlatformURL}" target="_blank">
                        ${ie.tipp?.hostPlatformURL}
                        <i class="ui icon share square"></i>
                    </a>
                </g:if>
                <br />

                <g:if test="${ie.medium}">
                    ${ie.medium}
                    <br />
                </g:if>

                ${message(code:'tipp.platform', default:'Platform')}:
                <g:if test="${ie.tipp?.platform.name}">
                    ${ie.tipp?.platform.name}
                </g:if>
                <g:else>
                    ${message(code:'default.unknown')}
                </g:else>
                <br />

                ${message(code:'tipp.package', default:'Package')}:
                <g:if test="${ie.tipp?.pkg}">
                    ${ie.tipp?.pkg}
                </g:if>
                <g:else>
                    ${message(code:'default.unknown')}
                </g:else>
                <br />

                <g:if test="${ie.availabilityStatus?.value=='Expected'}">
                    ${message(code:'default.on', default:'on')} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessStartDate}"/>
                </g:if>

                <g:if test="${ie.availabilityStatus?.value=='Expired'}">
                    ${message(code:'default.on', default:'on')} <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${ie.accessEndDate}"/>
                </g:if>

            </td>

            <td>
                <g:each in="${ie?.tipp?.title?.ids.sort{it.identifier.ns.ns}}" var="title_id">
                    <g:if test="${title_id.identifier.ns.ns.toLowerCase() != 'originediturl'}">
                        ${title_id.identifier.ns.ns}: <strong>${title_id.identifier.value}</strong>
                        <br />
                    </g:if>
                </g:each>
            </td>
        </tr>
    </g:each>

    </tbody>
</table>
--%>
</body>