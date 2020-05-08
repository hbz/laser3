<%--
  Created by IntelliJ IDEA.
  User: agalffy
  Date: 07.05.2020
  Time: 11:48
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title><g:message code="laser"/> : Check License - Subscription - Org links</title>
    </head>

    <body>
        <h1>License-Subscription-Org link checkup result</h1>
        <table class="ui table table-striped">
            <thead>
                <tr>
                    <th colspan="2"><g:link class="ui negative button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialogtriggerCleanup")}"
                                            data-confirm-term-how="clearUp"
                                            data-confirm-id="clearUp" name="triggerUpdate" action="synchronizeSubscriptionLicenseOrgLinks">Synchronisation auslösen</g:link></th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <th colspan="2">Subscribers without licensee org roles</th>
                </tr>
                <g:each in="${subscribersWithoutLicenseeRole}" var="entry">
                    <tr>
                        <td>${entry.getKey()}</td>
                        <td>${entry.getValue()}</td>
                    </tr>
                </g:each>
                <tr>
                    <th colspan="2">Licensees without subscription ownership for license</th>
                </tr>
                <g:each in="${licenseesWithoutSubscriptionOwnership}" var="entry">
                    <tr>
                        <td>${entry.getKey()}</td>
                        <td>${entry.getValue()}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </body>
</html>