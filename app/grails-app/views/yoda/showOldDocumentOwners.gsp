<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 26.03.2019
  Time: 08:39
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<laser:serviceInjection/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : Documents without owners and share configurations</title>
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
            <semui:crumb text="Documents without owners and share configurations" class="active"/>
        </semui:breadcrumbs>
        <% Set addedUsers = [] %>
        <div>
            <table class="ui celled la-table table">
                <thead>
                    <tr>
                        <td colspan="3">
                            <g:link controller="yoda" action="updateShareConfigurations" class="ui secondary button">Trigger update (handle with extreme care!)</g:link>
                        </td>
                    </tr>
                    <tr>
                        <th>Document title</th>
                        <th>Uploader</th>
                        <th>User affiliations</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${currentDocuments}" var="docctx">
                        <g:if test="${addedUsers.add(docctx.owner.creator)}">
                            <tr>
                                <td>${docctx.owner.title}</td>
                                <td>${docctx.owner.creator}</td>
                                <td>
                                    <ol>
                                        <g:each in="${docctx.owner.creator.authorizedAffiliations}" var="affiliation">
                                            <li>${affiliation.getSortString()}</li>
                                        </g:each>
                                    </ol>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                </tbody>
            </table>
        </div>
    </body>
</html>