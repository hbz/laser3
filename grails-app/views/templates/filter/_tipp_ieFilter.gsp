<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.RefdataCategory;" %>
<laser:serviceInjection />
<g:if test="${controllerName == 'package'}">
    <g:set var="seriesNames"
           value="${packageInstance ? controlledListService.getAllPossibleSeriesByPackage(packageInstance,actionName) : []}"/>
    <g:set var="subjects"
           value="${packageInstance ? controlledListService.getAllPossibleSubjectsByPackage(packageInstance,actionName) : []}"/>
    <g:set var="ddcs"
           value="${packageInstance ? controlledListService.getAllPossibleDdcsByPackage(packageInstance,actionName) : []}"/>
    <g:set var="languages"
           value="${packageInstance ? controlledListService.getAllPossibleLanguagesByPackage(packageInstance,actionName) : []}"/>
    <g:set var="yearsFirstOnline"
           value="${packageInstance ? controlledListService.getAllPossibleDateFirstOnlineYearByPackage(packageInstance,actionName) : []}"/>
    <g:set var="publishers"
           value="${packageInstance ? controlledListService.getAllPossiblePublisherByPackage(packageInstance,actionName) : []}"/>
    <g:set var="titleTypes"
           value="${packageInstance ? controlledListService.getAllPossibleTitleTypesByPackage(packageInstance,actionName) : []}"/>
    <g:set var="mediumTypes"
           value="${packageInstance ? controlledListService.getAllPossibleMediumTypesByPackage(packageInstance,actionName) : []}"/>
    <g:set var="coverageDepths"
           value="${packageInstance ? controlledListService.getAllPossibleCoverageDepthsByPackage(packageInstance,actionName) : []}"/>
</g:if>

<g:if test="${controllerName == 'subscription'}">
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
</g:if>
<g:set var="availableStatus" value="${RefdataCategory.getAllRefdataValues(RDConstants.TIPP_STATUS)-RDStore.TIPP_STATUS_REMOVED}"/>

