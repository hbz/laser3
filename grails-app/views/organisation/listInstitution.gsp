<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; de.laser.CustomerTypeService" %>
<laser:htmlStart message="menu.public.all_insts" serviceInjection="true"/>
        <g:set var="entityName" value="${message(code: 'org.label')}" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.public.all_insts" class="active" />
    </ui:breadcrumbs>

    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
            </ui:exportDropdownItem>
        </ui:exportDropdown>

        <%
            editable = (editable && (contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro())) || contextService.is_ORG_COM_EDITOR_or_ROLEADMIN()
        %>
        <g:if test="${editable}">
            <ui:actionsDropdown>
                <ui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_institution.label"/>
            </ui:actionsDropdown>
        </g:if>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon message="menu.public.all_insts" total="${consortiaMemberTotal}" floated="true" />

    <ui:messages data="${flash}" />

    <ui:filter>
        <g:form action="listInstitution" method="get" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow: [
                                      ['name', 'orgStatus', 'isLegallyObliged'],
                                      ['identifier', 'identifierNamespace', 'customerIDNamespace', 'isMyX'],
                                      ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup']
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

    <ui:paginate action="listInstitution" params="${params}" max="${max}" total="${consortiaMemberTotal}" />

    <laser:render template="/myInstitution/export/individuallyExportModalOrgs" model="[modalID: 'individuallyExportModal', orgType: 'institution', contactSwitch: true]" />

<laser:htmlEnd />
