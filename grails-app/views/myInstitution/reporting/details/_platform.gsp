<%@ page import="de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.GenericHelper; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.reporting.report.myInstitution.base.BaseDetails;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/top" />

<g:set var="filterCache" value="${GlobalExportHelper.getFilterCache(token)}"/>
<g:set var="esRecords" value="${filterCache.data.platformESRecords ?: [:]}"/>
<g:set var="esRecordIds" value="${esRecords.keySet().collect{Long.parseLong(it)} ?: []}"/>
<g:set var="wekb" value="${ElasticSearchHelper.getCurrentApiSource()}"/>

<g:set var="useLocalFields" value="${false}"/>%{-- DEBUG --}%

<div class="ui segment" id="reporting-detailsTable">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <%
                    String key = GlobalExportHelper.getCachedExportStrategy(token)
                    Map<String, Map> dtConfig = BaseConfig.getCurrentConfigDetailsTable( key ).clone()

                    if (query != 'platform-x-property') { dtConfig.remove('_?_propertyLocal') }

                    String wekbProperty
                    if (query == 'platform-x-propertyWekb') {
                        if (params.id != null && params.id != 0) {
                            wekbProperty = GlobalExportHelper.getQueryCache(token).dataDetails.find { it.id == params.long('id') }.esProperty
                            if (wekbProperty && dtConfig[wekbProperty]) {
                                dtConfig[wekbProperty].dtc = true
                            }
                        }
                    }
                    dtConfig.remove(null) // ?????
                %>
                <th></th>
                <g:each in="${dtConfig}" var="k,b">
                    <g:set var="label" value="${ BaseDetails.getFieldLabelforColumn( key, k ) }" />

                    <g:if test="${b.dtc}">
                        <th data-column="dtc:${k}">${label}</th>
                    </g:if>
                    <g:else>
                        <th data-column="dtc:${k}" class="hidden">${label}</th>
                    </g:else>
                </g:each>
            </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="plt" status="i">
                <tr>
                    <td>${i + 1}.</td>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="name">

                        <g:link controller="platform" action="show" id="${plt.id}" target="_blank">${plt.name}</g:link>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="org">

                        <g:if test="${plt.org}">
                            <g:link controller="org" action="show" id="${plt.org.id}" target="_blank">${plt.org.sortname ?: plt.org.name}</g:link>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="primaryUrl">

                        <g:if test="${plt.primaryUrl}">
                            <a href="${plt.primaryUrl}" target="_blank">${plt.primaryUrl}</a>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="serviceProvider">

                        ${plt.serviceProvider?.getI10n('value')}
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="softwareProvider">

                        ${plt.softwareProvider?.getI10n('value')}
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="status">

                        ${plt.status?.getI10n('value')}
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-ipAuthentication">

                        <g:if test="${useLocalFields}">
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="ipAuthentication" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="ipAuthentication" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-shibbolethAuthentication">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_SHIBBOLETH.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="shibbolethAuthentication" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-passwordAuthentication">

                        <g:if test="${useLocalFields}">
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="passwordAuthentication" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="passwordAuthentication" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-proxySupported">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_PROXY.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="proxySupported" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-statisticsFormat">

                        <g:if test="${useLocalFields}">
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="statisticsFormat" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="statisticsFormat" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-statisticsUpdate">

                        <g:if test="${useLocalFields}">
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="statisticsUpdate" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="statisticsUpdate" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-counterCertified">

                        <g:if test="${useLocalFields}">
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterCertified" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterCertified" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-counterR3Supported">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R3_REPORTS.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterR3Supported" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-counterR4Supported">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R4_REPORTS.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterR4Supported" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-counterR5Supported">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R5_REPORTS.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterR5Supported" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-counterR4SushiApiSupported">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R4_SUSHI_API.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterR4SushiApiSupported" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="platform-counterR5SushiApiSupported">

                        <g:if test="${useLocalFields}">
                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R5_SUSHI_API.id}" />
                        </g:if>
                        <g:else>
                            <laser:reportDetailsTableEsValue key="${key}" id="${plt.id}" field="counterR5SushiApiSupported" records="${esRecords}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <g:if test="${dtConfig.containsKey('_?_propertyLocal')}">
                        <laser:reportDetailsTableTD config="${dtConfig}" field="_?_propertyLocal">

                            <laser:reportObjectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${id}" />

                        </laser:reportDetailsTableTD>
                    </g:if>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="_+_lastUpdated">

                        <g:if test="${esRecordIds.contains(plt.id)}">
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${DateUtils.parseDateGeneric(esRecords.getAt(plt.id.toString()).lastUpdatedDisplay)}" />
                        </g:if>
                        <g:else>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${plt._getCalculatedLastUpdated()}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="_+_wekb">

                        <g:if test="${wekb?.baseUrl && plt.gokbId}">
                            <g:if test="${esRecordIds.contains(plt.id)}">
                                <a href="${wekb.baseUrl + '/public/platformContent/' + plt.gokbId}" target="_blank"><i class="icon external alternate"></i></a>
                            </g:if>
                            <g:else>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'reporting.chart.result.noCounterpart.label')}"
                                      data-position="top right">
                                    <i class="icon times grey"></i>
                                </span>
                            </g:else>
                        </g:if>
                    </laser:reportDetailsTableTD>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />
