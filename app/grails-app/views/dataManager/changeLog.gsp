<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'menu.datamanager.changelog', default:'Data Manager Change Log')}</title>
  </head>

  <body>

    <laser:breadcrumbs>
      <laser:crumb message="menu.datamanager.dash" controller="dataManager" action="index"/>
      <laser:crumb message="menu.datamanager.changelog" class="active"/>

      <li class="dropdown pull-right">
        <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">${message(code:'default.button.exports.label', default:'Exports')}<b class="caret"></b></a>
        <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
          <li><g:link controller="dataManager" action="changeLog" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link></li>
        </ul>
      </li>
    </laser:breadcrumbs>

    <laser:flash data="${flash}" />

    <div class="container">
      <h2>${message(code:'menu.datamanager.changelog', default:'Data Manager Change Log')}</h2>
      <h6>${message(code:'menu.institutions.change_log', default:'Change Log')} <span class="pull-right">${message(code:'datamanager.changeLog.num_changes', args:[num_hl])}</span></h6>
      <g:form action="changeLog" controller="dataManager" method="get">
        ${message(code:'datamanager.changeLog.from_date')}:
            <div class="input-append date">
              <input class="span2 datepicker-class" size="16" type="text" 
              name="startDate" value="${params.startDate}">
              <span class="add-on"><i class="icon-th"></i></span> 
            </div>
        &nbsp;
        ${message(code:'datamanager.changeLog.to_date')}:
            <div class="input-append date">
              <input class="span2 datepicker-class" size="16" type="text" 
              name="endDate" value="${params.endDate}">
              <span class="add-on"><i class="icon-th"></i></span> 
            </div>
        <div class="dropdown">
          <span style="vertical-align:bottom;">${message(code:'datamanager.changeLog.actor')} :<span>
          <a class="dropdown-toggle btn" data-toggle="dropdown" href="#">
              ${message(code:'datamanager.changeLog.actor_select')}
              <b class="caret"></b>
          </a>
          <ul class="dropdown-checkboxes dropdown-menu" role="menu">
              <li>
                  <label class="checkbox">
                      <input type="checkbox" name="change_actor_PEOPLE" value="Y"
                      ${params.change_actor_PEOPLE == "Y" ? 'checked' : ''} >
                      ${message(code:'datamanager.changeLog.actor_select.all_real')}
                  </label>
              </li>
              <li>
                  <label class="checkbox">
                      <input type="checkbox" name="change_actor_ALL" value="Y"
                      ${params.change_actor_ALL == "Y" ? 'checked' : ''} >
                      ${message(code:'datamanager.changeLog.actor_select.all_system')}
                  </label>
              </li>
              <g:each in="${actors}" var="a">

                <li>
                    <label class="checkbox">
                        <input type="checkbox" name="change_actor_${a[0]}" value="Y"
                          ${params."change_actor_${a[0]}" == "Y" ? 'checked' : ''} >
                          ${a[1]}
                    </label>                
                </li>
              </g:each>
          </ul>
        </div>

        <br/>

        ${message(code:'datamanager.changeLog.what_changed')}:
        <input type="checkbox" style="vertical-align:top;" name="packages" value="Y" ${params.packages=='Y'?'checked':''}/> <g:message code="package.plural" default="Packages"/> &nbsp;
        <input type="checkbox" style="vertical-align:top;" name="licenses" value="Y" ${params.licenses=='Y'?'checked':''}/> <g:message code="license.plural" default="Licenses"/> &nbsp;
        <input type="checkbox" style="vertical-align:top;" name="titles" value="Y" ${params.titles=='Y'?'checked':''}/> <g:message code="title.plural" default="Titles"/> &nbsp;
        <input type="checkbox" style="vertical-align:top;" name="tipps" value="Y" ${params.tipps=='Y'?'checked':''}/> <g:message code="tipp.plural" default="TIPPs"/> &nbsp; <br/>
        ${message(code:'datamanager.changeLog.how_changed')}:
        <input type="checkbox" style="vertical-align:top;" name="creates" value="Y" ${params.creates=='Y'?'checked':''}/> <g:message code="datamanager.changeLog.new_items" default="New Items"/> &nbsp;
        <input type="checkbox" style="vertical-align:top;" name="updates" value="Y" ${params.updates=='Y'?'checked':''}/> <g:message code="datamanager.changeLog.updates" default="Updates to existing items"/> &nbsp;
        <div><input  class="ui primary button" type="submit" value="${message(code:'default.button.submit.label', default:'Submit')}"/></div>
      </g:form>

    </div>

    <g:set var="counter" value="${offset?:-1 +1}" />

    <g:if test="${formattedHistoryLines?.size() > 0}">

      <div class="container alert-warn">
      <p>
      <g:link action="changeLog"> 
         <input type="button" value="${message(code:'default.button.clear_search.label', default:'Clear Search')}" class="ui negative button" />
      </g:link>
      </p>
        <table class="ui celled table">
          <thead>
            <tr>
              <td></td>
              <td>${message(code:'event.component')}</td>
              <td>${message(code:'event.actor')}</td>
              <td>${message(code:'event.name')}</td>
              <td>${message(code:'event.property')}</td>
              <td>${message(code:'event.old')}</td>
              <td>${message(code:'event.new')}</td>
              <td>${message(code:'event.timestamp')}</td>
            </tr>
          </thead>
          <tbody>
            <g:each in="${formattedHistoryLines}" var="hl">
              <tr>
                <td>${counter++}</td>
                <td><a href="${hl.link}">${hl.name}</a></td>
                <td>
                  <g:link controller="userDetails" action="edit" id="${hl.actor?.id}">${hl.actor?.displayName}</g:link>
                </td>
                <td>${hl.eventName}</td>
                <td>${hl.propertyName}</td>
                <td>${hl.oldValue}</td>
                <td>${hl.newValue}</td>
                <td>${hl.lastUpdated}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>

      <div class="pagination" style="text-align:center">
        <g:if test="${historyLines != null}" >
          <bootstrap:paginate  action="changeLog" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_hl}" />
        </g:if>
      </div>

    </g:if>
    <g:else>
      <div class="container alert-warn">
      </div>
    </g:else>
    <r:script language="JavaScript">
      $('.dropdown-menu').on('click', function(e) {
      if($(this).hasClass('dropdown-checkboxes')) {
          e.stopPropagation();
      }});

      $(".datepicker-class").datepicker({
        format: "yyyy-mm-dd"
      });
  </r:script>
  </body>
</html>
