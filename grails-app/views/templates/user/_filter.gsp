<%@ page import="de.laser.ui.Btn; de.laser.auth.Role" %>
<laser:serviceInjection/>

<ui:filter>
    <g:form controller="${controllerName}" action="${actionName}" params="${params.id ? [id:params.id] : []}" method="get" class="ui form">

        <div class="${orgField ? (filterableStatus ? 'five' : 'four') : (filterableStatus ? 'four' : 'three')} fields">
            <div class="field">
                <label for="name"><g:message code="default.search.text"/></label>
                <input type="text" id="name" name="name" value="${params.name}"/>
            </div>

            <g:if test="${orgField}">
                <div class="field">
                    <label for="org"><g:message code="user.org"/></label>
                    <g:select from="${availableComboOrgs}" noSelection="${['': message(code:'default.all')]}" class="ui search dropdown"
                              value="${params.org}" optionKey="${{it.id}}" optionValue="${{it.name}}" id="org" name="org" />
                </div>
            </g:if>

            <div class="field">
                <label for="role"><g:message code="default.role.label"/></label>
                <g:select from="${filterableRoles}"
                          noSelection="${['' : message(code:'default.all')]}"
                          class="ui dropdown clearable"
                          value="${params.role}" optionKey="${{it.id}}" optionValue="${{message(code:'cv.roles.'+it.authority)}}" id="role" name="role" />
            </div>

            <g:if test="${filterableStatus}">
                <div class="field">
                    <label for="status"><g:message code="default.status.label"/></label>
                    <g:select from="${filterableStatus}"
                              noSelection="${['' : message(code:'default.all')]}"
                              class="ui dropdown clearable"
                              value="${params.status}" optionKey="${{it.key}}" optionValue="${{it.value}}" id="status" name="status" />
                </div>
            </g:if>

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" value="${message(code:'default.button.search.label')}" class="${Btn.PRIMARY}"/>
            </div>
        </div>
    </g:form>
</ui:filter>