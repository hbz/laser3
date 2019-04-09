
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
                    <g:form class="ui form" url="[controller: 'accessPoint', action: 'edit_proxy']" id="${accessPointInstance.id}" method="POST">
                            <g:hiddenField name="id" value="${accessPointInstance?.id}" />    
                        <div class="inline-lists">
                            <dl>                
                                <dt><label for="name">
                                        <g:message code="accessPoint.name" default="Name" />
                                    </label>
                                </dt>
                                <dd> 
                                    <g:textField name="name" value="${accessPointInstance.name}"/>
                                </dd>
                                <br /><br />
                                <dt>
                                    <label for="accessMethod">
                                        <g:message code="accessMethod.label" default="Access Method" />
                                    </label>
                                </dt>
                                <dd>
                                    ${accessPointInstance.accessMethod.name}
                                </dd>
                                <br /><br />
                                <dt>
                                    <label for="authorizationMethod">
                                        <g:message code="accessMethod.type" default="Type" />
                                    </label>
                                </dt>
                                <dd>
                                    <laser:select class="values"
                                        name="authorizationMethod"
                                        from="${com.k_int.kbplus.OrgAccessPoint.getAllRefdataValues('Authorization Method')}"
                                        optionKey="id"
                                        optionValue="value" />
                                </dd>
                                <br /><br />
                                <dt>
                                    <label for="first_name">
                                        <g:message code="accessMethod.data" default="Data" />
                                    </label>
                                </dt>
                                <dd>
                                    <div class="two fields">
                                        <div class="field wide twelve fieldcontain">
                                            <g:textField name="modeDetails" value="${params.modeDetails}"/>
                                        </div>
                                        <div class="field wide five fieldcontain">
                                            <g:select id="iptype" name='iptype'
                                        noSelection="${['null':'CIDR']}"
                                        from="${['CIDR','IP-Range','IP']}"></g:select>
                                        </div>
                                    </div>
                                </dd>
                                <br /><br />
                                <dt>
                                    <label>
                                        <g:message code="accessPoint.validityRange" default="Gültigkeit" />
                                    </label>
                                </dt>
                                <dd>

                                    <semui:datepicker label ="Von:" id="validFrom" name="validFrom" placeholder ="default.date.label" value ="${params.validFrom}">
                                    </semui:datepicker>
                                    <semui:datepicker label ="Bis:" id="validTo" name="validTo" placeholder ="default.date.label" value ="${params.validTo}">
                                    </semui:datepicker>
                                </dd>
                            </dl>
                        </div>
                        
                        <div class="ui segment form-actions">
                                <button type="submit" class="ui button">
                                        <i class="icon-ok icon-white"></i>
                                        <g:message code="default.button.update.label" default="Update" />
                                </button>
                                <button type="submit" class="ui negative button" name="_action_delete" formnovalidate>
                                        <i class="icon-trash icon-white"></i>
                                        <g:message code="default.button.delete.label" default="Delete" />
                                </button>
                        </div>

                    </g:form>
                
		</div>
	</body>
</html>
