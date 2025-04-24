<%@ page import="de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.GenericHelper; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.reporting.report.myInstitution.base.BaseDetails;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/details_top" />

<g:set var="filterCache" value="${GlobalExportHelper.getFilterCache(token)}"/>
<g:set var="esRecords" value="${filterCache.data.platformESRecords ?: [:]}"/>
<g:set var="esRecordIds" value="${esRecords.keySet().collect{Long.parseLong(it)} ?: []}"/>

<g:set var="useLocalFields" value="${false}"/>%{-- DEBUG --}%

<div class="ui segment" id="reporting-detailsTable">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <%
                    String key = GlobalExportHelper.getCachedExportStrategy(token)
                    Map<String, Map> dtConfig = BaseConfig.getCurrentConfigDetailsTable( key ).clone()

                    if (query != 'platform-x-property') { dtConfig.remove('_dtField_?_propertyLocal') }

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

                    <uiReporting:detailsTableTD config="${dtConfig}" field="name">

                        <g:link controller="platform" action="show" id="${plt.id}" target="_blank">${plt.name}</g:link>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="provider">

                        <g:if test="${plt.provider}">
                            <g:link controller="provider" action="show" id="${plt.provider.id}" target="_blank">${plt.provider.sortname ?: plt.provider.name}</g:link>
                        </g:if>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="primaryUrl">

                        <g:if test="${plt.primaryUrl}">
                            <a href="${plt.primaryUrl}" target="_blank">${plt.primaryUrl}</a>
                        </g:if>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="serviceProvider">

                        ${plt.serviceProvider?.getI10n('value')}
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="softwareProvider">

                        ${plt.softwareProvider?.getI10n('value')}
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="status">

                        ${plt.status?.getI10n('value')}
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-ipAuthentication">

                        <g:if test="${useLocalFields}">
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="ipAuthentication" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="ipAuthentication" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-shibbolethAuthentication">

                        <g:if test="${useLocalFields}">
                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_SHIBBOLETH.id}" />
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="shibbolethAuthentication" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-passwordAuthentication">

                        <g:if test="${useLocalFields}">
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="passwordAuthentication" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="passwordAuthentication" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-otherProxies">

                        <g:if test="${useLocalFields}">
                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_PROXY.id}" />
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="otherProxies" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-statisticsFormat">

                        <g:if test="${useLocalFields}">
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="statisticsFormat" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="statisticsFormat" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-statisticsUpdate">

                        <g:if test="${useLocalFields}">
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="statisticsUpdate" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="statisticsUpdate" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-counterCertified">

                        <g:if test="${useLocalFields}">
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="counterCertified" records="${esRecords}" /> (we:kb)
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="counterCertified" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-counterR4Supported">

                        <g:if test="${useLocalFields}">
                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R4_REPORTS.id}" />
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="counterR4Supported" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-counterR5Supported">

                        <g:if test="${useLocalFields}">
                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R5_REPORTS.id}" />
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="counterR5Supported" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-counterR4SushiApiSupported">

                        <g:if test="${useLocalFields}">
                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R4_SUSHI_API.id}" />
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="counterR4SushiApiSupported" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="platform-counterR5SushiApiSupported">

                        <g:if test="${useLocalFields}">
                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${PropertyStore.PLA_COUNTER_R5_SUSHI_API.id}" />
                        </g:if>
                        <g:else>
                            <uiReporting:detailsTableEsValue key="${key}" id="${plt.id}" field="counterR5SushiApiSupported" records="${esRecords}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="accessPlatform">

                        ${plt.accessPlatform?.getI10n('value')}
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="accessibilityStatementUrl">

                        <g:if test="${plt.accessibilityStatementUrl}">
                            <a href="${plt.accessibilityStatementUrl}" target="_blank">${plt.accessibilityStatementUrl}</a>
                        </g:if>
                    </uiReporting:detailsTableTD>

                    <g:if test="${dtConfig.containsKey('_dtField_?_propertyLocal')}">
                        <uiReporting:detailsTableTD config="${dtConfig}" field="_dtField_?_propertyLocal">

                            <uiReporting:objectProperties owner="${plt}" tenant="${contextService.getOrg()}" propDefId="${id}" />

                        </uiReporting:detailsTableTD>
                    </g:if>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="_dtField_lastUpdated">

                        <g:if test="${esRecordIds.contains(plt.id)}">
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${DateUtils.parseDateGeneric(esRecords.getAt(plt.id.toString()).lastUpdatedDisplay)}" />
                        </g:if>
                        <g:else>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${plt._getCalculatedLastUpdated()}" />
                        </g:else>
                    </uiReporting:detailsTableTD>

                    <uiReporting:detailsTableTD config="${dtConfig}" field="_dtField_wekb">

                        <g:if test="${plt.gokbId}">
                            <g:if test="${esRecordIds.contains(plt.id)}">
                                <ui:wekbIconLink type="platform" gokbId="${plt.gokbId}"/>
                            </g:if>
                            <g:else>
                                <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code:'reporting.chart.result.noCounterpart.label')}" data-position="top right">
                                    <i class="${Icon.SYM.NO} grey"></i>
                                </span>
                            </g:else>
                        </g:if>
                    </uiReporting:detailsTableTD>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />
