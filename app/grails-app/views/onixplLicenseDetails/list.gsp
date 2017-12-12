
<%@ page import="com.k_int.kbplus.OnixplLicense" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'onixplLicense.label', default: 'OnixplLicense')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div>
				

					<h1 class="ui header"><g:message code="default.list.label" args="[entityName]" /></h1>

			<semui:messages data="${flash}" />
				
				<table class="ui celled striped table">
					<thead>
						<tr>
						
							<th class="header"><g:message code="onixplLicense.title.label" default="Title" /></th>

                            <th class="header"><g:message code="onixplLicense.licenses.label" default="${message(code:'laser', default:'LAS:eR')} ${message(code:'license.plural')}" /></th>

                            <g:sortableColumn property="Type" title="${message(code: 'onixplLicense.type.label', default: 'Type')}" />

                            <g:sortableColumn property="Status" title="${message(code: 'onixplLicense.status.label', default: 'Status')}" />

                            <g:sortableColumn property="Document" title="${message(code: 'onixplLicense.document.label', default: 'Document')}" />

                            <th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${onixplLicenseInstanceList}" var="onixplLicenseInstance">
						<tr>

                            <td>
                                ${onixplLicenseInstance.title}
                            </td>
						
							<td>
                                <ul>
                                    <g:each in="${onixplLicenseInstance.licenses}">
                                        <li>
                                            <g:link controller="licenseDetails" action="index" id="${it.id}">${it}</g:link>
                                        </li>
                                    </g:each>
                                </ul>
                            </td>

                            <td>
                                <g:each in="${onixplLicenseInstance.licenses}">
                                <li>
                                    ${it.type?.value}
                                </li>
                            </g:each>
                            </td>

                            <td>
                                <g:each in="${onixplLicenseInstance.licenses}">
                                    <li>
                                        ${it.status?.value}
                                    </li>
                                </g:each>
                            </td>
						
							<td>
                                <g:link controller="doc" action="show" id="${onixplLicenseInstance.doc.id}">${onixplLicenseInstance.doc?.title}</g:link>
                            </td>
						
							<td class="link">
								<g:link action="index" id="${onixplLicenseInstance.id}" class="ui tiny button">Show</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>

                <semui:paginate total="${onixplLicenseInstanceTotal}" />

		</div>
	</body>
</html>
