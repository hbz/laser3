<%@ page import="de.laser.RefdataValue" %>

<laser:htmlStart text="Kosten berechnen" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="Kosten berechnen" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="Kosten berechnen" type="yoda" />

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

<laser:htmlEnd />