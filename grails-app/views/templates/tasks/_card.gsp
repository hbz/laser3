<laser:serviceInjection />

<%--OVERWRITE editable for INST_EDITOR: ${editable} -&gt; ${accessService.checkMinUserOrgRole(user, institution, 'INST_EDITOR')} @ ${institution}--%>
<g:set var="overwriteEditable" value="${editable || accessService.checkMinUserOrgRole(user, institution, 'INST_EDITOR')}" />

<semui:card message="task.plural" class="notes la-js-hideable ${css_class}" href="#modalCreateTask" editable="${overwriteEditable}">
    <g:each in="${tasks}" var="tsk">
        <div class="ui grid">
            <div class="twelve wide column summary">
                <a onclick="JSPC.taskedit(${tsk?.id});">${tsk?.title}</a>
                <br />
                <div class="content">
                    ${message(code:'task.endDate.label')}
                    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tsk.endDate}"/>
                </div>
            </div>
            <div class="right aligned four wide column la-column-left-lessPadding">
                <g:link action="tasks" class="ui mini icon negative button"
                        params='[deleteId:tsk?.id, id: params.id, returnToShow: true]'>
                    <i class="trash alternate icon"></i>
                </g:link>
            </div>
        </div>
    </g:each>
</semui:card>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.taskedit = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
        func();
    }
</laser:script>
