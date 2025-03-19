<%@ page import="de.laser.ExportClickMeService; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService" %>
<laser:htmlStart message="menu.public.all_insts" />
        <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.public.all_insts" class="active" />
    </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.INSTITUTIONS, exportFileName: message(code: 'menu.institutions')]"/>
            </ui:exportDropdownItem>
        </ui:exportDropdown>

        <%
            editable = (editable && (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro())) || contextService.is_ORG_COM_EDITOR_or_ROLEADMIN()
        %>
        <ui:actionsDropdown>
            <g:if test="${editable}">
                <ui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_institution.label"/>
            </g:if>
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </ui:actionsDropdown>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.public.all_insts" total="${consortiaMemberTotal}" floated="true" type="institution" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="listInstitution" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [
                                      ['name', 'identifier', 'identifierNamespace', 'customerIDNamespace'],
                                      ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup'],
                                      ['discoverySystemsFrontend', 'discoverySystemsIndex', 'isLegallyObliged', 'isMyX']
                              ],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <g:if test="${consortiaMemberIds}">%{-- --}%
        <laser:render template="/templates/filter/orgFilterTable"
                  model="[orgList: availableOrgs,
                          currentConsortiaMemberIdList: consortiaMemberIds,
                          tmplShowCheckbox: false,
                          tmplConfigShow: [
                                  'sortname', 'name', 'isil', 'region', 'libraryNetwork', 'libraryType', 'status', 'legalInformation', 'isMyX'
                          ]
                  ]"/>
    </g:if>
    <g:else>%{-- --}%
    <laser:render template="/templates/filter/orgFilterTable"
              model="[orgList: availableOrgs,
                      tmplShowCheckbox: false,
                      tmplConfigShow: [
                              'sortname', 'name', 'isil', 'region', 'libraryNetwork', 'libraryType', 'status', 'legalInformation'
                      ]
              ]"/>
    </g:else>

    <laser:render template="/templates/copyEmailaddresses" model="[orgList: totalOrgs]"/>
    <ui:paginate action="listInstitution" params="${params}" max="${max}" total="${consortiaMemberTotal}" />


<g:render template="/clickMe/export/js"/>

<laser:htmlEnd />
