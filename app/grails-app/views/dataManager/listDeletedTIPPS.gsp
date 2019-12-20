<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 09.12.2019
  Time: 15:25
--%>

<%@ page import="com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.TitleInstancePackagePlatform; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<laser:serviceInjection/>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>Expunge deleted TIPPs</title>
    </head>

    <body>
        <table>
            <tbody>
                <%--<tr>
                    <th colspan="2">Duplicate TIPPs</th>
                </tr>
                <g:each in="${duplicateTIPPKeys}" var="key">
                    <tr>
                        <td colspan="2">
                            ${key}
                        </td>
                    </tr>
                </g:each>
                <tr>
                    <th colspan="2">Merge TIPP IDs</th>
                </tr>
                <g:each in="${excludes}" var="ex">
                    <tr>
                        <td colspan="2">
                            ${ex}
                        </td>
                    </tr>
                </g:each>--%>
                <tr>
                    <th>Deleted TIPPs without GOKb record</th>
                    <th>Deleted TIPPs with GOKb record</th>
                </tr>
                <tr>
                    <td>
                        <g:each in="${deletedWithoutGOKbRecord}" var="tipp">
                            <%
                                String outputWithoutGOKb
                                tipp.each { k, v ->
                                    outputWithoutGOKb = "${k.gokbId}: "
                                    if(v.issueEntitlements) {
                                        List<String> subscriptionHolders = []
                                        v.issueEntitlements.each { ie ->
                                            subscriptionHolders << g.link([controller:'subscription',action:'show',params:[id:ie.subscription.id]],"${ie.subscription.getConsortia() ?: ie.subscription.getSubscriber()} (Issue Entitlement Status: ${ie.status}, Subscription Status: ${ie.subscription.status})")
                                        }
                                        outputWithoutGOKb += "<ul><li>${subscriptionHolders.join('</li><li>')}</li></ul>"
                                    }
                                    else {
                                        outputWithoutGOKb += "no entitlements<br>"
                                    }
                                    String action = "actions to be taken: "
                                    switch(v.action) {
                                        case "remap": action += "remap to ${v.target}"
                                            break
                                        case "report": action += " ${v.report}"
                                            break
                                        case "updateStatus": action += " set status to ${v.status}"
                                            break
                                        default: action += v.action
                                            break
                                    }
                                    outputWithoutGOKb += action
                                }
                            %>
                            ${raw(outputWithoutGOKb)}
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${deletedWithGOKbRecord}" var="tipp">
                            <%
                                String outputWithGOKb
                                tipp.each { k, v ->
                                    outputWithGOKb = "${k.gokbId}: "
                                    if(v.issueEntitlements) {
                                        List<String> subscriptionHolders = []
                                        v.issueEntitlements.each { ie ->
                                            subscriptionHolders << g.link([controller: 'subscription',action:'show',params:[id:ie.subscription.id]],"${ie.subscription.getConsortia() ?: ie.subscription.getSubscriber()} (Issue Entitlement Status: ${ie.status}, Subscription Status: ${ie.subscription.status})")
                                        }
                                        outputWithGOKb += "<ul><li>${subscriptionHolders.join('</li><li>')}</li></ul>"
                                    }
                                    else {
                                        outputWithGOKb += "no entitlements<br>"
                                    }
                                    String action = "actions to be taken: "
                                    switch(v.action) {
                                        case "remap": action += "remap to ${v.target}"
                                            break
                                        case "report": action += " ${v.report}"
                                            break
                                        case "updateStatus": action += " set status to ${v.status}"
                                            break
                                        default: action += v.action
                                            break
                                    }
                                    outputWithGOKb += action
                                }
                            %>
                            ${raw(outputWithGOKb)}
                        </g:each>
                    </td>
                </tr>
            </tbody>
        </table>
        <g:form name="triggerCleanup" controller="dataManager" action="executeTIPPCleanup" params="[format:'csv']">
            <input type="submit" class="ui negative button <%--js-open-confirm-modal--%>" value="Daten bereinigen (bitte mit EXTREMER VORSICHT betätigen!!!)" <%--data-confirm-tokenMsg = "${message(code: 'confirmation.content.deleteTIPPsWithoutGOKBId')}" data-confirm-term-how="ok"--%>>
        </g:form>
    </body>
</html>