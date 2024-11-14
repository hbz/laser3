<%@page import="de.laser.*" %>
<laser:serviceInjection />

%{--<pre>--}%
%{--    #_modals.gsp--}%
%{--    contextOrg: ${contextOrg}--}%
%{--    institution: ${institution}--}%
%{--    orgInstance: ${orgInstance}--}%
%{--    inContextOrg: ${inContextOrg}--}%
%{--    parentAtChild: ${parentAtChild}--}%
%{--</pre>--}%

<g:if test="${contextService.isInstEditor()}">
    <laser:render template="/templates/notes/modal_create" model="${[ownobj: tmplConfig.ownobj, owntp: tmplConfig.owntp]}"/>
</g:if>
<g:if test="${taskService.hasWRITE()}">
    <laser:render template="/templates/tasks/modal_create" model="${[ownobj: tmplConfig.ownobj, owntp: tmplConfig.owntp]}"/>
</g:if>
<g:if test="${contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
    <laser:render template="/templates/documents/modal" model="${[ownobj: tmplConfig.ownobj, owntp: tmplConfig.owntp, inContextOrg: inContextOrg]}"/>
</g:if>
<g:if test="${workflowService.hasWRITE()}">
    <laser:render template="/templates/workflow/instantiate" model="${[target: tmplConfig.ownobj]}"/>
</g:if>
