<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Org;de.laser.addressbook.Person;de.laser.addressbook.PersonRole;de.laser.RefdataValue;de.laser.RefdataCategory;de.laser.storage.RDConstants;de.laser.ReaderNumber;de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:htmlStart message="menu.institutions.readerNumbers" />

        <g:set var="entityName" value="${message(code: 'org.label')}"/>

        <laser:render template="breadcrumb"
                      model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

        <ui:h1HeaderWithIcon text="${orgInstance.name}" type="${orgInstance.getCustomerType()}">
            <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
        </ui:h1HeaderWithIcon>

        <ui:controlButtons>
            <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
        </ui:controlButtons>

        <laser:render template="${customerTypeService.getNavTemplatePath()}" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}"/>

        <ui:messages data="${flash}"/>

        <laser:render template="/readerNumber/formModal" model="[formId: 'newForUni',withSemester: true,title:message(code: 'readerNumber.createForUni.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForPublic',withYear: true,title:message(code: 'readerNumber.createForPublic.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForState',withYear: true,title:message(code: 'readerNumber.createForState.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForResearchInstitute',withYear: true,title:message(code: 'readerNumber.createForResearchInstitute.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForScientificLibrary',withYear: true,title:message(code: 'readerNumber.createForScientificLibrary.label')]"/>

        <g:if test="${numbersWithSemester || numbersWithYear}">
            <g:if test="${numbersWithSemester}">
                <table class="ui table celled sortable la-js-responsive-table la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="semester" title="${message(code: 'readerNumber.semester.label')}" params="${[tableA:true]}"/>
                            <th><g:message code="default.lastUpdated.label"/></th>
                            <g:each in="${semesterCols}" var="column">
                                <th>${column}</th>
                            </g:each>
                            <g:if test="${semesterCols.size() > 1}">
                                <th><g:message code="readerNumber.sum.label"/></th>
                            </g:if>
                            <th><g:message code="readerNumber.notes"/></th>
                            <th class="center aligned">
                                <ui:optionsIcon />
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${numbersWithSemester}" var="numbersInstance">
                        <tr>
                            <td>${numbersInstance.getKey().getI10n("value")}</td>
                            <td>
                                <%
                                    Date semesterLastUpdated
                                %>
                                <g:each in="${semesterCols}" var="column">
                                    <%
                                        if(numbersInstance.getValue().get(column)) {
                                            ReaderNumber rn = numbersInstance.getValue().get(column)
                                            if(rn.lastUpdated > semesterLastUpdated)
                                                semesterLastUpdated = rn.lastUpdated
                                        }
                                    %>
                                </g:each>
                                <g:if test="${semesterLastUpdated}">
                                    <g:formatDate date="${semesterLastUpdated}" format="${message(code:'default.date.format.notime')}"/>
                                </g:if>
                            </td>
                            <g:each in="${semesterCols}" var="column">
                                <td>
                                    <g:set var="semesterNumber" value="${numbersInstance.getValue().get(column)}"/>
                                    <g:if test="${semesterNumber}">
                                        <ui:xEditable owner="${semesterNumber}" field="value"/>
                                        <%-- deactivated after command of November 27th, '24; moved in new column, where only most recent update date will be shown
                                        <span class="la-popup-tooltip la-delay" data-position="right center" data-content="${message(code:'default.lastUpdated.message')} ${formatDate(format:message(code:'default.date.format.notime'), date:number.lastUpdated)}">
                                            <i class="${Icon.TOOLTIP.INFO}"></i>
                                        </span>--%>
                                    </g:if>
                                </td>
                            </g:each>
                            <g:if test="${semesterCols.size() > 1}">
                                <td>
                                    <%
                                        Map<String,BigDecimal> sumRow = semesterSums.get(numbersInstance.getKey())
                                        BigDecimal students = sumRow.get(RDStore.READER_NUMBER_STUDENTS.getI10n("value")) ?: 0.0
                                        BigDecimal FTEs = sumRow.get(RDStore.READER_NUMBER_FTE.getI10n("value")) ?: 0.0
                                        BigDecimal staff = sumRow.get(RDStore.READER_NUMBER_SCIENTIFIC_STAFF.getI10n("value")) ?: 0.0
                                        boolean missing = students == 0.0 || FTEs == 0.0 || staff == 0.0
                                    %>
                                    <g:if test="${FTEs > 0 || staff > 0}">
                                        <g:if test="${users > 0}">
                                            <g:formatNumber number="${users}" format="${message(code:'default.decimal.format')}"/>
                                        </g:if>
                                        <g:if test="${(FTEs > 0 || staff > 0) && users > 0}">/</g:if>
                                        <g:if test="${FTEs > 0}">
                                            <g:formatNumber number="${students+FTEs}" minFractionDigits="2" maxFractionDigits="2" format="${message(code:'default.decimal.format')}"/>
                                        </g:if>
                                        <g:if test="${FTEs > 0 && staff > 0}">/</g:if>
                                        <g:if test="${staff > 0}">
                                            <g:formatNumber number="${students+staff}" minFractionDigits="2" maxFractionDigits="2" format="${message(code:'default.decimal.format')}"/>
                                        </g:if>
                                    </g:if>
                                    <g:elseif test="${students > 0}">
                                        <g:formatNumber number="${students}" format="${message(code:'default.decimal.format')}"/>
                                    </g:elseif>
                                    <g:elseif test="${users > 0}">
                                        <g:formatNumber number="${users}" format="${message(code:'default.decimal.format')}"/>
                                    </g:elseif>
                                </td>
                            </g:if>
                            <td>
                                <ui:xEditable type="readerNumber" owner="${numbersInstance.getValue().entrySet()[0].getValue()}" field="dateGroupNote"/>
                            </td>
                            <td class="x">
                                <g:if test="${editable}">
                                    <g:if test="${missing}">
                                        <a role="button" class="${Btn.MODERN.SIMPLE}" data-ui="modal" href="#newForSemester${numbersInstance.getKey().id}"
                                           aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                                        </a>
                                    </g:if>
                                    <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="readerNumber" action="delete"
                                            data-confirm-tokenMsg="${message(code: 'readerNumber.confirm.delete')}"
                                            data-confirm-term-how="ok" params="${[semester:numbersInstance.getKey().id,org:params.id]}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                    <laser:render template="/readerNumber/formModal" model="[formId: 'newForSemester'+numbersInstance.getKey().id,semester:numbersInstance.getKey().id,withSemester: true,title:message(code: 'readerNumber.createForUni.label')]"/>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </g:if>

            <g:if test="${numbersWithYear}">
                <table class="ui table celled sortable la-js-responsive-table la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="year" title="${message(code: 'readerNumber.year.label')}" params="${[tableB:true]}"/>
                            <th><g:message code="default.lastUpdated.label"/></th>
                            <g:each in="${yearCols}" var="column">
                                <th>${column}</th>
                            </g:each>
                            <g:if test="${yearCols.size() > 1}">
                                <th><g:message code="readerNumber.sum.label"/></th>
                            </g:if>
                            <th><g:message code="readerNumber.notes"/></th>
                            <th class="center aligned">
                                <ui:optionsIcon />
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${numbersWithYear}" var="numbersInstance">
                            <tr>
                                <td>${numbersInstance.getKey()}</td>
                                <td>
                                    <%
                                        Date yearLastUpdated
                                    %>
                                    <g:each in="${yearCols}" var="column">
                                        <%
                                            if(numbersInstance.getValue().get(column)) {
                                                ReaderNumber rn = numbersInstance.getValue().get(column)
                                                if(rn.lastUpdated > yearLastUpdated)
                                                    yearLastUpdated = rn.lastUpdated
                                            }
                                        %>
                                    </g:each>
                                    <g:if test="${yearLastUpdated}">
                                        <g:formatDate date="${yearLastUpdated}" format="${message(code:'default.date.format.notime')}"/>
                                    </g:if>
                                </td>
                                <g:each in="${yearCols}" var="column">
                                    <td>
                                        <g:set var="yearNumber" value="${numbersInstance.getValue().get(column)}"/>
                                        <g:if test="${yearNumber}">
                                            <ui:xEditable owner="${yearNumber}" field="value"/>
                                            <%-- see above; removed after command of November 27th, '24
                                            <span class="la-popup-tooltip la-delay" data-position="right center" data-content="${message(code:'default.lastUpdated.message')} ${formatDate(format:message(code:'default.date.format.notime'), date:number.lastUpdated)}">
                                                <i class="${Icon.TOOLTIP.INFO}"></i>
                                            </span>--%>
                                        </g:if>
                                    </td>
                                </g:each>
                                <g:if test="${yearCols.size() > 1}">
                                    <td><g:formatNumber number="${yearSums.get(numbersInstance.getKey())}" format="${message(code:'default.decimal.format')}"/></td>
                                </g:if>
                                <td><ui:xEditable type="readerNumber" owner="${numbersInstance.getValue().entrySet()[0].getValue()}" field="dateGroupNote"/></td>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM}" controller="readerNumber" action="delete"
                                                data-confirm-tokenMsg="${message(code: 'readerNumber.confirmRow.delete')}"
                                                data-confirm-term-how="ok" params="${[year:numbersInstance.getKey(),org:params.id]}"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="${Icon.CMD.DELETE}"></i>
                                        </g:link>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </g:if>
        </g:if>
        <g:else>
            <g:message code="readerNumber.noNumbersEntered"/>
        </g:else>

<laser:htmlEnd />