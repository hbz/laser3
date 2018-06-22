
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
                <g:render template="breadcrumb" model="${[ accessPointInstance:accessPointInstance, params:params ]}"/>
                <h1 class="ui header"><g:message code="default.edit.label" args="[entityName]" /></h1>
                <semui:messages data="${flash}" />
                <g:form class="ui form" url="[controller: 'accessPoint', action: 'addIP']" id="${accessPointInstance.id}" method="POST">
                    <g:hiddenField name="id" value="${accessPointInstance?.id}" />

                    <div class="la-inline-lists">
                        <div class="ui card">
                            <div class="content">
                                <dl>
                                    <dt><g:message code="org.name.label" default="Name" /></dt>
                                    <dd><semui:xEditable owner="${accessPointInstance}" field="name"/></dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="accessMethod.label" default="Access Method" /></dt>
                                    <dd>
                                        ${accessPointInstance.accessMethod.getI10n('value')}
                                        <g:hiddenField name="accessMethod" value="${accessPointInstance.accessMethod.id}"/>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="accessPoint.ipv4.range" default="IPv4 Range" /></dt>
                                    <dd>
                                        <g:each in="${accessPointInstance.getIpv4Cidr()}" var="ipv4Ranges">
                                            <div >${ipv4Ranges}</div>
                                        </g:each>
                                    </dd>
                                </dl>
                                <dl>
                                    <dt><g:message code="accessPoint.ipv6.range" default="IPv6 Range" /></dt>
                                    <dd>
                                        <g:each in="${accessPointInstance.getIpv6Cidr()}" var="ipv6Range">
                                            <div >${ipv4Ranges}</div>
                                        </g:each>
                                    </dd>
                                </dl>
                            </div>
                        </div><!-- .card -->
                    </div>

                    <table  class="ui celled la-table table">
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
                                <input type="Submit" class="ui tiny button" value="${message(code:'accessPoint.button.add', default:'Add')}" onClick="this.form.submit()"class="ui button"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </g:form>

                <div class="ui segment form-actions">
                    <g:link class="ui button" action="accessPoints" controller="organisations" id="${orgId}" >${message(code:'accessPoint.button.back', default:'Back')}</g:link>
                    <g:link class="ui negative button" action="delete" controller="accessPoint"
                            id="${accessPointInstance.id}" onclick="return confirm('${message(code: 'accessPoint.details.delete.confirm', args: [(accessPointInstance.name ?: 'this access point')])}')"
                    >${message(code:'default.button.delete.label', default:'Delete')}</g:link>
                </div>
		</div>
	</body>
</html>
