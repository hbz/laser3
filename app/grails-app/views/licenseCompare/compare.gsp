<%@page import="com.k_int.kbplus.License"%>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.my.comp_lic')}</title>
    </head>
    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}" />
            <semui:crumb class="active" message="menu.my.comp_lic" />
            <%--<li class="dropdown pull-right">
                <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">Exports<strong class="caret"></strong></a>&nbsp;
                <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
                    <li>
                        <g:link action="compare" params="${params+[format:'csv']}">CSV Export</g:link>
                    </li>
                </ul>
            </li>--%>
        </semui:breadcrumbs>
        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.my.comp_lic')}</h1>
        <g:render template="selectionForm" model="${[selectedLicenses:licenses]}" />
        <div class="ui grid">
            <table class="ui la-table la-table-small table">
                <g:set var="licenseCount" value="${licenses.size()}"/>
                <thead>
                    <th colspan="${licenseCount}">${message(code:'property.table.property')}</th>
                </thead>
                <tbody>
                    <g:each in="${groupedProperties}" var="groupedProps">
                        <%-- leave it for debugging
                        <tr>
                            <td colspan="999">${groupedProps}</td>
                        </tr>--%>
                        <g:if test="${groupedProps.getValue()}">
                            <g:render template="comparisonTableRow" model="[group:groupedProps.getValue().groupTree,key:groupedProps.getKey().name,propBinding:groupedProps.getValue().binding,licenses:licenses]" />
                        </g:if>
                    </g:each>
                    <g:if test="${orphanedProperties.size() > 0}">
                        <g:render template="comparisonTableRow" model="[group:orphanedProperties,key:message(code:'license.properties'),licenses:licenses]" />
                    </g:if>
                    <g:if test="${privateProperties.size() > 0}">
                        <g:render template="comparisonTableRow" model="[group:privateProperties,key:message(code:'license.properties.private')+' '+contextService.getOrg().name,licenses:licenses]" />
                    </g:if>
                </tbody>
            </table>
        </div>
    </body>
</html>
