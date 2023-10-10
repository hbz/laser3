<%@ page import="de.laser.CustomerTypeService; de.laser.Subscription; de.laser.RefdataCategory; de.laser.Doc; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.survey.SurveyConfig" %>
<laser:htmlStart message="subscription.details.copyDiscountScales.label" />

<laser:serviceInjection/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions"
              text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="subTransfer" id="${subscription.id}"
              text="${subscription.name}"/>
    <ui:crumb controller="subscription" action="manageDiscountScale" id="${subscription.id}"
              text="${message(code: 'subscription.details.manageDiscountScale.label')}"/>
    <ui:crumb class="active" text="${message(code: '')}"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}">
    <ui:xEditable owner="${subscription}" field="name" />
</ui:h1HeaderWithIcon>
<g:if test="${editable}">
    <ui:auditButton auditable="[subscription, 'name']" />
</g:if>

<ui:anualRings object="${subscription}" controller="subscription" action="copyDiscountScales" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<ui:messages data="${flash}"/>

<ui:filter>
    <g:form action="copyDiscountScales" controller="subscription" method="get" class="ui small form">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <input type="hidden" name="id" value="${params.id}"/>

        <div class="three fields">
            <!-- 1-1 -->
            <div class="field">
                <label for="q">${message(code: 'default.search.text')}
                    <span data-position="right center" data-variation="tiny"
                          class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.subscription')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="q" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>
            <!-- 1-2 -->
            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn"
                               placeholder="filter.placeholder" value="${validOn}"/>
            </div>

            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <ui:select class="ui dropdown" name="status"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.status}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">

            <!-- 2-1 + 2-2 -->
            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

            <!-- 2-3 -->
            <div class="field">
                <label>${message(code: 'subscription.form.label')}</label>
                <ui:select class="ui dropdown" name="form"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.form}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <!-- 2-4 -->
            <div class="field">
                <label>${message(code: 'subscription.resource.label')}</label>
                <ui:select class="ui dropdown" name="resource"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.resource}"
                           noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="two fields">
            <div class="field">
                <label>${message(code: 'menu.my.providers')}</label>
                <g:select class="ui dropdown search" name="provider"
                          from="${providers}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.provider}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui primary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>
        </div>
    </g:form>
</ui:filter>

<g:form action="copyDiscountScales" controller="subscription" method="post" class="ui form"
        params="[id: subscription.id]">

    <h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.copyDiscountScales.label')}</h2>
<ui:greySegment>
    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th></th>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>
            <th>${message(code: 'default.discount.label')}</th>
            <th>${message(code: 'default.note.label')}</th>
        </tr>
        </thead>
        <g:each in="${discountScales.sort{it.name}}" var="discountScale" status="i">
            <tr>
                <td><g:checkBox name="copyDiscountScale" value="${discountScale.id}" checked="false"/></td>
                <td>${i + 1}</td>
                <td><ui:xEditable overwriteEditable="false" owner="${discountScale}" field="name"/></td>
                <td><ui:xEditable overwriteEditable="false" owner="${discountScale}" field="discount"/></td>
                <td><ui:xEditable overwriteEditable="false" owner="${discountScale}" field="note" type="textarea"/></td>
            </tr>
        </g:each>
    </table>
