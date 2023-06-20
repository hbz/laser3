<%@ page import="de.laser.CustomerTypeService; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>

<laser:htmlStart message="menu.my.currentSubscriptionsTransfer" serviceInjection="true" />

        <ui:breadcrumbs>
            <ui:crumb message="menu.my.currentSubscriptionsTransfer" class="active" />
        </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.my.currentSubscriptionsTransfer" total="${num_sub_rows}" floated="true" />

    <ui:messages data="${flash}"/>

    <div class="subscription-results subscription-results la-clear-before">
        <g:if test="${subscriptions}">
            <table class="ui celled sortable table la-table la-js-responsive-table">
                <thead>
                <tr>
                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'sidewide.number')}
                    </th>
                    <g:sortableColumn scope="col" params="${params}" property="providerAgency" title="${message(code: 'default.provider.label')} / ${message(code: 'default.agency.label')}" rowspan="2" />

                    <g:sortableColumn params="${params}" property="s.name" title="${message(code: 'subscription')}" rowspan="2" scope="col" />

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate" title="${message(code: 'default.startDate.label')}"/>

                    <g:sortableColumn params="${params}" property="s.manualCancellationDate" title="${message(code: 'subscription.manualCancellationDate.label')}" rowspan="2" scope="col" />

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.offerRequested" title="${message(code: 'subscription.offerRequested.label')}"/>

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'subscription.offerNote.label')}
                    </th>

                    <g:sortableColumn params="${params}" property="s.offerAccepted" title="${message(code: 'subscription.offerAccepted.label')}" rowspan="2" scope="col" />

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'survey.label')}
                    </th>

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'subscription.survey.evaluation.label')}
                    </th>

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'subscription.survey.cancellation.label')}
                    </th>

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'subscription.discountScale.label')}
                    </th>

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'subscription.renewalFile.label')}
                    </th>

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.renewalSent" title="${message(code: 'subscription.renewalSent.label')}"/>

                    <th scope="col" rowspan="2" class="center aligned">
                        ${message(code:'subscription.participantTransferWithSurvey.label')}
                    </th>

                </tr>
                <tr>
                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate" title="${message(code: 'default.endDate.label')}"/>

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.offerRequestedDate" title="${message(code: 'subscription.offerRequestedDate.label')}"/>

                    <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.offerRequestedDate" title="${message(code: 'subscription.renewalSentDate.label')}"/>
                </tr>
                </thead>
                <tbody>
                <g:each in="${subscriptions}" var="s" status="i">
                    <tr>
                        <td class="center aligned">
                            ${ (params.int('offset') ?: 0)  + i + 1 }
                        </td>
                        <td>
                        <%-- as of ERMS-584, these queries have to be deployed onto server side to make them sortable --%>
                            <g:each in="${s.providers}" var="org">
                                <g:link controller="organisation" action="show" id="${org.id}">${fieldValue(bean: org, field: "name")}
                                    <g:if test="${org.sortname}">
                                        <br />
                                        (${fieldValue(bean: org, field: "sortname")})
                                    </g:if>
                                </g:link><br />
                            </g:each>
                            <g:each in="${s.agencies}" var="org">
                                <g:link controller="organisation" action="show" id="${org.id}">
                                    ${fieldValue(bean: org, field: "name")}
                                    <g:if test="${org.sortname}">
                                        <br />
                                        (${fieldValue(bean: org, field: "sortname")})
                                    </g:if> (${message(code: 'default.agency.label')})
                                </g:link><br />
                            </g:each>
                        </td>
                        <th scope="row" class="la-th-column">
                            <g:link controller="subscription" class="la-main-object" action="show" id="${s.id}">
                                <g:if test="${s.name}">
                                    ${s.name}
                                </g:if>
                                <g:else>
                                    -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                                </g:else>
                            </g:link>
                        </th>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                            <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label')}:"><g:formatDate formatName="default.date.format.notime" date="${s.endDate}"/></span>
                        </td>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${s.manualCancellationDate}"/>
                        </td>
                        <td>

                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
        <g:else>
            <g:if test="${filterSet}">
                <br /><strong><g:message code="filter.result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
            </g:if>
            <g:else>
                <br /><strong><g:message code="result.empty.object" args="${[message(code:"subscription.plural")]}"/></strong>
            </g:else>
        </g:else>

    </div>

<g:if test="${subscriptions}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                 max="${max}" total="${num_sub_rows}" />
</g:if>


<laser:htmlEnd />
