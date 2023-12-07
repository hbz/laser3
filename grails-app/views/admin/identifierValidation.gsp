<laser:htmlStart message="menu.admin.identifierValidation" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.identifierValidation" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.identifierValidation" type="admin"/>

<div class="ui grid">
    <div class="sixteen wide column">

        <table class="ui sortable celled la-table compact table">
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
                    <tr>
                        <td>${ns.ns}</td>
                        <td>${ns."name_${currentLang}"}</td>
                        <td>${ns.nsType}</td>
                        <td>${ns.validationRegex}</td>

                        <g:if test="${iMap.containsKey(ns.id)}">
                            <td>${iMap[ns.id].count}</td>
                            <td>${iMap[ns.id].valid.size()} / ${iMap[ns.id].invalid.size()}</td>
                            <td>${iMap[ns.id].invalid.collect{ '[' + it.id + ':'+ it.value + ']'}.join(', ')}</td>
                        </g:if>
                        <g:else>
                            <td></td>
                            <td></td>
                            <td></td>
                        </g:else>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div>
</div>

<laser:htmlEnd />
