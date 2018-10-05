<%@ page import="com.k_int.kbplus.License" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
  <meta name="layout" content="semanticUI"/>
  <title>${message(code:'laser', default:'LAS:eR')} : TEST</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui header"><semui:headerIcon />
        <g:if test="${license.type?.value == 'Template'}">${message(code:'license.label')} (${license.type.getI10n('value')}):</g:if>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </h1>

    <g:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${com.k_int.kbplus.License.get(memberId)}" />

        <h4>${member.getReferenceConcatenated()}</h4>

        <g:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>

</body>
</html>
