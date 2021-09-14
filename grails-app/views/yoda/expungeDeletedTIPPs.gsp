<%@ page import="de.laser.helper.RDStore; de.laser.TitleInstancePackagePlatform; grails.converters.JSON" %>
<laser:serviceInjection/>
<g:set var="contextOrg" value="${contextService.getOrg()}"/>
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
                    <th>TIPP ID + we:kb-ID</th>
                    <th>Titel</th>
                    <th>Status we:kb</th>
                    <th>Status LAS:eR</th>
                    <th>betroffener Lizenzbestand</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${tipps}" var="entry">
                    <tr>
                        <td>${entry.tipp.id}</td>
                        <td>${entry.name}</td>
                        <td>

                        </td>
                        <td>

                        </td>
                        <td>
                            <ul>
                                <g:each in="${issueEntitlements.get(entry.tipp)}" var="ie">
                                    <li>${ie.id} -> ${ie.subscription.dropdownNamingConvention(contextOrg)}</li>
                                </g:each>
                            </ul>
                        </td>
                    </tr>
                </g:each>
            </tbody>
            <tfoot>
                <tr>
                    <td>
                        <g:link action="purgeTIPPsWithoutGOKBId" params="[doIt: true]" class="ui negative button js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>
    </body>
</html>