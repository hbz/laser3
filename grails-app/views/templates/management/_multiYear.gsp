<%@ page import="de.laser.helper.RDConstants; de.laser.Subscription;" %>
<laser:serviceInjection/>

<g:if test="${filteredSubscriptions}">
    <div class="ui segment ">
        <h3 class="ui header"><g:message code="subscriptionsManagement.subscription" args="${args.superOrgType}"/></h3>

        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'subscription')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'default.status.label')}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>${subscription.name}</td>

                <td>
                    <g:formatDate formatName="default.date.format.notime" date="${subscription.startDate}"/>
                    <semui:auditButton auditable="[subscription, 'startDate']"/>
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                    <semui:auditButton auditable="[subscription, 'endDate']"/>
                </td>
                <td>
                    ${subscription.status.getI10n('value')}
                    <semui:auditButton auditable="[subscription, 'status']"/>
                </td>
                <td class="x">
                    <g:link controller="subscription" action="show" id="${subscription.id}"
                            class="ui icon button blue la-modern-button"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                        <i aria-hidden="true" class="write icon"></i></g:link>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

            <div class="divider"></div>

            <div class="ui segment">
                <g:form action="processSubscriptionPropertiesMembers" method="post" class="ui form propertiesSubscription">
                    <g:hiddenField id="pspm_id_${params.id}" name="id" value="${params.id}"/>
                    <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

                    <h4 class="ui header">${message(code: 'subscription.subscriptionPropertiesMembers.info', args: args.memberType)}</h4>

                    <div class="two fields">
                        <semui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from"/>

                        <semui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to"/>
                    </div>

                    <div class="three fields">
                        <div class="field">
                            <label>${message(code: 'default.status.label')}</label>
                            <%
                                def fakeList = []
                                fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                                fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
                            %>
                            <laser:select name="status" from="${fakeList}" optionKey="id" optionValue="value"
                                          noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.kind.label')}</label>
                            <laser:select name="kind"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.form.label')}</label>
                            <laser:select name="form"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.resource.label')}</label>
                            <laser:select name="resource"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.isPublicForApi.label')}</label>
                            <laser:select name="isPublicForApi"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                            <laser:select name="hasPerpetualAccess"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.hasPublishComponent.label')}</label>
                            <laser:select name="hasPublishComponent"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                    </div>

                    <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>


                    <h3 class="ui header">
                        ${message(code: 'subscription.propertiesMembers.subscriber')}
                        <semui:totalNumber total="${filteredSubChilds?.size()}"/>
                    </h3>
                    <table class="ui celled la-table table">
                        <thead>
                        <tr>
                            <th>
                                <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                            </th>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'default.sortname.label')}</th>
                            <th>${message(code: 'subscriptionDetails.members.members')}</th>
                            <th>${message(code: 'default.startDate.label')}</th>
                            <th>${message(code: 'default.endDate.label')}</th>
                            <th>${message(code: 'default.status.label')}</th>
                            <th>${message(code: 'subscription.kind.label')}</th>
                            <th>${message(code: 'subscription.form.label')}</th>
                            <th>${message(code: 'subscription.resource.label')}</th>
                            <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                            <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                            <th>${message(code: 'subscription.hasPublishComponent.label')}</th>
                            <th class="la-no-uppercase">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${filteredSubChilds}" status="i" var="zeile">
                            <g:set var="sub" value="${zeile.sub}"/>
                            <tr>
                                <td>
                                    <g:checkBox id="selectedMembers_${sub.id}" name="selectedMembers" value="${sub.id}" checked="false"/>
                                </td>
                                <td>${i + 1}</td>
                                <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                                <g:each in="${filteredSubscribers}" var="subscr">
                                    <td>
                                        ${subscr.sortname}
                                    </td>
                                    <td>
                                        <g:link controller="organisation" action="show"
                                                id="${subscr.id}">${subscr}</g:link>

                                        <g:if test="${sub.isSlaved}">
                                            <span data-position="top right"
                                                  class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                                <i class="thumbtack grey la-rotate icon"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${subscr.getCustomerType() in ['ORG_INST']}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${subscr.getCustomerTypeI10n()}">
                                                <i class="chess rook grey icon"></i>
                                            </span>
                                        </g:if>

                                    </td>
                                </g:each>
                                <g:if test="${!sub.getAllSubscribers()}">
                                    <td></td>
                                    <td></td>
                                </g:if>

                                <td>
                                    <semui:xEditable owner="${sub}" field="startDate" type="date" overwriteEditable="${editableOld}" validation="datesCheck"/>
                                    <semui:auditButton auditable="[sub, 'startDate']"/>
                                </td>
                                <td><semui:xEditable owner="${sub}" field="endDate" type="date" overwriteEditable="${editableOld}" validation="datesCheck"/>
                                <semui:auditButton auditable="[sub, 'endDate']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="status"
                                                            config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                            constraint="removeValue_deleted"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'status']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="kind"
                                                            config="${RDConstants.SUBSCRIPTION_KIND}"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'kind']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="form"
                                                            config="${RDConstants.SUBSCRIPTION_FORM}"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'form']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="resource"
                                                            config="${RDConstants.SUBSCRIPTION_RESOURCE}"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'resource']"/>
                                </td>
                                <td>
                                    <semui:xEditableBoolean owner="${sub}" field="isPublicForApi"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'isPublicForApi']"/>
                                </td>
                                <td>
                                    <semui:xEditableBoolean owner="${sub}" field="hasPerpetualAccess"
                                                            overwriteEditable="${editableOld}"/>
                                    <%--<semui:xEditableRefData owner="${sub}" field="hasPerpetualAccess"
                                                            config="${RDConstants.Y_N}"
                                                            overwriteEditable="${editableOld}"/>--%>
                                    <semui:auditButton auditable="[sub, 'hasPerpetualAccess']"/>
                                </td>
                                <td>
                                    <semui:xEditableBoolean owner="${sub}" field="hasPublishComponent"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'hasPublishComponent']"/>
                                </td>
                                <td>
                                    <g:if test="${sub.isMultiYear}">
                                        <span class="la-long-tooltip la-popup-tooltip la-delay"
                                              data-position="bottom center"
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
                </g:form>
            </div>
        </div>
    </g:if>

    <g:if test="${params.tab == 'providerAgency'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <div class="twelve wide column">

                    <div class="la-inline-lists">
                        <div class="ui card sixteen wide">
                            <div class="content">
                                <g:render template="/templates/links/orgLinksAsList"
                                          model="${[roleLinks    : visibleOrgRelations,
                                                    roleObject   : subscription,
                                                    roleRespValue: 'Specific subscription editor',
                                                    editmode     : editable,
                                                    showPersons  : false
                                          ]}"/>
                                <div class="ui la-vertical buttons">

                                    <g:render template="/templates/links/orgLinksSimpleModal"
                                              model="${[linkType: subscription.class.name,
                                                        parent: genericOIDService.getOID(subscription),
                                                        property: 'orgs',
                                                        recip_prop: 'sub',
                                                        tmplRole: RDStore.OR_PROVIDER,
                                                        tmplType: RDStore.OT_PROVIDER,
                                                        tmplEntity:message(code:'subscription.details.linkProvider.tmplEntity'),
                                                        tmplText:message(code:'subscription.details.linkProvider.tmplText'),
                                                        tmplButtonText:message(code:'subscription.details.linkProvider.tmplButtonText'),
                                                        tmplModalID:'modal_add_provider',
                                                        editmode: editable
                                              ]}" />
                                    <g:render template="/templates/links/orgLinksSimpleModal"
                                              model="${[linkType: subscription.class.name,
                                                        parent: genericOIDService.getOID(subscription),
                                                        property: 'orgs',
                                                        recip_prop: 'sub',
                                                        tmplRole: RDStore.OR_AGENCY,
                                                        tmplType: RDStore.OT_AGENCY,
                                                        tmplEntity: message(code:'subscription.details.linkAgency.tmplEntity'),
                                                        tmplText: message(code:'subscription.details.linkAgency.tmplText'),
                                                        tmplButtonText: message(code:'subscription.details.linkAgency.tmplButtonText'),
                                                        tmplModalID:'modal_add_agency',
                                                        editmode: editable
                                              ]}" />

                                </div><!-- la-js-hide-this-card -->
                            </div>
                        </div>

                        <div class="ui segment">
                            <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                                    total="${filteredSubChilds?.size()}"/></h3>
                            <table class="ui celled la-table table">
                                <thead>
                                <tr>
                                    <th>${message(code: 'sidewide.number')}</th>
                                    <th>${message(code: 'default.sortname.label')}</th>
                                    <th>${message(code: 'subscriptionDetails.members.members')}</th>
                                    <th></th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                <g:each in="${filteredSubChilds}" status="i" var="zeile">
                                    <g:set var="sub" value="${zeile.sub}"/>
                                    <tr>
                                        <td>${i + 1}</td>
                                        <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                                        <g:each in="${filteredSubscribers}" var="subscr">
                                            <td>
                                                ${subscr.sortname}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="show"
                                                        id="${subscr.id}">${subscr}</g:link>

                                                <g:if test="${sub.isSlaved}">
                                                    <span data-position="top right"
                                                          class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                                        <i class="thumbtack grey la-rotate icon"></i>
                                                    </span>
                                                </g:if>

                                                <g:if test="${subscr.getCustomerType() in ['ORG_INST']}">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                          data-content="${subscr.getCustomerTypeI10n()}">
                                                        <i class="chess rook grey icon"></i>
                                                    </span>
                                                </g:if>

                                            </td>
                                        </g:each>
                                        <g:if test="${!sub.getAllSubscribers()}">
                                            <td></td>
                                            <td></td>
                                        </g:if>
                                        <td>
                                            <div class="ui card ">
                                                <div class="content">
                                                    <g:render template="/templates/links/orgLinksAsList"
                                                              model="${[roleLinks    : sub.orgRelations?.findAll {
                                                                  !(it.org?.id == contextService.getOrg().id) && !(it.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id])
                                                              }.sort { it?.org?.sortname },
                                                                        roleObject   : sub,
                                                                        roleRespValue: 'Specific subscription editor',
                                                                        editmode     : editable,
                                                                        showPersons  : false
                                                              ]}"/>
                                                    <div class="ui la-vertical buttons la-js-hide-this-card">

                                                        <g:render template="/templates/links/orgLinksSimpleModal"
                                                                  model="${[linkType: sub.class.name,
                                                                            parent: genericOIDService.getOID(sub),
                                                                            property: 'orgs',
                                                                            recip_prop: 'sub',
                                                                            tmplRole: RDStore.OR_PROVIDER,
                                                                            tmplType: RDStore.OT_PROVIDER,
                                                                            tmplEntity:message(code:'subscription.details.linkProvider.tmplEntity'),
                                                                            tmplText:message(code:'subscription.details.linkProvider.tmplText'),
                                                                            tmplButtonText:message(code:'subscription.details.linkProvider.tmplButtonText'),
                                                                            tmplModalID:'modal_add_provider_'+sub.id,
                                                                            editmode: editable
                                                                  ]}" />
                                                        <g:render template="/templates/links/orgLinksSimpleModal"
                                                                  model="${[linkType: sub.class.name,
                                                                            parent: genericOIDService.getOID(sub),
                                                                            property: 'orgs',
                                                                            recip_prop: 'sub',
                                                                            tmplRole: RDStore.OR_AGENCY,
                                                                            tmplType: RDStore.OT_AGENCY,
                                                                            tmplEntity: message(code:'subscription.details.linkAgency.tmplEntity'),
                                                                            tmplText: message(code:'subscription.details.linkAgency.tmplText'),
                                                                            tmplButtonText: message(code:'subscription.details.linkAgency.tmplButtonText'),
                                                                            tmplModalID:'modal_add_agency_'+sub.id,
                                                                            editmode: editable
                                                                  ]}" />

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
                    </div>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${params.tab == 'documents'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <g:render template="/templates/documents/table"
                          model="${[instance: subscription, context: 'documents', redirect: 'subscriptionPropertiesMembers', owntp: 'subscription']}"/>
            </div>

            <div class="ui segment">
                <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                        total="${filteredSubChilds?.size()}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'default.documents.label')}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${filteredSubChilds}" status="i" var="zeile">
                        <g:set var="sub" value="${zeile.sub}"/>
                        <tr>
                            <td>${i + 1}</td>
                            <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                            <g:each in="${filteredSubscribers}" var="subscr">
                                <td>
                                    ${subscr.sortname}
                                </td>
                            </g:each>
                            <g:if test="${!sub.getAllSubscribers()}">
                                <td></td>
                                <td></td>
                            </g:if>
                            <td>
                                <g:render template="/templates/documents/table"
                                          model="${[instance: sub, context: 'documents', redirect: 'subscriptionPropertiesMembers', owntp: 'subscription']}"/>

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
        </div>
    </g:if>
    <g:if test="${params.tab == 'notes'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <g:render template="/templates/notes/table" model="${[instance: subscription, redirect: 'subscriptionPropertiesMembers']}"/>
            </div>

            <div class="ui segment">
                <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                        total="${filteredSubChilds?.size()}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'default.documents.label')}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${filteredSubChilds}" status="i" var="zeile">
                        <g:set var="sub" value="${zeile.sub}"/>
                        <tr>
                            <td>${i + 1}</td>
                            <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                            <g:each in="${filteredSubscribers}" var="subscr">
                                <td>
                                    ${subscr.sortname}
                                </td>
                            </g:each>
                            <g:if test="${!sub.getAllSubscribers()}">
                                <td></td>
                                <td></td>
                            </g:if>
                            <td>
                                <g:render template="/templates/notes/table"
                                          model="${[instance: sub, redirect: 'subscriptionPropertiesMembers']}"/>

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
        </div>
    </g:if>
    <g:if test="${params.tab == 'multiYear'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'subscription')}</th>
                        <th>${message(code: 'default.startDate.label')}</th>
                        <th>${message(code: 'default.endDate.label')}</th>
                        <th>${message(code: 'default.status.label')}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                    <td>${subscription.name}</td>

                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.startDate}"/>
                        <semui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                        <semui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        ${subscription.status.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'status']"/>
                    </td>
                    <td class="x">
                        <g:link controller="subscription" action="show" id="${subscription.id}"
                                class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="write icon"></i></g:link>
                    </td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <div class="ui segment">
                <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                        total="${filteredSubChilds?.size()}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                        <th>${message(code: 'default.startDate.label')}</th>
                        <th>${message(code: 'default.endDate.label')}</th>
                        <th>${message(code: 'default.status.label')}</th>
                        <th>${message(code: 'subscription.isMultiYear.label')}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${filteredSubChilds}" status="i" var="zeile">
                        <g:set var="sub" value="${zeile.sub}"/>
                        <tr>
                            <td>${i + 1}</td>
                            <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                            <g:each in="${filteredSubscribers}" var="subscr">
                                <td>
                                    ${subscr.sortname}
                                </td>
                                <td>
                                    <g:link controller="organisation" action="show"
                                            id="${subscr.id}">${subscr}</g:link>

                                    <g:if test="${sub.isSlaved}">
                                        <span data-position="top right"
                                              class="la-popup-tooltip la-delay"
                                              data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                            <i class="thumbtack grey la-rotate icon"></i>
                                        </span>
                                    </g:if>

                            <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${subscr.getCustomerTypeI10n()}">
                                    <i class="chess rook grey icon"></i>
                                </span>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <td>${sub.name}</td>
                    </g:if>
                    <td>
                        <semui:xEditable owner="${sub}" field="startDate" type="date" validation="datesCheck"/>
                        <semui:auditButton auditable="[sub, 'startDate']"/>
                    </td>
                    <td><semui:xEditable owner="${sub}" field="endDate" type="date" validation="datesCheck"/>
                    <semui:auditButton auditable="[sub, 'endDate']"/>
                    </td>
                    <td>
                        <semui:xEditableRefData owner="${sub}" field="status"
                                                config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                constraint="removeValue_deleted"/>
                        <semui:auditButton auditable="[sub, 'status']"/>
                    </td>
                    <td>
                        <semui:xEditableBoolean owner="${sub}" field="isMultiYear"/>
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
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });
</laser:script>


