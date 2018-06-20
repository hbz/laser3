
<%@ page import="com.k_int.kbplus.OrgAccessPoint" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="semanticUI">
		<g:set var="entityName" value="${message(code: 'accessPoint.label', default: 'Access Point')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>

            <div>
                <h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>
                <semui:messages data="${flash}" />
                <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIP']" id="${accessPointInstance.id}" method="POST">
                    <g:hiddenField name="id" value="${accessPointInstance?.id}" />
                    <div class="inline-lists">
                        <dl>
                            <dt><label for="name">
                                    <g:message code="accessPoint.name" default="Name" id="name"/>
                                </label>
                            </dt>
                            <dd>
                                ${accessPointInstance.name}
                            </dd>
                            <br /><br />

                            <dt>
                                <label for="accessMethod">
                                    <g:message code="accessMethod.label" default="Access Method" />
                                </label>
                            </dt>
                            <dd>
                                ${accessPointInstance.accessMethod}
                                <g:hiddenField name="accessMethod" value="${accessPointInstance.accessMethod.id}"/>
                            </dd>
                            <br /><br />
                            <dt>
                                <label>
                                    <g:message code="accessPoint.ipv4.range" default="IPv4 Range" />
                                </label>
                            </dt>
                            <dd>
                                <div class="one field">
                                    <div class="field wide twelve fieldcontain">
                                        <g:each in="${accessPointInstance.getCidr()}" var="ipv4Range">
                                            <div >${ipv4Range}</div>
                                        </g:each>
                                    </div>
                                </div>
                            </dd>
                            <dt>
                                <label>
                                    <g:message code="accessPoint.ipv6.range" default="IPv6 Range" />
                                </label>
                            </dt>
                            <dd>
                                <div class="one field">
                                    <div class="field wide twelve fieldcontain">
                                        <div >TODO</div>
                                    </div>
                                </div>
                            </dd>
                        </dl>
                    </div>


                    <table class="ui celled striped table">
                        <thead>
                        <tr>
                            <g:sortableColumn property="ipData" title="${message(code: 'accessPoint.ip.data', default: 'IP or IP Range')}" />
                            <th>${message(code: 'accessPoint.actions', default: 'Actions')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${accessPointDataList}" var="accessPointData">
                            <tr>
                                <td>${accessPointData.data}</td>
                                <td class="center aligned">
                                    <g:link action="deleteData" controller="accessPoint" id="${accessPointData.id}" class="ui negative icon button">
                                        <i class="delete icon"></i>
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>

                        <tr>
                            <td>
                                <div class="${hasErrors(bean: accessPoint, field: 'name', 'error')} ui form">
                                    <g:textField name="ip" value="${ip}"/>
                                </div>
                            </td>
                            <td class="center aligned">
                                <input type="Submit" class="ui tiny button" value="${message(code:'accessPoint.button.new', default:'Add')}" onClick="this.form.submit()"class="ui button"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </g:form>

                <div class="ui segment form-actions">
                    <g:link class="ui button" action="accessPoints" controller="organisations" id="${orgId}" >${message(code:'acessPoint.button.back', default:'Back')}</g:link>
                    <g:link class="ui negative button" action="delete" controller="accessPoint" id="${accessPointInstance.id}" >${message(code:'default.button.delete.label', default:'Delete')}</g:link>
                </div>
		</div>
	</body>
</html>
