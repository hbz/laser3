<%@ page import="de.laser.finance.CostItem; de.laser.Person; de.laser.storage.RDStore; de.laser.FormService; de.laser.SubscriptionPackage; de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">
    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
            <h3 class="ui header"><g:message code="subscriptionsManagement.package.label"
                                             args="${args.superOrgType}"/>
            </h3>

            <g:if test="${validPackages}">
                <div class="ui middle aligned selection list">
                    <g:each in="${validPackages}" var="subPkg">
                        <div class="item">
                            <g:link controller="package" action="show"
                                    id="${subPkg.pkg.id}">${subPkg.pkg.name} ${raw(subPkg.getIEandPackageSize())}</g:link>

                            <div class="right floated content">
                                <button class="ui negative button la-modern-button la-selectable-button unlinkPackages" ${!editable ? 'disabled="disabled"' : ''}
                                        data-package="${subPkg.pkg.id}" data-subscription="${subPkg.subscription.id}">
                                    <i class="unlink icon"></i>
                                </button>
                            </div>
                        </div>
                    </g:each>
                </div>

            </g:if>
            <g:else>
                <g:message code="subscriptionsManagement.noValidPackages" args="${args.superOrgType}"/>
            </g:else>
        </div>
    </g:if>

    <g:if test="${isLinkingRunning}">
        <div class="ui icon warning message">
            <i class="info icon"></i>

            <div class="content">
                <div class="header">Info</div>

                <p>${message(code: 'subscriptionsManagement.isLinkingRunning.info')}</p>
            </div>
        </div>
    </g:if>

    <g:form action="${actionName}" params="[tab: params.tab, id: params.id]" data-confirm-id="processLinkPackagesMembers_form"
            method="post"
            class="ui form packagesForm">
        <%--<g:hiddenField id="plpm_id_${params.id}" name="id" value="${params.id}"/>--%>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="ui segment">

            <div class="field required">
                <h4 class="ui header">${message(code: 'subscriptionsManagement.info.package')}</h4>

                <g:if test="${controllerName == "subscription"}">

                    <label><g:message code="subscriptionsManagement.package.label"
                                      args="${args.superOrgType}"/> <g:message code="messageRequiredField"/></label>

                    <g:if test="${validPackages}">
                        <g:select class="ui multiple search dropdown"
                                  optionKey="${{ it.pkg.id }}" optionValue="${{ it.getPackageNameWithCurrentTippsCount() }}"
                                  from="${validPackages}" name="selectedPackages" value=""
                                  required=""
                                  noSelection='["all": "${message(code: 'subscriptionsManagement.all.package')}"]'/>
                    </g:if>
                    <g:else>
                        <g:message code="subscriptionsManagement.noValidPackages" args="${args.superOrgType}"/>
                    </g:else>
                </g:if>

                <g:if test="${controllerName == "myInstitution"}">
                    <g:select class="ui multiple search dropdown"
                              optionKey="id" optionValue="${{ it.getPackageNameWithCurrentTippsCount() }}"
                              from="${validPackages}" name="selectedPackages" value=""
                              required=""
                              noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.package')}"]'/>
                </g:if>
            </div>


            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <div class="ui buttons">
                        <button class="ui green button" ${!editable || isLinkingRunning  ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="linkwithoutIE">${message(code: 'subscriptionsManagement.linkwithoutIE')}</button>

                        <div class="or" data-text="${message(code: 'default.or')}"></div>
                        <button class="ui green button" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="linkwithIE">${message(code: 'subscriptionsManagement.linkwithIE')}</button>
                    </div>
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <div class="ui buttons">
                        <button class="ui button negative js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE.confirm')}"
                                data-confirm-term-how="ok" ${!editable || isLinkingRunning  ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                data-confirm-id="processLinkPackagesMembers"
                                value="unlinkIEonly">${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE')}</button>

                        <div class="or" data-text="${message(code: 'default.or')}"></div>
                        <button class="ui button negative js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.unlinkInfo.withIE.confirm')}"
                                data-confirm-term-how="ok" ${!editable || isLinkingRunning  ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                data-confirm-id="processLinkPackagesMembers"
                                value="unlinkwithIE">${message(code: 'subscriptionsManagement.unlinkInfo.withIE')}</button>
                    </div>
                </div>
            </div>

            <div class="divider"></div>

            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber
                        total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber
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
                    <th>${message(code: 'subscription.packages.label')}</th>
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th>${message(code:'default.actions.label')}</th>
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

                            <div class="ui middle aligned selection list">
                                <g:each in="${sub.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index" id="${sub.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/>${raw(sp.getIEandPackageSize())}
                                        </g:link>
                                        <g:if test="${editable && childWithCostItems.find { SubscriptionPackage row -> row.id == sp.id }}">
                                            <br/><g:message code="subscription.delete.existingCostItems"/>
                                        </g:if>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
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
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>
    </g:form>
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

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        }
        else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });

  $('.unlinkPackages').on('click',function() {
      JSPC.app.unlinkPackage($(this).attr("data-package"),$(this).attr("data-subscription"));
  });

  JSPC.app.unlinkPackage = function (pkg_id, subscriptionID) {

    var req_url = "${createLink(controller: 'subscription', action: 'unlinkPackage')}?subscription="+subscriptionID+"&package="+pkg_id

        $.ajax({
            url: req_url
        }).done(function(result){
                $("#unlinkPackageModal").remove();
                $('#magicArea').html(result);
                $("#unlinkPackageModal").modal("show");
            });
     }

    $('.packagesForm').form({
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


