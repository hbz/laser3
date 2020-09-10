<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.GenericOIDService;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.my.comp_sub')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb text="${message(code: 'menu.my.subscriptions')}" controller="myInstitution" action="currentSubscriptions"/>
    <semui:crumb class="active" message="menu.my.comp_sub"/>
</semui:breadcrumbs>
<br>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'menu.my.comp_sub')}</h1>

<semui:form>
    <g:form class="ui form" action="${actionName}" method="post">
            <div class="ui field">
                <label for="selectedSubscriptions">${message(code: 'default.compare.subscriptions')}</label>

                <select id="selectedSubscriptions" name="selectedObjects" multiple="" class="ui search selection multiple dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${availableSubscriptions.sort { it.dropdownNamingConvention() }}" var="sub">
                        <option <%=(sub in objects) ? 'selected="selected"' : ''%>
                        value="${sub.id}" ">
                        ${sub.dropdownNamingConvention()}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <g:link controller="compare" action="${actionName}"
                        class="ui button">${message(code: 'default.button.comparereset.label')}</g:link>
                &nbsp;
                <input ${params.selectedObjects ? 'disabled' : ''} type="submit"
                                                                    value="${message(code: 'default.button.compare.label')}"
                                                                    name="Compare" class="ui button"/>
            </div>

    </g:form>
</semui:form>

<g:if test="${objects}">
    <g:render template="nav"/>
    <br>
    <br>

    <g:if test="${params.tab == 'compareProperties'}">
            <g:render template="compareProperties"/>
    </g:if>

    <g:if test="${params.tab == 'compareElements'}">
            <g:render template="compareElements"/>
    </g:if>
</g:if>

</body>
</html>
