<%@page import="com.k_int.kbplus.Subscription;com.k_int.kbplus.License;com.k_int.kbplus.CostItem;com.k_int.kbplus.PendingChange" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'myinst.todo.label', default:'ToDo List')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <%--<semui:crumb text="(${num_todos} ${message(code:'myinst.todo.items', default:'Items')})" message="myinst.todo.list" class="active" />--%>
            <semui:crumb message="myinst.todo.label" class="active" />
        </semui:breadcrumbs>

        <h1 class="ui left aligned icon header"><semui:headerIcon />
            ${message(code:'myinst.todo.label', default:'ToDo List')}
            <%--${message(code:'myinst.todo.pagination', args:[(params.offset?:1), (java.lang.Math.min(num_todos,(params.int('offset')?:0)+10)), num_todos])}--%>
        </h1>

        <%--<g:if test="${changes != null}" >
          <semui:paginate  action="todo" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_todos}" />
        </g:if>--%>

        <g:message code="profile.dashboardItemsTimeWindow"
                   default="You see events from the last {0} days."
                   args="${itemsTimeWindow}" />

        <br />

            <table class="ui celled la-table table">
                <thead>
                    <th></th>
                    <th></th>
                    <th></th>
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
                                    <br>
                                    <g:link controller="subscription" action="changes" id="${change.id}"> ${change.toString()}</g:link>
                                </g:if>
                                <g:if test="${change instanceof License}">
                                    <strong>${message(code:'license')}</strong>
                                    <br>
                                    <g:link controller="license" action="changes" id="${change.id}">${change.toString()}</g:link>
                                </g:if>
                                <g:if test="${change instanceof PendingChange && change.costItem}">
                                    <strong>${message(code:'financials.costItem')}</strong>
                                    <br>
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
          <semui:paginate action="change" controller="myInstitution" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_todos}" />
        </g:if>--%>

  </body>
</html>
