<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.wekb.TitleInstancePackagePlatform; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.base.AbstractReport" %>
<laser:serviceInjection />
<g:set var="action" value="${action ?: actionName}"/>
<g:set var="forTitles" value="${forTitles ?: actionName}"/>
<g:set var="configMap" value="${configMap ?: params}"/>
<g:set var="availableStatus" value="${RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS)-RDStore.TIPP_STATUS_REMOVED}" />
<g:set var="disableFilter" value="${(num_tipp_rows && num_tipp_rows > 100000)}" />

<g:if test="${action == 'currentPermanentTitles'}">
    <g:set var="availableStatus" value="${availableStatus-RDStore.TIPP_STATUS_EXPECTED}"/>
</g:if>

<ui:filter>
    <g:form controller="${controllerName}" action="${action}" id="${params.id}" method="get" class="ui form">
        <g:hiddenField name="sort" value="${params.sort}"/>
        <g:hiddenField name="order" value="${params.order}"/>

        <g:hiddenField name="surveyConfigID" value="${params.surveyConfigID}"/>
        <g:hiddenField name="tab" value="${params.tab}"/>
        <g:hiddenField name="tabStat" value="${params.tabStat}"/>
        %{--<g:hiddenField name="titleGroup" value="${params.titleGroup}"/>--}%

        <div class="four fields">
            <div class="field">
                <label for="filter">${message(code: 'default.search.text')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip"
                          data-content="${message(code: 'default.search.tooltip.tipp')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>
                <input name="filter" id="filter" value="${params.filter}"/>
            </div>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" class="la-popup-tooltip"
                          data-content="${message(code: 'default.search.tooltip.identifier')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>
                <input name="identifier" id="identifier" value="${params.identifier}"/>
            </div>

            <g:if test="${controllerName == 'subscription'}">
                <div class="field">
                    <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                    <select class="ui dropdown clearable" name="pkgfilter" id="pkgfilter">
                        <option value="">${message(code: 'default.all')}</option>
                        <g:each in="${subscription.packages}" var="sp">
                            <option value="${sp.pkg.id}" ${sp.pkg.id == params.long('pkgfilter') ? 'selected=true' : ''}>${sp.pkg.name}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <%-- removed as of ERMS-6370
            <g:if test="${!showStatsFilter && action != 'renewEntitlementsWithSurvey'}">
                <div class="field">
                    <ui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                      value="${params.asAt}"
                                      placeholder="subscription.details.asAt.placeholder"/>
                </div>
            </g:if>
            --%>

            <g:if test="${!disableStatus && !showStatsFilter && !(action in ['renewEntitlementsWithSurvey', 'current', 'planned', 'expired', 'deleted'])}">
                <div class="field">
                    <label for="status">${message(code: 'default.status.label')}</label>
                    <select name="status" id="status" multiple=""
                            class="ui search selection dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${availableStatus}" var="status">
                            <option <%=Params.getLongList(params, 'status').contains(status.id) ? 'selected="selected"' : ''%>
                                    value="${status.id}">
                                ${status.getI10n('value')}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <%-- removed as of ERMS-6370
            <div class="field">
                <label for="coverageDepth"><g:message code="tipp.coverageDepth"/></label>
                <div class="ui search selection fluid multiple dropdown" id="coverageDepth">
                    <input type="hidden" name="coverageDepth"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>
            --%>
        </div>

        <div class="four fields">
            <div class="field">
                <label for="provider">${message(code: 'tipp.provider')}</label>
                <div class="ui search selection fluid multiple dropdown" id="provider">
                    <input type="hidden" name="provider"/>
                    <div class="default text"><g:message code="select2.minChars.note"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>

            <div class="field">
                <label for="publishers">${message(code: 'tipp.publisher')}</label>
                <div class="ui search selection fluid multiple dropdown" id="publishers">
                    <input type="hidden" name="publishers"/>
                    <div class="default text"><g:message code="select2.minChars.note"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>

            <div class="field">
                <label for="first_author">${message(code: 'tipp.firstAuthor')}</label>
                <input name="first_author" id="first_author" value="${params.first_author}"/>
            </div>

            <div class="field">
                <label for="first_editor">${message(code: 'tipp.firstEditor')}</label>
                <input name="first_editor" id="first_editor" value="${params.first_editor}"/>
            </div>
        </div>

        <div class="three fields">
            <%--
            <div class="field ${disableFilter ? 'disabled' : ''}">
                <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="series_names">
                    <input type="hidden" name="series_names"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>

                <select name="series_names" id="series_names" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${seriesNames}" var="seriesName">
                        <option <%=(params.list('series_names')?.contains(seriesName)) ? 'selected="selected"' : ''%>
                                value="${seriesName}">
                            ${seriesName}
                        </option>
                    </g:each>
                </select>

            </div>
            --%>

            <div class="field">
                <label for="title_types">${message(code: 'default.search.titleTyp')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip"
                          data-content="${message(code: 'default.search.tooltip.titleTyp')}">
                        <i class="${Icon.TOOLTIP.HELP}"></i>
                    </span>
                </label>
                <div class="ui search selection fluid multiple dropdown" id="title_types">
                    <input type="hidden" name="title_types"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>

            <div class="field">
                <label for="openAccess">${message(code: 'tipp.openAccess')}</label>
                <select name="openAccess" id="openAccess" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.LICENSE_OA_TYPE)+RDStore.GENERIC_NULL_VALUE}" var="openAccess">
                        <option <%=(Params.getRefdataList(params, 'openAccess')?.contains(openAccess)) ? 'selected="selected"' : ''%> value="${openAccess.id}">
                            ${openAccess.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field ${disableFilter ? 'disabled' : ''}">
                <label for="subject_references">${message(code: 'titleInstance.subjectReference.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="subject_references">
                    <input type="hidden" name="subject_references"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="subject_references" id="subject_reference" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${subjects}" var="subject">
                        <option <%=(params.list('subject_references')?.contains(subject)) ? 'selected="selected"' : ''%>
                                value="${subject}">
                            ${subject}
                        </option>
                    </g:each>
                </select>--%>
            </div>

            <%-- removed as of ERMS-6370
            <div class="field">
                <label for="ddcs">${message(code: 'titleInstance.ddc.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="ddcs">
                    <input type="hidden" name="ddcs"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>

            <div class="field">
                <label for="languages">${message(code: 'titleInstance.language.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="languages">
                    <input type="hidden" name="languages"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>
            --%>
        </div>

        <%-- removed as of ERMS-6370
        <div class="five fields">
            <%-- rem <div class="field">
                <label for="yearsFirstOnline">${message(code: 'tipp.YearFirstOnline')}</label>
                <div class="ui search selection fluid multiple dropdown" id="yearsFirstOnline">
                    <input type="hidden" name="yearsFirstOnline"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>

            <div class="field">
                <label for="medium">${message(code: 'default.search.medium')}</label>
                <div class="ui search selection fluid multiple dropdown" id="medium">
                    <input type="hidden" name="medium"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
            </div>

        </div>--%>

        <div class="three fields">
            <g:if test="${controllerName == 'subscription' && !showStatsFilter && !notShow}">
                <div class="field">
                    <label>${message(code: 'issueEntitlement.perpetualAccessBySub.label')}</label>
                    <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.hasPerpetualAccess}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
                <g:if test="${action =='index' && subscription.ieGroups.size() > 0}">
                    <g:set var="groups" value="${['notInGroups': g.message(code: 'issueEntitlement.notInTitleGroups')]+subscription.ieGroups.collectEntries{[it.id, it.name]}}"/>
                    <div class="field">
                        <label>${message(code: 'issueEntitlementGroup.label')}</label>
                        <g:select class="ui fluid dropdown" name="titleGroup"
                                   from="${groups}"
                                   optionKey="key"
                                   optionValue="value"
                                   value="${params.titleGroup}"
                                   noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                    </div>

                    %{--<div class="field">
                        <label>${message(code: 'issueEntitlement.inTitleGroups')}</label>
                        <ui:select class="ui fluid dropdown" name="inTitleGroups"
                                      from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${params.inTitleGroups}"
                                      noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                    </div>--}%
                </g:if>
            </g:if>
        </div>

        <g:if test="${controllerName == 'subscription' && showStatsFilter}">
            <g:if test="${revision == AbstractReport.COUNTER_4}">
                <ui:msg class="info" showIcon="true" header="${message(code: 'default.usage.counter4reportInfo.header')}" message="default.usage.counter4reportInfo.text" hideClose="true"/>
            </g:if>
            <div class="five fields" id="filterDropdownWrapper">
                <div class="field">
                    <label for="reportType"><g:message code="default.usage.reportType"/></label>
                    <select name="reportType" id="reportType" class="ui selection dropdown">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${reportTypes}" var="reportType">
                            <option <%=(params.list('reportType')?.contains(reportType)) ? 'selected="selected"' : ''%>
                                    value="${reportType}">
                                <g:message code="default.usage.${reportType}"/>
                            </option>
                        </g:each>
                        <g:if test="${reportTypes.size() == 0}">
                            <option value="<g:message code="default.stats.noReport" />"><g:message code="default.stats.noReport" /></option>
                        </g:if>
                    </select>
                </div>

                <div class="field dynFilter">
                    <g:if test="${metricTypes}">
                        <label for="metricType"><g:message code="default.usage.metricType"/></label>
                        <%-- was multiple --%>
                        <select name="metricType" id="metricType" class="ui selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${metricTypes}" var="metricType">
                                <option <%=(metricType in params.metricType) ? 'selected="selected"' : ''%>
                                        value="${metricType}">
                                    ${metricType}
                                </option>
                            </g:each>
                            <g:if test="${metricTypes.size() == 0}">
                                <option value="<g:message code="default.stats.noMetric" />"><g:message code="default.stats.noMetric" /></option>
                            </g:if>
                        </select>
                    </g:if>
                </div>

                <div class="field dynFilter">
                    <g:if test="${accessMethods}">
                        <label for="accessMethod"><g:message code="default.usage.accessMethod"/></label>
                        <%-- was multiple --%>
                        <select name="accessMethod" id="accessMethod" class="ui selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${accessMethods}" var="accessMethod">
                                <option <%=(accessMethod in params.accessMethod) ? 'selected="selected"' : ''%>
                                        value="${accessMethod}">
                                    ${accessMethod}
                                </option>
                            </g:each>
                            <g:if test="${accessMethods.size() == 0}">
                                <option value="<g:message code="default.stats.noAccessMethod" />"><g:message code="default.stats.noAccessMethod" /></option>
                            </g:if>
                        </select>
                    </g:if>
                </div>

                <div class="field dynFilter">
                    <g:if test="${accessTypes}">
                        <label for="accessType"><g:message code="default.usage.accessType"/></label>
                        <%-- was multiple --%>
                        <select name="accessType" id="accessType" class="ui selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${accessTypes}" var="accessType">
                                <option <%=(accessType in params.accessType) ? 'selected="selected"' : ''%>
                                        value="${accessType}">
                                    ${accessType}
                                </option>
                            </g:each>
                            <g:if test="${accessTypes.size() == 0}">
                                <option value="<g:message code="default.stats.noAccessType" />"><g:message code="default.stats.noAccessType" /></option>
                            </g:if>
                        </select>
                    </g:if>
                </div>

                <g:if test="${platformInstanceRecords.size() > 1}">
                    <div class="field">
                        <label for="platform"><g:message code="platform"/></label>
                        <ui:select class="ui search selection dropdown" from="${platformInstanceRecords}" name="platform"/>
                    </div>
                </g:if>
                <g:elseif test="${platformInstanceRecords.size() == 1}">
                    <g:hiddenField name="platform" value="${platformInstanceRecords.values()[0].id}"/>
                </g:elseif>
            </div>
        </g:if>

            <div class="field la-field-right-aligned">
                <g:link controller="${controllerName}" action="${action}" id="${params.id}" params="[surveyConfigID: params.surveyConfigID, tab: params.tab, tabStat: params.tabStat]"
                   class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</g:link>
                <input name="filterSet" type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}"/>
            </div>
    </g:form>
</ui:filter>

<laser:script file="${this.getGroovyPageFileName()}">
    <g:if test="${subscription && platformsJSON}">
        $("#reportType").on('change', function() {
            let reportType = $(this).val();
            <g:applyCodec encodeAs="none">
                let platforms = ${platformsJSON};
            </g:applyCodec>
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="loadFilterList"/>",
                data: {
                    reportType: reportType,
                    platforms: platforms,
                    multiple: false,
                    subscription: ${subscription.id},
                }
            }).done(function(response) {
                $('.dynFilter').remove();
                $('#filterDropdownWrapper').append(response);
                r2d2.initDynamicUiStuff('#filterDropdownWrapper');
            });
        });
    </g:if>

    JSPC.app.ajaxDropdown = function(selector, url, valuesRaw, fieldName, minLength) {
        let values = [];
        let valuesString = valuesRaw.replace(/&amp;quot;/g, '&quot;');
        let minLengthInit = 0;
        if (typeof(minLength) !== 'undefined')
            minLengthInit = minLength;
        let fieldNameQuery = '';
        if (typeof(fieldName) !== 'undefined')
            fieldNameQuery = '&fieldName='+fieldName;
        if (valuesString.includes(',')) {
            values = valuesString.split(',');
        }
        else if(valuesString.length > 0) {
            values.push(valuesString);
        }

        <g:if test="${controllerName == 'package' || fillDropdownsWithPackage}">
            let by = 'pkg';
            let obj = '${genericOIDService.getOID(packageInstance)}';
        </g:if>
        <g:elseif test="${controllerName == 'title' || action in ['linkTitle', 'currentPermanentTitles']}">
            let by = 'status';
            let obj;
        </g:elseif>
        <g:elseif test="${controllerName == 'subscription'}">
            let by = 'sub';
            let obj = '${genericOIDService.getOID(subscription)}';
        </g:elseif>
        <g:else>
            let by;
            let obj;
        </g:else>

        selector.dropdown('destroy').dropdown({
            apiSettings: {
                url: url + '&by=' + by + '&obj=' + obj + '&forTitles=${forTitles}'+fieldNameQuery+'&query={query}',
                cache: false
            },
            clearable: true,
            throttle: 500,
            minCharacters: minLengthInit
        });
        if (values.length > 0) {
            selector.dropdown('queryRemote', '', () => {
                selector.dropdown('set selected', values);
            });
        }
    }

    JSPC.app.ajaxDropdown($('#series_names'),       '<g:createLink controller="ajaxJson" action="getAllPossibleSimpleFieldValues" params="${configMap}"/>', '${params.series_names}', 'seriesName', 1);
    JSPC.app.ajaxDropdown($('#subject_references'), '<g:createLink controller="ajaxJson" action="getAllPossibleSubjects" params="${configMap}"/>', '${params.subject_references}', 1);
    JSPC.app.ajaxDropdown($('#ddcs'),               '<g:createLink controller="ajaxJson" action="getAllPossibleDdcs" params="${configMap}"/>', '${params.ddcs}');
    JSPC.app.ajaxDropdown($('#languages'),          '<g:createLink controller="ajaxJson" action="getAllPossibleLanguages" params="${configMap}"/>', '${params.languages}');
    JSPC.app.ajaxDropdown($('#yearsFirstOnline'),   '<g:createLink controller="ajaxJson" action="getAllPossibleDateFirstOnlineYears" params="${configMap}"/>', '${params.yearsFirstOnline}');
    JSPC.app.ajaxDropdown($('#medium'),             '<g:createLink controller="ajaxJson" action="getAllPossibleMediumTypes" params="${configMap}"/>', '${params.medium}');
    JSPC.app.ajaxDropdown($('#title_types'),        '<g:createLink controller="ajaxJson" action="getAllPossibleTitleTypes" params="${configMap}"/>', '${params.title_types}');
    JSPC.app.ajaxDropdown($('#publishers'),         '<g:createLink controller="ajaxJson" action="getAllPossibleSimpleFieldValues" params="${configMap}"/>', '${params.publishers}', 'publisherName', 1);
    JSPC.app.ajaxDropdown($('#provider'),           '<g:createLink controller="ajaxJson" action="getAllPossibleProviders" params="${configMap}"/>', '${params.provider}', 1);
    JSPC.app.ajaxDropdown($('#coverageDepth'),      '<g:createLink controller="ajaxJson" action="getAllPossibleCoverageDepths" params="${configMap}"/>', '${params.coverageDepth}');
</laser:script>