<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.titles.JournalInstance; de.laser.titles.BookInstance; de.laser.helper.RDStore; de.laser.ApiSource" %>
<laser:serviceInjection/>
<semui:form>
    <table class="ui selectable celled table la-table la-ignore-fixed">
        <thead>
        <tr>
            <th rowspan="2">${message(code: 'default.compare.title')}</th>
            <g:each in="${objects}" var="object">
                <th colspan="3">
                    <g:if test="${object}"><g:link
                            controller="${object.getClass().getSimpleName().toLowerCase()}" action="show"
                            id="${object.id}">${object.dropdownNamingConvention()}</g:link></g:if>
                </th>
            </g:each>
        </tr>
        <tr>
            <g:each in="${objects}" var="object">
                <th>${message(code: 'subscription.details.date_header')}</th>
                <th>${message(code: 'subscription.details.access_dates')}</th>
                <th>${message(code: 'tipp.price')}</th>
                <th>${message(code: 'issueEntitlement.hasPerpetualAccess.label')}</th>
            </g:each>
        </tr>
        </thead>
        <tbody>
        <g:each in="${ies}" var="ie">
            <%
                TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) genericOIDService.resolveOID(ie.getKey())
            %>
            <tr>
                <td>
                    <!-- START TEMPLATE -->
                    <g:render template="/templates/title"
                              model="${[ie: null, tipp: tipp, apisources: ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true),
                                        showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
                    <!-- END TEMPLATE -->
                </td>
                <g:each in="${objects}" var="object">
                    <g:set var="ieValues" value="${ie.getValue()}"/>
                    <g:if test="${ieValues.containsKey(object)}">
                            <g:each var="ieValue" in="${ieValues.get(object)}">
                                <td class="coverageStatements la-tableCard" >

                                    <g:render template="/templates/tipps/coverages" model="${[ie: ieValue, tipp: ieValue.tipp]}"/>

                                </td>
                                <td>
                                    <!-- von --->
                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ieValue.accessStartDate}"/>
                                    <semui:dateDevider/>
                                    <!-- bis -->
                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ieValue.accessEndDate}"/>
                                </td>
                                <td>
                                    <g:if test="${ieValue.priceItems}">
                                        <g:each in="${ieValue.priceItems}" var="priceItem" status="i">
                                            <g:message code="tipp.price.listPrice"/>: <semui:xEditable field="listPrice"
                                                                                                 owner="${priceItem}"
                                                                                                 format=""/> <semui:xEditableRefData
                                                field="listCurrency" owner="${priceItem}"
                                                config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                                            <g:message code="tipp.price.localPrice"/>: <semui:xEditable field="localPrice"
                                                                                                  owner="${priceItem}"/> <semui:xEditableRefData
                                                field="localCurrency" owner="${priceItem}"
                                                config="Currency"/> <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                                        <%--<semui:xEditable field="startDate" type="date"
                                                         owner="${priceItem}"/><semui:dateDevider/><semui:xEditable
                                            field="endDate" type="date"
                                            owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                                            <g:if test="${i < ieValue.priceItems.size() - 1}"><hr></g:if>
                                        </g:each>
                                    </g:if>
                                </td>
                                <td>
                                    ${message(code: 'issueEntitlement.hasPerpetualAccess.label') + ':'}  <semui:xEditableBoolean owner="${ieValue}" field="hasPerpetualAccess"/>
                                </td>
                            </g:each>
                    </g:if><g:else>
                    <td class="center aligned" colspan="3">
                        <a class="ui circular label la-popup-tooltip la-delay"
                           data-content="<g:message code="default.compare.propertyNotSet"/>"><strong>–</strong></a>
                    </td>
                </g:else>
                </g:each>
            </tr>
        </g:each></tbody>
    </table>
</semui:form>
