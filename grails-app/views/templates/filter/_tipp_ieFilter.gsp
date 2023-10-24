<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.base.AbstractReport" %>
<laser:serviceInjection />
<g:set var="action" value="${action ?: actionName}"/>

<g:set var="availableStatus" value="${RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS)-RDStore.TIPP_STATUS_REMOVED}"/>

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
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.tipp')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>
                <input name="filter" id="filter" value="${params.filter}"/>
            </div>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.identifier')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>
                <input name="identifier" id="identifier" value="${params.identifier}"/>
            </div>

            <g:if test="${controllerName == 'subscription'}">
                <div class="field">
                    <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                    <select class="ui dropdown" name="pkgfilter" id="pkgfilter">
                        <option value="">${message(code: 'default.all')}</option>
                        <g:each in="${subscription.packages}" var="sp">
                            <option value="${sp.pkg.id}" ${sp.pkg.id.toString() == params.pkgfilter ? 'selected=true' : ''}>${sp.pkg.name}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${params.mode != 'advanced' && !showStatsFilter && action != 'renewEntitlementsWithSurvey'}">
                <div class="field">
                    <ui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                      value="${params.asAt}"
                                      placeholder="subscription.details.asAt.placeholder"/>
                </div>
            </g:if>
            <g:if test="${!showStatsFilter && !(action in ['renewEntitlementsWithSurvey', 'current', 'planned', 'expired', 'deleted'])}">
                <div class="field">
                    <label for="status">
                        ${message(code: 'default.status.label')}
                    </label>
                    <select name="status" id="status" multiple=""
                            class="ui search selection dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${availableStatus}" var="status">
                            <option <%=(params.list('status')?.contains(status.id.toString())) ? 'selected="selected"' : ''%>
                                    value="${status.id}">
                                ${status.getI10n('value')}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <div class="field">
                <label for="coverageDepth"><g:message code="tipp.coverageDepth"/></label>
                <div class="ui search selection fluid multiple dropdown" id="coverageDepth">
                    <input type="hidden" name="coverageDepth"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--
                <select name="coverageDepth" id="coverageDepth" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${coverageDepths}" var="coverageDepth">
                        <option <%=(params.list('coverageDepth')?.contains(coverageDepth.value)) ? 'selected="selected"' : ''%>
                                value="${coverageDepth}">
                            ${coverageDepth.getI10n("value")}
                        </option>
                    </g:each>
                </select>
                --%>
            </div>
        </div>

        <div class="four fields">
            <div class="field">
                <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="series_names">
                    <input type="hidden" name="series_names"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--
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
                --%>
            </div>

            <div class="field">
                <label for="subject_reference">${message(code: 'titleInstance.subjectReference.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="subject_reference">
                    <input type="hidden" name="subject_reference"/>
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

            <div class="field">
                <label for="ddcs">${message(code: 'titleInstance.ddc.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="ddcs">
                    <input type="hidden" name="ddcs"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="ddcs" id="ddc" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${ddcs}" var="ddc">
                        <option <%=(params.list('ddcs')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                value="${ddc.id}">
                            ${ddc.value} - ${ddc.getI10n("value")}
                        </option>
                    </g:each>
                </select>--%>
            </div>

            <div class="field">
                <label for="languages">${message(code: 'titleInstance.language.label')}</label>
                <div class="ui search selection fluid multiple dropdown" id="languages">
                    <input type="hidden" name="languages"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="languages" id="language" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${languages}" var="language">
                        <option <%=(params.list('languages')?.contains(language.id.toString())) ? 'selected="selected"' : ''%>
                                value="${language.id}">
                            ${language.getI10n("value")}
                        </option>
                    </g:each>
                </select>--%>
            </div>
        </div>

        <div class="four fields">
            <div class="field">
                <label for="yearsFirstOnline">${message(code: 'tipp.YearFirstOnline')}</label>
                <div class="ui search selection fluid multiple dropdown" id="yearsFirstOnline">
                    <input type="hidden" name="yearsFirstOnline"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="yearsFirstOnline" id="yearsFirstOnline" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${yearsFirstOnline}"
                            var="yearFirstOnline">
                        <option <%=(params.list('yearsFirstOnline')?.contains(yearFirstOnline.toString())) ? 'selected="selected"' : ''%>
                                value="${yearFirstOnline}">
                            ${yearFirstOnline}
                        </option>
                    </g:each>
                </select>--%>
            </div>

            <div class="field">
                <label for="medium">${message(code: 'default.search.medium')}</label>
                <div class="ui search selection fluid multiple dropdown" id="medium">
                    <input type="hidden" name="medium"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="medium" id="medium" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${mediumTypes}" var="mediumType">
                        <option <%=(params.list('medium')?.contains(mediumType.id.toString())) ? 'selected="selected"' : ''%>
                                value="${mediumType.id}">
                            ${mediumType.getI10n("value")}
                        </option>
                    </g:each>
                </select>--%>
            </div>

            <div class="field">
                <label for="title_types">${message(code: 'default.search.titleTyp')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.titleTyp')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>
                <div class="ui search selection fluid multiple dropdown" id="title_types">
                    <input type="hidden" name="title_types"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="title_types" id="title_types" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${titleTypes}"
                            var="titleType">
                        <option <%=(params.list('title_types')?.contains(titleType)) ? 'selected="selected"' : ''%>
                                value="${titleType}">
                            ${titleType.capitalize()}
                        </option>
                    </g:each>
                </select>--%>
            </div>

            <div class="field">
                <label for="publishers">${message(code: 'tipp.publisher')}</label>
                <div class="ui search selection fluid multiple dropdown" id="publishers">
                    <input type="hidden" name="publishers"/>
                    <div class="default text"><g:message code="default.select.choose.label"/></div>
                    <i class="dropdown icon"></i>
                </div>
                <%--<select name="publishers" id="publishers" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${publishers}"
                            var="publisher">
                        <option <%=(params.list('publishers')?.contains(publisher)) ? 'selected="selected"' : ''%>
                                value="${publisher}">
                            ${publisher}
                        </option>
                    </g:each>
                </select>--%>
            </div>

        </div>


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
                    <div class="field">
                        <label>${message(code: 'issueEntitlementGroup.label')}</label>
                        <g:select class="ui fluid dropdown" name="titleGroup"
                                   from="${subscription.ieGroups}"
                                   optionKey="id"
                                   optionValue="name"
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
                <ui:msg icon="ui info icon" class="info" header="${message(code: 'default.usage.counter4reportInfo.header')}" message="default.usage.counter4reportInfo.text" noClose="true"/>
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
                   class="ui reset secondary button">${message(code: 'default.button.reset.label')}</g:link>
                <input type="submit" class="ui primary button"
                       value="${message(code: 'default.button.filter.label')}"/>
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

    JSPC.app.ajaxDropdown = function(selector, url, valuesRaw) {
        let values = [];
        let valuesString = valuesRaw.replace(/&amp;quot;/g, '&quot;');
        if(valuesString.includes(',')) {
            values = valuesString.split(',');
        }
        else if(valuesString.length > 0) {
            values.push(valuesString);
        }
        selector.dropdown({
            apiSettings: {
                url: url,
                cache: false
            },
            clearable: true,
            minCharacters: 0
        });
        if(values.length > 0) {
            selector.dropdown('queryRemote', '', () => {
                selector.dropdown('set selected', values);
            });
        }
    }

    let by;
    let obj;
    let forTitles = '${action}';
    <g:if test="${controllerName == 'package' || fillDropdownsWithPackage}">
        by = 'pkg';
        obj = '${genericOIDService.getOID(packageInstance)}';
        <%--
        <g:set var="seriesNames"
               value="${packageInstance ? controlledListService.getAllPossibleSeriesByPackage(packageInstance,action) : []}"/>
        <g:set var="subjects"
               value="${packageInstance ? controlledListService.getAllPossibleSubjectsByPackage(packageInstance,action) : []}"/>
        <g:set var="ddcs"
               value="${packageInstance ? controlledListService.getAllPossibleDdcsByPackage(packageInstance,action) : []}"/>
        <g:set var="languages"
               value="${packageInstance ? controlledListService.getAllPossibleLanguagesByPackage(packageInstance,action) : []}"/>
        <g:set var="yearsFirstOnline"
               value="${packageInstance ? controlledListService.getAllPossibleDateFirstOnlineYearByPackage(packageInstance,action) : []}"/>
        <g:set var="publishers"
               value="${packageInstance ? controlledListService.getAllPossiblePublisherByPackage(packageInstance,action) : []}"/>
        <g:set var="titleTypes"
               value="${packageInstance ? controlledListService.getAllPossibleTitleTypesByPackage(packageInstance,action) : []}"/>
        <g:set var="mediumTypes"
               value="${packageInstance ? controlledListService.getAllPossibleMediumTypesByPackage(packageInstance,action) : []}"/>
        <g:set var="coverageDepths"
               value="${packageInstance ? controlledListService.getAllPossibleCoverageDepthsByPackage(packageInstance,action) : []}"/>
        --%>
    </g:if>
    <g:elseif test="${controllerName == 'subscription'}">
        by = 'sub';
        obj = '${genericOIDService.getOID(subscription)}';
        <%--
        <g:set var="seriesNames"
               value="${subscription ? controlledListService.getAllPossibleSeriesBySub(subscription) : []}"/>
        <g:set var="subjects"
               value="${subscription ? controlledListService.getAllPossibleSubjectsBySub(subscription) : []}"/>
        <g:set var="ddcs"
               value="${subscription ? controlledListService.getAllPossibleDdcsBySub(subscription) : []}"/>
        <g:set var="languages"
               value="${subscription ? controlledListService.getAllPossibleLanguagesBySub(subscription) : []}"/>
        <g:set var="yearsFirstOnline"
               value="${subscription ? controlledListService.getAllPossibleDateFirstOnlineYearBySub(subscription) : []}"/>
        <g:set var="publishers"
               value="${subscription ? controlledListService.getAllPossiblePublisherBySub(subscription) : []}"/>
        <g:set var="titleTypes"
               value="${subscription ? controlledListService.getAllPossibleTitleTypesBySub(subscription) : []}"/>
        <g:set var="mediumTypes"
               value="${subscription ? controlledListService.getAllPossibleMediumTypesBySub(subscription) : []}"/>
        <g:set var="coverageDepths"
               value="${subscription ? controlledListService.getAllPossibleCoverageDepthsBySub(subscription) : []}"/>
        --%>
    </g:elseif>
    <g:elseif test="${controllerName == 'title' || action == 'currentPermanentTitles'}">
        by = 'status';
        <%--
        <g:set var="seriesNames"
               value="${params.status ? controlledListService.getAllPossibleSeriesByStatus(params) : []}"/>
        <g:set var="subjects"
               value="${params.status ? controlledListService.getAllPossibleSubjectsByStatus(params) : []}"/>
        <g:set var="ddcs"
               value="${params.status ? controlledListService.getAllPossibleDdcsByStatus(params) : []}"/>
        <g:set var="languages"
               value="${params.status ? controlledListService.getAllPossibleLanguagesByStatus(params) : []}"/>
        <g:set var="yearsFirstOnline"
               value="${params.status ? controlledListService.getAllPossibleDateFirstOnlineYearByStatus(params) : []}"/>
        <g:set var="publishers"
               value="${params.status ? controlledListService.getAllPossiblePublisherByStatus(params) : []}"/>
        <g:set var="titleTypes"
               value="${params.status ? controlledListService.getAllPossibleTitleTypesByStatus(params) : []}"/>
        <g:set var="mediumTypes"
               value="${params.status ? controlledListService.getAllPossibleMediumTypesByStatus(params) : []}"/>
        <g:set var="coverageDepths"
               value="${params.status ? controlledListService.getAllPossibleCoverageDepthsByStatus(params) : []}"/>
        --%>
    </g:elseif>
    JSPC.app.ajaxDropdown($('#series_names'), '<g:createLink controller="ajaxJson" action="getAllPossibleSeries" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.series_names}');
    JSPC.app.ajaxDropdown($('#subject_reference'), '<g:createLink controller="ajaxJson" action="getAllPossibleSubjects" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.subject_reference}');
    JSPC.app.ajaxDropdown($('#ddcs'), '<g:createLink controller="ajaxJson" action="getAllPossibleDdcs" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.ddcs}');
    JSPC.app.ajaxDropdown($('#languages'), '<g:createLink controller="ajaxJson" action="getAllPossibleLanguages" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.languages}');
    JSPC.app.ajaxDropdown($('#yearsFirstOnline'), '<g:createLink controller="ajaxJson" action="getAllPossibleDateFirstOnlineYears" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.yearsFirstOnline}');
    JSPC.app.ajaxDropdown($('#medium'), '<g:createLink controller="ajaxJson" action="getAllPossibleMediumTypes" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.medium}');
    JSPC.app.ajaxDropdown($('#title_types'), '<g:createLink controller="ajaxJson" action="getAllPossibleTitleTypes" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.title_types}');
    JSPC.app.ajaxDropdown($('#publishers'), '<g:createLink controller="ajaxJson" action="getAllPossiblePublishers" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.publishers}');
    JSPC.app.ajaxDropdown($('#coverageDepth'), '<g:createLink controller="ajaxJson" action="getAllPossibleCoverageDepths" params="${params}"/>&by='+by+'&obj='+obj+'&forTitles='+forTitles+'&query={query}', '${params.coverageDepth}');
</laser:script>