<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.AuditConfig" %>

<laser:serviceInjection />

<semui:modal id="${auditAttr}_audit_single_config_modal" message="property.audit.menu" editmodal="editmodal">

    <g:form id="audit_config_form" class="ui form" url="[controller:'ajax', action:'processAuditConfigManager']" method="post">
        <g:hiddenField name="target" value="${ownobj.getId()}"/>

        <div class="field">
            <table id="org_role_tab" class="ui celled la-table la-table-small table">
                <thead>
                <tr>
                    <th>Eigenschaft</th>
                    <th>Aktueller Wert</th>
                    <th>Vererbung</th>
                </tr>
                </thead>
                <tbody>
                    <g:each in="${properties}" var="prop">
                        <tr>
                            <td>
                                <g:message code="license.${prop}" default="${prop} *" />
                            </td>
                            <td>
                                <g:if test="${ownobj.getProperty(prop) instanceof RefdataValue}">
                                    ${ownobj.getProperty(prop).getI10n('value')}
                                </g:if>
                                <g:else>
                                    ${ownobj.getProperty(prop)}
                                </g:else>
                            </td>
                            <td class="x">
                                <g:set var="auditMsg" value="${message(code:'property.audit.toggle', args: [])}" />

                                <g:if test="${AuditConfig.getConfig(ownobj, prop)}">
                                    <span data-position="top right" data-tooltip="${message(code:'property.audit.keepOnToggle.tooltip')}">
                                        <i class="icon lock grey"></i>

                                        <input type="checkbox" name="keepProperties" value="${prop}" />
                                    </span>
                                </g:if>

                                &nbsp;

                                <span data-position="top right" data-tooltip="${message(code:'property.audit.tooltip')}">
                                    <i class="icon thumbtack grey"></i>

                                    <g:if test="${AuditConfig.getConfig(ownobj, prop)}">
                                        <input type="checkbox" name="properties" value="${prop}" checked="checked"/>
                                    </g:if>
                                    <g:else>
                                        <input type="checkbox" name="properties" value="${prop}" />
                                    </g:else>
                                </span>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>


    </g:form>

</semui:modal>
