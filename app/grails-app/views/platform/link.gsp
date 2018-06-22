<%@ page import="com.k_int.kbplus.Platform" %>
<r:require module="annotations" />
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'platform.label', default: 'Platform')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb controller="platform" #action="index" message="platform.show.all" />
    <semui:crumb class="active" id="${platformInstance.id}" text="${platformInstance.name}" />
</semui:breadcrumbs>

<semui:modeSwitch controller="platform" action="show" params="${params}" />

<h1 class="ui header"><semui:headerIcon />

    <g:if test="${editable}"><span id="platformNameEdit"
                                   class="xEditableValue"
                                   data-type="textarea"
                                   data-pk="${platformInstance.class.name}:${platformInstance.id}"
                                   data-name="name"
                                   data-url='<g:createLink controller="ajax" action="editableSetValue"/>'>${platformInstance.name}</span>
    </g:if>
    <g:else>${platformInstance.name}</g:else>
</h1>

<semui:messages data="${flash}" />
<g:render template="nav" />
<div id="dynamicUpdate">
<semui:filter>
    <g:form action="linkAccessPoint" controller="platform" method="get" class="form-inline ui small form">
        <input type="hidden" name="platform_id" value="${platformInstance.id}">
    <div class="fields">
        <div class="field">
            <g:select class="ui dropdown" name="institutions"
                      from="${institution}"
                      optionKey="id"
                      optionValue="name"
                      value="${selectedInstitution} "
                      onchange="${remoteFunction (
                          action: 'dynamicLink',
                          params: '{platform_id:'+platformInstance.id+', institution_id:this.value}',
                          update: [success: 'dynamicUpdate', failure: 'ohno'],
                      )}"/>
        </div>
        <div class="field">
            <g:select class="ui dropdown" name="AccessPoints"
                          from="${accessPointList}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.status}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
        </div>
        <div class="field">
            <g:submitButton name="submit" class="ui button" value="${message(code:'accessPoint.link.button.label', default:'Zugangsverfahren verknüpfen')}" onClick="return confirmSubmit()"/>

            %{--<g:actionSubmit action="linkAccessPoint" class="ui primary button" value="${message(code:'accessPoint.link.button.label', default:'Zugangsverfahren verknüpfen')}" onClick="return confirmSubmit()"/>--}%
        </div>
    </div>
    </g:form>
</semui:filter>
<div class="ui grid">
    <table class="ui sortable celled la-table table ignore-floatThead la-bulk-header">
        <thead>
        <tr>
            <th></th>
            <th>Aktive Zugangspunkte</th>
            <th>Aktion</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${accessPointLinks}" var="oapl">
            <tr>
                <td><input type="checkbox" name="_bulkflag.aopp" class="bulkcheck"/></td>
                <td>${oapl.oap.name}</td>
                <td>
                    <g:if test="${oapl.oap.hasActiveLink()}">
                        <g:link controller="platform" action="removeAccessPoint" id="${platformInstance.id}" params="[oapl_id: oapl.id]" onclick="return confirm('Zugangspunkt entfernen?')">
                        <i class="unlink icon red"></i>
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="platform" action="linkAccessPoint" id="${platformInstance.id}" params="[oapl_id: oapl.id]" onclick="return confirm('Zugangspunkt hinzufügen?')">
                    <i class="linkify icon"></i>
                        </g:link>
                    </g:else>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</div>
<r:script language="JavaScript">
    function selectAll() {
        $('.bulkcheck').attr('checked')? $('.bulkcheck').attr('checked', false) : $('.bulkcheck').attr('checked', true);
    }
    function confirmSubmit() {
        if ( $('#bulkOperationSelect').val() === 'remove' ) {
          var agree=confirm('${message(code:'default.continue.confirm', default:'Are you sure you wish to continue?')}');
          if (agree)
            return true ;
          else
            return false ;
        }
      }
</r:script>
</body>
</html>
