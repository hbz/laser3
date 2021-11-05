<%@ page import="de.laser.helper.RDStore; de.laser.TitleInstancePackagePlatform; grails.converters.JSON" %>
<laser:serviceInjection/>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : Datenbereinigung: Gelöschte TIPPs</title>
    </head>

    <body>
        <semui:messages data="${flash}"/>
        <table>
            <thead>
                <tr>
                    <td colspan="5">
                        <g:link action="expungeDeletedTIPPs" params="[doIt: true]" class="ui negative button js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                </tr>
                <tr>
                    <th>TIPP ID + we:kb-ID</th>
                    <th>Titel</th>
                    <th>Status we:kb</th>
                    <th>Status LAS:eR</th>
                    <th>betroffener Lizenzbestand</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${titles}" var="entry">
                    <tr>
                        <td>${entry.tippId} + ${entry.wekbId}</td>
                        <td>${entry.name}</td>
                        <td>${entry.wekbStatus}</td>
                        <td>${entry.laserStatus}</td>
                        <td>
                            <ul>
                                <g:each in="${entry.issueEntitlements}" var="ie">
                                    <li>${ie.id} -> ${ie.subscriptionName}</li>
                                </g:each>
                            </ul>
                        </td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="5">
                        <g:link action="expungeDeletedTIPPs" params="[doIt: true]" class="ui negative button js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>
    </body>
</html>