<div class="ui grid">

    <div class="sixteen wide column">

        <table class="ui celled striped table">
            <thead>
            <tr>
                <th>${message(code: 'task.title.label', default: 'Title')}</th>

                <th>${message(code: 'task.endDate.label', default: 'End Date')}</th>

                <th>
                    ${message(code: 'task.tenantOrg.label', default: 'tenantOrg')} <br />
                    ${message(code: 'task.tenantUser.label', default: 'tenantUser')}
                </th>

                <th>${message(code: 'task.status.label', default: 'status')}</th>

                <th>${message(code: 'task.owner.label', default: 'owner')}</th>

                <th>${message(code: 'task.createDate.label', default: 'Create Date')}</th>

                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${taskInstanceList}" var="taskInstance">
                <tr>
                    <td>${fieldValue(bean: taskInstance, field: "title")}</td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${taskInstance?.endDate}"/></td>

                    <td>
                        <g:if test="${taskInstance.tenantOrg}">${fieldValue(bean: taskInstance, field: "tenantOrg")} <br /></g:if>
                        <g:if test="${taskInstance.tenantUser}">${fieldValue(bean: taskInstance, field: "tenantUser")}</g:if>
                    </td>

                    <td>
                        <g:xEditableRefData config="Task Status" owner="${taskInstance}" field="status" />
                    </td>

                    <td>${fieldValue(bean: taskInstance, field: "owner")}</td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${taskInstance?.createDate}"/></td>

                    <td class="link">
                        <g:link controller="task" action="show" id="${taskInstance.id}" class="ui button">${message(code:'default.button.show.label', default:'Show')}</g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
        <div class="pagination">
            <!--bootstrap:paginate total="${taskInstanceTotal}" /-->
        </div>
    </div><!-- .sixteen -->

</div><!-- .grid -->