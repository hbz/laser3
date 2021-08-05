<g:if test="${tmplCmd == 'create'}">
    <semui:modal id="wfModal" text="${tmplModalTitle}">
        <g:render template="${tmpl}" model="${[tmplIsModal: true, cmd: "${tmplCmd}"]}"/>
    </semui:modal>
</g:if>
<g:else>
    <semui:modal id="wfModal" text="${tmplModalTitle}" isEditModal="true">
        <g:render template="${tmpl}" model="${[tmplIsModal: true, cmd: "${tmplCmd}"]}"/>
    </semui:modal>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#wfModal .wfModalLink').on('click', function(e) {
        e.preventDefault();
        $('#wfModal').modal('hide');
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), true);
        func();
    });
</laser:script>