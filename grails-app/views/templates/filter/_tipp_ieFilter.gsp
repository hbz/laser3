<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.helper.RDConstants; de.laser.RefdataValue;" %>
<laser:serviceInjection />
<g:if test="${controllerName == 'package'}">
    <g:set var="seriesNames"
           value="${packageInstance ? controlledListService.getAllPossibleSeriesByPackage(packageInstance) : []}"/>
    <g:set var="subjects"
           value="${packageInstance ? controlledListService.getAllPossibleSubjectsByPackage(packageInstance) : []}"/>
    <g:set var="yearsFirstOnline"
           value="${packageInstance ? controlledListService.getAllPossibleDateFirstOnlineYearByPackage(packageInstance) : []}"/>
    <g:set var="publishers"
           value="${packageInstance ? controlledListService.getAllPossiblePublisherByPackage(packageInstance) : []}"/>
</g:if>

<g:if test="${controllerName == 'subscription'}">
    <g:set var="seriesNames"
           value="${subscription ? controlledListService.getAllPossibleSeriesBySub(subscription) : []}"/>
    <g:set var="subjects"
           value="${subscription ? controlledListService.getAllPossibleSubjectsBySub(subscription) : []}"/>
    <g:set var="yearsFirstOnline"
           value="${subscription ? controlledListService.getAllPossibleDateFirstOnlineYearBySub(subscription) : []}"/>
    <g:set var="publishers"
           value="${subscription ? controlledListService.getAllPossiblePublisherBySub(subscription) : []}"/>

</g:if>

<g:render template="/templates/filter/javascript"/>
<semui:filter showFilterButton="true">
    <g:form controller="${controllerName}" action="${actionName}" params="${params}" method="get" class="ui form">
        <input type="hidden" name="sort" value="${params.sort}">
        <input type="hidden" name="order" value="${params.order}">

        <div class="three fields">
            <div class="field">
                <label for="filter">${message(code: 'default.search.text')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.tipp')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>
                <input name="filter" id="filter" value="${params.filter}"/>
            </div>

            <g:if test="${controllerName == 'subscription'}">
                <div class="field">
                    <label for="pkgfilter">${message(code: 'subscription.details.from_pkg')}</label>
                    <select class="ui dropdown" name="pkgfilter" id="pkgfilter">
                        <option value="">${message(code: 'subscription.details.from_pkg.all')}</option>
                        <g:each in="${subscription.packages}" var="sp">
                            <option value="${sp.pkg.id}" ${sp.pkg.id.toString() == params.pkgfilter ? 'selected=true' : ''}>${sp.pkg.name}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${params.mode != 'advanced'}">
                <div class="field">
                    <semui:datepicker label="subscription.details.asAt" id="asAt" name="asAt"
                                      value="${params.asAt}"
                                      placeholder="subscription.details.asAt.placeholder"/>
                </div>
            </g:if>
        </div>

        <div class="two fields">
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
        </div>

        <div class="three fields">
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'default.search.tooltip.identifier')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>
                <input name="identifier" id="identifier" value="${params.identifier}"/>
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
                    <g:each in="${controlledListService.getAllPossibleTitleTypes()}"
                            var="titleType">
                        <option <%=(params.list('title_types')?.contains(titleType)) ? 'selected="selected"' : ''%>
                                value="${titleType}">
                            ${titleType ? RefdataValue.getByValueAndCategory(titleType, RDConstants.TITLE_MEDIUM).getI10n('value') : titleType}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="publisher">${message(code: 'tipp.publisher')}
                </label>
                <g:select name="publisher" id="publisher"
                          from="${publishers}"
                          class="ui fluid search selection dropdown"
                          value="${params.publisher}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="two fields">

            <div class="field">
                <label for="yearsFirstOnline">${message(code: 'tipp.YearFirstOnline')}
                </label>
                <g:select name="yearsFirstOnline" id="yearsFirstOnline"
                          from="${yearsFirstOnline}"
                          class="ui fluid search selection dropdown"
                          value="${params.yearsFirstOnline}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label')}"/>
            </div>
        </div>
    </g:form>
</semui:filter>