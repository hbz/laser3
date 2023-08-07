<%@ page import="de.laser.CustomerTypeService; de.laser.PersonRole; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>

<g:set var="showOnlyPublic" value="${true}"/>

<div class="ui cards">
<g:if test="${!isProviderOrAgency}">
    <div class="card">
        <div class="content">
            <div class="header la-primary-header">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}</div>

            <div class="description">

                <g:set var="persons"
                       value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS)}"/>
                <g:each in="${persons}" var="prs">
                    <laser:render template="/templates/cpa/person_full_details" model="${[
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
            <div class="header la-primary-header">${RDStore.PRS_FUNC_FC_BILLING_ADDRESS.getI10n('value')}</div>

            <div class="description">
                <g:set var="persons"
                       value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_FC_BILLING_ADDRESS)}"/>
                <g:each in="${persons}" var="prs">
                    <laser:render template="/templates/cpa/person_full_details" model="${[
                            person                 : prs,
                            personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_FC_BILLING_ADDRESS, prs),
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
                    <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_FC_BILLING_ADDRESS.getI10n('value')]"/>
                </g:if>
            </div>
        </div>
    </div>
</g:if>
    <div class="card">
        <div class="content">
            <div class="header la-primary-header">
                <g:if test="${existsWekbRecord == true}">
                    <i class="ui light grey la-gokb icon la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')}"></i>
                </g:if>
                ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
            </div>

            <div class="description">

                <g:set var="techSupports"
                       value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, existsWekbRecord)}"/>
                <g:each in="${techSupports}" var="prs">
                    <laser:render template="/templates/cpa/person_full_details" model="${[
                            person                 : prs,
                            personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_TECHNICAL_SUPPORT, prs),
                            personContext          : orgInstance,
                            tmplShowDeleteButton   : (isProviderOrAgency && (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ))),
                            tmplShowFunctions      : false,
                            tmplShowPositions      : true,
                            tmplShowResponsiblities: true,
                            showContacts           : true,
                            tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax'],
                            controller             : 'organisation',
                            action                 : 'show',
                            id                     : orgInstance.id,
                            editable               : (isProviderOrAgency && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ) && !existsWekbRecord),
                            noSelection            : true
                    ]}"/>
                </g:each>
                <g:if test="${!techSupports}">
                    <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')]"/>
                </g:if>
            </div>
        </div>
    </div>
    <g:if test="${isProviderOrAgency}">
        <div class="card">
            <div class="content">
                <div class="header la-primary-header">
                    <g:if test="${existsWekbRecord == true}">
                        <i class="ui light grey la-gokb icon la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')}"></i>
                    </g:if>
                    ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                </div>

                <div class="description">
                    <g:set var="serviceSupports"
                           value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_SERVICE_SUPPORT, existsWekbRecord)}"/>
                    <g:each in="${serviceSupports}" var="prs">
                        <laser:render template="/templates/cpa/person_full_details" model="${[
                                person                 : prs,
                                personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_SERVICE_SUPPORT, prs),
                                personContext          : orgInstance,
                                tmplShowDeleteButton   : (isProviderOrAgency && (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ))),
                                tmplShowFunctions      : false,
                                tmplShowPositions      : true,
                                tmplShowResponsiblities: true,
                                showContacts           : true,
                                tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax'],
                                controller             : 'organisation',
                                action                 : 'show',
                                id                     : orgInstance.id,
                                editable               : (isProviderOrAgency && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ) && !existsWekbRecord),
                                noSelection            : true
                        ]}"/>
                    </g:each>
                    <g:if test="${!serviceSupports}">
                        <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')]"/>
                    </g:if>
                </div>
            </div>
        </div>
        <div class="card">
            <div class="content">
                <div class="header la-primary-header">
                    <g:if test="${existsWekbRecord == true}">
                        <i class="ui light grey la-gokb icon la-popup-tooltip" data-content="${message(code:'org.isWekbCurated.header.label')}"></i>
                    </g:if>
                    ${RDStore.PRS_FUNC_METADATA.getI10n('value')}
                </div>

                <div class="description">
                    <g:set var="serviceSupports"
                           value="${orgInstance.getContactPersonsByFunctionType(showOnlyPublic, RDStore.PRS_FUNC_METADATA, existsWekbRecord)}"/>
                    <g:each in="${serviceSupports}" var="prs">
                        <laser:render template="/templates/cpa/person_full_details" model="${[
                                person                 : prs,
                                personRole             : PersonRole.findByOrgAndFunctionTypeAndPrs(orgInstance, RDStore.PRS_FUNC_METADATA, prs),
                                personContext          : orgInstance,
                                tmplShowDeleteButton   : (isProviderOrAgency && (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ))),
                                tmplShowFunctions      : false,
                                tmplShowPositions      : true,
                                tmplShowResponsiblities: true,
                                showContacts           : true,
                                tmplConfigShow         : ['E-Mail', 'Mail', 'Url', 'Phone', 'Mobil', 'Fax'],
                                controller             : 'organisation',
                                action                 : 'show',
                                id                     : orgInstance.id,
                                editable               : (isProviderOrAgency && contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_BASIC ) && !existsWekbRecord),
                                noSelection            : true
                        ]}"/>
                    </g:each>
                    <g:if test="${!serviceSupports}">
                        <g:message code="person.function.notExist" args="[RDStore.PRS_FUNC_METADATA.getI10n('value')]"/>
                    </g:if>
                </div>
            </div>
        </div>
    </g:if>
</div>