<%@page import="com.k_int.kbplus.License"%>
<laser:serviceInjection/>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser')} : ${message(code:'menu.my.comp_lic')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.getDesignation()}" />
            <semui:crumb class="active" message="menu.my.comp_lic" />
            <%--<li class="dropdown la-float-right">
                <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">Exports<strong class="caret"></strong></a>&nbsp;
                <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
                    <li>
                        <g:link action="compare" params="${params+[format:'csv']}">CSV Export</g:link>
                    </li>
                </ul>
            </li>--%>
        </semui:breadcrumbs>
        <br>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.my.comp_lic')}</h1>

        <g:render template="selectionForm" model="${[selectedLicenses:licenses]}" />

        <br>

        <div class="ui grid">

                    <g:each in="${groupedProperties}" var="groupedProps">
                        <%-- leave it for debugging
                        <tr>
                            <td colspan="999">${groupedProps}</td>
                        </tr>--%>
                        <g:if test="${groupedProps.getValue()}">
                            <g:render template="comparisonTable" model="[group:groupedProps.getValue().groupTree,key:groupedProps.getKey().name,propBinding:groupedProps.getValue().binding,licenses:licenses]" />
                        </g:if>
                    </g:each>
                    <g:if test="${orphanedProperties.size() > 0}">
                        <g:render template="comparisonTable" model="[group:orphanedProperties,key:message(code:'license.properties'),licenses:licenses]" />
                    </g:if>
                    <g:if test="${privateProperties.size() > 0}">
                        <g:render template="comparisonTable" model="[group:privateProperties,key:message(code:'license.properties.private')+' '+contextService.getOrg().name,licenses:licenses]" />
                    </g:if>

        </div>
    <br>
    </body>
</html>
