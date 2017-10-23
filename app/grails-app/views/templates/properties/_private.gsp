%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- initPropertiesScript("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition" %>

<g:if test="${newProp}">
    <g:hasErrors bean="${newProp}">
        <bootstrap:alert class="alert-error">
            <ul>
                <g:eachError bean="${newProp}" var="error">
                    <li> <g:message error="${error}"/></li>
                </g:eachError>
            </ul>
        </bootstrap:alert>
    </g:hasErrors>
</g:if>

<g:if test="${error}">
    <bootstrap:alert class="alert-danger">${error}</bootstrap:alert>
</g:if>

<g:if test="${editable}">
	<g:formRemote url="[controller: 'ajax', action: 'addPrivatePropertyValue']" method="post" 
			name="cust_prop_add_value"
	        class="form-inline" 
	        update="${custom_props_div}"
	        onComplete="initPropertiesScript('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id})">
	        
	    <input type="hidden" name="propIdent"  desc="${prop_desc}" class="customPropSelect"/>
	    <input type="hidden" name="ownerId"    value="${ownobj?.id}"/>
	    <input type="hidden" name="tenantId"   value="${tenant?.id}"/>
	    <input type="hidden" name="editable"   value="${editable}"/>
	    <input type="hidden" name="ownerClass" value="${ownobj?.class}"/>

        <input type="submit" value="${message(code:'default.add.label', args:[message(code:'default.property.label')], default:'Add Property')}" class="ui primary button"/>
	</g:formRemote>
</g:if>

<table class="ui celled table">
    <thead>
	    <tr>
            <th>${message(code:'license.property.table.property')}</th>
            <th>${message(code:'license.property.table.value')}</th>
            <th>${message(code:'license.property.table.notes')}</th>
            <th>${message(code:'license.property.table.delete')}</th>
	    </tr>
    </thead>
    <tbody>
        <g:each in="${ownobj.privateProperties}" var="prop">
            <g:if test="${prop.type?.tenant?.id == tenant?.id}">
                <tr>
                    <td>
                        ${prop.type.getI10n('name')}
                        <g:if test="${prop.type.mandatory}">
                            <span  class="badge badge-warning" title="${message(code: 'default.mandatory.tooltip')}"> &#8252; </span>
                        </g:if>
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span class="badge badge-info" title="${message(code:'default.multipleOccurrence.tooltip')}"> &#9733; </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${prop.type.type == Integer.toString()}">
                            <g:xEditable owner="${prop}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${prop.type.type == String.toString()}">
                            <g:xEditable owner="${prop}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == BigDecimal.toString()}">
                            <g:xEditable owner="${prop}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            <g:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}"/>
                        </g:elseif>
                    </td>
                    <td>
                        <g:xEditable owner="${prop}" type="textarea" field="note"/>
                    </td>
                    <td>
                        <g:if test="${editable == true}">
                        <g:remoteLink controller="ajax" action="deletePrivateProperty"
                            before="if(!confirm('Delete the property ${prop.type.name}?')) return false"
                            params='[propclass: prop.getClass(),ownerId:"${ownobj.id}", ownerClass:"${ownobj.class}", editable:"${editable}"]' id="${prop.id}"
                            onComplete="initPropertiesScript('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}', ${tenant?.id})"
                            update="${custom_props_div}">${message(code:'default.button.delete.label', default:'Delete')}</g:remoteLink>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>
</table>