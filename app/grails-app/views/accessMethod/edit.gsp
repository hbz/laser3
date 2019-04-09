<%@ page import="com.k_int.kbplus.PlatformAccessMethod" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'accessMethod.label', default: 'Access Method')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

            <div>
                <g:render template="breadcrumb" model="${[ accessMethod:accessMethod, params:params ]}"/>
                <h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>
                <semui:messages data="${flash}" />




                <g:form class="ui form" url="[controller: 'accessMethod', action: 'update']" method="POST">
                    <g:hiddenField name="id" value="${accessMethod.id}" />
                    <div class="la-inline-lists">
                        <div class="ui card">
                            <div class="content">
                                <dl>
                                    <dt><g:message code="accessMethod.type" default="Type" /></dt>
                                    <dd>${accessMethod.accessMethod.getI10n('value')}</dd>
                                </dl>
                                <dl>

                                    <dt><g:message code="accessMethod.valid_from" default="Valid From" /></dt>
                                    <dd>
                                        <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${accessMethod.validFrom}" />
                                        <g:hiddenField name="validFrom" value="${accessMethod.validFrom}" />
                                    </dd>
                                </dl><dl>

                                    <dt><g:message code="accessMethod.valid_to" default="Valid To" /></dt>
                                    <dd>
                                        <semui:datepicker hideLabel="true" id="validTo" name="validTo" value ="${accessMethod.validTo}">
                                        </semui:datepicker>
                                    </dd>

                                </dl>
                            </div>
                        </div><!-- .card -->
                    </div>


                    <div class="ui segment form-actions">
                        <g:link class="ui button" controller="platform" action="accessMethods"  id="${platfId}" >${message(code:'accessMethod.button.back', default:'Back')}</g:link>
                        <input type="Submit" class="ui button" value="${message(code:'accessMethod.button.update', default:'Update')}" onClick="this.form.submit()" />
                        <g:link class="ui negative button" action="delete" controller="accessMethod"
                                id="${accessMethod.id}" onclick="return confirm('${message(code: 'accessMethod.delete.confirm', args: [(accessMethod.accessMethod ?: 'this access method')])}')"
                        >${message(code:'default.button.delete.label', default:'Delete')}</g:link>
                    </div>

                </g:form>

            </div>
	</body>
</html>
