<%@ page import="grails.converters.JSON; de.laser.utils.DateUtils; de.laser.storage.RDStore; de.laser.Subscription; de.laser.SubscriptionPackage; de.laser.IssueEntitlement; de.laser.stats.Counter4ApiSource; de.laser.stats.Counter4Report; de.laser.stats.Counter5Report" %>
<laser:htmlStart message="subscription.details.stats.label" serviceInjection="true"/>

<g:set var="subjects" value="${controlledListService.getAllPossibleSubjectsBySub(subscription)}"/>
<g:set var="ddcs" value="${controlledListService.getAllPossibleDdcsBySub(subscription)}"/>
<g:set var="languages" value="${controlledListService.getAllPossibleLanguagesBySub(subscription)}"/>

        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <g:link class="item" action="stats" params="${params+[exportXLS:true, data: 'fetchAll']}">${message(code:'default.usage.exports.all')}</g:link>
                    <g:link class="item" action="stats" params="${params+[exportXLS:true, data: 'fetchFiltered']}">${message(code:'default.usage.exports.filtered')}</g:link>
                </ui:exportDropdownItem>
            </ui:exportDropdown>
            <laser:render template="actions" />
        </ui:controlButtons>
        <ui:h1HeaderWithIcon>
            <laser:render template="iconSubscriptionIsChild"/>
            ${subscription.name}
        </ui:h1HeaderWithIcon>
        <ui:anualRings object="${subscription}" controller="subscription" action="stats" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <laser:render template="nav" />

        <ui:objectStatus object="${subscription}" status="${subscription.status}" />
        <laser:render template="message" />
        <ui:messages data="${flash}" />
        <div class="ui icon info message">
            <i class="info icon"></i>
            <g:message code="default.usage.exports.warning"/>
        </div>
        <g:if test="${wekbServerUnavailable}">
            <div class="ui icon error message">
                <i class="exclamation icon"></i>
                ${wekbServerUnavailable}
            </div>
        </g:if>
        <g:else>
            <aside class="ui segment la-metabox accordion">
                <div class="title">
                    <g:message code="default.usage.platformMetadataHeader"/><i class="dropdown icon la-dropdown-accordion"></i>
                </div>
                <div class="content">
                    <g:each in="${platformInstanceRecords.values()}" var="platformInstanceRecord">
                        <h4>
                            ${platformInstanceRecord.name}
                        </h4>
                        <laser:render template="/templates/platformStatsDetails" model="[platformInstanceRecord: platformInstanceRecord]"/>
                    </g:each>
                </div>
            </aside>
            <div class="la-metabox-spacer"></div>
        </g:else>
        <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
            <div class="ui segment">
                <table class="ui celled table">
                    <tr>
                        <th><g:message code="default.usage.consortiaTableHeader"/></th>
                    </tr>
                    <g:each in="${Subscription.executeQuery('select new map(sub.id as memberSubId, org.sortname as memberName, org.globalUID as memberId, sub.startDate as startDate, sub.endDate as endDate) from OrgRole oo join oo.org org join oo.sub sub where sub.instanceOf = :parent and oo.roleType in (:subscrRoles) and exists (select sp.id from SubscriptionPackage sp where sp.subscription = sub) order by org.sortname asc', [parent: subscription, subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])}" var="row">
                        <tr>
                            <td>
                                <g:link action="stats" id="${row.memberSubId}">${row.memberName}
                                    <g:if test="${subscriptionService.areStatsAvailable(platforms, subscription.packages, [row.memberId], [startDate: row.startDate, endDate: row.endDate])}">
                                        <span class="la-popup-tooltip la-delay" data-content="${message(code: 'default.usage.statsAvailable')}"><i class="chart bar outline icon"></i></span>
                                    </g:if>
                                </g:link>
                            </td>
                        </tr>
                    </g:each>
                </table>
            </div>
        </g:if>
        <g:else>
            <ui:filter showFilterButton="true" addFilterJs="true">
                <g:form action="stats" class="ui form" method="get">
                    <g:hiddenField name="tab" value="${params.tab}"/>
                    <g:hiddenField name="id" value="${subscription.id}"/>
                    <g:hiddenField name="sort" value="${params.sort}"/>
                    <g:hiddenField name="order" value="${params.order}"/>
                    <div class="four fields">
                        <div class="field">
                            <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>

                            <select name="series_names" id="series_names" multiple=""
                                    class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${controlledListService.getAllPossibleSeriesBySub(subscription)}" var="seriesName">
                                    <option <%=(params.list('series_names')?.contains(seriesName)) ? 'selected="selected"' : ''%>
                                            value="${seriesName}">
                                        ${seriesName}
                                    </option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field">
                            <label for="subject_reference">${message(code: 'titleInstance.subjectReference.label')}</label>

                            <select name="subject_references" id="subject_reference" multiple=""
                                    class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${subjects}" var="subject">
                                    <option <%=(params.list('subject_references')?.contains(subject)) ? 'selected="selected"' : ''%>
                                            value="${subject}">
                                        ${subject}
                                    </option>
                                </g:each>
                            </select>
                        </div>

                        <div class="field">
                            <label for="ddc">${message(code: 'titleInstance.ddc.label')}</label>

                            <select name="ddcs" id="ddc" multiple=""
                                    class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${ddcs}" var="ddc">
                                    <option <%=(params.list('ddcs')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                            value="${ddc.id}">
                                        ${ddc.value} - ${ddc.getI10n("value")}
                                    </option>
                                </g:each>
                                <g:if test="${ddcs.size() == 0}">
                                    <option value="<g:message code="titleInstance.noDdc.label" />"><g:message code="titleInstance.noDdc.label" /></option>
                                </g:if>
                            </select>
                        </div>

                        <div class="field">
                            <label for="language">${message(code: 'titleInstance.language.label')}</label>

                            <select name="languages" id="language" multiple="multiple"
                                    class="ui search selection dropdown">
                                <option value="">${message(code: 'default.select.choose.label')}</option>

                                <g:each in="${languages}" var="language">
                                    <option <%=(params.list('languages')?.contains(language.id.toString())) ? 'selected="selected"' : ''%>
                                            value="${language.id}">
                                        ${language.getI10n("value")}
                                    </option>
                                </g:each>
                                <g:if test="${languages.size() == 0}">
                                    <option value="<g:message code="titleInstance.noLanguage.label" />"><g:message code="titleInstance.noLanguage.label" /></option>
                                </g:if>
                            </select>
                        </div>
                    </div>
                    <div class="four fields">
                        <div class="field">
                            <label for="reportType"><g:message code="default.usage.reportType"/></label>
                            <select name="reportType" id="reportType" multiple="multiple" class="ui search selection dropdown">
                                <option value=""><g:message code="default.select.choose.label"/></option>
                                <g:each in="${reportTypes}" var="reportType">
                                    <option <%=(params.list('reportType')?.contains(reportType)) ? 'selected="selected"' : ''%>
                                            value="${reportType}">
                                        <g:message code="default.usage.${reportType}"/>
                                    </option>
                                </g:each>
                                <g:if test="${reportTypes.size() == 0}">
                                    <option value="<g:message code="default.stats.noReport" />"><g:message code="default.stats.noReport" /></option>
                                </g:if>
                            </select>
                        </div>

                        <div class="field">
                            <label for="metricType"><g:message code="default.usage.metricType"/></label>
                            <select name="metricType" id="metricType" class="ui search selection dropdown">
                                <option value=""><g:message code="default.select.choose.label"/></option>
                                <g:each in="${metricTypes}" var="metricType">
                                    <option <%=(params.metricType == metricType) ? 'selected="selected"' : ''%>
                                            value="${metricType}">
                                        ${metricType}
                                    </option>
                                </g:each>
                                <g:if test="${metricTypes.size() == 0}">
                                    <option value="<g:message code="default.stats.noMetric" />"><g:message code="default.stats.noMetric" /></option>
                                </g:if>
                            </select>
                        </div>

                        <div class="field">
                            <g:if test="${accessTypes}">
                                <label for="accessType"><g:message code="default.usage.accessType"/></label>
                                <select name="accessType" id="accessType" class="ui search selection dropdown">
                                    <option value=""><g:message code="default.select.choose.label"/></option>
                                    <g:each in="${accessTypes}" var="accessType">
                                        <option <%=(params.accessType == accessType) ? 'selected="selected"' : ''%>
                                                value="${accessType}">
                                            ${accessType}
                                        </option>
                                    </g:each>
                                    <g:if test="${accessTypes.size() == 0}">
                                        <option value="<g:message code="default.stats.noAccess" />"><g:message code="default.stats.noAccess" /></option>
                                    </g:if>
                                </select>
                            </g:if>
                        </div>

                        <div class="field la-field-right-aligned">
                            <g:link action="stats" id="${subscription.id}" class="ui reset secondary button">${message(code: 'default.button.reset.label')}</g:link>
                            <input type="submit" class="ui primary button"
                                   value="${message(code: 'default.button.filter.label')}"/>
                        </div>
                    </div>
                </g:form>
            </ui:filter>
            <ui:tabs class="la-overflowX-auto">
                <ui:tabsItem controller="subscription" action="stats" params="${params + [tab: 'total']}" text="${message(code: 'default.usage.allUsageGrid.header')}" tab="total"/>
                <g:each in="${monthsInRing}" var="month">
                    <ui:tabsItem controller="subscription" action="stats" params="${params + [tab: DateUtils.getSDF_yyyyMM().format(month)]}" text="${DateUtils.getSDF_yyyyMM().format(month)}" tab="${DateUtils.getSDF_yyyyMM().format(month)}"/>
                </g:each>
            </ui:tabs>
            <div class="ui bottom attached tab active segment">
                <g:if test="${params.tab == 'total'}">
                    <table class="ui celled la-js-responsive-table la-table table">
                        <thead>
                            <tr>
                                <th><g:message code="default.date.label"/></th>
                                <th><g:message code="default.count.label"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${sums}" var="row">
                                <g:set var="reportMonth" value="${row.getKey()}"/>
                                <g:set var="sumsByReport" value="${row.getValue()}"/>
                                <g:each in="${sumsByReport}" var="sumByReport">
                                    <g:set var="sum" value="${sumByReport.getValue()}"/>
                                    <tr>
                                        <td><g:formatDate date="${reportMonth}" format="yyyy-MM"/></td>
                                        <g:set var="reportType" value="${sumByReport.getKey() in Counter4Report.COUNTER_4_REPORTS ? sumByReport.getKey() : sumByReport.getKey().toLowerCase()}"/>
                                        <td><g:link action="stats" params="${params + [tab: DateUtils.getSDF_yyyyMM().format(reportMonth), reportType: reportType, metricType: params.metricType]}">${sum}</g:link></td>
                                    </tr>
                                </g:each>
                            </g:each>
                        </tbody>
                    </table>
                </g:if>
                <g:else>
                    <table class="ui sortable celled la-js-responsive-table la-table table">
                        <thead>
                            <tr>
                                <g:if test="${usages && usages[0].title}">
                                    <g:sortableColumn title="${message(code:"default.title.label")}" property="title.name" params="${params}"/>
                                </g:if>
                                <g:sortableColumn title="${message(code:"default.count.label")}" property="r.reportCount" params="${params}"/>
                            </tr>
                        </thead>
                        <tbody>
                            <g:each in="${usages}" var="row">
                                <tr>
                                    <g:if test="${row.title}">
                                        <td>
                                            <g:link controller="tipp" action="show" id="${row.title.id}">${row.title.name}</g:link>
                                        </td>
                                    </g:if>
                                    <td>${row.reportCount}</td>
                                </tr>
                            </g:each>
                        </tbody>
                    </table>
                    <ui:paginate total="${total}" params="${params}" max="${max}" offset="${offset}"/>
                </g:else>
            </div>
        </g:else>
        <laser:script file="${this.getGroovyPageFileName()}">

            $("#reportType").on('change', function() {
                <g:applyCodec encodeAs="none">
                    let platforms = ${platformsJSON};
                </g:applyCodec>
                $.ajax({
                    url: "<g:createLink controller="ajaxJson" action="adjustMetricList"/>",
                    data: {
                        reportTypes: $(this).val(),
                        platforms: platforms,
                        customer: '${customer}'
                    }
                }).done(function(response){
                    let dropdown = '<option value=""><g:message code="default.select.choose.label"/></option>';
                    for(let i = 0; i < response.metricTypes.length; i++) {
                        if(i === 0)
                            dropdown += '<option selected="selected" value="'+response.metricTypes[i]+'">'+response.metricTypes[i]+'</option>';
                        else
                            dropdown += '<option value="'+response.metricTypes[i]+'">'+response.metricTypes[i]+'</option>';
                    }
                    $("#metricType").html(dropdown);
                });
            });
        </laser:script>
<laser:htmlEnd />