<ui:filter>
    <g:form controller="${controllerName}" action="${actionName}" id="${params.id}" method="get" class="ui form">
        <g:hiddenField name="sort" value="${params.sort}"/>
        <g:hiddenField name="order" value="${params.order}"/>

        <g:hiddenField name="surveyConfigID" value="${params.surveyConfigID}"/>
        <g:hiddenField name="tab" value="${params.tab}"/>
        <g:hiddenField name="tabStat" value="${params.tabStat}"/>
        <g:hiddenField name="titleGroup" value="${params.titleGroup}"/>

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
            <g:if test="${params.mode != 'advanced' && !showStatsFilter && actionName != 'renewEntitlementsWithSurvey'}">
                <div class="field">
                    <ui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                      value="${params.asAt}"
                                      placeholder="subscription.details.asAt.placeholder"/>
                </div>
            </g:if>
            <g:if test="${!showStatsFilter && !(actionName in ['renewEntitlementsWithSurvey', 'current', 'planned', 'expired', 'deleted'])}">
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
            </div>
        </div>

        <div class="four fields">
            <div class="field">
                <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>

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

            <div class="field">
                <label for="subject_reference">${message(code: 'titleInstance.subjectReference.label')}</label>

                <select name="subject_references" id="subject_reference" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${subjects}" var="subject">
                        <option <%=(params.list('subject_references')?.contains(subject)) ? 'selected="selected"' : ''%>
                                value="${subject}">
                            ${subject}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="ddc">${message(code: 'titleInstance.ddc.label')}</label>

                <select name="ddcs" id="ddc" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${ddcs}" var="ddc">
                        <option <%=(params.list('ddcs')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                value="${ddc.id}">
                            ${ddc.value} - ${ddc.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="language">${message(code: 'titleInstance.language.label')}</label>

                <select name="languages" id="language" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${languages}" var="language">
                        <option <%=(params.list('languages')?.contains(language.id.toString())) ? 'selected="selected"' : ''%>
                                value="${language.id}">
                            ${language.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">
            <div class="field">
                <label for="yearsFirstOnline">${message(code: 'tipp.YearFirstOnline')}
                </label>
                <select name="yearsFirstOnline" id="yearsFirstOnline" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${yearsFirstOnline}"
                            var="yearFirstOnline">
                        <option <%=(params.list('yearsFirstOnline')?.contains(yearFirstOnline.toString())) ? 'selected="selected"' : ''%>
                                value="${yearFirstOnline}">
                            ${yearFirstOnline}
                        </option>
                    </g:each>
                </select>

            </div>

            <div class="field">
                <label for="medium">
                    ${message(code: 'default.search.medium')}
                </label>
                <select name="medium" id="medium" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${mediumTypes}" var="mediumType">
                        <option <%=(params.list('medium')?.contains(mediumType.id.toString())) ? 'selected="selected"' : ''%>
                                value="${mediumType.id}">
                            ${mediumType.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="title_types">${message(code: 'default.search.titleTyp')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.titleTyp')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>
                <select name="title_types" id="title_types" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${titleTypes}"
                            var="titleType">
                        <option <%=(params.list('title_types')?.contains(titleType)) ? 'selected="selected"' : ''%>
                                value="${titleType}">
                            ${titleType.capitalize()}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="publishers">${message(code: 'tipp.publisher')}</label>
                <select name="publishers" id="publishers" multiple=""
                        class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${publishers}"
                            var="publisher">
                        <option <%=(params.list('publishers')?.contains(publisher)) ? 'selected="selected"' : ''%>
                                value="${publisher}">
                            ${publisher}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>


        <div class="three fields">
            <g:if test="${controllerName == 'subscription' && !showStatsFilter}">
                <div class="field">
                    <label>${message(code: 'issueEntitlement.perpetualAccessBySub.label')}</label>
                    <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.hasPerpetualAccess}"
                                  noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                </div>
                <g:if test="${actionName =='index' && subscription.ieGroups.size() > 0}">
                    <div class="field">
                        <label>${message(code: 'issueEntitlement.inTitleGroups')}</label>
                        <ui:select class="ui fluid dropdown" name="inTitleGroups"
                                      from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${params.inTitleGroups}"
                                      noSelection="${['': message(code: 'default.select.choose.label')]}"/>
                    </div>
                </g:if>
            </g:if>
        </div>

        <g:if test="${controllerName == 'subscription' && showStatsFilter}">
            <div class="${accessTypes ? print('three') : print('two')} fields">
                <div class="field">
                    <label for="metricType"><g:message code="default.usage.metricType"/></label>
                    <select name="metricType" id="metricType" multiple="multiple" class="ui selection dropdown">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${metricTypes}" var="metricType">
                            <option <%=(params.metricType == metricType) ? 'selected="selected"' : ''%>
                                    value="${metricType}">
                                ${metricType}
                            </option>
                        </g:each>
                        <g:if test="${metricTypes.size() == 0}">
                            <option value="<g:message code="default.stats.noMetric" />"><g:message code="default.stats.noMetric" /></option>
                        </g:if>
                    </select>
                </div>

                <g:if test="${accessTypes}">
                    <div class="field">
                        <label for="accessType"><g:message code="default.usage.accessType"/></label>
                        <select name="accessType" id="accessType" class="ui selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${accessTypes}" var="accessType">
                                <option <%=(params.accessType == accessType) ? 'selected="selected"' : ''%>
                                        value="${accessType}">
                                    ${accessType}
                                </option>
                            </g:each>
                            <g:if test="${accessTypes.size() == 0}">
                                <option value="<g:message code="default.stats.noAccessType" />"><g:message code="default.stats.noAccessType" /></option>
                            </g:if>
                        </select>
                    </div>
                </g:if>

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
            </div>
        </g:if>

            <div class="field la-field-right-aligned">
                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="[surveyConfigID: params.surveyConfigID, tab: params.tab, tabStat: params.tabStat]"
                   class="ui reset secondary button">${message(code: 'default.button.reset.label')}</g:link>
                <input type="submit" class="ui primary button"
                       value="${message(code: 'default.button.filter.label')}"/>
            </div>
    </g:form>
</ui:filter>