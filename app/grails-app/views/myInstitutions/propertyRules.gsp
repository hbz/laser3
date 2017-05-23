<%@ page import="com.k_int.kbplus.Org; com.k_int.properties.PropertyDefinition" %>

<!doctype html>
<html>
    <head>
        <meta name="layout" content="mmbootstrap">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>KB+ <g:message code="default.show.label" args="[entityName]" /></title>
        <r:require module="annotations" />
    </head>
    <body>

        <div class="container">
            <ul class="breadcrumb">
                <li>
        	        <g:link controller="home" action="index">Home</g:link>
        	        <span class="divider">/</span>
                </li>
            <li>
        	    <g:link controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}">${institution.name}</g:link>
        	    <span class="divider">/</span>
            </li>
		    <li>
			    <g:link controller="myInstitutions" action="propertyRules" params="${[shortcode:params.shortcode]}">Manage Property Rules</g:link>
		    </li>
        </ul>
    </div>

    <g:if test="${flash.message}">
        <div class="container">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </div>
    </g:if>

    <g:if test="${flash.error}">
        <div class="container">
            <bootstrap:alert class="error-info">${flash.error}</bootstrap:alert>
        </div>
    </g:if>

    <div class="container">
        <h1>${institution?.name} - Manage Property Rules</h1>
    </div>
    
	<div class="container">
		<p>Define properties that are mandatory for members of this organisations.</p>

        <h4>Add new Rules</h4>

        <g:each in="${pprPropertyDescriptions}" var="pdesc">
            <fieldset>
                <g:form class="form-horizontal" params="${['shortcode':params.shortcode]}" action="propertyRules" method="post">

                    <g:select name="privatePropertyRule.propertyOwnerType"
                              from="${pdesc}"
                              optionValue="value"
                              optionKey="key" />

                    <g:select name="privatePropertyRule.propertyDefinition.id"
                              from="${PropertyDefinition.findAllByDescr(pdesc.value)}"
                              optionValue="name"
                              optionKey="id" />

                    <g:field type="hidden" name="privatePropertyRule.propertyTenant" value="${institution.id}" />

                    <g:field type="hidden" name="cmd" value="add" />
                    <button type="submit" class="btn btn-primary">Add</button>
                </g:form>
            </fieldset>
        </g:each>

        <g:if test="${ppRules}">
            <h4>Existing Rules</h4>
            <fieldset>
                <g:form class="form-horizontal" params="${['shortcode':params.shortcode]}" action="propertyRules" method="post">
                        <g:each in="${ppRules}" var="ppr">
                            <p>
                                ${ppr.propertyDefinition.descr} : ${ppr.propertyDefinition.name}
                                <br />
                                <g:checkBox name="propertyRuleDeleteIds" value="${ppr?.id}" checked="false" /> Delete
                            </p>
                        </g:each>

                    <g:field type="hidden" name="cmd" value="delete" />
                    <button type="submit" class="btn btn-primary">Delete</button>
                </g:form>
            </fieldset>
        </g:if>

    </div>

  </body>
</html>
