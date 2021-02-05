<laser:serviceInjection />

<g:set var="user" value="${contextService.getUser()}"/>
<g:set var="org" value="${contextService.getOrg()}"/>

<semui:actionsDropdown>
%{--    <g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR') && ! ['list'].contains(actionName)}">
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
    </g:if>
    <g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <div class="divider"></div>
    </g:if>--}%

    <semui:actionsDropdownItemDisabled controller="package" action="compare" message="menu.public.comp_pkg" />

</semui:actionsDropdown>

%{--
<g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
    <g:render template="/templates/documents/modal" model="${[ownobj: packageInstance, institution: contextService.getOrg(), owntp: 'pkg']}"/>
    <g:render template="/templates/tasks/modal_create" model="${[ownobj:packageInstance, owntp:'pkg']}"/>
</g:if>
<g:if test="${accessService.checkMinUserOrgRole(user,org,'INST_EDITOR') && ! ['list'].contains(actionName)}">
    <g:render template="/templates/notes/modal_create" model="${[ownobj: packageInstance, owntp: 'pkg']}"/>
</g:if>--}%
