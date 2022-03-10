<%@ page import="de.laser.Subscription; de.laser.License; de.laser.Person; de.laser.helper.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == 'subscription'}">
        <div class="ui segment">
            <h3 class="ui header"><g:message code="subscriptionsManagement.license.label"/>
            </h3>

            <g:if test="${validLicenses}">
                <div class="ui middle aligned selection list">
                    <g:each in="${validLicenses}" var="license">
                        <div class="item">
                            <g:link controller="license" action="show"
                                    id="${license.id}">${license.reference}</g:link>
                        </div>
                    </g:each>
                </div>

            </g:if>
            <g:else>
                <g:message code="subscriptionsManagement.noValidLicenses"/>
            </g:else>

        </div>

        <div class="ui segment">
            <h4 class="ui header">${message(code: 'subscriptionsManagement.deleteLicensesInfo', args: [num_sub_rows ?: filteredSubscriptions.size()])}</h4>

            <g:link class="ui button negative js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.deleteLicenses.button.confirm')}"
                    data-confirm-term-how="ok" action="${actionName}" controller="${controllerName}" id="${params.id}"
                    params="[processOption: 'unlinkAll', tab: 'linkLicense']">${message(code: 'subscriptionsManagement.deleteAllLicenses.button')}</g:link>
        </div>
    </g:if>

    <div class="divider"></div>

    <div class="ui segment">
        <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'linkLicense']" method="post"
                class="ui form licenseForm" data-confirm-id="deleteLicenses_form">
            <g:hiddenField id="pllm_id_${params.id}" name="id" value="${params.id}"/>
            <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

            <div class="field required">
                <h4 class="ui header">${message(code: 'subscriptionsManagement.info.license')}</h4>

                <label>${message(code: 'subscription.linktoLicense')} ${message(code: 'messageRequiredField')}</label>
                <g:if test="${validLicenses}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.dropdownNamingConvention() }}"
                              from="${validLicenses}" name="selectedLicense"
                              required=""
                              noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.license')}"]'/>
                </g:if><g:else>
                    <g:if test="${controllerName == 'subscription'}">
                        <g:message code="subscriptionsManagement.noValidLicenses" args="${args.superOrgType}"/>
                    </g:if>
                    <g:else>
                        <g:message code="subscriptionsManagement.noValidLicenses"/>
                    </g:else>
                </g:else>
            </div>


            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <div class="ui buttons">
                        <button class="ui button" ${!editable ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="linkLicense">${message(code: 'subscriptionsManagement.linkLicenses.button')}</button>
                    </div>
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <div class="ui buttons">
                        <button class="ui button negative js-open-confirm-modal" ${!editable ? 'disabled="disabled"' : ''}
                                data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.deleteLicenses.button.confirm')}"
                                data-confirm-term-how="ok"
                                name="processOption"
                                data-confirm-id="deleteLicenses"
                                value="unlinkLicense">${message(code: 'subscriptionsManagement.deleteLicenses.button')}</button>
                    </div>

                </div>
            </div>


            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <semui:totalNumber
                        total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <semui:totalNumber
                            total="${filteredSubscriptions.size()}/${num_sub_rows}"/>
                </g:else>
            </h3>
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th>
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                    </g:if>
                    <th>${message(code: 'sidewide.number')}</th>
                    <g:if test="${controllerName == "subscription"}">
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <th>${message(code: 'default.subscription.label')}</th>
                    </g:if>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.linktoLicense')}</th>
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th class="la-action-info">${message(code: 'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                    <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                    <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriber() : zeile.orgs}"/>
                    <tr>
                        <g:if test="${editable}">
                            <td>
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}"
                                                    checked="false"/>
                                    </div>
                                </div>
                            </td>
                        </g:if>
                        <td>${(offset ?: 0) + i + 1}</td>
                        <g:if test="${controllerName == "subscription"}">
                            <td>
                                ${subscr.sortname}
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                                <g:if test="${sub.isSlaved}">
                                    <span data-position="top right"
                                          class="la-popup-tooltip la-delay"
                                          data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                        <i class="grey la-thumbtack-regular icon"></i>
                                    </span>
                                </g:if>

                                <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                                          data-position="bottom center"
                                          data-content="${subscr.getCustomerTypeI10n()}">
                                        <i class="chess rook grey icon"></i>
                                    </span>
                                </g:if>
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "myInstitution"}">
                            <td>${sub.name}</td>
                        </g:if>

                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                        <td>${sub.status.getI10n('value')}</td>
                        <td>
                            <g:each in="${License.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType', [subscription: sub, linkType: RDStore.LINKTYPE_LICENSE])}"
                                    var="license">
                                <g:link controller="license" action="show"
                                        id="${license.id}">${license.reference}</g:link><br/>
                            </g:each>
                        </td>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:if>
                        </td>

                        <td class="x">
                            <g:link controller="subscription" action="show" id="${sub.id}"
                                    class="ui icon button blue la-modern-button"
                                    data-position="left center"
                                    role="button">
                                <i aria-hidden="true" class="write icon"></i></g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:form>
    </div>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object"
                                args="${[message(code: "subscription.plural")]}"/></strong>
    </g:if>
    <g:else>
        <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
    </g:else>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });

    $('.licenseForm').form({
        on: 'blur',
        inline: true,
        fields: {
            noSubscription: {
                identifier: 'selectedSubs',
                rules: [
                    {
                        type: 'checked',
                        prompt: '<g:message code="subscriptionsManagement.noSelectedSubscriptions.table"/>'
                    }
                ]
            }
        }
    });
</laser:script>


