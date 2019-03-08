<%@ page import="com.k_int.kbplus.auth.UserOrg" %>

<g:if test="${tmplProfile}">
    <g:form name="affiliationRequestForm" controller="profile" action="processJoinRequest" class="ui form" method="get">

        <div class="two fields">
            <div class="field">
                <label>Organisation</label>
                <g:select name="org"
                          from="${com.k_int.kbplus.Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')}"
                          optionKey="id"
                          optionValue="name"
                          class="ui fluid search dropdown"/>
            </div>

            <div class="field">
                <label>Role</label>
                <g:select name="formalRole"
                          from="${com.k_int.kbplus.auth.Role.findAllByRoleType('user')}"
                          optionKey="id"
                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                          class="ui fluid dropdown"/>
            </div>
        </div>

        <div class="field">
            <label></label>
            <button id="submitARForm" data-complete-text="Request Membership" type="submit" class="ui button">${message(code: 'profile.membership.request.button', default:'Request Membership')}</button>
        </div>
    </g:form>
</g:if>
<g:if test="${tmplAdmin}">
    <g:form controller="userDetails" action="addAffiliation" class="ui form" method="get" params="${[id: userInstance.id]}">

        <div class="two fields">
            <div class="field">
                <label>Organisation</label>
                <g:select name="org"
                          from="${com.k_int.kbplus.Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')}"
                          optionKey="id"
                          optionValue="name"
                          class="ui fluid search dropdown"/>
            </div>

            <div class="field">
                <label>Role</label>
                <g:select name="formalRole"
                          from="${com.k_int.kbplus.auth.Role.findAllByRoleType('user')}"
                          optionKey="id"
                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                          class="ui fluid dropdown"/>
            </div>
        </div>

        <div class="field">
            <label></label>
            <button type="submit" class="ui orange button">${message(code: 'profile.membership.add.button')} und Mail versenden</button>
        </div>
    </g:form>
</g:if>
