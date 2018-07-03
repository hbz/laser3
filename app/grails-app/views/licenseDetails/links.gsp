<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<!doctype html>
<html>
<head>
  <meta name="layout" content="semanticUI"/>
  <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'license.label', default:'License')}</title>
</head>
<body>

<g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

<h1 class="ui header">
  <semui:headerIcon />
  <semui:xEditable owner="${license}" field="reference" />
</h1>

<g:render template="nav" />

<table class="ui celled la-table table">
    <thead>
        <tr>
            <th></th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${license.outgoinglinks}" var="link">
            <tr>
                <td>
                    <g:link controller="licenseDetails" action="show" id="${link.linkTarget.id}">${link.linkTarget.genericLabel}</g:link>
                </td>
                <td>
                    <g:each in="${link.linkTarget.orgLinks}" var="orgRole">
                        <g:if test="${orgRole?.roleType.value in ['Licensee_Consortial', 'Licensee']}">
                            <g:link controller="organisations" action="show" id="${orgRole?.org.id}">
                                ${orgRole?.org.getDesignation()}
                            </g:link>
                            , ${orgRole?.roleType.getI10n('value')}
                        </g:if>
                    </g:each>
                </td>
            </tr>
        </g:each>
      <%--
      <g:each in="${license.incomingLinks}" var="links">
        <tr>
          <td>Incoming</td>
          <td>${links.linkTarget.genericLabel}</td>
        </tr>
      </g:each>
      --%>
    </tbody>

</body>
</html>
