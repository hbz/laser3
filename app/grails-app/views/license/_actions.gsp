<%@ page import="de.laser.interfaces.TemplateSupport" %>
<laser:serviceInjection />

<semui:actionsDropdown>

    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
    </g:if>
    <g:if test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER,ORG_CONSORTIUM','INST_EDITOR')}">
        <semui:actionsDropdownItem message="template.addNote" data-semui="modal" href="#modalCreateNote" />
    </g:if>
    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <g:if test="${license.getLicensingConsortium()?.id == contextService.getOrg()?.id && ! license.isTemplate()}">
            <g:if test="${!( license.instanceOf && ! license.hasTemplate())}">
                <div class="divider"></div>

                <semui:actionsDropdownItem controller="license" action="addMembers" params="${[id:license?.id]}" message="myinst.emptyLicense.child" />
            </g:if>
        </g:if>

        <div class="divider"></div>

        <semui:actionsDropdownItem controller="license" action="copyLiceInstitutionsServicense" params="${[id:license?.id]}" message="myinst.copyLicense" />

        <g:if test="${actionName == 'show'}">
            <g:if test="${(license.getLicensingConsortium()?.id == contextService.getOrg()?.id) || (license.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_LOCAL && license.getLicensee()?.id == contextService.getOrg()?.id) && ! license.isTemplate()}">
                <div class="divider"></div>
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalsgruppen konfigurieren" />
            </g:if>


                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate icon"></i> Vertrag löschen</g:link>
        </g:if>

    </g:if>
</semui:actionsDropdown>

<g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
    <g:render template="/templates/tasks/modal_create" model="${[ownobj:license, owntp:'license']}"/>
    <g:render template="/templates/documents/modal" model="${[ownobj:license, owntp:'license']}"/>
</g:if>
<g:if test="${accessService.checkPermAffiliation('ORG_BASIC_MEMBER,ORG_CONSORTIUM','INST_EDITOR')}">
    <g:render template="/templates/notes/modal_create" model="${[ownobj: license, owntp: 'license']}"/>
</g:if>