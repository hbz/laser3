<%@page import="de.laser.Subscription; de.laser.License; de.laser.finance.CostItem; de.laser.PendingChange" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'myinst.menu.acceptedChanges.label')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="myinst.acceptedChanges.label" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            ${message(code:'myinst.menu.acceptedChanges.label')}
        </h1>

        <%--<g:if test="${changes != null}" >
          <semui:paginate  action="todo" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${num_todos}" />
        </g:if>
        <semui:msg class="info" header="${message(code: 'message.information')}" message="profile.dashboardItemsTimeWindow" args="${itemsTimeWindow}"/>--%>

            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th></th>
                    <th></th>
                    <th></th>
                </tr>
                </thead>
                <g:each in="${changes}" var="changeSet">
                    <g:set var="change" value="${changeSet[0]}" />
                    <tr>
                        <td>
                            <a class="ui green circular label">${changeSet[1]}</a>
                        </td>
                        <td>
                            <strong>
                                <g:if test="${change instanceof Subscription}">
                                    <strong>${message(code:'subscription')}</strong>
                                    <br />
                                    <g:link controller="subscription" action="changes" id="${change.id}"> ${change.toString()}</g:link>
                                </g:if>
                                <g:if test="${change instanceof License}">
                                    <strong>${message(code:'license.label')}</strong>
                                    <br />
                                    <g:link controller="license" action="changes" id="${change.id}">${change.toString()}</g:link>
                                </g:if>
                                <g:if test="${change instanceof PendingChange && change.costItem}">
                                    <strong>${message(code:'financials.costItem')}</strong>
                                    <br />
                                    ${raw(change.desc)}
                                    <g:link class="ui green button" controller="finance" action="acknowledgeChange" id="${change.id}"><g:message code="pendingChange.acknowledge"/></g:link>
                                </g:if>
                            </strong>
                        </td>
                        <td>

                        </td>
                    </tr>
                  </g:each>
            </table>

        <%--<g:if test="${changes != null}" >
          <semui:paginate action="change" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${num_todos}" />
        </g:if>--%>

  </body>
</html>
