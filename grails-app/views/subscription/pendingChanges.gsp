<%@ page import="de.laser.Subscription; de.laser.License; de.laser.RefdataValue" %>
<laser:htmlStart message="pendingChange.plural" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ license:subscription, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:h1HeaderWithIcon>
        <semui:xEditable owner="${subscription}" field="name" />
    </semui:h1HeaderWithIcon>
    <semui:anualRings object="${subscription}" controller="subscription" action="pendingChanges" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <laser:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${Subscription.get(memberId)}" />

        <h4 class="ui header">${member.getNameConcatenated()}</h4>

        <laser:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>


<laser:htmlEnd />
