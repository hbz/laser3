<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 02.05.2019
  Time: 08:45
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <title>Kostenposten berechnen</title>
    </head>

    <body>
        <div>
            <table class="ui celled la-table la-table-small table" id="calcResults">
                <g:each in="${costItems.entrySet()}" var="ci">
                    <tr>
                        <td>${ci.getKey().sub?.dropdownNamingConvention()} / ${ci.getKey().owner.name}</td>
                        <td>${ci.getKey().costInBillingCurrency} * ${ci.getKey().currencyRate} = </td>
                        <td>${ci.getValue()}</td>
                    </tr>
                </g:each>
            </table>
        </div>
    </body>
</html>