<%@ page import="de.laser.RefdataValue" contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : Kostenposten berechnen</title>
    </head>

    <body>
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

    </body>
</html>