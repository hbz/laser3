<%@ page import="de.laser.survey.SurveyInfo; de.laser.utils.AppUtils; de.laser.convenience.Marker; java.time.temporal.ChronoUnit; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.survey.SurveyResult; de.laser.Subscription; de.laser.PersonRole; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.ReaderNumber; de.laser.Contact; de.laser.auth.User; de.laser.auth.Role; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.SubscriptionsQueryService; de.laser.storage.RDConstants; de.laser.storage.RDStore; java.text.SimpleDateFormat; de.laser.License; de.laser.Org; de.laser.OrgRole; de.laser.OrgSetting; de.laser.Vendor; de.laser.remote.ApiSource; de.laser.AlternativeName; de.laser.RefdataCategory;" %>
<laser:serviceInjection/>

<table id="${tableID ?: ''}" class="ui sortable celled la-js-responsive-table la-table table ${fixedHeader ?: ''}">
    <thead>
        <tr>
            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <th>${message(code: 'sidewide.number')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                    <g:sortableColumn title="${message(code: 'org.sortname.label')}" property="lower(o.sortname)" params="${request.getParameterMap()}"/>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <g:sortableColumn title="${message(code: 'org.fullName.label')}" property="lower(o.name)" params="${request.getParameterMap()}"/>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                    <th>${message(code: 'org.isWekbCurated.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                    <th>${message(code: 'default.status.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                    <th class="la-th-wrap">${message(code: 'org.subscriptions.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                    <th>${message(code: 'platform')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                    <th class="center aligned">
                        <ui:markerIcon type="WEKB_CHANGES" />
                    </th>
                </g:if>

            </g:each>
        </tr>
    </thead>
    <tbody>
        <g:each in="${vendorList}" var="vendor" status="i">

            <g:each in="${tmplConfigShow}" var="tmplConfigItem">

                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}<br />
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                    <td>
                        ${vendor.sortname}
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <th scope="row" class="la-th-column la-main-object">
                        <div class="la-flexbox">
                            <g:link controller="vendor" action="show" id="${vendor.id}">
                                ${vendor.name}
                            </g:link>
                        </div>
                    </th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                    <td class="center aligned">
                        <ui:wekbButtonLink type="vendor" gokbId="${vendor.gokbId}" />
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                    <td class="center aligned">
                        <g:if test="${vendor.status == RDStore.VENDOR_STATUS_CURRENT}">
                            <span class="la-popup-tooltip la-delay" data-position="top right">
                                <i class="ui icon green circle"></i>
                            </span>
                        </g:if>
                        <g:if test="${vendor.status == RDStore.VENDOR_STATUS_RETIRED}">
                            <span class="la-popup-tooltip la-delay" data-position="top right" <g:if test="${vendor.retirementDate}">data-content="<g:message code="org.retirementDate.label"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${vendor.retirementDate}"/>"</g:if>>
                                <i class="ui icon yellow circle"></i>
                            </span>
                        </g:if>
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                    <td>
                        <g:if test="${currentSubscriptions}">
                            <ul class="la-simpleList">
                                <g:each in="${currentSubscriptions.get(vendor.id)}" var="sub">
                                    <li><g:link controller="subscription" action="show" id="${sub.id}">${sub}</g:link></li>
                                </g:each>
                            </ul>
                        </g:if>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                    <td class="center aligned">
                        <g:if test="${vendor.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
    %{--                        <ui:markerIcon type="WEKB_CHANGES" color="purple" />--}%
                            <ui:cbItemMarkerAction package="${vendor}" simple="true"/>
                        </g:if>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                    <td class="center aligned">
                        <g:if test="${currentVendorIdList && (vendor.id in currentVendorIdList)}">
                            <span class="la-popup-tooltip la-delay" data-content="${message(code: 'menu.my.vendors')}"><i class="icon yellow star"></i></span>
                        </g:if>
                    </td>
                </g:if>

            </g:each><!-- tmplConfigShow -->
            </tr>
        </g:each><!-- vendorList -->
    </tbody>
</table>