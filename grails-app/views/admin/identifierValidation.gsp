<%@ page import="de.laser.ui.Icon; de.laser.IdentifierNamespace" %>
<laser:htmlStart message="menu.admin.identifierValidation" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.identifierValidation" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.identifierValidation" type="admin"/>

<div class="ui fluid card">
    <div class="content">

        <table class="ui la-table compact table">
            <thead>
            <tr>
                <th><g:message code="identifierNamespace.ns.label"/></th>
                <th><g:message code="default.name.label"/> (${currentLang})</th>
                <th><g:message code="default.type.label"/></th>
                <th><g:message code="identifierNamespace.validationRegex.label"/></th>
                <th><g:message code="default.count.label"/></th>
                <th>Gültig / Ungültig</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${nsList}" var="ns">
                    <g:set var="isCore" value="${ns.ns in IdentifierNamespace.CORE_ORG_NS || ns.ns in IdentifierNamespace.CORE_PROVIDER_NS || ns.ns in IdentifierNamespace.CORE_TITLE_NS}" />
                    <g:set var="isInvalid" value="${iMap[ns.id].invalid.size() > 0}" />

                    <tr>
                        <td>
                            <g:if test="${isCore}">
                                ${ns.ns}
                                <strong data-position="top left" class="la-popup-tooltip" data-content="Core Namespace">
                                    <i class="${Icon.TOOLTIP.IMPORTANT} orange" aria-hidden="true"></i>
                                </strong>
                            </g:if>
                            <g:else>
                                ${ns.ns}
                            </g:else>
                        </td>
                        <td>${ns."name_${currentLang}"}</td>
                        <td>${ns.nsType}</td>
                        <td>${ns.validationRegex}</td>
                        <td>${iMap[ns.id].count}</td>

                        <td class="${isInvalid ? 'error' : 'positive'} center aligned">
                            <strong>${iMap[ns.id].valid.size()} / ${iMap[ns.id].invalid.size()}</strong>
                        </td>
                        <td>${iMap[ns.id].invalid.collect{ '[' + it.id + ':'+ it.value + ']'}.join(', ')}</td>
                    </tr>

                </g:each>
            </tbody>
        </table>

    </div>
</div>

<laser:htmlEnd />
