<%@ page import="com.k_int.kbplus.License; com.k_int.kbplus.GenericOIDService;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.my.comp_lic')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb text="${message(code: 'menu.my.licenses')}" controller="myInstitution" action="currentLicenses"/>
    <semui:crumb class="active" message="menu.my.comp_lic"/>
</semui:breadcrumbs>
<br>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'menu.my.comp_lic')}</h1>

<semui:form>
    <g:form class="ui form" action="${actionName}" method="post">
            <div class="ui field">
                <label for="selectedLicenses">${message(code: 'default.compare.licenses')}</label>

                <select id="selectedLicenses" name="selectedObjects" multiple="" class="ui search selection multiple dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${availableLicenses.sort { it.dropdownNamingConvention() }}" var="lic">
                        <option <%=(lic in objects) ? 'selected="selected"' : ''%>
                        value="${lic.id}" ">
                        ${lic.dropdownNamingConvention()}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <g:link controller="compare" action="${actionName}"
                        class="ui button">${message(code: 'default.button.comparereset.label')}</g:link>
                &nbsp;
                <input ${params.selectedLicenses ? 'disabled' : ''} type="submit"
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
