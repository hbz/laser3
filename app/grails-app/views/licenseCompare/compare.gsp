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
        <h2 class="ui header">${message(code:'menu.institutions.comp_lic')}</h2>
        <div class="ui grid">
            <table class="ui la-table la-table-small table">
                <thead>
                    <th>${message(code:'property.table.property')}</th>
                    <g:each in="${licenses}" var="license" status="counter">
                        <th>${license.reference}</th>
                    </g:each>
                </thead>
                <tbody>
                    <g:each in="${map}" var="entry">
                        <tr>
                            <td>${entry.getKey()}</td>
                            <g:each in="${licenses}" var="lic">
                                <g:if test="${entry.getValue().containsKey(lic.reference)}">
                                    <td>
                                        <g:set var="point" value="${entry.getValue().get(lic.reference)}"/>
                                        <g:if test="${['stringValue','intValue','decValue'].contains(point.getValueType())}">
                                            <span class="cell-inner">  <strong>${point.getValue()}</strong></span>
                                        </g:if>
                                        <g:else>
                                            <g:set var="val" value="${point.getValue()}"/>
                                            <g:if test="${val == 'Y' || val=="Yes"}">
                                                <span class="cell-inner">
                                                    <span title="${val}" class="onix-status onix-tick" />
                                                </span>
                                            </g:if>
                                            <g:elseif test="${val=='N' || val=="No"}">
                                                <span class="cell-inner">
                                                    <span title="${val}" class="onix-status onix-pl-prohibited" />
                                                </span>
                                            </g:elseif>
                                            <g:elseif test="${['O','Other','Specified'].contains(val)}">
                                                <span class="cell-inner">
                                                    <span title="${val}" class="onix-status onix-info" />
                                                </span>
                                            </g:elseif>
                                            <g:elseif test="${['U','Unknown','Not applicable','Not Specified'].contains(val)}">
                                                <span class="cell-inner-undefined">
                                                    <span title="${val}" class="onix-status onix-pl-undefined" ></span>
                                                </span>
                                            </g:elseif>
                                            <g:else>
                                                 <span class="cell-inner">  <strong>${point.getValue()}</strong></span>
                                            </g:else>
                                        </g:else>
                                    </td>
                                </g:if>
                                <g:else>
                                    <td>
                                        <span class="cell-inner-undefined">
                                            <span title='Not Set' class="onix-status onix-pl-undefined" ></span>
                                        </span>
                                    </td>
                                </g:else>
                            </g:each>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
    </body>
</html>
