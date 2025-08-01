<%@ page import="de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.storage.PropertyStore; de.laser.properties.SubscriptionProperty; de.laser.Subscription; de.laser.storage.RDStore; de.laser.ui.Btn; de.laser.finance.CostItem; de.laser.ui.Icon" %>
<laser:serviceInjection/>
<div class="subscription-results subscription-results la-clear-before">
    <g:if test="${subscriptions}">
        <table class="ui celled sortable table la-table la-js-responsive-table">
            <thead>
            <tr>
                <g:if test="${tmplShowCheckbox}">
                    <th scope="col" rowspan="2" class="center aligned">
                        <g:checkBox name="subListToggler" id="subListToggler" checked="false"/>
                    </th>
                </g:if>
                <th scope="col" rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn params="${params}" property="s.name"
                                  title="${message(code: 'subscription.slash.name')}"
                                  rowspan="2" scope="col"/>
                <th rowspan="2" scope="col">
                    ${message(code: 'package.plural')}
                </th>

                <g:sortableColumn scope="col" params="${params}" property="provider" title="${message(code: 'provider.label')}" rowspan="2"/>
                <g:sortableColumn scope="col" params="${params}" property="vendor" title="${message(code: 'vendor.label')}" rowspan="2"/>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.startDate"
                                  title="${message(code: 'subscription.startDate.label')}"/>

                <th rowspan="2" class="two wide center aligned">
                    <ui:optionsIcon/>
                </th>
            </tr>

            <tr>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="s.endDate"
                                  title="${message(code: 'subscription.endDate.label')}"/>
            </tr>
            </thead>
            <g:each in="${subscriptions}" var="s" status="i">
                <tr>
                    <g:if test="${tmplShowCheckbox}">
                        <td class="center aligned">
                            <g:checkBox id="selectedSubs_${s.id}" name="selectedSubs" value="${s.id}" checked="false"/>
                        </td>
                    </g:if>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <%
                        LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(s, false)
                        Long navPrevSub = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                        Long navNextSub = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
                    %>
                    <th scope="row" class="la-th-column">
                            <g:if test="${s.name}">
                                ${s.name}
                                <g:if test="${s?.referenceYear}">
                                    ( ${s.referenceYear} )
                                </g:if>
                            </g:if>
                            <g:else>
                                -- ${message(code: 'myinst.currentSubscriptions.name_not_set')}  --
                            </g:else>
                        <g:each in="${allLinkedLicenses}" var="row">
                            <g:if test="${s == row.destinationSubscription}">
                                <g:set var="license" value="${row.sourceLicense}"/>
                                <div class="la-flexbox la-minor-object">
                                    <i class="${Icon.LICENSE} la-list-icon"></i>
                                        ${license.reference}<br/>
                                </div>
                            </g:if>
                        </g:each>
                    </th>
                    <td>
                    <!-- packages -->
                        <g:each in="${s.packages}" var="sp" status="ind">
                            <g:if test="${ind < 10}">
                                <div class="la-flexbox">
                                    <g:if test="${s.packages.size() > 1}">
                                        <i class="${Icon.PACKAGE} la-list-icon"></i>
                                    </g:if>
                                    <g:link controller="package" action="show" id="${sp.pkg.id}"
                                            title="${sp.pkg.provider?.name}">
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
                        <g:each in="${s.providers}" var="provider">
                            <g:link controller="provider" action="show" id="${provider.id}">${fieldValue(bean: provider, field: "name")}
                                <g:if test="${provider.abbreviatedName}">
                                    <br/> (${fieldValue(bean: provider, field: "abbreviatedName")})
                                </g:if>
                            </g:link><br/>
                        </g:each>
                    </td>

                    <td>
                        <g:each in="${s.vendors}" var="vendor">
                            <g:link controller="vendor" action="show" id="${vendor.id}">
                                ${fieldValue(bean: vendor, field: "name")}
                                <g:if test="${vendor.abbreviatedName}">
                                    <br/> (${fieldValue(bean: vendor, field: "abbreviatedName")})
                                </g:if>
                            </g:link><br/>
                        </g:each>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${s.startDate}"/><br/>
                        <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label.shy')}:"><g:formatDate
                                formatName="default.date.format.notime" date="${s.endDate}"/></span>
                    </td>
                    <td class="x">
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <g:if test="${filterSet}">
            <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
        </g:else>
    </g:else>
</div>

<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#subListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
            }
        })
    </laser:script>

</g:if>