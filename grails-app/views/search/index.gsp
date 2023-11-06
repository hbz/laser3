<%@ page import="de.laser.utils.LocaleUtils; de.laser.utils.DateUtils; de.laser.survey.SurveyConfig; de.laser.I10nTranslation; de.laser.RefdataValue; de.laser.DocContext;de.laser.storage.RDStore; java.text.SimpleDateFormat;" %>

<laser:htmlStart message="search.advancedSearch" serviceInjection="true"/>

<%
    SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMddTHHmmssZ()
    SimpleDateFormat sdfNoTime = DateUtils.getLocalizedSDF_noTime()
    String languageSuffix = LocaleUtils.getCurrentLang()
    String period
%>

<ui:breadcrumbs>
    <ui:crumb message="search.advancedSearch" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="search.advancedSearch" type="Search" />

<%
    def addFacet = { params, facet, val ->
        Map newparams = [:]
        newparams.putAll(params)
        newparams.remove('offset');
        newparams.remove('max');
        def current = newparams[facet]
        if (current == null) {
            newparams[facet] = val
        } else if (current instanceof String[]) {
            newparams.remove(current)
            newparams[facet] = current as List
            newparams[facet].add(val);
        } else {
            newparams[facet] = [current, val]
        }
        newparams
    }
    def removeFacet = { params, facet, val ->
        Map newparams = [:]
        newparams.putAll(params)
        def current = newparams[facet]
        newparams.remove('offset');
        newparams.remove('max');
        if (current == null) {
        } else if (current instanceof String[]) {
            newparams.remove(current)
            newparams[facet] = current as List
            newparams[facet].remove(val);
        } else if (current?.equals(val.toString())) {
            newparams.remove(facet);
        }
        newparams
    }
%>




