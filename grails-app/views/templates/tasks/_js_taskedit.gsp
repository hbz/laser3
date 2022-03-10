<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.taskedit = function (id) {
    var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id, true);
        func();
    }
</laser:script>