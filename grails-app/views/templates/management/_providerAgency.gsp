<%@ page import="de.laser.wekb.ProviderRole; de.laser.wekb.VendorRole; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Subscription;" %>
<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
%{--            <h3 class="ui header"><g:message code="consortium.superSubscriptionType"/></h3>--}%


                            <laser:render template="/templates/links/providerLinksAsList"
                                      model="${[roleLinks    : providerRoles,
                                                roleObject   : subscription,
                                                roleRespValue: 'Specific subscription editor',
                                                editmode     : editable,
                                                showPersons  : false
                                      ]}"/>
                            <laser:render template="/templates/links/vendorLinksAsList"
                                          model="${[vendorRoles  : vendorRoles,
                                                    roleObject   : subscription,
                                                    roleRespValue: 'Specific subscription editor',
                                                    editmode     : editable,
                                                    showPersons  : true
                                          ]}"/>

                            <div class="ui la-vertical buttons">
                                <laser:render template="/templates/links/providerLinksSimpleModal"
                                          model="${[linkType      : subscription.class.name,
                                                    parent        : genericOIDService.getOID(subscription),
                                                    recip_prop    : 'subscription',
                                                    tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                    tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                    tmplButtonText: message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                    tmplModalID   : 'modal_add_provider',
                                                    editmode      : editable
                                          ]}"/>
                                <laser:render template="/templates/links/vendorLinksSimpleModal"
                                              model="${[linkType      : subscription.class.name,
                                                        parent        : genericOIDService.getOID(subscription),
                                                        recip_prop    : 'subscription',
                                                        tmplEntity    : message(code: 'subscription.details.linkAgency.tmplEntity'),
                                                        tmplText      : message(code: 'subscription.details.linkAgency.tmplText'),
                                                        tmplButtonText: message(code: 'subscription.details.linkAgency.tmplButtonText'),
                                                        tmplModalID   : 'modal_add_agency',
                                                        editmode      : editable
                                              ]}"/>
                            </div>
        </div>
    </g:if>
    <div class="ui segment">

        <h3 class="ui header">
            <g:if test="${controllerName == "subscription"}">
                ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber total="${filteredSubscriptions.size()}"/>
            </g:if><g:else>
                ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber total="${num_sub_rows}"/>
            </g:else>
        </h3>
        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
            <tr>
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
                <th></th>
                <th>${message(code:'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubscriptions}" status="i" var="zeile">
                <g:set var="sub" value="${zeile instanceof Subscription ? zeile : zeile.sub}"/>
                <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriberRespConsortia() : zeile.orgs}"/>
                <tr>
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
                            <td>${sub.referenceYear}</td>
                        </g:if>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <td>${sub.name}</td>
                    </g:if>
                    <td>
                        <div class="ui card">
                            <div class="content">
                                <laser:render template="/templates/links/providerLinksAsList"
                                          model="${[providerRoles: ProviderRole.findAllBySubscription(sub, [sort: 'provider.sortname']),
                                                    roleObject   : sub,
                                                    roleRespValue: 'Specific subscription editor',
                                                    editmode     : editable,
                                                    showPersons  : false
                                          ]}"/>
                                <laser:render template="/templates/links/vendorLinksAsList"
                                              model="${[vendorRoles  : VendorRole.findAllBySubscription(sub, [sort: 'vendor.sortname']),
                                                        roleObject   : sub,
                                                        roleRespValue: 'Specific subscription editor',
                                                        editmode     : editable,
                                                        showPersons  : false
                                              ]}"/>
                                <div class="ui la-vertical buttons">

                                    <laser:render template="/templates/links/providerLinksSimpleModal"
                                              model="${[linkType      : sub.class.name,
                                                        parent        : genericOIDService.getOID(sub),
                                                        recip_prop    : 'subscription',
                                                        tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                        tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                        tmplButtonText: message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                        tmplModalID   : 'modal_add_provider_' + sub.id,
                                                        editmode      : editable
                                              ]}"/>
                                    <laser:render template="/templates/links/vendorLinksSimpleModal"
                                                  model="${[linkType      : sub.class.name,
                                                            parent        : genericOIDService.getOID(sub),
                                                            recip_prop    : 'subscription',
                                                            tmplEntity    : message(code: 'subscription.details.linkAgency.tmplEntity'),
                                                            tmplText      : message(code: 'subscription.details.linkAgency.tmplText'),
                                                            tmplButtonText: message(code: 'subscription.details.linkAgency.tmplButtonText'),
                                                            tmplModalID   : 'modal_add_agency' + sub.id,
                                                            editmode      : editable
                                                  ]}"/>

                                </div>
                            </div>
                        </div>
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
    </div>

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
