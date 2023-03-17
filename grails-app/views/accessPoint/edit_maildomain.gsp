<%@ page import="de.laser.oap.OrgAccessPoint; de.laser.storage.RDConstants" %>

<g:set var="entityName" value="${message(code: 'accessPoint.label')}"/>
<laser:htmlStart text="${message(code: "default.edit.label", args: [entityName])}" serviceInjection="true"/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('body').attr('class', 'organisation_accessPoint_edit_${accessPoint.accessMethod}');
</laser:script>

<laser:render template="breadcrumb" model="${[accessPoint: accessPoint, params: params]}"/>

<g:if test="${accessService.checkConstraint_INST_EDITOR_PERM_BASIC( inContextOrg )}">
    <ui:controlButtons>
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="edit_mailDomain"
                        params="[id: accessPoint.id, exportXLSX: true]">${message(code: 'accessPoint.exportAccessPoint')}</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </ui:controlButtons>
</g:if>

<ui:h1HeaderWithIcon text="${orgInstance.name}"/>

<laser:render template="/organisation/nav"
              model="${[orgInstance: accessPoint.org, inContextOrg: inContextOrg, tmplAccessPointsActive: true]}"/>

<h2 class="ui header la-noMargin-top"><g:message code="default.edit.label" args="[entityName]"/></h2>

<g:link class="ui right floated button" controller="organisation" action="accessPoints"
        id="${orgInstance.id}">
    ${message(code: 'default.button.back')}
</g:link>
<br>
<br>

<ui:messages data="${flash}"/>


<div class="la-inline-lists">
    <div class="ui card">
        <div class="content">
            <dl>
                <dt><g:message code="default.name.label"/></dt>
                <dd><ui:xEditable owner="${accessPoint}" field="name"/></dd>
            </dl>
            <dl>
                <dt><g:message code="default.note.label"/></dt>
                <dd><ui:xEditable owner="${accessPoint}" field="note"/></dd>
            </dl>
            <dl>
                <dt><g:message code="accessMethod.label"/></dt>
                <dd>
                    ${accessPoint.accessMethod.getI10n('value')}
                    <g:hiddenField id="accessMethod_id_${accessPoint.accessMethod.id}" name="accessMethod"
                                   value="${accessPoint.accessMethod.id}"/>
                </dd>
            </dl>
        </div>
    </div>


    <h3 class="ui header">${message(code: 'accessPoint.mailDomain.configuration')}
    </h3>

    <table class="ui celled la-js-responsive-table la-table table very compact">
        <thead>
        <tr>
            <th class="fifteen wide">Mail-Domain</th>
            <th class="one wide">${message(code: 'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${accessPointDataList.accessPointMailDomains}" var="accessPointData">
            <tr>
                <td>${accessPointData.mailDomain}</td>
                <td class="center aligned">
                    <g:if test="${accessService.checkConstraint_INST_EDITOR_PERM_BASIC( inContextOrg )}">
                        <g:link action="deleteAccessPointData" controller="accessPoint" id="${accessPointData.id}"
                                class="ui negative icon button js-open-confirm-modal"
                                data-confirm-tokenMsg="${message(code: 'confirm.dialog.delete.generic', args: [accessPointData.mailDomain])}"
                                data-confirm-term-how="delete"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="trash very alternate icon"></i>
                        </g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <g:if test="${!accessPoint.hasProperty('entityId') && accessService.checkConstraint_INST_EDITOR_PERM_BASIC( inContextOrg )}">
        <div class="ui divider"></div>

        <div class="content">
            <g:form class="ui form" url="[controller: 'accessPoint', action: 'addMailDomain']"
                    method="POST">
                <g:hiddenField name="id" id="mailDomain_id" value="${accessPoint.id}"/>
                <g:hiddenField name="accessMethod" id="mailDomain_accessMethod" value="${accessPoint.accessMethod}"/>

                <div class="ui form">
                    <div class="field">
                        <label for="mailDomain_id">Mail-Domain</label>

                        <g:field type="text" name="mailDomain" id="mailDomain_id" value="${mailDomain}"/>
                    </div>
                    <input type="submit" class="ui button"
                           value="${message(code: 'accessPoint.button.add')}"/>
                </div>
            </g:form>
        </div>
    </g:if>

</div>


<br/>

<div class="la-inline-lists">
    <laser:render template="link"
                  model="${[accessPoint: accessPoint, params: params, linkedPlatforms: linkedPlatforms, linkedPlatformSubscriptionPackages: linkedPlatformSubscriptionPackages]}"/>
</div>

</div>


<laser:htmlEnd/>
