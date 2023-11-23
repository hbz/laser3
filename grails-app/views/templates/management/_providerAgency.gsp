<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Subscription;" %>
<g:if test="${filteredSubscriptions}">

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
%{--            <h3 class="ui header"><g:message code="consortium.superSubscriptionType"/></h3>--}%


                            <laser:render template="/templates/links/orgLinksAsList"
                                      model="${[roleLinks    : visibleOrgRelations,
                                                roleObject   : subscription,
                                                roleRespValue: 'Specific subscription editor',
                                                editmode     : editable,
                                                showPersons  : false
                                      ]}"/>

                            <div class="ui la-vertical buttons">
                                <laser:render template="/templates/links/orgLinksSimpleModal"
                                          model="${[linkType      : subscription.class.name,
                                                    parent        : genericOIDService.getOID(subscription),
                                                    property      : 'orgs',
                                                    recip_prop    : 'sub',
                                                    tmplRole      : RDStore.OR_PROVIDER,
                                                    tmplType      : RDStore.OT_PROVIDER,
                                                    tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                    tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                    tmplButtonText: message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                    tmplModalID   : 'modal_add_provider',
                                                    editmode      : editable
                                          ]}"/>
                                <laser:render template="/templates/links/orgLinksSimpleModal"
                                          model="${[linkType      : subscription.class.name,
                                                    parent        : genericOIDService.getOID(subscription),
                                                    property      : 'orgs',
                                                    recip_prop    : 'sub',
                                                    tmplRole      : RDStore.OR_AGENCY,
                                                    tmplType      : RDStore.OT_AGENCY,
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
                <g:set var="subscr" value="${zeile instanceof Subscription ? zeile.getSubscriber() : zeile.orgs}"/>
                <tr>
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

                            <ui:customerTypeProIcon org="${subscr}" />
                        </td>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <td>${sub.name}</td>
                    </g:if>
                    <td>
                        <div class="ui card">
                            <div class="content">
                                <laser:render template="/templates/links/orgLinksAsList"
                                          model="${[roleLinks    : sub.orgRelations?.findAll {
                                              !(it.org?.id == contextService.getOrg().id) && !(it.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id])
                                          }.sort { it?.org?.sortname },
                                                    roleObject   : sub,
                                                    roleRespValue: 'Specific subscription editor',
                                                    editmode     : editable,
                                                    showPersons  : false
                                          ]}"/>
                                <div class="ui la-vertical buttons la-js-hide-this-card">

                                    <laser:render template="/templates/links/orgLinksSimpleModal"
                                              model="${[linkType      : sub.class.name,
                                                        parent        : genericOIDService.getOID(sub),
                                                        property      : 'orgs',
                                                        recip_prop    : 'sub',
                                                        tmplRole      : RDStore.OR_PROVIDER,
                                                        tmplType      : RDStore.OT_PROVIDER,
                                                        tmplEntity    : message(code: 'subscription.details.linkProvider.tmplEntity'),
                                                        tmplText      : message(code: 'subscription.details.linkProvider.tmplText'),
                                                        tmplButtonText: message(code: 'subscription.details.linkProvider.tmplButtonText'),
                                                        tmplModalID   : 'modal_add_provider_' + sub.id,
                                                        editmode      : editable
                                              ]}"/>
                                    <laser:render template="/templates/links/orgLinksSimpleModal"
                                              model="${[linkType      : sub.class.name,
                                                        parent        : genericOIDService.getOID(sub),
                                                        property      : 'orgs',
                                                        recip_prop    : 'sub',
                                                        tmplRole      : RDStore.OR_AGENCY,
                                                        tmplType      : RDStore.OT_AGENCY,
                                                        tmplEntity    : message(code: 'subscription.details.linkAgency.tmplEntity'),
                                                        tmplText      : message(code: 'subscription.details.linkAgency.tmplText'),
                                                        tmplButtonText: message(code: 'subscription.details.linkAgency.tmplButtonText'),
                                                        tmplModalID   : 'modal_add_agency_' + sub.id,
                                                        editmode      : editable
                                              ]}"/>

                                </div><!-- la-js-hide-this-card -->
                            </div>
                        </div>
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
