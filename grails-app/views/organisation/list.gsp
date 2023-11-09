<%@ page import="de.laser.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart message="menu.public.all_orgs" serviceInjection="true"/>

        <g:set var="entityName" value="${message(code: 'org.label')}" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.public.all_orgs" class="active" />
        </ui:breadcrumbs>

            <ui:controlButtons>
                <ui:exportDropdown>
                    <g:if test="${filterSet}">
                        <ui:exportDropdownItem>
                            <g:link class="item js-open-confirm-modal"
                                    data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                    data-confirm-term-how="ok" controller="organisations" action="list"
                                    params="${params+[exportXLS:true]}">
                                ${message(code:'default.button.exports.xls')}
                            </g:link>
                        </ui:exportDropdownItem>
                        <ui:exportDropdownItem>
                            <g:link class="item js-open-confirm-modal"
                                    data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                    data-confirm-term-how="ok" controller="organisations" action="list"
                                    params="${params+[format:'csv']}">
                                ${message(code:'default.button.exports.csv')}
                            </g:link>
                        </ui:exportDropdownItem>
                    </g:if>
                    <g:else>
                        <ui:exportDropdownItem>
                            <g:link class="item" action="list" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                        </ui:exportDropdownItem>
                        <ui:exportDropdownItem>
                            <g:link class="item" action="list" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}</g:link>
                        </ui:exportDropdownItem>
                    </g:else>
                </ui:exportDropdown>

                <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN') || contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Inst_Pro()}">
                    <ui:actionsDropdown>
                        <ui:actionsDropdownItem controller="organisation" action="create" message="org.create_new.label"/>
                    </ui:actionsDropdown>
                </g:if>

            </ui:controlButtons>

        <ui:h1HeaderWithIcon message="menu.public.all_orgs" total="${orgListTotal}" floated="true" />

        <ui:messages data="${flash}" />

        <ui:filter>
            <g:form action="list" method="get" class="ui form">
                <laser:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: [['name', 'identifier', 'type'],
                                                   ['country&region', 'libraryNetwork'],
                                                   ['discoverySystemsFrontend', 'discoverySystemsIndex'],
                                                   ['sector', 'libraryType', 'subjectGroup']],
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:form>
        </ui:filter>

        <laser:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'wibid', 'isil', 'type', 'sector', 'region', 'libraryNetwork', 'libraryType', 'status']
              ]"/>

        <ui:paginate total="${orgListTotal}" params="${params}" max="${max}" offset="${offset}" />

<laser:htmlEnd />
