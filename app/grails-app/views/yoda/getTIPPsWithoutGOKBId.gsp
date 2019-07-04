<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 03.07.2019
  Time: 15:33
--%>
<laser:serviceInjection/>
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.TitleInstancePackagePlatform; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<g:set var="contextOrg" value="${contextService.getOrg()}"/>
<g:set var="toDelete" value="${[:]}"/>
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
                <g:each in="${tipps}" var="tipp">
                    <%
                        boolean hasAltTIPP = false
                        TitleInstancePackagePlatform altTIPP = TitleInstancePackagePlatform.executeQuery("select tipp from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg and tipp.title = :title and tipp.status = :current and tipp.gokbId != null",[pkg:tipp.pkg,title:tipp.title,current:RDStore.TIPP_STATUS_CURRENT])[0]
                        if(altTIPP) {
                            hasAltTIPP = true
                            toDelete[tipp.id] = altTIPP.id
                        }
                    %>
                    <tr>
                        <td>${tipp.id}</td>
                        <td>${tipp.title.id}</td>
                        <td>${tipp.title.title}</td>
                        <td>
                            <g:if test="${hasAltTIPP}">
                                alternatives TIPP im gleichen Paket: ${altTIPP.id}, verfügt über GOKb ID ${altTIPP.gokbId}
                            </g:if>
                            <g:else>
                                kein alternatives TIPP vorhanden!
                            </g:else>
                        </td>
                        <td>
                            <ul>
                                <g:each in="${issueEntitlements.get(tipp)}" var="ie">
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
                        <g:link action="purgeTIPPsWithoutGOKBId" params="${[doIt: true, toDelete: toDelete as JSON]}" class="ui negative button js-open-confirm-modal" data-confirm-term-content = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}"
                                data-confirm-term-how="ok">Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)</g:link>
                    </td>
                    <td>
                        <g:link action="purgeTIPPsWithoutGOKBId" params="${[doIt: false, toDelete: toDelete as JSON]}" class="ui button">Testlauf (gefahrlos)</g:link>
                    </td>
                </tr>
            </tfoot>
        </table>
    </body>
</html>