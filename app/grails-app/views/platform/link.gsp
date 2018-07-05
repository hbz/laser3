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
  <g:render template="apLinkContent" model="result" />
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
