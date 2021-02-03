<%@ page import="de.laser.auth.*" %>
<laser:serviceInjection />

<g:if test="${controllerName == 'user' || (controllerName in ['myInstitution', 'organisation'] && ! userInstance.isAuthorizedInstMember(orgInstance))}">
    <div class="ui segment form">

        <g:form controller="${controllerName}" action="addAffiliation" class="ui form" method="get">

            <g:if test="${controllerName == 'myInstitution'}">
                <input type="hidden" name="uoid" value="${genericOIDService.getOID(userInstance)}" />
            </g:if>
            <g:if test="${controllerName == 'organisation'}">
                <input type="hidden" name="uoid" value="${genericOIDService.getOID(userInstance)}" />
                <input type="hidden" name="id" value="${orgInstance.id}" />
            </g:if>
            <g:if test="${controllerName == 'user'}">
                <input type="hidden" name="id" value="${userInstance.id}" />
            </g:if>

            <div class="two fields">
                <div class="field">
                    <label for="org">${orgLabel ?: 'Organisation'}</label>
                    <g:select name="org" id="org"
                              from="${availableOrgs}"
                              optionKey="id"
                              optionValue="${{(it.sortname ?: '') + ' (' + it.name + ')'}}"
                              class="ui fluid search dropdown"/>
                </div>

                <div class="field">
                    <label for="formalRole">Role</label>
                    <g:select name="formalRole" id="formalRole"
                              from="${Role.findAllByRoleType('user')}"
                              optionKey="id"
                              optionValue="${ {role->g.message(code:'cv.roles.' + role.authority) } }"
                              value="${Role.findByAuthority('INST_USER').id}"
                              class="ui fluid dropdown"/>
                </div>
            </div>

            <div class="field">
                <button type="submit" class="ui button">${message(code: 'profile.membership.add.button')}</button>
            </div>
        </g:form>

    </div>
</g:if>