<%@ page import="de.laser.IssueEntitlement; de.laser.TitleInstancePackagePlatform; grails.converters.JSON" contentType="text/html;charset=UTF-8" %>
<laser:serviceInjection/>
<html>
    <head>
        <meta name="layout" content="laser">
        <title><g:message code="laser" /> : Expunge deleted TIPPs</title>
    </head>

    <body>
        <table class="ui table">
            <tbody>
                <tr>
                    <th>Deleted TIPPs without we:kb record</th>
                    <th>Deleted TIPPs with we:kb record</th>
                </tr>
                <tr>
                    <td>
                        <g:each in="${deletedWithoutGOKbRecord}" var="tipp">
                            <%
                                String outputWithoutGOKb
                                tipp.each { k, v ->
                                    outputWithoutGOKb = "${k.gokbId}: "
                                    if(v) {
                                        List<String> subscriptionHolders = []
                                        v.each { entry ->
                                            //outputWithoutGOKb += entry.ie
                                            String action = "actions to be taken: "
                                            switch(entry.action) {
                                                case "remap": action += "remap to ${entry.target}"
                                                    break
                                                case "report": action += " ${entry.report}"
                                                    break
                                                case "updateStatus": action += " set status to ${entry.status}"
                                                    break
                                                default: action += entry.action
                                                    break
                                            }
                                            subscriptionHolders << "${g.link([controller:'subscription',action:'show',params:[id:entry.ie.subscription.id]],"${entry.ie.subscription.getConsortia() ?: entry.ie.subscription.getSubscriber()} (Issue Entitlement Status: ${entry.ie.status}, Subscription Status: ${entry.ie.subscription.status})")} - ${action}"
                                        }
                                        outputWithoutGOKb += "<ul><li>${subscriptionHolders.join('</li><li>')}</li></ul>"
                                    }
                                    else {
                                        outputWithoutGOKb += "no entitlements<br />"
                                    }
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
                                    outputWithGOKb = "${k}: "
                                    if (v.issueEntitlements) {
                                        List<String> subscriptionHolders = []
                                        v.issueEntitlements.each { ie ->
                                            subscriptionHolders << g.link([controller: 'subscription', action: 'show', params: [id: ie.subscription.id]], "${ie.subscription.getConsortia() ?: ie.subscription.getSubscriber()} (Issue Entitlement Status: ${ie.status}, Subscription Status: ${ie.subscription.status})")
                                        }
                                        outputWithGOKb += "<ul><li>${subscriptionHolders.join('</li><li>')}</li></ul>"
                                    }
                                     else {
                                        outputWithGOKb += "no entitlements<br />"
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

        <g:form name="triggerCleanup" controller="dataManager" action="executeTIPPCleanup" params="[format:'csv']" data-confirm-id="clearUp_form">
            <g:hiddenField name="id" value="clearUp" />

            <div class="ui icon negative button js-open-confirm-modal"
                 data-confirm-tokenMsg="${message(code: "confirm.dialogtriggerCleanup")}"
                 data-confirm-term-how="clearUp"
                 data-confirm-id="clearUp">
                    <g:message code="admin.cleanupTIPP.submit"/>
            </div>
        </g:form>
    </body>
</html>