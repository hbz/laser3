<%@ page import="de.laser.Org;de.laser.Person;de.laser.PersonRole;de.laser.RefdataValue;de.laser.RefdataCategory;de.laser.storage.RDConstants;de.laser.ReaderNumber;de.laser.utils.DateUtils; de.laser.storage.RDStore" %>
<laser:htmlStart message="menu.institutions.readerNumbers" serviceInjection="true"/>

        <g:set var="entityName" value="${message(code: 'org.label')}"/>

        <semui:breadcrumbs>
            <g:if test="${institutionalView}">
                <semui:crumb message="menu.my.insts" controller="myInstitution" action="manageMembers" params="[comboType:RDStore.COMBO_TYPE_CONSORTIUM]"/>
                <semui:crumb text="${orgInstance.sortname}" class="active"/>
            </g:if>
            <g:else>
                <semui:crumb text="${orgInstance.sortname}" class="active"/>
            </g:else>
        </semui:breadcrumbs>

        <g:if test="${editable}">
            <semui:controlButtons>
                <laser:render template="actions" />
            </semui:controlButtons>
        </g:if>

        <semui:h1HeaderWithIcon text="${orgInstance.name}" />

        <laser:render template="nav" model="${[orgInstance: orgInstance, inContextOrg: orgInstance.id == contextService.getOrg().id]}"/>

        <semui:messages data="${flash}"/>

        <laser:render template="/readerNumber/formModal" model="[formId: 'newForUni',withSemester: true,title:message(code: 'readerNumber.createForUni.label'), semester: RefdataValue.getCurrentSemester().id]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForPublic',withDueDate: true,title:message(code: 'readerNumber.createForPublic.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForState',withDueDate: true,title:message(code: 'readerNumber.createForState.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForResearchInstitute',withDueDate: true,title:message(code: 'readerNumber.createForResearchInstitute.label')]"/>
        <laser:render template="/readerNumber/formModal" model="[formId: 'newForScientificLibrary',withDueDate: true,title:message(code: 'readerNumber.createForScientificLibrary.label')]"/>

        <g:if test="${numbersWithSemester || numbersWithDueDate}">
            <g:if test="${numbersWithSemester}">
                <table class="ui table celled sortable la-js-responsive-table la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="semester" title="${message(code: 'readerNumber.semester.label')}" params="${[tableA:true]}"/>
                            <g:each in="${semesterCols}" var="column">
                                <th>
                                    ${column}
                                    <g:if test="${editable}">
                                        <%--<g:if test="${!(column in RefdataCategory.getAllRefdataValues(RDConstants.NUMBER_TYPE).collect {rdv->rdv.getI10n("value")})}">
                                            <g:link class="ui icon button js-open-confirm-modal" controller="readerNumber" action="delete"
                                                    data-confirm-tokenMsg="${message(code: 'readerNumber.confirm.delete')}"
                                                    data-confirm-term-how="ok" params="${[referenceGroup:column,org:params.id]}">
                                                <i class="red times icon"></i>
                                            </g:link>
                                        </g:if>--%>
                                    </g:if>
                                </th>
                            </g:each>
                            <th><g:message code="readerNumber.sum.label"/></th>
                            <th><g:message code="readerNumber.notes"/></th>
                            <th>${message(code:'default.actions.label')}</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${numbersWithSemester}" var="numbersInstance">
                        <tr>
                            <td>${numbersInstance.getKey().getI10n("value")}</td>
                            <g:each in="${semesterCols}" var="column">
                                <td>
                                    <g:set var="number" value="${numbersInstance.getValue().get(column)}"/>
                                    <g:if test="${number}">
                                        <semui:xEditable owner="${number}" field="value" format="number"/>
                                    </g:if>
                                </td>
                            </g:each>
                            <%
                                Map<String,BigDecimal> sumRow = semesterSums.get(numbersInstance.getKey())
                                BigDecimal students = sumRow.get(RDStore.READER_NUMBER_STUDENTS.getI10n("value")) ?: 0.0
                                BigDecimal FTEs = sumRow.get(RDStore.READER_NUMBER_FTE.getI10n("value")) ?: 0.0
                                BigDecimal staff = sumRow.get(RDStore.READER_NUMBER_SCIENTIFIC_STAFF.getI10n("value")) ?: 0.0
                                boolean missing = students == 0.0 || FTEs == 0.0 || staff == 0.0
                            %>
                            <td>
                                <g:if test="${FTEs > 0}">
                                    <g:formatNumber number="${students+FTEs}" minFractionDigits="2" maxFractionDigits="2" format="${message(code:'default.decimal.format')}"/>
                                </g:if>
                                <g:if test="${FTEs > 0 && staff > 0}">/</g:if>
                                <g:if test="${staff > 0}">
                                    <g:formatNumber number="${students+staff}" minFractionDigits="2" maxFractionDigits="2" format="${message(code:'default.decimal.format')}"/>
                                </g:if>
                            </td>
                            <td>
                                <semui:xEditable type="readerNumber" owner="${numbersInstance.getValue().entrySet()[0].getValue()}" field="dateGroupNote"/>
                            </td>
                            <td class="x">
                                <g:if test="${editable}">
                                    <g:if test="${missing}">
                                        <a role="button" class="ui icon button blue la-modern-button" data-semui="modal" href="#newForSemester${numbersInstance.getKey().id}"
                                           aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                            <i aria-hidden="true" class="write icon"></i>
                                        </a>
                                    </g:if>
                                    <g:link class="ui icon negative button la-modern-button js-open-confirm-modal" controller="readerNumber" action="delete"
                                            data-confirm-tokenMsg="${message(code: 'readerNumber.confirm.delete')}"
                                            data-confirm-term-how="ok" params="${[semester:numbersInstance.getKey().id,org:params.id]}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="trash alternate outline icon"></i>
                                    </g:link>
                                    <laser:render template="/readerNumber/formModal" model="[formId: 'newForSemester'+numbersInstance.getKey().id,semester:numbersInstance.getKey().id,withSemester: true,title:message(code: 'readerNumber.createForUni.label')]"/>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </g:if>

            <g:if test="${numbersWithDueDate}">
                <table class="ui table celled sortable la-js-responsive-table la-table">
                    <thead>
                        <tr>
                            <g:sortableColumn property="dueDate" title="${message(code: 'readerNumber.dueDate.label')}" params="${[tableB:true]}"/>
                            <g:each in="${dueDateCols}" var="column">
                                <th>${column}</th>
                            </g:each>
                            <th><g:message code="readerNumber.sum.label"/></th>
                            <th><g:message code="readerNumber.notes"/></th>
                            <th>${message(code:'default.actions.label')}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${numbersWithDueDate}" var="numbersInstance">
                            <tr>
                                <td><g:formatDate date="${numbersInstance.getKey()}" format="${message(code:'default.date.format.notime')}"/></td>
                                <g:each in="${dueDateCols}" var="column">
                                    <td>
                                        <g:set var="number" value="${numbersInstance.getValue().get(column)}"/>
                                        <g:if test="${number}">
                                            <semui:xEditable owner="${number}" field="value" type="number"/>
                                        </g:if>
                                    </td>
                                </g:each>
                                <td><g:formatNumber number="${dueDateSums.get(numbersInstance.getKey())}" format="${message(code:'default.decimal.format')}"/></td>
                                <td><semui:xEditable type="readerNumber" owner="${numbersInstance.getValue().entrySet()[0].getValue()}" field="dateGroupNote"/></td>
                                <td class="x">
                                    <g:if test="${editable}">
                                        <g:link class="ui icon negative button la-modern-button js-open-confirm-modal" controller="readerNumber" action="delete"
                                                data-confirm-tokenMsg="${message(code: 'readerNumber.confirmRow.delete')}"
                                                data-confirm-term-how="ok" params="${[dueDate:numbersInstance.getKey(),org:params.id]}"
                                                role="button"
                                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                            <i class="trash alternate outline icon"></i>
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