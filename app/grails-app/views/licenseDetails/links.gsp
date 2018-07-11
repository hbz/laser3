<%@ page import="com.k_int.kbplus.License" %>
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

<semui:controlButtons>
    <g:render template="actions" />
</semui:controlButtons>

<h1 class="ui header"><semui:headerIcon />
%{--${license.licensee?.name}--}%
${message(code:'license.details.type', args:["${license.type?.getI10n('value')}"], default:'License')} :
<semui:xEditable owner="${license}" field="reference"/>
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
        <g:each in="${License.findAllWhere(instanceOf: license)}" var="lic">
            <tr>
                <td>
                    <g:link controller="licenseDetails" action="show" id="${lic.id}">${lic.genericLabel}</g:link>

                    <g:if test="${lic.isSlaved?.value?.equalsIgnoreCase('yes')}">
                        <span data-position="top right" data-tooltip="${message(code:'license.details.isSlaved.tooltip')}">
                            <i class="anchor blue icon"></i>
                        </span>
                    </g:if>
                </td>
                <td>
                    <g:each in="${lic.orgLinks}" var="orgRole">
                        <g:if test="${orgRole?.roleType.value in ['Licensee_Consortial', 'Licensee']}">
                            <g:link controller="organisations" action="show" id="${orgRole?.org.id}">
                                ${orgRole?.org.getDesignation()}
                            </g:link>
                            , ${orgRole?.roleType.getI10n('value')} <br />
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
