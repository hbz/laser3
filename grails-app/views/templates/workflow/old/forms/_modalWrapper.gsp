<laser:serviceInjection />

<g:if test="${tmplCmd == 'usage'}">
    <g:set var="wfEditPerm" value="${workflowOldService.hasUserPerm_edit()}" />

    <g:if test="${wfEditPerm}">
        <ui:modal id="wfModal" text="${tmplModalTitle}" msgSave="${message(code:'default.button.save')}">
            <laser:render template="/templates/workflow/forms/wfUsage" model="${[formUrl: "${tmplFormUrl}", wfEditPerm: true]}"/>
        </ui:modal>
    </g:if>
    <g:else>
        <ui:modal id="wfModal" text="${tmplModalTitle}" hideSubmitButton="true">
            <laser:render template="/templates/workflow/forms/wfUsage" model="${[formUrl: "${tmplFormUrl}", wfEditPerm: false]}"/>
        </ui:modal>
    </g:else>
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
    <ui:modal id="wfModal" text="${tmplModalTitle}" msgSave="${message(code:'default.button.save')}">
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