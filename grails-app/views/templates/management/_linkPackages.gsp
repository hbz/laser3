<%@ page import="de.laser.wekb.Package; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.finance.CostItem; de.laser.addressbook.Person; de.laser.storage.RDStore; de.laser.FormService; de.laser.SubscriptionPackage; de.laser.Subscription" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">
    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
            <g:if test="${validPackages}">
                <div class="ui middle aligned selection list">
                    <g:each in="${validPackages}" var="subPkg">
                        <div class="item">
                            <g:link controller="package" action="show" id="${subPkg.pkg.id}">${subPkg.pkg.name} <ui:ieAndPkgSize sp="${subPkg}" /></g:link>

                            <div class="right floated content">
                                <button class="${Btn.MODERN.NEGATIVE} la-selectable-button unlinkPackages" ${!editable || isUnlinkingRunning ? 'disabled="disabled"' : ''}
                                        data-package="${subPkg.pkg.id}" data-subscription="${subPkg.subscription.id}">
                                    <i class="${Icon.CMD.UNLINK}"></i>
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

    <g:if test="${isLinkingRunning || isUnlinkingRunning}">
        <ui:msg class="warning" showIcon="true" hideClose="true" header="Info" message="subscriptionsManagement.isLinkingRunning.info" />
    </g:if>

    <h3 class="ui header">${message(code: 'subscriptionsManagement.info.package')}</h3>

    <g:form action="${actionName}" params="[tab: params.tab, id: params.id]" data-confirm-id="processLinkPackagesMembers_form"
            method="post"
            class="ui form packagesForm">
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="ui segments">
        <div class="ui segment">
            <div class="field required">

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
                        <button class="${Btn.POSITIVE}" ${!editable || isLinkingRunning || isUnlinkingRunning  ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="linkwithoutIE">${message(code: 'subscriptionsManagement.linkwithoutIE')}</button>

                        <div class="or" data-text="${message(code: 'default.or')}"></div>
                        <button class="${Btn.POSITIVE}" ${!editable || isLinkingRunning || isUnlinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="linkwithIE">${message(code: 'subscriptionsManagement.linkwithIE')}</button>
                    </div>
                </div>
                <div class="eight wide field" style="text-align: right;">
                    <div class="ui buttons">
                        <button class="${Btn.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE.confirm')}"
                                data-confirm-term-how="ok" ${!editable || isLinkingRunning || isUnlinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                data-confirm-id="processLinkPackagesMembers"
                                value="unlinkIEonly">${message(code: 'subscriptionsManagement.unlinkInfo.onlyIE')}</button>

                        <div class="or" data-text="${message(code: 'default.or')}"></div>
                        <button class="${Btn.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.unlinkInfo.withIE.confirm')}"
                                data-confirm-term-how="ok" ${!editable || isLinkingRunning || isUnlinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                data-confirm-id="processLinkPackagesMembers"
                                value="unlinkwithIE">${message(code: 'subscriptionsManagement.unlinkInfo.withIE')}</button>
                    </div>
                </div>
            </div>
        </div><!-- .segment -->

        <div class="ui segment">

            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber
                        total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber
                            total="${num_sub_rows}"/>
                </g:else>
            </h3>

            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th class="center aligned">
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                    </g:if>
                    <th>${message(code: 'sidewide.number')}</th>
                    <g:if test="${controllerName == "subscription"}">
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                        <g:if test="${params.showMembersSubWithMultiYear}">
                            <th>${message(code: 'subscription.referenceYear.label.shy')}</th>
                        </g:if>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <th>${message(code: 'default.subscription.label')}</th>
                    </g:if>
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscription.packages.label')}</th>
                    <th class="la-no-uppercase">
                        <ui:multiYearIcon />
                    </th>
                    <th>${message(code:'default.actions.label')}</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                    <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                    <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriberRespConsortia() : zeile.orgs}"/>
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
                                    <ui:auditIcon type="auto2" />
                                </g:if>

                                <ui:customerTypeProIcon org="${subscr}" />
                            </td>
                            <g:if test="${params.showMembersSubWithMultiYear}">
                                ${sub.referenceYear}
                            </g:if>
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
                                        <g:if test="${sub.instanceOf && sub.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}">
                                            <g:link controller="subscription" action="index" id="${sub.instanceOf.id}"
                                                    params="[pkgfilter: sp.pkg.id]">
                                                ${sp.pkg.name}<br/>(Bestand der Elternlizenz wird übernommen)
                                            </g:link>
                                        </g:if>
                                        <g:elseif test="${subscriptionService.countCurrentIssueEntitlements(sub) > 0}">
                                            <g:link controller="subscription" action="index" id="${sub.id}"
                                                    params="[pkgfilter: sp.pkg.id]">
                                                ${sp.pkg.name}<br/><ui:ieAndPkgSize sp="${sp}" />
                                            </g:link>
                                        </g:elseif>
                                        <g:else>
                                            <g:link controller="subscription" action="addEntitlements" id="${sub.id}"
                                                    params="[pkgfilter: sp.pkg.id]">
                                                ${sp.pkg.name}<br/><ui:ieAndPkgSize sp="${sp}" />
                                            </g:link>
                                        </g:else>
                                        <g:if test="${editable && childWithCostItems.find { Package row -> row.id == sp.pkg.id }}">
                                            <br/><g:message code="subscription.delete.existingCostItems"/>
                                        </g:if>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <ui:multiYearIcon isConsortial="true" color="orange" />
                            </g:if>
                        </td>
                        <td class="x">
                            <g:link controller="subscription" action="show" id="${sub.id}"
                                    class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div><!-- .segment -->
        </div><!-- .segments -->
    </g:form>
</g:if>
<g:else>
    <g:if test="${filterSet}">
        <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
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


