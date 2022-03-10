<g:if test="${tmplCmd == 'usage'}">
    <semui:modal id="wfModal" text="${tmplModalTitle}" isEditModal="true">
        <g:render template="/templates/workflow/forms/wfUsage" model="${[formUrl: "${tmplFormUrl}"]}"/>
    </semui:modal>
</g:if>
<g:elseif test="${tmplCmd == 'create'}">
    <semui:modal id="wfModal" text="${tmplModalTitle}">
        <%
            Map model1 = [formUrl: "${tmplFormUrl}", cmd: "${tmplCmd}"]
            if (tmplTab) { model1.tab = tmplTab }
        %>
        <g:render template="${tmpl}" model="${model1}"/>
    </semui:modal>
</g:elseif>
<g:else>
    <semui:modal id="wfModal" text="${tmplModalTitle}" isEditModal="true">
        <%
            Map model2 = [formUrl: "${tmplFormUrl}", cmd: "${tmplCmd}"]
            if (tmplTab) { model2.tab = tmplTab }
            if (tmplInfo) { model2.info = tmplInfo }
        %>
        <g:render template="${tmpl}" model="${model2}"/>
    </semui:modal>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#wfModal .wfModalLink').on('click', function(e) {
        e.preventDefault();
        $('#wfModal').modal('hide');
        var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);
        func();
    });
</laser:script>