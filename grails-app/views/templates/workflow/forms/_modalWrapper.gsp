<semui:modal id="wfModal" text="${tmplModalTitle}">
    <g:render template="${tmpl}" model="${[tmplIsModal: true, cmd: 'edit']}"/>
</semui:modal>