<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.taskcreate = bb8.ajax4SimpleModalFunction("#modalCreateTask", "<g:createLink controller="myInstitution" action="modal_create"/>", true);
</laser:script>