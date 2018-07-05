<% def accService = grailsApplication.mainContext.getBean("accessService") %>
<!-- OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accService.checkMinUserOrgRole(user, contextOrg, 'INST_EDITOR')} -->
<g:set var="overwriteEditable" value="${editable || accService.checkMinUserOrgRole(user, contextOrg, 'INST_EDITOR')}" />

<semui:card message="task.plural" class="notes" href="#modalCreateTask" editable="${overwriteEditable}">
    <g:each in="${tasks}" var="tsk">
        <div class="ui small feed content">
            <!--<div class="event">-->
                    <div id="summary" class="summary">
                        <a onclick="taskedit(${tsk?.id});">${tsk?.title}</a>
                        <br />
                        <div class="content">
                            ${message(code:'task.endDate.label')}
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk.endDate}"/>
                        </div>
                    </div>
        <!--</div>-->
        </div>
    </g:each>
</semui:card>

<g:render template="/templates/tasks/modal_create" />

<r:script>
    function taskedit(id) {

        $.ajax({
            url: '<g:createLink controller="ajax" action="TaskEdit"/>?id='+id,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalEditTask").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal({
                    onVisible: function() {
                        $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                        ajaxPostFunc();
                    }
                }).modal('show')
            }
        });
    }
</r:script>
