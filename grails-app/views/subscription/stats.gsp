<%@ page import="org.springframework.context.i18n.LocaleContextHolder; de.laser.Subscription; java.time.format.TextStyle " %>
<laser:serviceInjection />
<%-- r:require module="annotations" / --%>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'subscription.details.stats.label')}</title>
    </head>
    <body>
        <semui:debugInfo>
            <g:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </semui:debugInfo>
        <g:render template="breadcrumb" model="${[ params:params ]}"/>
        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>
        <h1 class="ui icon header la-noMargin-top">
            <semui:headerIcon />${subscription.name}
        </h1>
        <semui:anualRings object="${subscription}" controller="subscription" action="show" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <g:render template="nav" />

        <semui:objectStatus object="${subscription}" status="${subscription.status}" />
        <g:render template="message" />
        <semui:messages data="${flash}" />
        <g:render template="/templates/filter/javascript"/>
        <semui:filter showFilterButton="true">
            <g:form action="stats" class="ui form" params="${[tab: params.tab, id: subscription.id, sort: params.sort, order: params.order]}">
                <div class="four fields">
                    <div class="field">
                        <label for="series_names">${message(code: 'titleInstance.seriesName.label')}</label>

                        <select name="series_names" id="series_names" multiple=""
                                class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>

                            <g:each in="${controlledListService.getAllPossibleSeriesBySub(subscription)}" var="seriesName">
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

                            <g:each in="${controlledListService.getAllPossibleSubjectsBySub(subscription)}" var="subject">
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

                            <g:each in="${controlledListService.getAllPossibleDdcsBySub(subscription)}" var="ddc">
                                <option <%=(params.list('ddcs')?.contains(ddc.id.toString())) ? 'selected="selected"' : ''%>
                                        value="${ddc.id}">
                                    ${ddc.value} - ${ddc.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>

                    <div class="field">
                        <label for="language">${message(code: 'titleInstance.language.label')}</label>

                        <select name="languages" id="language" multiple="multiple"
                                class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>

                            <g:each in="${controlledListService.getAllPossibleLanguagesBySub(subscription)}" var="language">
                                <option <%=(params.list('languages')?.contains(language.id.toString())) ? 'selected="selected"' : ''%>
                                        value="${language.id}">
                                    ${language.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                </div>
                <div class="two fields">
                    <div class="field">
                        <label for="reportMonth"><g:message code="default.usage.reportMonth"/></label>
                        <select name="reportMonth" id="reportMonth" multiple="multiple" class="ui selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${monthsInRing}" var="reportMonth">
                                <g:set var="rm" value="${formatDate(date: reportMonth, format: 'yyyy-MM-dd')}"/>
                                <option <%=(params.list('reportMonth')?.contains(rm)) ? 'selected="selected"' : ''%>
                                        value="${rm}">
                                    <g:formatDate date="${reportMonth}" format="yyyy-MM"/>
                                </option>
                            </g:each>
                        </select>
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
        <semui:tabs>
            <semui:tabsItem controller="subscription" action="stats" params="${params + [tab: 'counter4']}" text="COUNTER 4" tab="counter4" counts="${c4total}"/>
            <semui:tabsItem controller="subscription" action="stats" params="${params + [tab: 'counter5']}" text="COUNTER 5" tab="counter5" counts="${c5total}"/>
        </semui:tabs>
        <div class="ui bottom attached tab active segment">
            <table class="ui celled la-table table">
                <thead>
                    <tr>
                        <th><g:message code="default.usage.reportType"/></th>
                        <th><g:message code="default.usage.date"/></th>
                        <th><g:message code="default.usage.metricType"/></th>
                        <th><g:message code="default.usage.reportCount"/></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${sums}" var="row">
                        <tr>
                            <td>${row.reportType}</td>
                            <td><g:formatDate date="${row.reportMonth}" format="yyyy-MM"/></td>
                            <td>${row.metricType}</td>
                            <td>${row.reportCount}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
            <table class="ui sortable celled la-table table">
                <thead>
                    <tr>
                        <g:sortableColumn title="${message(code:"default.usage.reportType")}" property="r.reportType"/>
                        <g:sortableColumn title="${message(code:"default.title.label")}" property="title.name"/>
                        <g:sortableColumn title="${message(code:"default.usage.date")}" property="r.reportFrom"/>
                        <g:sortableColumn title="${message(code:"default.usage.metricType")}" property="r.metricType"/>
                        <g:sortableColumn title="${message(code:"default.usage.reportCount")}" property="r.reportCount"/>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${usages}" var="row">
                        <tr>
                            <td>${row.reportType}</td>
                            <td>${row.title.name}</td>
                            <td><g:formatDate date="${row.reportFrom}" format="yyyy-MM"/></td>
                            <td>${row.metricType}</td>
                            <td>${row.reportCount}</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
            <semui:paginate total="${total}" params="${params}" max="${max}" offset="${offset}"/>
        </div>
    </body>
</html>
