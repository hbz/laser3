
<laser:render template="/templates/tasks/table" model="${[cmbTaskInstanceList: cmbTaskInstanceList.drop((int) offset).take((int) max), cmbTaskInstanceCount: cmbTaskInstanceList.size()]}"/>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.editTask = function (id) {
        var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
        func();
    }
</laser:script>