<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.License; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.addressbook.Person; de.laser.Subscription" %>

<laser:htmlStart text="${message(code:'license.details.incoming.childs',args:[message(code:'consortium.subscriber')])}" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <g:if test="${contextService.isInstEditor()}">
            <laser:render template="${customerTypeService.getActionsTemplatePath()}" />
        </g:if>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon visibleProviders="${visibleProviders}">
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>
    <ui:totalNumber total="${validMemberLicenses.size() ?: 0}"/>
    <ui:anualRings object="${license}" controller="license" action="members" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

<laser:render template="${customerTypeService.getNavTemplatePath()}" />

<ui:filter>
    <g:form action="members" controller="license" params="${[id:params.id]}" method="get" class="ui form">
        <div class="three fields">
            <%--
            <div class="field">
                <label for="subscription">${message(code:'subscription')}</label>
                <select id="subscription" name="subscription" multiple="" class="ui selection fluid dropdown">
                    <option value="">${message(code:'default.select.choose.label')}</option>
                    <g:each in="${subscriptionsForFilter}" var="sub">
                        <option <%=Params.getLongList(params, 'subscription').contains(sub.id) ? 'selected="selected"' : '' %> value="${sub.id}">${sub.dropdownNamingConvention()}</option>
                    </g:each>
                </select>
            </div>
            --%>
            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>

            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <ui:select class="ui dropdown clearable" name="status"
                              from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="two fields">
            <div class="field">
                <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                            <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox" <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" <g:if test="${params.subRunTime}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input name="filterSet" type="hidden" value="true">
                <input type="submit" value="${message(code:'default.button.filter.label')}" class="${Btn.PRIMARY}"/>
            </div>
        </div>
    </g:form>
</ui:filter>

<table class="ui celled la-js-responsive-table la-table table">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'license.member')}</th>
            <th class="la-no-uppercase">
                <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                      data-content="${message(code: 'default.previous.label')}">
                    <i class="${Icon.LNK.PREV}"></i>
                </span>
            </th>
            <th>${message(code:'default.startDate.label.shy')}</th>
            <th>${message(code:'default.endDate.label.shy')}</th>
            <th class="la-no-uppercase">
                <span class="la-long-tooltip la-popup-tooltip" data-position="bottom center"
                      data-content="${message(code: 'default.next.label')}">
                    <i class="${Icon.LNK.NEXT}"></i>
                </span>
            </th>
            <th>${message(code: 'license')}</th>
            <th>${message(code: 'license.subs.count')}</th>
        </tr>
    </thead>
    <tbody>

        <g:each in="${validMemberLicenses}" status="i" var="row">
            <g:set var="lic" value="${row.license}"/>
            <%
                LinkedHashMap<String, List> links = linksGenerationService.generateNavigation(lic)
                License navPrevLicense = (links?.prevLink && links?.prevLink?.size() > 0) ? links?.prevLink[0] : null
                License navNextLicense = (links?.nextLink && links?.nextLink?.size() > 0) ? links?.nextLink[0] : null
            %>
            <tr>
                <td>${i + 1}</td>
                <td>${lic.reference}</td>
                <td class="center aligned">
                    <g:if test="${navPrevLicense}">
                        <g:link controller="license" action="show" id="${navPrevLicense.id}"><i class="${Icon.LNK.PREV}"></i></g:link>
                    </g:if>
                </td>
                <td><g:formatDate formatName="default.date.format.notime" date="${lic.startDate}"/></td>
                <td><g:formatDate formatName="default.date.format.notime" date="${lic.endDate}"/></td>
                <td class="center aligned">
                    <g:if test="${navNextLicense}">
                        <g:link controller="license" action="show" id="${navNextLicense.id}"><i class="${Icon.LNK.NEXT}"></i></g:link>
                    </g:if>
                </td>
                <td>
                    <g:link controller="license" action="show" id="${lic.id}" class="${Btn.MODERN.SIMPLE}"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                        <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i></g:link>
                </td>
                <td>
                    <g:if test="${row.subs > 0}">
                        <g:link action="linkedSubs" id="${lic.id}"><ui:totalNumber total="${row.subs}"/></g:link>
                    </g:if>
                    <g:else>
                        <g:link action="linkMemberLicensesToSubs" id="${lic.id}"><ui:totalNumber total="${row.subs}"/></g:link>
                    </g:else>
                </td>
            </tr>
        </g:each>

    </tbody>
</table>

<laser:htmlEnd />
