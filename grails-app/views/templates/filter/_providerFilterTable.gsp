<%@ page import="de.laser.wekb.Platform; de.laser.ui.Icon; de.laser.utils.AppUtils; de.laser.convenience.Marker; java.time.temporal.ChronoUnit; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.survey.SurveyResult; de.laser.Subscription; de.laser.addressbook.PersonRole; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.ReaderNumber; de.laser.addressbook.Contact; de.laser.auth.User; de.laser.auth.Role; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.SubscriptionsQueryService; de.laser.storage.RDConstants; de.laser.storage.RDStore; java.text.SimpleDateFormat; de.laser.License; de.laser.Org; de.laser.OrgRole; de.laser.OrgSetting; de.laser.wekb.Vendor; de.laser.AlternativeName; de.laser.RefdataCategory;" %>
<laser:serviceInjection/>

<table id="${tableID ?: ''}" class="ui sortable celled la-js-responsive-table la-table table ${fixedHeader ?: ''}">
    <thead>
    <tr>
        <g:if test="${tmplShowCheckbox}">
            <th>
                <g:if test="${providerList}">
                    <g:checkBox name="providerListToggler" id="providerListToggler" checked="${allChecked ? 'true' : 'false'}"/>
                </g:if>
            </th>
        </g:if>

        <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">

            <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                <th>${message(code: 'sidewide.number')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                <th>${message(code: 'default.shortname.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <g:sortableColumn title="${message(code: 'org.fullName.label')}" property="lower(p.name)" params="${request.getParameterMap()}"/>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('altname')}">
                <th>${message(code: 'altname.plural')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                <th>${message(code: 'org.isWekbCurated.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                <th>${message(code: 'org.subscriptions.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                <th>${message(code: 'default.status.label')}</th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                <th>${message(code: 'platform')}</th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                <th class="center aligned">
                    <ui:markerIcon type="WEKB_CHANGES" />
                </th>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                <th class="center aligned">
                    <g:if test="${actionName == 'listProvider'}">
                        <ui:myXIcon tooltip="${message(code: 'menu.my.providers')}" />
                    </g:if>
                </th>
            </g:if>

        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:each in="${providerList}" var="provider" status="i">
        <tr <g:if test="${tmplShowCheckbox && currProvSharedLinks.get(provider.id) == true}">class="disabled"</g:if>>
        <g:if test="${tmplShowCheckbox}">
            <td>
                <g:if test="${currProvSharedLinks.get(provider.id) == true}">
                    <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                </g:if>
                <g:else>
                    <g:checkBox id="selectedProviders_${provider.id}" name="selectedProviders" value="${provider.id}" checked="${provider.id in currProviders ? 'true' : 'false'}"/>
                </g:else>
            </td>
        </g:if>

        <g:each in="${tmplConfigShow}" var="tmplConfigItem">

            <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}<br />
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                <td>
                    ${provider.sortname}
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                <th scope="row" class="la-th-column la-main-object">
                    <div class="la-flexbox">
                        <g:link controller="provider" action="show" id="${provider.id}">
                            ${provider.name}
                        </g:link>
                    </div>
                </th>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('altname')}">
                <%
                    SortedSet<String> altnames = new TreeSet<String>()
                    altnames.addAll(provider.altnames.name)
                %>
                <td>
                    <ul class="la-simpleList">
                        <g:each in="${altnames}" var="altname" status="a">
                            <g:if test="${a < 10}">
                                <li>${altname}</li>
                            </g:if>
                        </g:each>
                    </ul>
                    <g:if test="${altnames.size() > 10}">
                        <div class="ui accordion">
                            <div class="title"><g:message code="default.further"/><i class="dropdown icon"></i></div>
                            <div class="content">
                                <ul class="la-simpleList">
                                    <g:each in="${altnames.drop(10)}" var="altname">
                                        <li>${altname}</li>
                                    </g:each>
                                </ul>
                            </div>
                        </div>
                    </g:if>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                <td class="center aligned">
                    <g:if test="${provider.gokbId != null}">
                        <ui:wekbButtonLink type="org" gokbId="${provider.gokbId}" />
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                <td>
                    <%
                        //to controller!
                        (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(
                                [provider: provider.id, actionName: actionName, status: RDStore.SUBSCRIPTION_CURRENT.id, date_restr: params.subValidOn ? DateUtils.parseDateGeneric(params.subValidOn) : null]
                        )
                        List<Subscription> currentSubscriptions = Subscription.executeQuery("select s " + base_qry, qry_params)
                    %>
                    <g:if test="${currentSubscriptions}">
                        <g:each in="${currentSubscriptions}" var="sub">
                            <div class="la-flexbox">
                                <g:if test="${currentSubscriptions.size() > 1}">
                                    <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                </g:if>
                                <g:link controller="subscription" action="show" id="${sub.id}">${sub}</g:link>
                            </div>
                        </g:each>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                <td class="center aligned">
                    <g:if test="${provider.status == RDStore.PROVIDER_STATUS_CURRENT}">
                        <span class="la-popup-tooltip" data-position="top right">
                            <i class="${Icon.SYM.CIRCLE} green"></i>
                        </span>
                    </g:if>
                    <g:if test="${provider.status == RDStore.PROVIDER_STATUS_RETIRED}">
                        <span class="la-popup-tooltip" data-position="top right" <g:if test="${provider.retirementDate}">data-content="<g:message code="org.retirementDate.label"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${provider.retirementDate}"/>"</g:if>>
                            <i class="${Icon.SYM.CIRCLE} yellow"></i>
                        </span>
                    </g:if>
                </td>
            </g:if>
            <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                <td>
                    <g:each in="${Platform.findAllByProvider(provider)}" var="platform">
                        <g:if test="${platform.gokbId != null}">
                            <ui:wekbIconLink type="platform" gokbId="${platform.gokbId}" />
                        </g:if>
                        <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                        <br />
                    </g:each>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                <td class="center aligned">
                    <g:if test="${provider.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                        <ui:cbItemMarkerAction provider="${provider}" type="${Marker.TYPE.WEKB_CHANGES}" simple="true"/>
                    </g:if>
                </td>
            </g:if>

            <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                <td class="center aligned">
                    <g:if test="${currentProviderIdList && (provider.id in currentProviderIdList)}">
                        <span class="la-popup-tooltip" data-content="${message(code: 'menu.my.providers')}"><i class="${Icon.SIG.MY_OBJECT} yellow"></i></span>
                    </g:if>
                </td>
            </g:if>

        </g:each><!-- tmplConfigShow -->
        </tr>
    </g:each><!-- providerList -->
    </tbody>
</table>
<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $("#providerListToggler").change(function() {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedProviders]").prop('checked', true);
            } else {
                $("tr[class!=disabled] input[name=selectedProviders]").prop('checked', false);
            }
        });
    </laser:script>
</g:if>
