<% def accService = grailsApplication.mainContext.getBean("accessService") %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')} -->
<g:set var="overwriteEditable" value="${editable || accService.checkMinUserOrgRole(user, contextService.getOrg(), 'INST_EDITOR')}" />

<div class="ui grid">

    <div class="sixteen wide column">

        <%--
        <g:if test="${editable && controllerName != 'myInstitution'}">
            <input type="button" class="ui button" value="${message(code:'task.create.new', default:'Add new Task')}" data-semui="modal" href="#modalCreateTask"/>
        </g:if>
        --%>

        <table class="ui sortable celled la-table table">
            <thead>
            <tr>
                <g:sortableColumn property="title" title="${message(code: 'task.title.label', default: 'Title')}"/>
                <g:sortableColumn property="endDate" title="${message(code: 'task.endDate.label', default: 'End Date')}"/>
               <g:sortableColumn property="status" title="${message(code: 'task.status.label', default: 'Status')}"/>
                <g:if test="${controllerName == 'myInstitution'}">
                    <th>${message(code: 'task.object.label', default: 'Object')}</th>
                </g:if>
                <th>
                    ${message(code: 'task.responsibleOrg.label', default: 'responsibleOrg')} <br />
                    ${message(code: 'task.responsibleUser.label', default: 'responsibleUser')}
                </th>
                <g:sortableColumn property="creator" title="${message(code: 'task.creator.label', default: 'Creator')}"/>
                <g:sortableColumn property="createDate" title="${message(code: 'task.createDate.label', default: 'Create Date')}"/>
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
                    <g:if test="${controllerName == 'myInstitution'}">
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
                    </g:if>

                    <td>
                        <g:if test="${taskInstance.responsibleOrg}">${taskInstance.responsibleOrg?.name} <br /></g:if>
                        <g:if test="${taskInstance.responsibleUser}">${taskInstance.responsibleUser}</g:if>
                    </td>

                    <td>${fieldValue(bean: taskInstance, field: "creator")}</td>

                    <td><g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${taskInstance?.createDate}"/></td>

                    <td class="x">
                        <g:if test="${overwriteEditable}">
                            <a onclick="taskedit(${taskInstance.id});" class="ui icon button">
                                <i class="write icon"></i>
                            </a>
                        </g:if>
                        <g:if test="${(user == taskInstance.creator) || contextService.getUser().hasAffiliation("INST_ADM")}">
                            <g:link action="tasks" params="[deleteId:taskInstance.id, id: params.id]" class="ui icon negative button">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
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