<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 09.12.2019
  Time: 15:25
--%>

<%@ page import="com.k_int.kbplus.IssueEntitlement; com.k_int.kbplus.TitleInstancePackagePlatform" contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <title>Expunge deleted TIPPs</title>
        <laser:serviceInjection/>
    </head>

    <body>
        <table>
            <%-- continue here: put up map of IEs and packages pending on it --%>
            <tr>
                <th>Deleted TIPPs without GOKb record</th>
            </tr>
            <g:each in="${deletedWithoutGOKbRecord}" var="tipp">
                <tr>
                    <td>
                        <%
                            String outputWithoutGOKb
                            tipp.entrySet().each { TitleInstancePackagePlatform k,List<IssueEntitlement> v ->
                                outputWithoutGOKb = "${k.gokbId}: "
                                if(v) {
                                    List<String> subscriptionHolders
                                    v.each { IssueEntitlement ie -> //does not work, continue here
                                        subscriptionHolders << ie.subscription.dropdownNamingConvention(contextService.org)
                                    }
                                    outputWithoutGOKb += "<ul><li>${subscriptionHolders.join('</li><li>')}</li></ul>"
                                }
                                else {
                                    outputWithoutGOKb += "no entitlements"
                                }
                            }
                        %>
                        ${outputWithoutGOKb}
                    </td>
                </tr>
            </g:each>
            <tr>
                <th>Deleted TIPPs with GOKb record</th>
            </tr>
            <g:each in="${deletedWithGOKbRecord}" var="tipp">
                <tr>
                    <td>
                        <%
                            String outputWithGOKb
                            tipp.entrySet().each { TitleInstancePackagePlatform k,List<IssueEntitlement> v ->
                                outputWithGOKb = "${k.gokbId}: "
                                if(v) {
                                    List<String> subscriptionHolders
                                    v.each { IssueEntitlement ie ->
                                        subscriptionHolders << ie.subscription.dropdownNamingConvention(contextService.org)
                                    }
                                    outputWithGOKb += "<ul><li>${subscriptionHolders.join('</li><li>')}</li></ul>"
                                }
                                else {
                                    outputWithGOKb += "no entitlements"
                                }
                            }
                        %>
                        ${outputWithGOKb}
                    </td>
                </tr>
            </g:each>
        </table>
    </body>
</html>