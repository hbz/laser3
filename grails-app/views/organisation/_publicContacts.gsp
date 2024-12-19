<%@ page import="de.laser.addressbook.PersonRole; de.laser.CustomerTypeService; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>

<g:set var="showOnlyPublic" value="${true}"/>

<div class="ui cards">
    <div class="card">
        <div class="content">
            <div class="header la-primary-header">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</div>

            <div class="description">

                <g:set var="persons"
                       value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)}"/>
                <g:each in="${persons}" var="prs">
                    <laser:render template="/addressbook/person_full_details" model="${[
                            person                 : prs,
                            personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, prs),
                            personContext          : orgInstance,
                            tmplShowDeleteButton   : true,
                            tmplShowFunctions      : false,
                            tmplShowPositions      : true,
                            tmplShowResponsiblities: true,
                            tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax', 'address'],
                            controller             : 'organisation',
                            action                 : 'show',
                            id                     : orgInstance.id,
                            editable               : false,
                            noSelection            : true
                    ]}"/>
                </g:each>
                <g:if test="${!persons}">
                    <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')]"/>
                </g:if>
            </div>
        </div>
    </div>
    <div class="card">
        <div class="content">
            <div class="header la-primary-header">${RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value')}</div>

            <div class="description">
                <g:set var="persons"
                       value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_INVOICING_CONTACT)}"/>
                <g:each in="${persons}" var="prs">
                    <laser:render template="/addressbook/person_full_details" model="${[
                            person                 : prs,
                            personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_INVOICING_CONTACT, prs),
                            personContext          : orgInstance,
                            tmplShowDeleteButton   : true,
                            tmplShowFunctions      : false,
                            tmplShowPositions      : true,
                            tmplShowResponsiblities: true,
                            tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax', 'address'],
                            controller             : 'organisation',
                            action                 : 'show',
                            id                     : orgInstance.id,
                            editable               : false,
                            noSelection            : true
                    ]}"/>
                </g:each>
                <g:if test="${!persons}">
                    <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value')]"/>
                </g:if>
            </div>
        </div>
    </div>
    <div class="card">
        <div class="content">
            <div class="header la-primary-header">
                ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
            </div>

            <div class="description">

                <g:set var="techSupports"
                       value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_TECHNICAL_SUPPORT)}"/>
                <g:each in="${techSupports}" var="prs">
                    <laser:render template="/addressbook/person_full_details" model="${[
                            person                 : prs,
                            personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, prs),
                            personContext          : orgInstance,
                            tmplShowDeleteButton   : (contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )),
                            tmplShowFunctions      : false,
                            tmplShowPositions      : true,
                            tmplShowResponsiblities: true,
                            showContacts           : true,
                            tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax'],
                            controller             : 'organisation',
                            action                 : 'show',
                            id                     : orgInstance.id,
                            editable               : (contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )),
                            noSelection            : true
                    ]}"/>
                </g:each>
                <g:if test="${!techSupports}">
                    <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')]"/>
                </g:if>
            </div>
        </div>
    </div>
</div>