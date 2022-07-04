<%@ page import="de.laser.License" %>
<%@ page import="de.laser.RefdataValue" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
  <meta name="layout" content="laser">
  <title>${message(code:'laser')} : ${message(code:'pendingChange.plural')}</title>
</head>
<body>

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:headerWithIcon>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </semui:headerWithIcon>

    <laser:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${License.get(memberId)}" />

        <h4 class="ui header">${member.getReferenceConcatenated()}</h4>

        <laser:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>

</body>
</html>
