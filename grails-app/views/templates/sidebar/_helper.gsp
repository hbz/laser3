<%@page import="de.laser.*" %>
<laser:serviceInjection />

<g:if test="${tmplConfig && tmplConfig.addActionDropdownItems}">

    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_BASIC, 'INST_EDITOR')}">
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateNote" message="template.notes.add"/>
    </g:if>
    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_PRO, 'INST_EDITOR')}">
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateTask" message="task.create.new"/>
    </g:if>
    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')}">
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateDocument" message="template.documents.add"/>
    </g:if>
    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_PRO, 'INST_EDITOR')}"><!-- TODO: workflows-permissions -->
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateWorkflow" message="workflow.instantiate"/>
    </g:if>
</g:if>

<g:if test="${tmplConfig && tmplConfig.addActionModals}">

    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_BASIC, 'INST_EDITOR')}">
        <laser:render template="/templates/notes/modal_create" model="${[ownobj: tmplConfig.ownobj, owntp: tmplConfig.owntp]}"/>
    </g:if>
    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_PRO, 'INST_EDITOR')}">
        <laser:render template="/templates/tasks/modal_create" model="${[ownobj: tmplConfig.ownobj, owntp: tmplConfig.owntp]}"/>
    </g:if>
    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')}">
        <laser:render template="/templates/documents/modal" model="${[ownobj: tmplConfig.ownobj, owntp: tmplConfig.owntp, institution: tmplConfig.institution]}"/>
    </g:if>
    <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.PERMS_PRO, 'INST_EDITOR')}"><!-- TODO: workflows-permissions -->
        <laser:render template="/templates/workflow/instantiate" model="${[target: tmplConfig.ownobj]}"/>
    </g:if>
</g:if>