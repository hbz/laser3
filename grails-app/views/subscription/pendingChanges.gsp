<%@ page import="de.laser.Subscription; de.laser.License; de.laser.RefdataValue" %>
<laser:htmlStart message="pendingChange.plural" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ license:subscription, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <ui:h1HeaderWithIcon>
        <ui:xEditable owner="${subscription}" field="name" />
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${subscription}" controller="subscription" action="pendingChanges" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


    <laser:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${Subscription.get(memberId)}" />

        <h4 class="ui header">${member.getNameConcatenated()}</h4>

        <laser:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>


<laser:htmlEnd />
