<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : >Datenbereinigung: Issue Entitlements ohne Subscription Package</title>
    </head>

    <body>
        <semui:messages data="${flash}"/>
        <table class="ui table celled">
            <thead>
                <tr>
                    <th>Lizenzname</th>
                    <th>Paketname</th>
                    <th>betroffene Titel</th>
                    <th>mit der Lizenz verknüpfte Pakete</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${ieList}" var="${issueEntitlement}">
                    <tr>
                        <td>${issueEntitlement.subscription} (${issueEntitlement.subscription.id})</td>
                        <td>${issueEntitlement.tipp.pkg.gokbId}</td>
                        <td>${issueEntitlement.name}</td>
                        <td>${issueEntitlement.subscription.packages.collect{ it.pkg.gokbId }}</td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="2">
                        <g:link action="createSubscriptionPackagesFromIssueEntitlements" params="${[doIt: true]}" class="ui negative button js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.createSubscriptionPackages')}"
                                data-confirm-term-how="ok">Lizenzen verknüpfen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                    <td colspan="2">
                        <g:link action="createSubscriptionPackagesFromIssueEntitlements" params="${[doIt: false]}" class="ui button">Testlauf (gefahrlos)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>
    </body>
</html>