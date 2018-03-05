%{-- To use, add the g:render custom_props inside a div with id=custom_props_div_xxx, add g:javascript src=properties.js --}%
%{-- on head of container page, and on window load execute  --}%
%{-- mcp.initProperties("<g:createLink controller='ajax' action='lookup'/>", "#custom_props_div_xxx"); --}%

<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.License" %>

<g:if test="${newProp}">
    <semui:errors bean="${newProp}" />
</g:if>

<g:if test="${error}">
    <bootstrap:alert class="alert-danger">${error}</bootstrap:alert>
</g:if>


<table class="ui celled la-table la-table-small table">
    <thead>
        <tr>
            <th>${message(code:'property.table.property')}</th>
            <th>${message(code:'property.table.value')}</th>
            <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                <th>${message(code:'property.table.paragraph')}</th>
            </g:if>
            <th>${message(code:'property.table.notes')}</th>
            <th>${message(code:'property.table.delete')}</th>
        </tr>
    </thead>
    <tbody>
        <g:each in="${ownobj.customProperties}" var="prop">
            <g:if test="${prop.type.descr == prop_desc}">
                <tr>
                    <td>
                        ${prop.type.getI10n('name')}
                        <g:if test="${prop.type.multipleOccurrence}">
                            <span class="badge badge-info" title="${message(code:'default.multipleOccurrence.tooltip')}"> &#9733; </span>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${prop.type.type == Integer.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="intValue"/>
                        </g:if>
                        <g:elseif test="${prop.type.type == String.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="stringValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == BigDecimal.toString()}">
                            <semui:xEditable owner="${prop}" type="text" field="decValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == Date.toString()}">
                            <semui:xEditable owner="${prop}" type="date" field="dateValue"/>
                        </g:elseif>
                        <g:elseif test="${prop.type.type == RefdataValue.toString()}">
                            <semui:xEditableRefData owner="${prop}" type="text" field="refValue" config="${prop.type.refdataCategory}"/>
                        </g:elseif>
                    </td>
                    <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                        <td>
                            <semui:xEditable owner="${prop}" type="text" field="paragraph"/>
                        </td>
                    </g:if>
                    <td>
                        <semui:xEditable owner="${prop}" type="textarea" field="note"/>
                    </td>
                    <td class="x">
                        <g:if test="${editable == true}">
                            <g:remoteLink controller="ajax" action="deleteCustomProperty"
                                          before="if(!confirm('Delete the property ${prop.type.name}?')) return false"
                                          params='[propclass: prop.getClass(),ownerId:"${ownobj.id}",ownerClass:"${ownobj.class}", custom_props_div:"${custom_props_div}", editable:"${editable}"]' id="${prop.id}"
                                          onComplete="mcp.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')"
                                          update="${custom_props_div}" class="ui icon negative button">
                                <i class="trash alternate icon"></i>
                                <!--${message(code:'default.button.delete.label', default:'Delete')}-->
                            </g:remoteLink>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
    </tbody>

    <g:if test="${editable}">
        <tfoot>
            <tr>
                <g:if test="${ownobj instanceof com.k_int.kbplus.License}">
                    <td colspan="5">
                </g:if>
                <g:else>
                    <td colspan="4">
                </g:else>

                    <g:formRemote url="[controller: 'ajax', action: 'addCustomPropertyValue']" method="post"
                                  name="cust_prop_add_value"
                                  class="ui form"
                                  update="${custom_props_div}"
                                  onComplete="mcp.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')">

                        <input type="hidden" name="propIdent" desc="${prop_desc}" class="customPropSelect"/>
                        <input type="hidden" name="ownerId" value="${ownobj.id}"/>
                        <input type="hidden" name="editable" value="${editable}"/>
                        <input type="hidden" name="ownerClass" value="${ownobj.class}"/>

                        <input type="hidden" name="custom_props_div" value="${custom_props_div}"/>

                        <input type="submit" value="${message(code:'default.button.add.label')}" class="ui button"/>
                    </g:formRemote>
                </td>
            </tr>
        </tfoot>
    </g:if>

</table>

<!--
<div id="cust_prop_add_modal" class="modal hide">

TODO !!! this modal dialog has not been refactored ..

    <g:formRemote url="[controller: 'ajax', action: 'addCustomPropertyType']" method="post"
                  id="create_cust_prop"
                  name="modal_create_cust_prop"
                  update="${custom_props_div}"
                  onComplete="mcp.initProperties('${createLink(controller:'ajax', action:'lookup')}', '#${custom_props_div}')">
        <input type="hidden" name="ownerId" value="${ownobj.id}"/>
        <input type="hidden" name="ownerClass" value="${ownobj.class}"/>
        <input type="hidden" name="editable" value="${editable}"/>

        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">×</button>

            <h3 class="ui header">Create Custom Property Definition</h3>
        </div>

        <input type="hidden" name="parent" value="${parent}"/>

        <div class="modal-body">
            <dl>
                <dt>
                    <label class="control-label">Property Definition:</label>
                </dt>
                <dd>
                    <label class="property-label">Name:</label>
                    <input type="text" name="cust_prop_name" />
                </dd>
                <dd>
                    <label class="property-label">Type:</label>
                    <g:select from="${PropertyDefinition.validTypes.entrySet()}"
                              optionKey="value" optionValue="key"
                              name="cust_prop_type"
                              id="cust_prop_modal_select" />
                </dd>

                <div class="hide" id="cust_prop_ref_data_name">
                    <dd>
                        <label class="property-label">Refdata Category:</label>
                        <input type="hidden" name="refdatacategory" id="cust_prop_refdatacatsearch"/>
                    </dd>
                </div>
                <dd>
                    <label class="property-label">Context:</label> <g:select name="cust_prop_desc" from="${PropertyDefinition.AVAILABLE_CUSTOM_DESCR}"/>
                </dd>
                <dd>
                    Create value for this property: <g:checkBox name="autoAdd" checked="true"/>
                </dd>
            </dl>
        </div>

        <div class="modal-footer">
            <input id="new_cust_prop_add_btn" type="submit" class="ui button" value="${message(code:'default.button.add.label', default:'Add')}">
            <a href="#" data-dismiss="modal" class="ui button">${message(code:'default.button.close.label', default:'Close')}</a>
        </div>
    </g:formRemote>

</div>-->