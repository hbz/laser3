<%@ page import="de.laser.titles.BookInstance; de.laser.remote.ApiSource; de.laser.Subscription; de.laser.Package; de.laser.RefdataCategory; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyShow.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>

    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
    <ui:xEditable owner="${surveyInfo}" field="name"/>
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>



<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<ui:messages data="${flash}"/>

<br />

<div class="sixteen wide column">

    <div class="row">
        <div class="column">

            <g:if test="${entitlements && entitlements.size() > 0}">

                <g:if test="${subscription.packages.size() > 1}">
                    <a class="ui right floated button" data-href="#showPackagesModal" data-ui="modal"><g:message
                            code="subscription.details.details.package.label"/></a>
                </g:if>

                <g:if test="${subscription.packages.size() == 1}">
                    <g:link class="ui right floated button" controller="package" action="show"
                            id="${subscription.packages[0].pkg.id}"><g:message
                            code="subscription.details.details.package.label"/></g:link>
                </g:if>
            </g:if>
            <g:else>
                ${message(code: 'subscription.details.no_ents')}
            </g:else>

        </div>
    </div><!--.row-->

    <br>
    <br>

    <div class="la-inline-lists">

        <div class="ui icon positive message">
            <i class="info icon"></i>

            <div class="content">
                <div class="header"></div>

                <p>
                    <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                    <g:message code="showSurveyInfo.pickAndChoose.Package"/>
                </p>
                <br/>
                <g:link controller="subscription" class="ui button" action="index" target="_blank"
                        id="${surveyConfig.subscription.id}">
                    ${surveyConfig.subscription.name} (${surveyConfig.subscription.status.getI10n('value')})
                </g:link>

                <g:link controller="subscription" class="ui button" action="linkPackage" target="_blank"
                        id="${surveyConfig.subscription.id}">
                    <g:message code="subscription.details.linkPackage.label"/>
                </g:link>

                <g:link controller="subscription" class="ui button" action="addEntitlements" target="_blank"
                        id="${surveyConfig.subscription.id}">
                    <g:message code="subscription.details.addEntitlements.label"/>
                </g:link>

            </div>

        </div>


        <div class="row">
            <div class="column">
                <laser:render template="/templates/filter/tipp_ieFilter"/>
            </div>
        </div>

        <div class="row">
            <div class="column">

                <div class="ui blue large label"><g:message code="title.plural"/>: <div class="detail">${num_ies_rows}</div>
                </div>

                <g:set var="counter" value="${offset + 1}"/>

                <table class="ui sortable celled la-js-responsive-table la-table table la-ignore-fixed la-bulk-header">
                    <thead>
                    <tr>

                        <th>${message(code: 'sidewide.number')}</th>
                        <g:sortableColumn class="eight wide" params="${params}" property="tipp.sortname"
                                          title="${message(code: 'title.label')}"/>
                        <th class="one wide">${message(code: 'subscription.details.print-electronic')}</th>
                        <th class="four wide">${message(code: 'default.date.label')}</th>
                        <th class="two wide">${message(code: 'subscription.details.access_dates')}</th>
                        <th class="two wide"><g:message code="subscription.details.prices"/></th>
                        <th class="one wide"></th>
                    </tr>
                    <tr>
                        <th rowspan="2" colspan="3"></th>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="startDate"
                                          title="${message(code: 'default.from')}"/>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}"
                                          property="accessStartDate"
                                          title="${message(code: 'default.from')}"/>

                        <th rowspan="2" colspan="2"></th>
                    </tr>
                    <tr>
                        <g:sortableColumn class="la-smaller-table-head" property="endDate"
                                          title="${message(code: 'default.to')}"/>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}"
                                          property="accessEndDate"
                                          title="${message(code: 'default.to')}"/>
                    </tr>
                    <tr>
                        <th colspan="9"></th>
                    </tr>
                    </thead>
                    <tbody>

                    <g:if test="${entitlements}">

                        <g:each in="${entitlements}" var="ie">
                            <tr>

                                <td>${counter++}</td>
                                <td>
                                    <!-- START TEMPLATE -->
                                    <laser:render template="/templates/title_short"
                                              model="${[ie: ie, tipp: ie.tipp,
                                                        showPackage: true, showPlattform: true, showCompact: true, showEmptyFields: false, overwriteEditable: false]}"/>
                                    <!-- END TEMPLATE -->
                                </td>

                                <td>
                                    <ui:xEditableRefData owner="${ie}" field="medium"
                                                            config="${de.laser.storage.RDConstants.IE_MEDIUM}"
                                                            overwriteEditable="${false}"/>
                                </td>
                                <td class="coverageStatements la-tableCard" data-entitlement="${ie.id}">

                                    <laser:render template="/templates/tipps/coverages"
                                              model="${[ie: ie, tipp: ie.tipp, overwriteEditable: false]}"/>

                                </td>
                                <td>
                                    <!-- von --->

                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ie.accessStartDate}"/>

                                    <ui:dateDevider/>
                                    <!-- bis -->

                                    <g:formatDate format="${message(code: 'default.date.format.notime')}"
                                                  date="${ie.accessEndDate}"/>

                                </td>
                                <td>
                                    <g:if test="${ie.priceItems}">
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
                                    </g:if>

                                </td>
                                <td class="x">

                                </td>
                            </tr>
                        </g:each>
                    </g:if>
                    </tbody>
                </table>

            </div>
        </div><!--.row-->
    </div>
</div>
<g:if test="${entitlements}">
    <ui:paginate action="surveyTitles" controller="survey" params="${params}"
                    max="${max}" total="${num_ies_rows}"/>
</g:if>

<div id="magicArea"></div>

<ui:modal id="showPackagesModal" message="subscription.packages.label" hideSubmitButton="true">
    <div class="ui ordered list">
        <g:each in="${subscription.packages.sort { it.pkg.name.toLowerCase() }}" var="subPkg">
            <div class="item">
                ${subPkg.pkg.name}
                <g:if test="${subPkg.pkg.contentProvider}">
                    (${subPkg.pkg.contentProvider.name})
                </g:if>:
                <g:link controller="package" action="show" id="${subPkg.pkg.id}"><g:message
                        code="subscription.details.details.package.label"/></g:link>
            </div>
        </g:each>
    </div>

</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#finishProcess').progress();
</laser:script>

<laser:htmlEnd />
