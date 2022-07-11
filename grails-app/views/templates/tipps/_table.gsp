<%@ page import="de.laser.remote.ApiSource; de.laser.Platform; de.laser.titles.BookInstance" %>

<table class="ui sortable celled la-js-responsive-table la-table table ignore-floatThead la-bulk-header">
    <thead>
    <tr>
        <th></th>
        <g:sortableColumn class="ten wide" params="${params}" property="tipp.sortname"
                          title="${message(code: 'title.label')}"/>
        <th class="two wide">${message(code: 'tipp.coverage')}</th>
        <th class="two wide">${message(code: 'tipp.access')}</th>
        <th class="two wide">${message(code: 'tipp.price')}</th>
    </tr>
    <tr>
        <th colspan="2" rowspan="2"></th>
        <th>${message(code: 'default.from')}</th>
        <th>${message(code: 'default.from')}</th>
    </tr>
    <tr>
        <th>${message(code: 'default.to')}</th>
        <th>${message(code: 'default.to')}</th>
    </tr>
    </thead>
    <tbody>

    <g:set var="counter" value="${(offset ?: 0) + 1}"/>
    <g:each in="${tipps}" var="tipp">
        <tr>
            <td>${counter++}</td>
            <td>
                <!-- START TEMPLATE -->
                <laser:render template="/templates/title_short"
                          model="${[ie: null, tipp: tipp,
                                    showPackage: showPackage, showPlattform: showPlattform, showCompact: true, showEmptyFields: false]}"/>
                <!-- END TEMPLATE -->
            </td>

            <td class="coverageStatements la-js-responsive-table la-tableCard">

                <laser:render template="/templates/tipps/coverages" model="${[ie: null, tipp: tipp]}"/>

            </td>
            <td>
                <!-- von -->
                <g:formatDate date="${tipp.accessStartDate}" format="${message(code: 'default.date.format.notime')}"/>
                <ui:dateDevider/>
                <!-- bis -->
                <g:formatDate date="${tipp.accessEndDate}" format="${message(code: 'default.date.format.notime')}"/>
            </td>
            <td>
                <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                    <g:message code="tipp.price.listPrice"/>: <ui:xEditable field="listPrice"
                                                                         owner="${priceItem}"
                                                                         format=""/> <ui:xEditableRefData
                        field="listCurrency" owner="${priceItem}"
                        config="Currency"/> <%--<g:formatNumber number="${priceItem.listPrice}" type="currency" currencyCode="${priceItem.listCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%><br/>
                <%--<g:formatNumber number="${priceItem.localPrice}" type="currency" currencyCode="${priceItem.localCurrency.value}" currencySymbol="${priceItem.listCurrency.value}"/>--%>
                <%--<ui:xEditable field="startDate" type="date"
                                 owner="${priceItem}"/><ui:dateDevider/><ui:xEditable
                    field="endDate" type="date"
                    owner="${priceItem}"/>  <g:formatDate format="${message(code:'default.date.format.notime')}" date="${priceItem.startDate}"/>--%>
                    <g:if test="${i < tipp.priceItems.size() - 1}"><hr></g:if>
                </g:each>
            </td>
        </tr>

    </g:each>

    </tbody>

</table>