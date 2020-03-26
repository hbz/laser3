<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 03.07.2019
  Time: 15:33
--%>
<laser:serviceInjection/>
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.TitleInstancePackagePlatform; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<g:set var="contextOrg" value="${contextService.getOrg()}"/>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <title>Datenbereinigung: TIPPs ohne GOKb ID</title>
    </head>

    <body>
        <semui:messages data="${flash}"/>
        <table>
            <thead>
                <tr>
                    <th>TIPP ID</th>
                    <th>Titel ID</th>
                    <th>Titel</th>
                    <th>alternatives TIPP mit GOKb ID</th>
                    <th>betroffener Lizenzbestand</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${tipps}" var="entry">
                    <tr>
                        <td>${entry.tipp.id}</td>
                        <td>${entry.tipp.title.id}</td>
                        <td>${entry.tipp.title.title}</td>
                        <td>
                            <g:if test="${entry.tipp.altTIPP}">
                                alternatives TIPP im gleichen Paket: ${entry.altTIPP.id}, verfügt über GOKb ID ${entry.altTIPP.gokbId}
                            </g:if>
                            <g:else>
                                kein alternatives TIPP vorhanden!
                            </g:else>
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
                        <g:link action="purgeTIPPsWithoutGOKBId" params="${[doIt: true, toDelete: toDelete as JSON, toUUIDfy: toUUIDfy as JSON]}" class="ui negative button js-open-confirm-modal" data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                    <td>
                        <g:link action="purgeTIPPsWithoutGOKBId" params="${[doIt: false, toDelete: toDelete as JSON, toUUIDfy: toUUIDfy as JSON]}" class="ui button">Testlauf (gefahrlos)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>
    </body>
</html>