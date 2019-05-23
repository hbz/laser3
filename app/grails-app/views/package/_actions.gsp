<laser:serviceInjection />

<semui:actionsDropdown>
    <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
    <g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />

        <div class="divider"></div>
    </g:if>

    <semui:actionsDropdownItem controller="package" action="compare" message="menu.public.comp_pkg" />

    <g:if test="${actionName == 'show'}">
        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_PACKAGE_EDITOR">
            <g:link class="item" controller="announcement" action="index" params='[at:"Package Link: ${pkg_link_str}",as:"RE: Package ${packageInstance.name}"]'>${message(code: 'package.show.announcement')}</g:link>
        </sec:ifAnyGranted>
    </g:if>

</semui:actionsDropdown>

<g:if test="${(editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')) && ! ['list'].contains(actionName)}">
    <g:render template="/templates/documents/modal" model="${[ownobj: packageInstance, institution: contextService.org, owntp: 'pkg']}"/>
    <g:render template="/templates/tasks/modal_create" model="${[ownobj:packageInstance, owntp:'pkg']}"/>
</g:if>

<g:render template="/templates/notes/modal_create" model="${[ownobj: packageInstance, owntp: 'pkg']}"/>
