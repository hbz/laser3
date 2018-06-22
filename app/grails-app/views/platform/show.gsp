<%@ page import="com.k_int.kbplus.Platform" %>
<r:require module="annotations" />
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="platform" action="index" message="platform.show.all" />
            <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
        </semui:breadcrumbs>

        <semui:modeSwitch controller="platform" action="show" params="${params}" />

        <h1 class="ui header"><semui:headerIcon />

            <g:if test="${editable}"><span id="platformNameEdit"
                                                        class="xEditableValue"
                                                        data-type="textarea"
                                                        data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                                        data-name="name"
                                                        data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${platformInstance.name}</span>
            </g:if>
            <g:else>${platformInstance.name}</g:else>
          </h1>

        <g:render template="nav" contextPath="." />

        <semui:messages data="${flash}" />

        <fieldset class="inline-lists">
            <dl>

                <dt>${message(code:'platform.name', default:'Platform Name')}</dt>
                <dd><semui:xEditable owner="${platformInstance}" field="name"/></dd>

                <dt>${message(code:'platform.primaryUrl', default:'Primary URL')}</dt>
                <dd><semui:xEditable owner="${platformInstance}" field="primaryUrl"/></dd>

                <dt>${message(code:'platform.serviceProvider', default:'Service Provider')}</dt>
                <dd><semui:xEditableRefData owner="${platformInstance}" field="serviceProvider" config="YN"/></dd>

                <dt>${message(code:'platform.softwareProvider', default:'Software Provider')}</dt>
                <dd><semui:xEditableRefData owner="${platformInstance}" field="softwareProvider" config="YN"/></dd>

                <g:if test="${params.mode=='advanced'}">

                    <dt>${message(code:'platform.type', default:'Type')}</dt>
                    <dd><semui:xEditableRefData owner="${platformInstance}" field="type" config="YNO"/></dd>

                    <dt>${message(code:'platform.status', default:'Status')}</dt>
                    <dd><semui:xEditableRefData owner="${platformInstance}" field="status" config="UsageStatus"/></dd>

                    <dt><g:message code="platform.globalUID.label" default="Global UID" /></dt>
                    <dd><g:fieldValue bean="${platformInstance}" field="globalUID"/></dd>

                </g:if>
            </dl>
        </fieldset>

        ${message(code:'platform.show.availability', default:'Availability of titles in this platform by package')}

            <table class="ui celled la-rowspan table">
                <thead>
                  <tr>
                    <th rowspan="2" style="width: 25%;">${message(code:'title.label', default:'Title')}</th>
                    <th rowspan="2" style="width: 20%;">${message(code:'identifier.plural', default:'Identifiers')}</th>
                    <th colspan="${packages.size()}">${message(code:'platform.show.provided_by', default:'Provided by package')}</th>
                  </tr>
                  <tr>
                    <g:each in="${packages}" var="p">
                      <th><g:link controller="packageDetails" action="show" id="${p.id}">${p.name} (${p.contentProvider?.name})</g:link></th>
                    </g:each>
                  </tr>
                </thead>
              <g:each in="${titles}" var="t">
                <tr>
                  <td style="text-align:left;"><g:link controller="titleDetails" action="show" id="${t.title.id}">${t.title.title}</g:link>&nbsp;</td>
                  <td>
                    <g:each in="${t.title.ids.sort{it.identifier.ns.ns}}" var="tid">
                      <g:if test="${tid.identifier.ns.ns != 'originediturl'}">
                        <div><span>${tid.identifier.ns.ns}</span>: <span>${tid.identifier.value}</span></div>
                      </g:if>
                      <g:else>
                        <div><span>GOKb</span>: <a href="${tid.identifier.value}">${message(code:'component.originediturl.label')}</a></div>
                      </g:else>
                    </g:each>
                  </td>
                  <g:each in="${crosstab[t.position]}" var="tipp">
                    <g:if test="${tipp}">
                      <td>${message(code:'platform.show.from', default:'from')}: <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${tipp.startDate}"/>
                            <g:if test="${tipp.startVolume}"> / ${message(code:'tipp.volume', default:'volume')}: ${tipp.startVolume} </g:if>
                            <g:if test="${tipp.startIssue}"> / ${message(code:'tipp.issue', default:'issue')}: ${tipp.startIssue} </g:if> <br/>
                          ${message(code:'platform.show.to', default:'to')}:  <g:formatDate format="${session.sessionPreferences?.globalDateFormat}" date="${tipp.endDate}"/>
                            <g:if test="${tipp.endVolume}"> / ${message(code:'tipp.volume', default:'volume')}: ${tipp.endVolume}</g:if>
                            <g:if test="${tipp.endIssue}"> / ${message(code:'tipp.issue', default:'issue')}: ${tipp.endIssue}</g:if> <br/>
                          ${message(code:'tipp.coverageDepth', default:'coverage Depth')}: ${tipp.coverageDepth}</br>
                        <g:link controller="titleInstancePackagePlatform" action="show" id="${tipp.id}">${message(code:'platform.show.full_tipp', default:'Full TIPP Details')}</g:link>
                      </g:if>
                      <g:else>
                        <td></td>
                      </g:else>
                    </td>
                  </g:each>
                </tr>
              </g:each>
            </table>

  </body>
</html>
