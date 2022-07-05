<%@ page import="de.laser.PlatformAccessMethod" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="laser">
		<g:set var="entityName" value="${message(code: 'accessMethod.label')}" />
		<title>${message(code:'laser')} : <g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

                <laser:render template="breadcrumb" model="${[ accessMethod:accessMethod, params:params ]}"/>

                <semui:h1HeaderWithIcon message="default.edit.label" args="[entityName]" />

                <semui:messages data="${flash}" />

                <g:form class="ui form" url="[controller: 'accessMethod', action: 'update']" method="POST">
                    <g:hiddenField id="accessMethod_id_${accessMethod.id}" name="id" value="${accessMethod.id}" />
                    <div class="la-inline-lists">
                        <div class="ui card">
                            <div class="content">
                                <dl>
                                    <dt><g:message code="default.type.label" /></dt>
                                    <dd>${accessMethod.accessMethod.getI10n('value')}</dd>
                                </dl>
                                <dl>

                                    <dt><g:message code="accessMethod.valid_from" /></dt>
                                    <dd>
                                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${accessMethod.validFrom}" />
                                        <g:hiddenField name="validFrom" value="${accessMethod.validFrom}" />
                                    </dd>
                                </dl><dl>

                                    <dt><g:message code="accessMethod.valid_to" /></dt>
                                    <dd>
                                        <semui:datepicker hideLabel="true" id="validTo" name="validTo" value ="${accessMethod.validTo}">
                                        </semui:datepicker>
                                    </dd>

                                </dl>
                            </div>
                        </div><!-- .card -->
                    </div>


                    <div class="ui segment form-actions">
                        <g:link class="ui button" controller="platform" action="accessMethods"  id="${platfId}" >${message(code:'default.button.back')}</g:link>
                        <input type="Submit" class="ui button" value="${message(code:'accessMethod.button.update')}" onClick="this.form.submit()" />
                        <g:link class="ui negative button" action="delete" controller="accessMethod"
                                id="${accessMethod.id}" onclick="return confirm('${message(code: 'accessMethod.delete.confirm', args: [(accessMethod.accessMethod ?: 'this access method')])}')"
                        >${message(code:'default.button.delete.label')}</g:link>
                    </div>

                </g:form>

	</body>
</html>
