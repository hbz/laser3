<%@ page import="de.laser.License;de.laser.RefdataCategory;de.laser.interfaces.CalculatedType;de.laser.helper.RDStore;de.laser.helper.RDConstants;de.laser.RefdataValue;de.laser.Links" %>
<laser:serviceInjection />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>${message(code:'laser')} : ${message(code:'license.current')}</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
        body {
            font-size: 16px;
            font-family: sans-serif;
        }
        h1 > span {
            font-size: 80%;
            color: rgba(0,0,0, 0.35);
        }
        table {
            margin-top: 3em;
            border-spacing: 0;
            border-collapse: collapse;
            border-width: 0;
        }
        table thead tr {
            text-align: left;
            color: #FFFFFF;
            background-color: #2471a3;
        }
        table thead tr th {
            padding: 0.6em 0.6em;
            border-color: #2471a3;
            border-bottom: 0.5em solid #FFFFFF;
        }
        table tbody tr.even {
            background-color: #F6F7F7;
        }
        table tbody tr td {
            padding: 0.35em 0.6em;
        }
        </style>
    </head>
    <body>
        <article>
            <h1>
                ${message(code:'license.current')} (${licenseCount})
            </h1>
            <g:if test="${licenses}">
                <table>
                    <thead>
                        <tr>
                            <th><g:message code="sidewide.number"/></th>
                            <th><g:message code="license.slash.name"/></th>
                            <g:if test="${'memberLicenses' in licenseFilterTable}">
                                <th><g:message code="license.details.incoming.childs"/></th>
                            </g:if>
                            <th><g:message code="license.licensor.label"/></th>
                            <g:if test="${'licensingConsortium' in licenseFilterTable}">
                                <th><g:message code="consortium"/></th>
                            </g:if>
                            <th><g:message code="license.start_date"/></th>
                            <th><g:message code="license.end_date"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${licenses}" var="l" status="jj">
                            <tr <% if((jj+1) % 2 == 0) { print 'class="even"' } else { print 'class="odd"' } %>>
                                <td>${ (params.int('offset') ?: 0)  + jj + 1 }</td>
                                <td>
                                    <g:link action="show" absolute="true" controller="license" id="${l.id}">
                                        ${l.reference ?: message(code:'missingLicenseReference')}
                                    </g:link>
                                    <ul>
                                        <g:each in="${allLinkedSubscriptions.get(l)}" var="sub">
                                            <li><g:link controller="subscription" action="show" id="${sub.id}" absolute="true">${sub.name}</g:link></li>
                                        </g:each>
                                    </ul>
                                </td>
                                <g:if test="${'memberLicenses' in licenseFilterTable}">
                                    <td>
                                        <ul>
                                            <g:each in="${l.derivedLicenses}" var="lChild">
                                                <li><g:link controller="license" action="show" id="${lChild.id}" absolute="true">${lChild}</g:link></li>
                                            </g:each>
                                        </ul>
                                    </td>
                                </g:if>
                                <td>
                                    <g:set var="licensor" value="${l.getLicensor()}"/>
                                    <g:if test="${licensor}">
                                        <g:link controller="organisation" action="show" id="${licensor.id}" absolute="true">${licensor.name}</g:link>
                                    </g:if>
                                </td>
                                <g:if test="${'licensingConsortium' in licenseFilterTable}">
                                    <td>${l.getLicensingConsortium()?.name}</td>
                                </g:if>
                                <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.startDate}"/></td>
                                <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${l.endDate}"/></td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <strong><g:message code="filter.result.empty.object" args="${[message(code:"license.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <strong><g:message code="result.empty.object" args="${[message(code:"license.plural")]}"/></strong>
                </g:else>
            </g:else>
        </article>
    </body>
</html>