<div class="ui la-search segment">
    <g:form action="index" controller="search" method="post" class="ui form" >

        <g:each in="${['rectype', 'endYear', 'startYear', 'consortiaName', 'providerName', 'status']}" var="facet">
            <g:each in="${params.list(facet)}" var="selected_facet_value">
                <input type="hidden" name="${facet}" value="${selected_facet_value}"/>
            </g:each>
        </g:each>

        <div class="field">
            <label><g:message code="search.text" />:</label>
            <input type="text" placeholder="${message(code: 'search.placeholder')}" name="q" value="${params.q}"/>
        </div>


        <div class="fields">
            <div class="three wide field">
                <g:select class="ui dropdown"
                          from="${['AND', 'OR', 'NOT']}"
                          name="advancedSearchOption"
                          valueMessagePrefix="search.advancedSearch.option"
                          value="${params.advancedSearchOption}"/>
            </div>

            <div class="fourteen wide field">
                <input type="text" placeholder="${message(code: 'search.placeholder')}"
                       name="advancedSearchText" value="${params.advancedSearchText}"/>
            </div>
        </div>

        <div class="fields">
            <div class="three wide field">
                <g:select class="ui dropdown"
                          from="${['AND', 'OR', 'NOT']}"
                          name="advancedSearchOption2"
                          valueMessagePrefix="search.advancedSearch.option"
                          value="${params.advancedSearchOption2}"/>
            </div>

            <div class="fourteen wide field">
                <input type="text" placeholder="${message(code: 'search.placeholder')}"
                       name="advancedSearchText2" value="${params.advancedSearchText2}"/>
            </div>
        </div>

        <div class="fields">
            <div class="three wide field">
                <g:select class="ui dropdown"
                          from="${['AND', 'OR', 'NOT']}"
                          name="advancedSearchOption3"
                          valueMessagePrefix="search.advancedSearch.option"
                          value="${params.advancedSearchOption3}"/>
            </div>

            <div class="fourteen wide field">
                <input type="text" placeholder="${message(code: 'search.placeholder')}"
                       name="advancedSearchText3" value="${params.advancedSearchText3}"/>
            </div>
        </div>

        <div class="field">
            <g:if test="${contextOrg.isCustomerType_Consortium()}">
                <div class="ui checkbox">
                    <input type="checkbox" name="showMembersObjects" tabindex="0" ${params.showMembersObjects ? 'checked' : ''}>
                    <label><g:message code="search.advancedSearch.showMembersObjects"/></label>
                </div>
            </g:if>
        </div>

        <div class="three fields">
            <div class="field">
                <label>${message(code: 'search.objects')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioAllObjects" type="radio" value="allObjects" name="searchObjects"
                                   tabindex="0" class="hidden"
                                   <g:if test="${params.searchObjects == 'allObjects'}">checked=""</g:if>>
                            <label for="radioAllObjects">
                                <g:message code="menu.public"/>
                            </label>
                        </div>
                    </div>

                    <div class="field">
                        <div class="ui radio checkbox">
                            <input id="radioMyObjects" type="radio" value="myObjects" name="searchObjects"
                                   tabindex="0"
                                   class="hidden"
                                   <g:if test="${params.searchObjects == 'myObjects'}">checked=""</g:if>>
                            <label for="radioMyObjects">${message(code: 'menu.my')}</label>
                        </div>
                    </div>
                </div>

            </div>

            <div class="field">

            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset secondary button">${message(code: 'default.button.searchreset.label')}</a>
                <button name="search" type="submit" value="true" class="ui primary button">
                    <g:message code="search.button" />
                </button>
            </div>
        </div>

    </g:form>
</div>

<g:if test="${hits}">
        <br />
        <div class="ui stackable grid">
            <div class="four wide column">
                <div class="ui la-filter segment">

                    <h3 class="ui header"><i class="circular filter inverted icon la-filter-icon"></i> Filter</h3>

                    <g:each in="${facets}" var="facet">
                        <div class="panel panel-default">
                            <div class="panel-heading">
                                <h4 class="ui header"><g:message code="facet.so.${facet.key}"
                                                                 default="${facet.key}"/></h4>
                            </div>

                            <div class="panel-body">
                                <ul>
                                    <g:each in="${facet.value.sort {
                                        message(code: "facet.so.${facet.key}.${it.display.toLowerCase()}")
                                    }}" var="v">
                                        <li>

                                            <g:if test="${params.list(facet.key).contains(v.term.toString())}">
                                                <g:if test="${facet.key == 'rectype'}">
                                                    ${message(code: "facet.so.${facet.key}.${v.display.toLowerCase()}")} (${v.count})
                                                </g:if>
                                                <g:elseif test="${facet.key == 'status'}">
                                                    ${v.display}
                                                    ${RefdataValue.getByValue(v.display) ? RefdataValue.getByValue(v.display).getI10n('value') : v.display} (${v.count})
                                                </g:elseif>
                                                <g:else>
                                                    ${v.display} (${v.count})
                                                </g:else>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="search" action="index"
                                                        params="${addFacet(params, facet.key, v.term)}">
                                                    <g:if test="${facet.key == 'rectype'}">
                                                        ${message(code: "facet.so.${facet.key}.${v.display.toLowerCase()}")}
                                                    </g:if>
                                                    <g:elseif test="${facet.key == 'status'}">
                                                        ${RefdataValue.getByValue(v.display) ? RefdataValue.getByValue(v.display).getI10n('value') : v.display}
                                                    </g:elseif>
                                                    <g:else>
                                                        ${v.display}
                                                    </g:else>
                                                </g:link> (${v.count})
                                            </g:else>
                                        </li>
                                    </g:each>
                                </ul>
                            </div>
                        </div>
                    </g:each>
                </div>
            </div>
            <div class="twelve wide column">
                <h3 class="ui header">${message(code: 'search.search.filter')} <ui:totalNumber total="${resultsTotal}"/></h3>
                <p>
                    <g:each in="${['rectype', 'endYear', 'startYear', 'consortiaName', 'providerName', 'status']}" var="facet">
                        <g:each in="${params.list(facet)}" var="fv">

                            <span class="ui label la-advanced-label"><g:message code="facet.so.${facet}"/>:

                                <g:if test="${facet == 'rectype'}">
                                    ${message(code: "facet.so.${facet}.${fv.toLowerCase()}")}
                                </g:if>
                                <g:elseif test="${facet == 'status'}">
                                    ${RefdataValue.getByValue(fv) ? RefdataValue.getByValue(fv).getI10n('value') : fv}
                                </g:elseif>
                                <g:else>
                                    ${fv}
                                </g:else>

                                <g:link controller="search" action="index" params="${removeFacet(params, facet, fv)}">
                                    <i class="delete icon"></i>
                                </g:link>
                            </span>
                        </g:each>
                    </g:each>
                </p>
                <table class="ui sortable celled la-js-responsive-table la-table table">
                    <thead>
                        <tr>
                            <th class="six wide">Title/Name</th>
                            <th class="ten wide ">${message(code: 'search.additionalinfo')}</th>
                        </tr>
                    </thead>
                    <g:each in="${hits}" var="hit">
                        <g:set var="object" value="${hit.getSourceAsMap()}" />

                        <%
                            if (object.objectClassName) {
                                // workaround: hibernate proxies
                                String subst = object.objectClassName.split('\\$hibernateproxy\\$')[0]
                                object.objectClassName = subst
                                //obj.url = obj.url.replace( URLEncoder.encode(obj.ocn), subst )
                                //obj.description = obj.description.replace( 'search.object.' + obj.ocn + ': ', '')
                            }
                        %>
                        <tr>
                            <g:if test="${object.rectype == 'Org'}">
                                <g:set var="providerAgency" value="${RDStore.OT_PROVIDER.value in object.type?.value || RDStore.OT_AGENCY.value in object.type?.value }"/>
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${(providerAgency) ? message(code: 'spotlight.provideragency') : message(code: 'spotlight.'+object.rectype.toLowerCase())}">
                                        <i class="circular icon la-organisation"></i>
                                    </span>

                                    <g:link controller="organisation" action="show"
                                            id="${object.dbId}">${object.name}</g:link>

                                </td>
                                <td>
                                    <strong><g:message code="org.orgType.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.type?.sort { it.getAt('value_'+languageSuffix) }}" var="type">
                                            <div class="item">
                                            ${type.getAt('value_'+languageSuffix)}
                                            </div>
                                        </g:each>
                                    </div>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                    <strong><g:message code="org.platforms.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.platforms?.sort { it.name }}" var="platform">
                                            <div class="item">
                                            <g:link controller="platform" action="show"
                                                    id="${platform.dbId}">${platform.name}</g:link>
                                            </div>
                                        </g:each>
                                    </div>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'TitleInstance'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.typTitle?.toLowerCase()}")}">
                                        <i class="circular icon la-${object.typTitle?.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="title" action="show"
                                            id="${object.dbId}">${object.name}</g:link>
                                </td>
                                <td>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'BookInstance'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.typTitle?.toLowerCase()}")}">
                                        <i class="circular icon la-${object.typTitle?.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="title" action="show"
                                            id="${object.dbId}">${object.name}</g:link>
                                </td>
                                <td>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'DatabaseInstance'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.typTitle?.toLowerCase()}")}">
                                        <i class="circular icon la-${object.typTitle?.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="title" action="show"
                                            id="${object.dbId}">${object.name}</g:link>
                                </td>
                                <td>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'JournalInstance'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.typTitle?.toLowerCase()}")}">
                                        <i class="circular icon la-${object.typTitle?.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="title" action="show"
                                            id="${object.dbId}">${object.name}</g:link>
                                </td>
                                <td>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'Package'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon la-${object.rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="package" action="show"
                                            id="${object.dbId}">${object.name}</g:link>
                                </td>
                                <td>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                    <strong>${message(code: 'package.compare.overview.tipps')}</strong>:
                                    <g:link controller="package" action="index"
                                        id="${object.dbId}">${object.titleCountCurrent}</g:link>

                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'Platform'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon la-${object.rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="platform" action="show"
                                            id="${object.dbId}">${object.name}</g:link>
                                </td>
                                <td>
                                    <strong>${message(code: 'package.compare.overview.tipps')}</strong>:
                                        <g:link controller="platform" action="platformTipps"
                                        id="${object.dbId}">${object.titleCountCurrent}</g:link>
                                    <br />
                                    <strong>${message(code: 'platform.primaryURL')}</strong>:
                                        <g:if test="${object.primaryUrl}">
                                            ${object.primaryUrl}
                                            <ui:linkWithIcon href="${object.primaryUrl}"/>
                                        </g:if>
                                    <br />
                                    <strong>${message(code: 'platform.provider')}</strong>:
                                <g:link controller="organisation" action="show"
                                        id="${object.orgId}">${object.orgName}</g:link>

                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'Subscription'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon la-${object.rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="subscription" action="show"
                                            id="${object.dbId}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <%
                                        period = object.startDate ? sdfNoTime.format( sdf.parse(object.startDate) ) : ''
                                        period = object.endDate ? period + ' - ' + sdfNoTime.format( sdf.parse(object.endDate) ) : ''
                                        period = period ? period : ''
                                    %>
                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>

                                    <strong>${message(code: 'subscription.status.label')}</strong>: ${object.status?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'default.type.label')}</strong>: ${object.type?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'subscription.periodOfValidity.label')}</strong>: ${period}
                                    <br />
                                    <g:if test="${object.membersCount && contextOrg.isCustomerType_Consortium()}">
                                        <strong>${message(code: 'subscription.details.consortiaMembers.label')}</strong>:
                                        <g:link controller="subscription" action="members"
                                                id="${object.dbId}">${object.membersCount}</g:link>
                                    </g:if>
                                    <g:if test="${object.members && contextOrg.isCustomerType_Consortium()}">
                                        <strong>${message(code: 'subscription.details.consortiaMembers.label')}</strong>:
                                        <article class="la-readmore">
                                        <g:each in="${object.members}" var="member">
                                        <g:link controller="subscription" action="members"
                                                id="${object.dbId}">${member.name}</g:link>
                                        </g:each>
                                        </article>
                                    </g:if>
                                    <br />
                                    <g:if test="${RDStore.SUBSCRIPTION_TYPE_CONSORTIAL.value in object.type?.value && !(contextOrg.isCustomerType_Consortium())}">
                                    <strong>${message(code: 'facet.so.consortiaName')}</strong>: ${object.consortiaName}
                                    </g:if>

                                    <strong><g:message code="subscription.packages.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.packages?.sort { it.pkgname }}" var="pkg">
                                            <div class="item">
                                                <g:link controller="package" action="show"
                                                    id="${pkg.pkgid}">${pkg.pkgname} ${pkg.providerName ? '('+pkg.providerName+')' : ''}</g:link>
                                            </div>
                                        </g:each>
                                    </div>
                                </td>
                            </g:if>
                            <g:if test="${object.rectype == 'License'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon la-${object.rectype.toLowerCase()}"></i>
                                    </span>

                                    <g:link controller="license" action="show"
                                            id="${object.dbId}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <%
                                        period = object.startDate ? sdfNoTime.format( sdf.parse(object.startDate) ) : ''
                                        period = object.endDate ? period + ' - ' + sdfNoTime.format( sdf.parse(object.endDate) ) : ''
                                        period = period ? period : ''
                                    %>

                                    <strong><g:message code="default.identifiers.label"/></strong>:
                                    <div class="ui bulleted list">
                                        <g:each in="${object.identifiers?.sort { it.type }}" var="id">
                                            <div class="item">
                                                ${id.type}: ${id.value} &nbsp;
                                            </div>
                                        </g:each>
                                    </div>
                                    <strong>${message(code: 'default.status.label')}</strong>: ${object.status?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'default.type.label')}</strong>: ${object.type?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'subscription.periodOfValidity.label')}</strong>: ${period}
                                    <br />
                                    <g:if test="${object.membersCount && contextOrg.isCustomerType_Consortium()}">
                                        <strong>${message(code: 'subscription.details.consortiaMembers.label')}</strong>:
                                        <g:link controller="license" action="members"
                                                id="${object.dbId}">${object.membersCount}</g:link>
                                    </g:if>
                                    <g:if test="${object.members && contextOrg.isCustomerType_Consortium()}">
                                        <strong>${message(code: 'subscription.details.consortiaMembers.label')}</strong>:
                                        <g:link controller="subscription" action="members" id="${object.dbId}"> ${object.members.size()}</g:link>
                                        <article class="la-readmore">
                                        <g:each in="${object.members}" var="member">
                                            ${member.name},
                                        </g:each>
                                        </article>
                                    </g:if>
                                    <br />
                                    <g:if test="${!(contextOrg.isCustomerType_Consortium())}">
                                        <strong>${message(code: 'facet.so.consortiaName')}</strong>: ${object.consortiaName}
                                    </g:if>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'SurveyConfig'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted pink chart pie"></i>
                                    </span>

                                    <g:link controller="survey" action="show"
                                            id="${SurveyConfig.get(object.dbId).surveyInfo.id}"
                                            params="[surveyConfigID: object.dbId]">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>

                                </td>
                                <td>
                                    <%
                                        period = object.startDate ? sdfNoTime.format( sdf.parse(object.startDate) ) : ''
                                        period = object.endDate ? period + ' - ' + sdfNoTime.format( sdf.parse(object.endDate) ) : ''
                                        period = period ? period : ''
                                    %>

                                    <strong>${message(code: 'default.status.label')}</strong>: ${object.status?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'renewalEvaluation.period')}</strong>: ${period}
                                    <br />
                                    <g:if test="${contextOrg.isCustomerType_Consortium()}">
                                        <strong>${message(code: 'surveyParticipants.label')}</strong>: ${object.membersCount}
                                    </g:if>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'SurveyOrg'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted pink chart pie"></i>
                                    </span>

                                    <g:link controller="myInstitution" action="currentSurveys"
                                            params="${[name: '"'+object.name+'"', startDate: '"'+object.startDate+'"']}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <%
                                        period = object.startDate ? sdfNoTime.format( sdf.parse(object.startDate) ) : ''
                                        period = object.endDate ? period + ' - ' + sdfNoTime.format( sdf.parse(object.endDate) ) : ''
                                        period = period ? period : ''
                                    %>

                                    <strong>${message(code: 'default.status.label')}</strong>: ${object.status?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'renewalEvaluation.period')}</strong>: ${period}
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'Task'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted green calendar check outline"></i>
                                    </span>

                                    <g:link controller="myInstitution" action="tasks"
                                            params="[taskName: object.name]">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <g:if test="${object.objectClassName}">
                                    <strong>${message(code: 'task.typ')}</strong>:${message(code: 'search.object.'+object.objectClassName)}
                                    <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.objectName}</g:link>
                                    </g:if>
                                    <g:else>
                                        ${message(code: 'task.general')}
                                    </g:else>
                                    <br />
                                    <strong>${message(code: 'task.status.label')}</strong>: ${object.status?.getAt('value_'+languageSuffix) }
                                    <br />
                                    <strong>${message(code: 'task.endDate.label')}</strong>:
                                        <g:if test="${hit.getSourceAsMap()?.endDate}">
                                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(object.endDate)}"/>
                                        </g:if>
                                    <br />
                                    <strong>${message(code: 'default.description.label')}</strong>: <article class="la-readmore">${hit.getSourceAsMap()?.description}</article>
                                </td>
                            </g:if>
                            <g:if test="${object.rectype == 'Note'}">
                                <td>

                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                            <i class="circular icon inverted grey sticky note"></i>
                                    </span>

                                    <g:link controller="${object.objectClassName}" action="notes" id="${object.objectId}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong>${message(code: 'search.object.'+object.objectClassName)}</strong>:
                                    <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.objectName}</g:link>
                                    <br />
                                    <strong>${message(code: 'default.description.label')}</strong>: <article class="la-readmore">${hit.getSourceAsMap()?.description}</article>
                                </td>
                            </g:if>

                            <g:if test="${object.rectype == 'Document'}">
                                <td>

                                    <g:set var="docContext" value="${DocContext.get(object.dbId)}"/>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular icon inverted grey file alternate outline"></i>
                                    </span>


                                    <g:link controller="${object.objectClassName}" action="documents" id="${object.objectId}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong>${message(code: 'search.object.'+object.objectClassName)}</strong>:
                                    <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.objectName}</g:link>
                                    <br />
                                    <strong>${message(code: 'license.docs.table.type')}</strong>: ${docContext ? docContext.owner?.type?.getI10n('value'): ""}

                                </td>
                            </g:if>
                            <g:if test="${object.rectype == 'IssueEntitlement'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        <i class="circular la-book icon"></i>
                                    </span>

                                    <g:link controller="${object.objectClassName}" action="index" id="${object.objectId}" params="[filter: object.name]">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong>${message(code: 'search.object.'+object.objectClassName)}</strong>:
                                    <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.objectName}</g:link>
                                    <br />

                                </td>
                            </g:if>
                            <g:if test="${object.rectype == 'SubscriptionProperty'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                        Subscription<i class="circular la-subscription icon"></i>
                                    </span>

                                    <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong>${message(code: 'search.object.'+object.objectClassName)}</strong>:
                                <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.objectName}</g:link>
                                    <br />
                                    <strong>${message(code: 'default.description.label')}</strong>: <article class="la-readmore">${hit.getSourceAsMap()?.description}</article>
                                </td>
                            </g:if>
                            <g:if test="${object.rectype == 'LicenseProperty'}">
                                <td>
                                    <span data-position="top right" class="la-popup-tooltip la-delay"
                                          data-content="${message(code: "facet.so.rectype.${object.rectype.toLowerCase()}")}">
                                            <i class="circular la-license icon" ></i>
                                    </span>

                                    <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.name}</g:link>

                                    <div class="ui grid">
                                        <div class="right aligned wide column">
                                            <g:if test="${object.visible == 'Private'}">
                                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                                      data-content="${message(code: 'search.myObject')}">
                                                    <i class="shield alternate grey large icon"></i>
                                                </span>
                                            </g:if>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <strong>${message(code: 'search.object.'+object.objectClassName)}</strong>:
                                <g:link controller="${object.objectClassName}" action="show" id="${object.objectId}">${object.objectName}</g:link>
                                    <br />
                                    <strong>${message(code: 'default.description.label')}</strong>: <article class="la-readmore">${hit.getSourceAsMap()?.description}</article>
                                </td>
                            </g:if>
                        </tr>
                    </g:each>
                </table>

                <ui:paginate action="index" controller="search" params="${params}"
                                max="${max}" total="${resultsTotal}"/>

            </div>
        </div>

</g:if>

<laser:htmlEnd />
