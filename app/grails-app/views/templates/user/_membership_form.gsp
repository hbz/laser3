<%@ page import="com.k_int.kbplus.auth.UserOrg" %>

<g:if test="${tmplProfile}"><%-- /profile/index --%>
    <g:form name="affiliationRequestForm" controller="profile" action="processJoinRequest" class="ui form" method="get">

        <div class="two fields">
            <div class="field">
                <label>Organisation</label>
                <g:select name="org"
                          from="${availableOrgs}"
                          optionKey="id"
                          optionValue="name"
                          class="ui fluid search dropdown"/>
            </div>

            <div class="field">
                <label>Role</label>
                <g:select name="formalRole"
                          from="${availableOrgRoles}"
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

<g:if test="${tmplUserEdit}"><%-- /user/edit --%>
    <g:form controller="user" action="addAffiliation" class="ui form" method="get" params="${[id: userInstance.id]}">

        <div class="two fields">
            <div class="field">
                <label>Organisation</label>
                <g:select name="org"
                          from="${availableOrgs}"
                          optionKey="id"
                          optionValue="name"
                          class="ui fluid search dropdown"/>
            </div>

            <div class="field">
                <label>Role</label>
                <g:select name="formalRole"
                          from="${availableOrgRoles}"
                          optionKey="id"
                          optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                          class="ui fluid dropdown"/>
            </div>
        </div>

        <div class="field">
            <label></label>
            <%--<button type="submit" class="ui orange button">${message(code: 'profile.membership.add.button')} und Mail versenden</button>--%>
            <button type="submit" class="ui button">${message(code: 'profile.membership.add.button')}</button>
        </div>
    </g:form>
</g:if>

<g:if test="${tmplUserCreate}"><%-- /user/create --%>
    <div class="two fields">
        <div class="field">
            <label>Organisation</label>
            <g:select name="org"
                      from="${availableOrgs}"
                      optionKey="id"
                      optionValue="name"
                      class="ui fluid search dropdown"/>
        </div>

        <div class="field">
            <label>Role</label>
            <g:select name="formalRole"
                      from="${availableOrgRoles}"
                      optionKey="id"
                      optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                      class="ui fluid dropdown"/>
        </div>
    </div>
</g:if>
