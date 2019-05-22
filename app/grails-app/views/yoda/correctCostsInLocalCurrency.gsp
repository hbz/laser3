<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 02.05.2019
  Time: 08:45
--%>

<%@ page import="com.k_int.kbplus.RefdataValue" contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <title>Kostenposten berechnen</title>
    </head>

    <body>
        <div>
            <table class="ui celled table" id="calcResults">
                <g:each in="${costItems.entrySet()}" var="entry">
                    <g:set var="ci" value="${entry.getKey()}"/>
                    <%
                        String matching = "positive"
                        if(ci.billingCurrency != RefdataValue.getByValueAndCategory('EUR','Currency'))
                            matching = "negative"
                    %>
                    <tr class="${matching}">
                        <td>${ci.sub?.dropdownNamingConvention()} / ${ci.owner.name}</td>
                        <td>${ci.costInBillingCurrency} ${ci.billingCurrency} * ${ci.currencyRate} = </td>
                        <td>${entry.getValue()} EUR</td>
                    </tr>
                </g:each>
            </table>
        </div>
    </body>
</html>