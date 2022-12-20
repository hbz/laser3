<%
    List<de.laser.Task> cmbList = []
    cmbList.addAll(taskInstanceList)
    cmbList.addAll(myTaskInstanceList)
    cmbList = cmbList.unique()
%>

<laser:render template="/templates/tasks/table" model="${[taskInstanceList: cmbList, taskInstanceCount: cmbList.size()]}"/>

%{--<laser:render template="/templates/tasks/table" model="${[taskInstanceList: taskInstanceList, taskInstanceCount: taskInstanceList.size()]}"/>--}%
%{--<laser:render template="/templates/tasks/table" model="${[taskInstanceList: myTaskInstanceList, taskInstanceCount: myTaskInstanceList.size()]}"/>--}%

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editTask = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
        func();
    }
</laser:script>