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

    <g:render template="breadcrumb" model="${[ license:subscription, params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui left aligned icon header"><semui:headerIcon />
        <semui:xEditable owner="${subscriptionInstance}" field="name" />

        <span class="la-forward-back">
            <g:if test="${navPrevSubscription}">
                <g:link controller="subscriptionDetails" action="members" params="[id:navPrevSubscription.id]"><i class="chevron left icon"></i></g:link>
            </g:if>
            <g:else>
                <i class="chevron left icon disabled"></i>
            </g:else>
            <g:if test="${navNextSubscription}">
                <g:link controller="subscriptionDetails" action="members" params="[id:navNextSubscription.id]"><i class="chevron right icon"></i></g:link>
            </g:if>
            <g:else>
                <i class="chevron right icon disabled"></i>
            </g:else>
        </span>
    </h1>

    <g:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${com.k_int.kbplus.Subscription.get(memberId)}" />

        <h4>${member.getNameConcatenated()}</h4>

        <g:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>


</body>
</html>
