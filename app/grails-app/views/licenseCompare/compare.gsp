<%@page import="com.k_int.kbplus.License"%>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.comp_lic')}</title>
    </head>
    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}" />
            <semui:crumb class="active" message="menu.institutions.comp_lic" />
            <%--<li class="dropdown pull-right">
                <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">Exports<strong class="caret"></strong></a>&nbsp;
                <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
                    <li>
                        <g:link action="compare" params="${params+[format:'csv']}">CSV Export</g:link>
                    </li>
                </ul>
            </li>--%>
        </semui:breadcrumbs>
        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.institutions.comp_lic')}</h1>
        <g:render template="selectionForm" model="${[selectedLicenses:licenses]}" />
        <div class="ui grid">
            <table class="ui la-table la-table-small table">
                <g:set var="licenseCount" value="${licenses.size()}"/>
                <thead>
                    <th>${message(code:'property.table.property')}</th>
                    <g:each in="${licenses}" var="license">
                        <th>${license.reference}</th>
                    </g:each>
                </thead>
                <tbody>
                    <g:each in="${groupedProperties}" var="groupedProps">
                        <%-- leave it for debugging
                        <tr>
                            <td colspan="999">${groupedProps}</td>
                        </tr>--%>
                        <tr>
                            <th colspan="${licenseCount}">${groupedProps.getKey().name}</th>
                        </tr>
                        <g:render template="comparisonTableRow" model="[group:groupedProps.getValue(),licenses:licenses]" />
                    </g:each>
                    <tr>
                        <th colspan="${licenses.size()+1}" scope="colgroup">${message(code:'license.properties')}</th>
                    </tr>
                    <g:render template="comparisonTableRow" model="[group:orphanedProperties,licenses:licenses]" />
                </tbody>
            </table>
        </div>
    </body>
</html>
