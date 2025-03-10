<%@ page import="de.laser.wekb.TitleInstancePackagePlatform; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<g:each in="${ies}" var="ie">
    <%
        TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) genericOIDService.resolveOID(ie.getKey())
    %>
    <tr>
        <td>
            <!-- START TEMPLATE -->
            <laser:render template="/templates/titles/title_short"
                      model="${[ie: null, tipp: tipp,
                                showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
            <!-- END TEMPLATE -->
        </td>
        <g:each in="${objects}" var="object">
            <g:set var="ieValues" value="${ie.getValue()}"/>
            <g:if test="${ieValues.containsKey(object)}">
                <g:each var="ieValue" in="${ieValues.get(object)}">
                    <td class="coverageStatements la-tableCard" >

                        <laser:render template="/templates/tipps/coverages" model="${[ie: ieValue, tipp: ieValue.tipp]}"/>

                    </td>
                    <td>
                        <!-- von --->
                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                      date="${ieValue.accessStartDate}"/>
                        <ui:dateDevider/>
                        <!-- bis -->
                        <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                      date="${ieValue.accessEndDate}"/>
                    </td>
                    <td>
                        <g:if test="${ieValue.priceItems}">
                            <g:each in="${ieValue.priceItems}" var="priceItem" status="i">
                                <g:message code="tipp.price.listPrice"/>: <ui:xEditable field="listPrice"
                                                                                           owner="${priceItem}"
                                                                                           format=""/> <ui:xEditableRefData
                                    field="listCurrency" owner="${priceItem}"
                                    config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                <g:message code="tipp.price.localPrice"/>: <ui:xEditable field="localPrice"
                                                                                            owner="${priceItem}"/> <ui:xEditableRefData
                                    field="localCurrency" owner="${priceItem}"
                                    config="Currency"/> <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                            <%--<ui:xEditable field="startDate" type="date"
                                             owner="${priceItem}"/><ui:dateDevider/><ui:xEditable
                                field="endDate" type="date"
                                owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                <g:if test="${i < ieValue.priceItems.size() - 1}"><hr></g:if>
                            </g:each>
                        </g:if>
                    </td>
                    <td>
                        ${message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}  ${ieValue.perpetualAccessBySub ? "${RDStore.YN_YES.getI10n('value')}: ${ieValue.perpetualAccessBySub.dropdownNamingConvention()}" : RDStore.YN_NO.getI10n('value') }
                    </td>
                </g:each>
            </g:if>
            <g:else>
                <td></td>
                <td></td>
                <td></td>
                <td></td>
            </g:else>
        </g:each>
    </tr>
</g:each>