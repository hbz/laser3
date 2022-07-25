<g:if test="${tmplCmd == 'usage'}">
    <ui:modal id="wfModal" text="${tmplModalTitle}" isEditModal="true">
        <laser:render template="/templates/workflow/forms/wfUsage" model="${[formUrl: "${tmplFormUrl}"]}"/>
    </ui:modal>
</g:if>
<g:elseif test="${tmplCmd == 'create'}">
    <ui:modal id="wfModal" text="${tmplModalTitle}">
        <%
            Map model1 = [formUrl: "${tmplFormUrl}", cmd: "${tmplCmd}"]
            if (tmplTab) { model1.tab = tmplTab }
        %>
        <laser:render template="${tmpl}" model="${model1}"/>
    </ui:modal>
</g:elseif>
<g:else>
    <ui:modal id="wfModal" text="${tmplModalTitle}" isEditModal="true">
        <%
            Map model2 = [formUrl: "${tmplFormUrl}", cmd: "${tmplCmd}"]
            if (tmplTab) { model2.tab = tmplTab }
            if (tmplInfo) { model2.info = tmplInfo }
        %>
        <laser:render template="${tmpl}" model="${model2}"/>
    </ui:modal>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#wfModal .wfModalLink').on('click', function(e) {
        e.preventDefault();
        $('#wfModal').modal('hide');
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });
</laser:script>