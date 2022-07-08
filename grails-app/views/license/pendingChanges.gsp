<%@ page import="de.laser.License; de.laser.RefdataValue" %>
<laser:htmlStart message="pendingChange.plural" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <semui:controlButtons>
        <laser:render template="actions" />
    </semui:controlButtons>

    <semui:h1HeaderWithIcon>
        <semui:xEditable owner="${license}" field="reference" id="reference"/>
    </semui:h1HeaderWithIcon>

    <laser:render template="nav" />

    <g:each in="${pendingChanges}" var="memberId, pcList">
        <g:set var="member" value="${License.get(memberId)}" />

        <h4 class="ui header">${member.getReferenceConcatenated()}</h4>

        <laser:render template="/templates/pendingChanges" model="${['pendingChanges':pcList, 'flash':flash, 'model':member, 'tmplSimpleView':true]}"/>
    </g:each>

<laser:htmlEnd />
