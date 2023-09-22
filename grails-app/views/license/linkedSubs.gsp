<%@ page import="de.laser.Subscription; de.laser.RefdataCategory; de.laser.Person; de.laser.storage.RDStore; de.laser.License; de.laser.RefdataValue; de.laser.interfaces.CalculatedType; de.laser.storage.RDConstants" %>

<laser:htmlStart text="${message(code:'license.details.incoming.childs',args:[message(code:'consortium.subscriber')])}" serviceInjection="true"/>

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <g:if test="${userService.hasFormalAffiliation(user, institution, 'INST_EDITOR')}">
            <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
        </g:if>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon visibleOrgRelations="${visibleOrgRelations}">
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>
    <ui:totalNumber total="${subscriptions.size() ?: 0}"/>
    <ui:anualRings object="${license}" controller="license" action="linkedSubs" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

<laser:render template="${customerTypeService.getNavTemplatePath()}" />

<g:if test="${license.instanceOf && (institution.id == license.getLicensingConsortium()?.id)}">
    <ui:msg class="negative" header="${message(code:'myinst.message.attention')}" noClose="true">
        <g:message code="myinst.licenseDetails.message.ChildView" />
        <g:message code="myinst.licenseDetails.message.ConsortialView" />
        <g:link controller="license" action="linkedSubs" id="${license.instanceOf.id}">
            <g:message code="myinst.subscriptionDetails.message.here" />
        </g:link>.
    </ui:msg>
</g:if>

    <ui:filter>
        <g:form action="linkedSubs" controller="license" params="${[id:params.id]}" method="get" class="ui form">
            <div class="three fields">
                <div class="field">
                    <label for="subscription">${message(code:'subscription')}</label>
                    <select id="subscription" name="subscription" multiple="" class="ui search selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${subscriptionsForFilter}" var="sub">
                            <option <%=(params.list('subscription').contains(sub.id.toString())) ? 'selected="selected"' : '' %> value="${sub.id}">${sub.dropdownNamingConvention()}</option>
                        </g:each>
                    </select>
                </div>
                <div class="field">
                    <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
                </div>

                <div class="field">
                    <label>${message(code: 'default.status.label')}</label>
                    <ui:select class="ui dropdown" name="status"
                                  from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.status}"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
            </div>
            <g:if test="${consAtMember}">
                <%
                    List<List<String>> tmplConfigShow = [['name', 'identifier', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['subRunTimeMultiYear']]
                %>
                <laser:render template="/templates/filter/orgFilter"
                          model="[
                                  tmplConfigShow: tmplConfigShow,
                                  tmplConfigFormFilter: true
                          ]"/>
            </g:if>
            <g:else>
                <div class="field">
                    <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                                <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox" <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                                <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" <g:if test="${params.subRunTime}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="field">
                    <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                    <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                               from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                               optionKey="id"
                               optionValue="value"
                               value="${params.hasPerpetualAccess}"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
                    <input name="filterSet" type="hidden" value="true">
                    <input type="submit" value="${message(code:'default.button.filter.label')}" class="ui primary button"/>
                </div>
            </g:else>
        </g:form>
    </ui:filter>

<table class="ui celled la-js-responsive-table la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <g:if test="${consAtMember}">
                <th>${message(code:'default.sortname.label')}</th>
                <th>${message(code:'subscriptionDetails.members.members')}</th>
            </g:if>

            <th>${message(code: 'default.subscription.label')}</th>
            <th>
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'default.previous.label')}">
                    <i class="arrow left icon"></i>
                </span>
            </th>
            <th>${message(code:'default.startDate.label')}</th>
            <th>${message(code:'default.endDate.label')}</th>
            <th>
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'default.next.label')}">
                    <i class="arrow right icon"></i>
                </span>
            </th>
            <th>${message(code:'default.status.label')}</th>
            <th class="la-no-uppercase">
                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                    <i class="map orange icon"></i>
                </span>
            </th>
        </tr>
    </thead>
    <tbody>

        <g:each in="${subscriptions}" status="i" var="row">
            <g:if test="${consAtMember}">
                <g:set var="sub" value="${row.sub}"/>
                <g:set var="filteredSubscribers" value="${row.orgs}"/>
            </g:if>
            <g:else>
                <g:set var="sub" value="${row}"/>
            </g:else>
            <%
                LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(sub)
                Subscription navPrevSubscription = (links.prevLink.size() > 0) ? links.prevLink[0] : null
                Subscription navNextSubscription = (links.nextLink.size() > 0) ? links.nextLink[0] : null
            %>
            <tr>
                <td>${i + 1}</td>
                <g:each in="${filteredSubscribers}" var="subscr">
                    <td>${subscr.sortname}</td>
                    <td>
                        <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                        <g:if test="${sub.isSlaved}">
                            <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'license.details.isSlaved.tooltip')}">
                                <i class="grey la-thumbtack-regular icon"></i>
                            </span>
                        </g:if>

                        <ui:customerTypeProIcon org="${subscr}" />

                        <div class="ui list">
                            <g:each in="${Person.getPublicByOrgAndFunc(subscr, 'General contact person')}" var="gcp">
                                <div class="item">
                                    ${gcp}
                                    (${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')})
                                </div>
                            </g:each>
                            <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', institution)}" var="gcp">
                                <div class="item">
                                    ${gcp}
                                    (${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                                </div>
                            </g:each>
                            <g:each in="${Person.getPublicByOrgAndObjectResp(subscr, sub, 'Specific subscription editor')}" var="sse">
                                <div class="item">
                                    ${sse}
                                    (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')})
                                </div>
                            </g:each>
                            <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', institution)}" var="sse">
                                <div class="item">
                                    ${sse}
                                    (${RDStore.PRS_RESP_SPEC_SUB_EDITOR.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                                </div>
                            </g:each>
                        </div>
                    </td>
                </g:each>
                <td>
                    <g:link controller="subscription" action="show" id="${sub.id}">${sub.name}</g:link>
                </td>
                <td class="center aligned">
                    <g:if test="${navPrevSubscription}">
                        <g:link controller="subscription" action="show" id="${navPrevSubscription.id}"><i class="arrow left icon"></i></g:link>
                    </g:if>
                </td>
                <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                <td class="center aligned">
                    <g:if test="${navNextSubscription}">
                        <g:link controller="subscription" action="show" id="${navNextSubscription.id}"><i class="arrow right icon"></i></g:link>
                    </g:if>
                </td>
                <td>
                    ${sub.status.getI10n("value")}
                </td>
                <td>
                    <g:if test="${sub.isMultiYear}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </g:if>
                </td>
            </tr>
        </g:each>

    </tbody>
</table>

<laser:htmlEnd />
