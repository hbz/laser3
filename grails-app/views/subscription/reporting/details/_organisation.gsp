<%@ page import="de.laser.Org; de.laser.storage.RDStore;" %>
<laser:serviceInjection />

<laser:render template="/subscription/reporting/details/timeline/base.part1" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <th scope="col" class="center aligned">
                    ${message(code:'sidewide.number')}
                </th>
                <th scope="col">${message(code:'org.sortname.label')}</th>
                <th scope="col">${message(code:'org.name.label')}</th>
                <th scope="col">${message(code:'org.customerType.label')}</th>
                <th scope="col">${message(code:'org.orgType.label')}</th>
                <th scope="col">${message(code:'org.libraryNetwork.label')}</th>
                <th scope="col">${message(code:'org.libraryType.label')}</th>

                <g:if test="${query == 'member-subjectGroup'}">
                    <th>${message(code:'org.subjectGroup.label')}</th>
                </g:if>
                %{-- <g:if test="${query != 'member-country'}">
                    <th>${message(code:'org.country.label')}</th>
                </g:if>
                <g:if test="${query != 'member-region'}">
                    <th>${message(code:'org.region.label')}</th>
                </g:if> --}%
                <g:if test="${query == 'member-eInvoicePortal'}">
                    <th>${message(code:'org.eInvoicePortal.label')}</th>
                </g:if>
                %{--
                <g:if test="${query != 'member-funderHskType'}">
                    <th>${message(code:'org.funderHSK.label')}</th>
                </g:if>
                <g:if test="${query != 'member-funderType'}">
                    <th>${message(code:'org.funderType.label')}</th>
                </g:if>
                --}%
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="org" status="i">
                <tr>
                    <td class="center aligned">${i + 1}</td>
                    <td>
                        <g:if test="${org.sortname}">
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.sortname}</g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                    </td>
                    <td>${org.getCustomerTypeI10n()}</td>
                    <td>
                        <g:each in="${org.orgType}" var="ot">
                            ${ot.getI10n('value')} <br/>
                        </g:each>
                    </td>
                    <td>${org.libraryNetwork?.getI10n('value')}</td>
                    <td>${org.libraryType?.getI10n('value')}</td>
                    <g:if test="${query == 'member-subjectGroup'}">
                        <td>
                            <g:each in="${org.subjectGroup}" var="sg">
                                ${sg.subjectGroup.getI10n('value')} <br/>
                            </g:each>
                        </td>
                    </g:if>
                    %{--
                    <g:if test="${query != 'member-country'}">
                        <td>${org.country?.getI10n('value')}</td>
                    </g:if>
                    <g:if test="${query != 'member-region'}">
                        <td>${org.region?.getI10n('value')}</td>
                    </g:if>
                    --}%
                    <g:if test="${query == 'member-eInvoicePortal'}">
                        <td>${org.eInvoicePortal?.getI10n('value')}</td>
                    </g:if>
                    %{--
                    <g:if test="${query != 'member-funderHskType'}">
                        <td>${org.funderHskType?.getI10n('value')}</td>
                    </g:if>
                    <g:if test="${query != 'member-funderType'}">
                        <td>${org.funderType?.getI10n('value')}</td>
                    </g:if>
                    --}%
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<laser:render template="/subscription/reporting/details/loadJavascript"  />
<laser:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />