
<g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM")}">
    <g:render template="/templates/tasks/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'']}"  />
    <div id="container-documents">
        <g:render template="/templates/documents/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'']}" />
    </div>
</g:if>

<div id="container-notes">
    <g:render template="/templates/notes/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'']}" />
</div>
