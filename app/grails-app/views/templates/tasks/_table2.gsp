<div class="ui grid">

    <div class="sixteen wide column">

        <h2 class="ui header">Von mir erstellte Aufgaben</h2>

        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'task.title.label', default: 'Title')}</th>

                <th>${message(code: 'task.endDate.label', default: 'End Date')}</th>

                <th>${message(code: 'task.status.label', default: 'Status')}</th>


                <th>${message(code: 'task.object.label', default: 'Object')}</th>

                <th>${message(code: 'task.responsibleEmployee.label')}</th>

                <th>${message(code: 'task.createDate.label', default: 'Create Date')}</th>

                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${taskInstanceList.sort{ a,b -> b.endDate.compareTo(a.endDate) }}" var="taskInstance">
                <tr>
                    <td>${fieldValue(bean: taskInstance, field: "title")}</td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${taskInstance?.endDate}"/></td>

                    <td>
                        <semui:xEditableRefData config="Task Status" owner="${taskInstance}" field="status" overwriteEditable="${overwriteEditable}" />
                    </td>

                    <td>
                        <g:if test="${taskInstance.license}">
                            <g:link controller="licenseDetails" action="show" id="${taskInstance.license?.id}">${fieldValue(bean: taskInstance, field: "license")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.org}">
                            <g:link controller="organisations" action="show" id="${taskInstance.org?.id}">${fieldValue(bean: taskInstance, field: "org")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.pkg}">
                            <g:link controller="packageDetails" action="show" id="${taskInstance.pkg?.id}">${fieldValue(bean: taskInstance, field: "pkg")}</g:link> <br />
                        </g:if>
                        <g:if test="${taskInstance.subscription}">
                            <g:link controller="subscriptionDetails" action="show" id="${taskInstance.subscription?.id}">${fieldValue(bean: taskInstance, field: "subscription")}</g:link>
                        </g:if>
                    </td>

                    <td>${fieldValue(bean: taskInstance, field: "responsibleUser")}
                    </td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${taskInstance?.createDate}"/></td>

                    <td class="x">
                        <a onclick="taskedit(${taskInstance.id});" class="ui icon button">
                            <i class="write icon"></i>
                        </a>
                        <g:link controller="myInstitution" action="tasks" params="[deleteId:taskInstance.id]" class="ui icon negative button">
                            <i class="trash alternate icon"></i>
                        </g:link>
                </tr>
                    </td>
            </g:each>
            </tbody>
        </table>

        <semui:paginate total="${taskInstanceTotal}" />

    </div><!-- .sixteen -->

</div><!-- .grid -->
<r:script>
    function taskedit(id) {

        $.ajax({
            url: '<g:createLink controller="ajax" action="TaskEdit"/>?id='+id,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalEditTask").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal('show');
            }
        });
    }
</r:script>