</ui:greySegment>


    <div class="ui icon message">
        <i class="info icon"></i>
         <div class="content">
    <div class="header">
        ${message(code: 'subscription.details.copyDiscountScales.process.info')}
    </div>
         </div>
    </div>

    <ui:h1HeaderWithIcon message="myinst.currentSubscriptions.label" total="${num_sub_rows}" floated="true" />

        <div class="subscription-results">
            <g:if test="${subscriptions}">
                <table class="ui celled sortable table la-js-responsive-table la-table">
                    <thead>
                    <tr>
                        <th rowspan="2" class="center aligned"></th>
                        <th rowspan="2" class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <g:sortableColumn params="${params}" property="s.name"
                                          title="${message(code: 'subscription.slash.name')}"
                                          rowspan="2"/>
                        <th rowspan="2">
                            ${message(code: 'license.details.linked_pkg')}
                        </th>

                        <g:sortableColumn params="${params}" property="orgRole§provider"
                                          title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}"
                                          rowspan="2"/>

                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.startDate"
                                          title="${message(code: 'default.startDate.label')}"/>


                        <th scope="col" rowspan="2">
                            <a href="#" class="la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.numberOfLicenses.label')}"
                               data-position="top center">
                                <i class="university large icon"></i>
                            </a>
                        </th>
                        <th scope="col" rowspan="2">
                            <a href="#" class="la-popup-tooltip la-delay"
                               data-content="${message(code: 'subscription.numberOfCostItems.label')}"
                               data-position="top center">
                                <i class="money bill large icon"></i>
                            </a>
                        </th>
                        <th rowspan="2">
                            ${message(code: 'subscription.discountScales.label')}
                        </th>
                    </tr>

                    <tr>
                        <g:sortableColumn class="la-smaller-table-head" params="${params}" property="s.endDate"
                                          title="${message(code: 'default.endDate.label')}"/>
                    </tr>
                    </thead>
                    <g:each in="${subscriptions}" var="s" status="i">
                        <g:if test="${!s.instanceOf}">
                            <g:set var="childSubIds" value="${Subscription.executeQuery('select s.id from Subscription s where s.instanceOf = :parent',[parent:s])}"/>
                            <tr>
                                <td>
                                    <g:checkBox name="targetSubs" value="${s.id}" checked="false"/>
                                </td>
                                <td class="center aligned">
                                    ${(params.int('offset') ?: 0) + i + 1}
                                </td>
                                <td>
                                    <g:link controller="subscription" action="show" id="${s.id}">
                                        <g:if test="${s.name}">
                                            ${s.name}
                                        </g:if>
                                        <g:else>
                                            -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                                        </g:else>
                                        <g:if test="${s.instanceOf}">
                                            <g:if test="${s.consortia && s.consortia == institution}">
                                                ( ${s.subscriber.name} )
                                            </g:if>
                                        </g:if>
                                    </g:link>
                                    <g:each in="${allLinkedLicenses}" var="row">
                                        <g:if test="${s == row.destinationSubscription}">
                                            <g:set var="license" value="${row.sourceLicense}"/>
                                            <div class="la-flexbox la-minor-object">
                                                <i class="icon balance scale la-list-icon"></i>
                                                <g:link controller="license" action="show" id="${license.id}">${license.reference}</g:link><br />
                                            </div>
                                        </g:if>
                                    </g:each>
                                </td>
                                <td>
                                <!-- packages -->
                                    <g:each in="${s.packages.sort { it.pkg.name }}" var="sp" status="ind">
                                        <g:if test="${ind < 10}">
                                            <div class="la-flexbox">
                                                <i class="icon gift la-list-icon"></i>
                                                <g:link controller="subscription" action="index" id="${s.id}"
                                                        params="[pkgfilter: sp.pkg.id]"
                                                        title="${sp.pkg.contentProvider?.name}">
                                                    ${sp.pkg.name}
                                                </g:link>
                                            </div>
                                        </g:if>
                                    </g:each>
                                    <g:if test="${s.packages.size() > 10}">
                                        <div>${message(code: 'myinst.currentSubscriptions.etc.label', args: [s.packages.size() - 10])}</div>
                                    </g:if>
                                <!-- packages -->
                                </td>
                                <td>
                                    <g:each in="${s.providers}" var="org">
                                        <g:link controller="organisation" action="show"
                                                id="${org.id}">${org.name}</g:link><br />
                                    </g:each>
                                    <g:each in="${s.agencies}" var="org">
                                        <g:link controller="organisation" action="show"
                                                id="${org.id}">${org.name} (${message(code: 'default.agency.label')})</g:link><br />
                                    </g:each>
                                </td>
                                <td>
                                    <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br />
                                    <g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/>
                                </td>

                                <td>
                                    <g:link controller="subscription" action="members" params="${[id: s.id]}">
                                        ${childSubIds.size()}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link mapping="subfinance" controller="finance" action="index"
                                            params="${[sub: s.id]}">
                                        ${childSubIds.isEmpty() ? 0 : CostItem.executeQuery('select count(ci.id) from CostItem ci where ci.sub.id in (:subs) and ci.owner = :context and ci.costItemStatus != :deleted',[subs:childSubIds, context:institution, deleted:RDStore.COST_ITEM_DELETED])[0]}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="subscription" action="manageDiscountScale" id="${s.id}">
                                        ${s.discountScales.size()}
                                    </g:link>
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                </table>
            </g:if>
            <g:else>
                <g:if test="${filterSet}">
                    <br /><strong><g:message code="filter.result.empty.object"
                                           args="${[message(code: "subscription.plural")]}"/></strong>
                </g:if>
                <g:else>
                    <br /><strong><g:message code="result.empty.object"
                                           args="${[message(code: "subscription.plural")]}"/></strong>
                </g:else>
            </g:else>
        </div>

        <br />

        <div class="paginateButtons" style="text-align:center">
            <button type="submit" name="processCopyButton" value="yes" class="ui button">${message(code: 'subscription.details.copyDiscountScales.process')}</button>
        </div>

        <g:if test="${num_sub_rows}">
            <ui:paginate action="copyDiscountScales" controller="subscription" params="${params}"
                            max="${max}" total="${num_sub_rows}"/>
        </g:if>

    </g:form>

<laser:htmlEnd />