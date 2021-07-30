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