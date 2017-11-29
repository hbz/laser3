<div class="ui grid">

    <div class="sixteen wide column">

        <h2 class="ui header">Meine Aufgaben</h2>

        <table class="ui celled striped table">
            <thead>
            <tr>
                <th>${message(code: 'task.title.label', default: 'Title')}</th>

                <th>${message(code: 'task.endDate.label', default: 'End Date')}</th>

                <th>${message(code: 'task.object.label', default: 'Object')}</th>

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
                        <g:if test="${taskInstance.license}">
                            <g:link controller="licenseDetails" action="index" id="${taskInstance.license?.id}">${fieldValue(bean: taskInstance, field: "license")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.org}">
                            <g:link controller="organisations" action="show" id="${taskInstance.org?.id}">${fieldValue(bean: taskInstance, field: "org")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.pkg}">
                            <g:link controller="packageDetails" action="show" id="${taskInstance.pkg?.id}">${fieldValue(bean: taskInstance, field: "pkg")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.subscription}">
                            <g:link controller="subscriptionDetails" action="details" id="${taskInstance.subscription?.id}">${fieldValue(bean: taskInstance, field: "subscription")}</g:link>
                        </g:if>
                    </td>

                    <td>${fieldValue(bean: taskInstance, field: "owner")}</td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${taskInstance?.createDate}"/></td>

                    <td class="link">
                        <g:link controller="task" action="show" id="${taskInstance.id}" class="ui button">${message(code:'default.button.show.label', default:'Show')}</g:link>
                        <g:link controller="task" action="edit" id="${taskInstance.id}" class="ui button">${message(code:'default.button.edit.label', default:'Edit')}</g:link>
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