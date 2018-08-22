<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${editable}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />

        <g:if test="${license.getLicensingConsortium()?.id == contextService.getOrg()?.id && ! license.isTemplate()}">
            <g:if test="${!( license.instanceOf && ! license.hasTemplate())}">
                <div class="divider"></div>

                <semui:actionsDropdownItem controller="licenseDetails" action="addMembers" params="${[id:license?.id]}" message="myinst.emptyLicense.child" />
            </g:if>
        </g:if>

        <semui:actionsDropdownItem controller="licenseDetails" action="copyLicense" params="${[id:license?.id]}" message="myinst.copyLicense" />
    </semui:actionsDropdown>

    <g:render template="/templates/tasks/modal_create" model="${[ownobj:license, owntp:'license']}"/>
    <g:render template="/templates/documents/modal" model="${[ownobj:license, owntp:'license']}"/>
    <g:render template="/templates/notes/modal_create" model="${[ownobj: license, owntp: 'license']}"/>
</g:if>