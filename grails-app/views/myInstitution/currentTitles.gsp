<%@ page import="de.laser.storage.RDStore; de.laser.IssueEntitlement;de.laser.Platform; de.laser.remote.ApiSource;" %>
<laser:htmlStart message="myinst.currentTitles.label" />

<ui:breadcrumbs>
    <ui:crumb message="myinst.currentTitles.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [format: 'csv']}">
                    <g:message code="default.button.exports.csv"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles" params="${params + [format: 'csv']}">CSV Export</g:link>
            </g:else>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [exportXLSX: true]}">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles" params="${params + [exportXLSX: true]}">
                    <g:message code="default.button.exports.xls"/>
                </g:link>
            </g:else>
        </ui:exportDropdownItem>
        <ui:exportDropdownItem>
            <g:if test="${filterSet}">
                <g:link class="item js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                        data-confirm-term-how="ok" controller="myInstitution" action="currentTitles"
                        params="${params + [exportKBart: true]}">
                    KBART Export
                </g:link>
            </g:if>
            <g:else>
                <g:link class="item" action="currentTitles"
                        params="${params + [exportKBart: true]}">KBART Export</g:link>
            </g:else>
        </ui:exportDropdownItem>
    <%--<ui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'json']}">JSON Export</g:link>
    </ui:exportDropdownItem>
    <ui:exportDropdownItem>
        <g:link class="item" action="currentTitles" params="${params + [format:'xml']}">XML Export</g:link>
    </ui:exportDropdownItem>--%>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="myinst.currentTitles.label" total="${num_ti_rows}" floated="true" />

<ui:messages data="${flash}"/>

<ui:filter showFilterButton="true" addFilterJs="true">
    <g:form id="filtering-form" action="currentTitles" controller="myInstitution" method="get" class="ui form">

        <g:set var="filterSub" value="${params.filterSub ? params.list('filterSub') : "all"}"/>
        <g:set var="filterPvd" value="${params.filterPvd ? params.list('filterPvd') : "all"}"/>
        <g:set var="filterHostPlat" value="${params.filterHostPlat ? params.list('filterHostPlat') : "all"}"/>


        <div class="two fields">
            <div class="field">
                <label>${message(code: 'default.search.text')}</label>
                <input type="hidden" name="sort" value="${params.sort}">
                <input type="hidden" name="order" value="${params.order}">
                <input type="text" name="filter" value="${params.filter}" style="padding-left:5px;"
                       placeholder="${message(code: 'default.search.ph')}"/>
            </div>

            <ui:datepicker label="myinst.currentTitles.subs_valid_on" id="validOn" name="validOn"
                              value="${validOn}"/>

        </div>

        <div class="two fields">
            <div class="field">
                <label for="filterSub">${message(code: 'subscription.plural')}</label>
                <select id="filterSub" name="filterSub" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterSub.contains("all")) ? ' selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_subs')}</option>
                    <g:each in="${subscriptions}" var="s">
                        <option <%=(filterSub.contains(s.id.toString())) ? 'selected="selected"' : ''%> value="${s.id}"
                                                                                                        title="${s.dropdownNamingConvention(institution)}">
                            ${s.dropdownNamingConvention(institution)}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="filterPvd">${message(code: 'default.agency.provider.plural.label')}</label>
                <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterPvd.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_providers')}</option>
                    <g:each in="${providers}" var="p">
                        <%
                            def pvdId = p[0].toString()
                            def pvdName = p[1]
                        %>
                        <option <%=(filterPvd.contains(pvdId)) ? 'selected' : ''%> value="${pvdId}" title="${pvdName}">
                            ${pvdName}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="two fields">
            <div class="field">
                <label for="filterPvd">${message(code: 'default.host.platforms.label')}</label>
                <select name="filterHostPlat" multiple="" class="ui search selection fluid dropdown">
                    <option <%--<%= (filterHostPlat.contains("all")) ? 'selected' : '' %>--%>
                            value="">${message(code: 'myinst.currentTitles.all_host_platforms')}</option>
                    <g:each in="${hostplatforms}" var="hp">
                        <%
                            def hostId = hp[0].toString()
                            def hostName = hp[1]
                        %>
                        <option <%=(filterHostPlat.contains(hostId)) ? 'selected' : ''%> value="${hostId}"
                                                                                         title="${hostName}">
                            ${hostName}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
                <input type="hidden" name="filterSet" value="true"/>
                <input type="submit" class="ui primary button"
                       value="${message(code: 'default.button.filter.label')}"/>
            </div>

            <%--<div class="field">
                <label for="filterPvd">${message(code: 'default.all_other.platforms.label')}</label>
                <select name="filterOtherPlat" multiple="" class="ui search selection fluid dropdown">
                    <option <%= (filterOtherPlat.contains("all")) ? 'selected' : '' %>
                            value="">${message(code: 'myinst.currentTitles.all_other_platforms')}</option>
                    <g:each in="${otherplatforms}" var="op">

                        <%
                            def platId = op[0].id.toString()
                            def platName = op[0].name
                        %>
                        <option <%=(filterOtherPlat.contains(platId)) ? 'selected' : ''%> value="${platId}"
                                                                                          title="${platName}">
                            ${platName}
                        </option>
                    </g:each>
                </select>
            </div>--%>
        </div>

    <%--<div class="two fields">

    <%-- class="field">
        <label for="filterMultiIE">${message(code: 'myinst.currentTitles.dupes')}</label>

        <div class="ui checkbox">
            <input type="checkbox" class="hidden" name="filterMultiIE" id="filterMultiIE"
                   value="${true}" <%=(params.filterMultiIE) ? ' checked="true"' : ''%>/>
        </div>
    </div>
    </div>--%>

    </g:form>
