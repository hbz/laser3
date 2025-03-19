<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Subscription; de.laser.License; de.laser.addressbook.Person; de.laser.storage.RDStore; de.laser.FormService" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == 'subscription'}">
        <div class="ui segment">
%{--            <h3 class="ui header"><g:message code="subscriptionsManagement.license.label"/></h3>--}%
            <g:if test="${validLicenses}">
                <div class="ui middle aligned selection list">
                    <g:each in="${validLicenses}" var="license">
                        <div class="item">
                            <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link>
                        </div>
                    </g:each>
                </div>
            </g:if>
            <g:else>
                <g:message code="subscriptionsManagement.noValidLicenses"/>
            </g:else>
        </div>
    </g:if>

    <h3 class="ui header">${message(code: 'subscriptionsManagement.info.license')}</h3>

    <g:form action="${actionName}" controller="${controllerName}" params="[tab: 'linkLicense']" method="post"
            class="ui form licenseForm" data-confirm-id="deleteLicenses_form">
        <g:hiddenField id="pllm_id_${params.id}" name="id" value="${params.id}"/>
        <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

        <div class="ui segments">
        <div class="ui segment">
            <div class="field required">
                <label>${message(code: 'subscription.linktoLicense')} ${message(code: 'messageRequiredField')}</label>
                <g:if test="${validLicenses}">
                    <g:select class="ui search multiple dropdown"
                              optionKey="id" optionValue="${{ it.dropdownNamingConvention() }}"
                              from="${validLicenses}" name="selectedLicense"
                              required=""
                              noSelection="['': message(code: 'subscriptionsManagement.noSelection.license')]"/>
                </g:if>
                <g:else>
                    <g:if test="${controllerName == 'subscription'}">
                        <g:message code="subscriptionsManagement.noValidLicenses" args="${args.superOrgType}"/>
                    </g:if>
                    <g:else>
                        <g:message code="subscriptionsManagement.noValidLicenses"/>
                    </g:else>
                </g:else>
            </div>

            <g:if test="${validLicenses}">
                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <div class="ui buttons">
                            <button class="${Btn.POSITIVE}" ${!editable ? 'disabled="disabled"' : ''} type="submit"
                                    name="processOption"
                                    value="linkLicense">${message(code: 'subscriptionsManagement.linkLicenses.button')}</button>
                        </div>
                    </div>
                    <div class="eight wide field" style="text-align: right;">
                        <div class="ui buttons">
                            <button class="${Btn.NEGATIVE_CONFIRM}" ${!editable ? 'disabled="disabled"' : ''}
                                    data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.deleteLicenses.button.confirm')}"
                                    data-confirm-term-how="ok"
                                    name="processOption"
                                    data-confirm-id="deleteLicenses"
                                    value="unlinkLicense">${message(code: 'subscriptionsManagement.deleteLicenses.button')}</button>
                        </div>
                    </div>
                </div>
            </g:if>
            <g:else>
                <div class="sixteen wide field" style="text-align: right;">
                    <div class="ui buttons">
                        <span class="la-popup-tooltip"
                              data-content="${message(code: 'license.details.unlink')}">
                            <g:link class="${Btn.MODERN.NEGATIVE_CONFIRM} la-selectable-button"
                                    data-confirm-tokenMsg="${message(code: "subscriptionsManagement.deleteLicenses.button.confirm")}"
                                    data-confirm-term-how="unlink"
                                    controller="subscription" action="unlinkAllLicenses"
                                    params="${[id: params.id]}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                                ${message(code: 'subscriptionsManagement.deleteAllLicenses.button')}
                            </g:link>
                        </span>
                    </div>
                </div>
            </g:else>
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
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="${managementService.checkTogglerState(subIDs, "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}")}"/>
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
                    <th>${message(code: 'subscription.linktoLicense')}</th>
                    <th class="la-no-uppercase">
                        <ui:multiYearIcon />
                    </th>
                    <th class="center aligned">
                        <ui:optionsIcon />
                    </th>
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
                                        <g:checkBox class="selectedSubs" id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}"
                                                    checked="${selectionCache.containsKey('selectedSubs_'+sub.id)}"/>
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
                                <ui:customerTypeOnlyProIcon org="${subscr}" />
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
                            <g:each in="${License.executeQuery('select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType', [subscription: sub, linkType: RDStore.LINKTYPE_LICENSE])}"
                                    var="license">
                                <g:link controller="license" action="show"
                                        id="${license.id}">${license.reference}</g:link><br/>
                            </g:each>
                        </td>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <ui:multiYearIcon isConsortial="true" color="orange" />
                            </g:if>
                        </td>
                        <td class="x">
                            <g:link controller="subscription" action="show" id="${sub.id}"
                                    class="${Btn.MODERN.SIMPLE}"
                                    data-position="left center"
                                    role="button">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i></g:link>
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

<laser:script file="${this.getGroovyPageFileName()}">
    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
        $.ajax({
            method: "post",
            url: "<g:createLink controller="ajaxJson" action="updatePaginationCache" />",
            data: {
                allIds: [${subIDs.join(',')}],
                cacheKeyReferer: "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}"
            }
        }).done(function(result){
            console.log("updated cache for all subscriptions: "+result.state);
        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    });

    $(".selectedSubs").change(function() {
        let selId = $(this).attr("id");
        $.ajax({
            method: "post",
            url: "<g:createLink controller="ajaxJson" action="updatePaginationCache" />",
            data: {
                selId: selId,
                cacheKeyReferer: "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}"
            }
        }).done(function(result){
            console.log("updated cache for "+selId+": "+result.state);
        }).fail(function(xhr,status,message){
            console.log("error occurred, consult logs!");
        });
    });

    $("input[name=selectedSubs]").checkbox({
        onChange: function() {
            $('#membersListToggler').prop('checked', false);
        }
    });

    /*
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
    */
</laser:script>


