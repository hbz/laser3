<%@ page import="de.laser.Subscription; de.laser.License; de.laser.RefdataValue" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
  <meta name="layout" content="laser">
  <title>${message(code:'laser')} : ${message(code:'pendingChange.plural')}</title>
</head>
<body>

    <laser:render template="breadcrumb" model="${[ license:subscription, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:headerWithIcon>
        <semui:xEditable owner="${subscription}" field="name" />
    </semui:headerWithIcon>
    <semui:anualRings object="${subscription}" controller="subscription" action="pendingChanges" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <laser:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${Subscription.get(memberId)}" />

        <h4 class="ui header">${member.getNameConcatenated()}</h4>

        <laser:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>


</body>
</html>