</ui:filter>

<div class="la-clear-before">
    <div>
        <div>
            <g:if test="${titles}">
                <g:set var="counter" value="${offset + 1}"/>
                <table class="ui sortable celled la-js-responsive-table la-table table ">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <g:sortableColumn params="${params}" property="tipp.sortname"
                                          title="${message(code: 'title.label')}"/>
                        <th style="width: 50%">
                            <div class="ui three column grid">
                                <div class="sixteen wide column">
                                    ${message(code: 'myinst.currentTitles.sub_content')}
                                </div>

                                <div class="eight wide column">
                                    ${message(code: 'default.date.label')}
                                    <br/>
                                    ${message(code: 'default.from')}
                                    <br/>
                                    ${message(code: 'default.to')}
                                </div>

                                <div class="eight wide column">
                                    ${message(code: 'subscription.details.access_dates')}
                                    <br/>
                                    ${message(code: 'default.from')}
                                    <br/>
                                    ${message(code: 'default.to')}
                                </div>

                                <div class="sixteen wide column">
                                    <g:message code="subscription.details.prices"/>
                                </div>

                                <div class="eight wide column">
                                    <g:message code="issueEntitlement.perpetualAccessBySub.label"/>
                                </div>
                            </div>
                        </th>
                    </tr>
                    </thead>
                    <g:each in="${titles}" var="tipp" status="jj">
                        <tr>
                            <td>${(params.int('offset') ?: 0) + jj + 1}</td>
                            <td>
                                <!-- START TEMPLATE -->
                                <laser:render template="/templates/title_short"
                                          model="${[ie         : null, tipp: tipp,
                                                    showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false]}"/>
                                <!-- END TEMPLATE -->

                            </td>
                            <%
                                String instanceFilter = ''
                                if (institution.getCustomerType() == "ORG_CONSORTIUM")
                                    instanceFilter += ' and sub.instanceOf = null'
                                Set<IssueEntitlement> title_coverage_info = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.org = :context and ie.tipp = :tipp and sub.status = :current and ie.status != :ieStatus' + instanceFilter, [ieStatus: RDStore.TIPP_STATUS_REMOVED, context: institution, tipp: tipp, current: RDStore.SUBSCRIPTION_CURRENT])
                            %>
                            <td>

                                <div class="ui three column grid">
                                    <g:each in="${title_coverage_info}" var="ie">
                                        <div class="sixteen wide column">
                                            <i class="icon clipboard outline la-list-icon"></i>
                                            <g:link controller="subscription" action="index"
                                                    id="${ie.subscription.id}">${ie.subscription.dropdownNamingConvention(institution)}</g:link>
                                            &nbsp;
                                            <br/>
                                            <g:link controller="issueEntitlement" action="show"
                                                    id="${ie.id}">${message(code: 'myinst.currentTitles.full_ie')}</g:link>
                                            <br/>
                                        </div>

                                        <div class="eight wide centered column coverageStatements la-tableCard">

                                            <laser:render template="/templates/tipps/coverages"
                                                      model="${[ie: ie, tipp: ie.tipp]}"/>

                                        </div>

                                        <div class="eight wide centered column">

                                        <!-- von --->
                                            <g:if test="${editable}">
                                                <ui:xEditable owner="${ie}" type="date" field="accessStartDate"/>
                                                <i class="grey question circle icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'subscription.details.access_start.note')}"></i>
                                            </g:if>
                                            <g:else>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.accessStartDate}"/>
                                            </g:else>
                                            <ui:dateDevider/>
                                        <!-- bis -->
                                            <g:if test="${editable}">
                                                <ui:xEditable owner="${ie}" type="date" field="accessEndDate"/>
                                                <i class="grey question circle icon la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'subscription.details.access_end.note')}"></i>
                                            </g:if>
                                            <g:else>
                                                <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                              date="${ie.accessEndDate}"/>
                                            </g:else>
                                        </div>

                                        <div class="sixteen wide column">
                                            <g:each in="${ie.priceItems}" var="priceItem" status="i">
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
                                                <g:if test="${i < ie.priceItems.size() - 1}"><hr></g:if>
                                            </g:each>
                                        </div>

                                        <div class="eight wide column">
                                            ${message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}
                                            <%
                                                if (ie.perpetualAccessBySub) {
                                                    println g.link([action: 'index', controller: 'subscription', id: ie.perpetualAccessBySub.id], "${RDStore.YN_YES.getI10n('value')}: ${ie.perpetualAccessBySub.dropdownNamingConvention()}")
                                                } else {
                                                    println RDStore.YN_NO.getI10n('value')
                                                }
                                            %>
                                        </div>
                                    </g:each>
                                </div>
                            </td>
                        </tr>
                    </g:each>

                </table>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br/><strong><g:message code="filter.result.empty.object"
                                            args="${[message(code: "title.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <br/><strong><g:message code="result.empty.object"
                                            args="${[message(code: "title.plural")]}"/></strong>
                </g:else>
            </g:else>
        </div>
    </div>


    <g:if test="${titles}">
        <ui:paginate action="currentTitles" controller="myInstitution" params="${params}"
                        max="${max}" total="${num_ti_rows}"/>
    </g:if>

</div>

<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>

<laser:htmlEnd />
