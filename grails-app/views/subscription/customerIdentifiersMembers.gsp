<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 25.08.2021
  Time: 15:58
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code: 'laser')} : ${message(code: 'org.customerIdentifier.plural')}</title>
    </head>

    <body>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="currentSubscriptions"
                         text="${message(code: 'myinst.currentSubscriptions.label')}"/>
            <semui:crumb controller="subscription" action="show" id="${subscription.id}"
                         text="${subscription.name}"/>

            <semui:crumb class="active"
                         text="${message(code: 'subscription.details.subscriberManagement.label', args: args.memberType)}"/>

        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${subscription.name}</h1>

        <semui:anualRings object="${subscription}" controller="subscription" action="${actionName}"
                          navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <g:render template="navSubscriberManagement"/>

        <semui:messages data="${flash}"/>

        <h2 class="ui header">
            <g:message code="subscription"/>:
            <g:link controller="subscription" action="show" id="${subscription.id}">${subscription.name}</g:link>
        </h2>
        <table class="ui la-table table">
            <thead>
                <tr>
                    <th class="three wide">${message(code:'consortium.member')}</th>
                    <th class="four wide">${message(code:'default.provider.label')} : ${message(code:'platform.label')}</th>
                    <th class="three wide">${message(code:'org.customerIdentifier')}</th>
                    <th class="three wide">${message(code:'org.requestorKey')}</th>
                    <th class="two wide">${message(code:'default.note.label')}</th>
                    <th class="one wide">${message(code:'default.actions')}</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${keyPairs}" var="pair" status="rowno">
                    <tr>
                        <td>${pair.customer.sortname ?: pair.customer.name}</td>
                        <td>
                            ${pair.getProvider()} : ${pair.platform.name}
                        </td>
                        <td><semui:xEditable owner="${pair}" field="value"/></td>
                        <td><semui:xEditable owner="${pair}" field="requestorKey"/></td>
                        <td><semui:xEditable owner="${pair}" field="note"/></td>
                        <td>
                            <g:link controller="subscription"
                                    action="deleteCustomerIdentifier"
                                    id="${subscription.id}"
                                    params="${[deleteCI:pair.id]}"
                                    class="ui button icon red js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.customeridentifier", args: [""+pair.getProvider()+" : "+pair.platform+" "+pair.value])}"
                                    data-confirm-term-how="delete"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </body>
</html>