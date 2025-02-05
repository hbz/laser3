<%@page import="de.laser.interfaces.CalculatedType; de.laser.*" %>
<laser:serviceInjection />

%{--<pre>--}%
%{--    #_aside.gsp--}%
%{--    contextOrg: ${contextOrg}--}%
%{--    institution: ${institution}--}%
%{--    orgInstance: ${orgInstance}--}%
%{--    inContextOrg: ${inContextOrg}--}%
%{--    parentAtChild: ${parentAtChild}--}%
%{--</pre>--}%

<div id="container-notes">
    <laser:render template="/templates/notes/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'']}" />
</div>

<g:if test="${taskService.hasREAD()}">
    <div id="container-tasks">
        <laser:render template="/templates/tasks/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'']}" />
    </div>
</g:if>

<div id="container-documents">
    <laser:render template="/templates/documents/card" model="${[ownobj:ownobj, owntp:owntp, css_class:'']}" />
</div>

<g:if test="${workflowService.hasREAD()}">
    <div id="container-workflows">
        <laser:render template="/templates/workflow/card" model="${[checklists: checklists]}" />
    </div>
</g:if>