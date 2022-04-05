<%@ page import="de.laser.reporting.report.ElasticSearchHelper; de.laser.IdentifierNamespace; de.laser.reporting.report.GenericHelper; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.TitleInstancePackagePlatform; de.laser.helper.DateUtils; de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.storage.RDStore; de.laser.reporting.report.myInstitution.base.BaseDetails;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/top" />

<g:set var="filterCache" value="${GlobalExportHelper.getFilterCache(token)}"/>
<g:set var="esRecords" value="${filterCache.data.packageESRecords ?: [:]}"/>
<g:set var="esRecordIds" value="${esRecords.keySet().collect{Long.parseLong(it)}}"/>
<g:set var="wekb" value="${ElasticSearchHelper.getCurrentApiSource()}"/>

<div class="ui segment" id="reporting-detailsTable">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <%
                    String key = GlobalExportHelper.getCachedExportStrategy(token)
                    Map<String, Map> dtConfig = BaseConfig.getCurrentConfigDetailsTable( key )
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
            <g:each in="${list}" var="pkg" status="i">
                <tr>
                    <td>${i + 1}.</td>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="name">

                        <g:link controller="package" action="show" id="${pkg.id}" target="_blank">${pkg.name}</g:link>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="sortname">

                        <g:if test="${pkg.sortname}">
                            <g:link controller="package" action="show" id="${pkg.id}" target="_blank">${pkg.sortname}</g:link>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-altname">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            ${esRecords.get(pkg.id as String).altname?.join(', ')}
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-id">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <%
                                print esRecords.get(pkg.id as String).identifiers.collect { identifier ->
                                    IdentifierNamespace ns = IdentifierNamespace.findByNsAndNsType(identifier.namespace, 'de.laser.Package')
                                    String namespace = ns ? (ns.getI10n('name') ?: ns.ns) : GenericHelper.flagUnmatched(identifier.namespaceName ?: identifier.namespace)
                                    return namespace + ':' + identifier.value
                                }.join(',<br/>')
                            %>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="contentType">

                        ${pkg.contentType?.getI10n('value')}
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="packageStatus">

                        ${pkg.packageStatus?.getI10n('value')}
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="orProvider">

                        <g:each in="${pkg.orgs.findAll{ it.roleType in [ RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER ]}}" var="ro">
                            <g:link controller="org" action="show" id="${ro.org.id}" target="_blank">${ro.org.sortname ?: ro.org.name}</g:link><br />
                        </g:each>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="nominalPlatform">

                        <g:if test="${pkg.nominalPlatform}">
                            <g:link controller="platform" action="show" id="${pkg.nominalPlatform.id}" target="_blank">${pkg.nominalPlatform.name}</g:link>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="file">

                        ${pkg.file?.getI10n('value')}
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="_+_currentTitles">

                        <%
                            List tipps = TitleInstancePackagePlatform.executeQuery(
                                    'select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkg and tipp.status = :status',
                                    [pkg: pkg, status: RDStore.TIPP_STATUS_CURRENT]
                            )
                            println tipps[0] > 0 ? tipps[0] : ''
                        %>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-breakable">

                        <laser:reportDetailsTableEsValue key="${key}" id="${pkg.id}" field="breakable" records="${esRecords}" />
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-paymentType">

                        <laser:reportDetailsTableEsValue key="${key}" id="${pkg.id}" field="paymentType" records="${esRecords}" />
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-openAccess">

                        <laser:reportDetailsTableEsValue key="${key}" id="${pkg.id}" field="openAccess" records="${esRecords}" />
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-consistent">

                        <laser:reportDetailsTableEsValue key="${key}" id="${pkg.id}" field="consistent" records="${esRecords}" />
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-scope">

                        <laser:reportDetailsTableEsValue key="${key}" id="${pkg.id}" field="scope" records="${esRecords}" />
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-ddc">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:each in="${esRecords.get(pkg.id as String).ddcs}" var="ddc">
                                ${ RefdataValue.getByValueAndCategory(ddc.value as String, RDConstants.DDC)?.getI10n('value') ?: GenericHelper.flagUnmatched( ddc.value_de ) } <br />
                            </g:each>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-nationalRange">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:each in="${esRecords.get(pkg.id as String).nationalRanges}" var="nationalRange">
                                ${ RefdataValue.getByValueAndCategory(nationalRange.value as String, RDConstants.COUNTRY)?.getI10n('value') ?: GenericHelper.flagUnmatched( nationalRange.value ) } <br />
                            </g:each>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-regionalRange">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:each in="${esRecords.get(pkg.id as String).regionalRanges}" var="regionalRange">
                                ${ RefdataValue.getByValueAndCategory(regionalRange.value as String, RDConstants.REGIONS_DE)?.getI10n('value') ?: GenericHelper.flagUnmatched( regionalRange.value ) } <br />
                            </g:each>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-language">

                        <g:each in="${pkg.languages}" var="lang">
                            ${lang.language.getI10n('value')} <br />
                        </g:each>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-description">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            ${esRecords.get(pkg.id as String).description}
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-descriptionURL">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:set var="descriptionURL" value="${esRecords.get(pkg.id as String).descriptionURL}" />
                            <g:if test="${descriptionURL}">
                                <a href="${descriptionURL}" target="_blank">${descriptionURL}</a>
                            </g:if>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-curatoryGroup">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:each in="${esRecords.get(pkg.id as String).curatoryGroups}" var="curatoryGroup">
                                <%
                                    String cgType
                                    if (curatoryGroup.type) {
                                        cgType = RefdataValue.getByValueAndCategory(curatoryGroup.type as String, RDConstants.ORG_TYPE)?.getI10n('value') ?: GenericHelper.flagUnmatched( curatoryGroup.type )
                                        cgType = '(' + cgType + ')'
                                    }
                                %>
                                ${curatoryGroup.name} ${cgType}<br />
                            </g:each>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="package-x-archivingAgency">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:each in="${esRecords.get(pkg.id as String).packageArchivingAgencies}" var="archivingAgency">
                                ${archivingAgency.archivingAgency}<br />
                            </g:each>
                        </g:if>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="_+_lastUpdated">

                        <g:if test="${esRecordIds.contains(pkg.id)}">
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${DateUtils.parseDateGeneric(esRecords.getAt(pkg.id.toString()).lastUpdatedDisplay)}" />
                        </g:if>
                        <g:else>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkg._getCalculatedLastUpdated()}" />
                        </g:else>
                    </laser:reportDetailsTableTD>

                    <laser:reportDetailsTableTD config="${dtConfig}" field="_+_wekb">

                        <g:if test="${wekb?.baseUrl && pkg.gokbId}">
                            <g:if test="${esRecordIds.contains(pkg.id)}">
                                <a href="${wekb.baseUrl + '/public/packageContent/' + pkg.gokbId}" target="_blank"><i class="icon external alternate"></i></a>
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

<g:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />
