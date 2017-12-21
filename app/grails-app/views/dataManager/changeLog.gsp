<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'menu.datamanager.changelog', default:'Data Manager Change Log')}</title>
  </head>

  <body>
  
    <semui:breadcrumbs>
      <semui:crumb message="menu.datamanager.dash" controller="dataManager" action="index"/>
      <semui:crumb message="menu.datamanager.changelog" class="active"/>

      <li class="dropdown pull-right">
        <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">${message(code:'default.button.exports.label', default:'Exports')}<b class="caret"></b></a>
        <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
          <li><g:link controller="dataManager" action="changeLog" params="${params+[format:'csv']}">${message(code:'default.button.exports.csv', default:'CSV Export')}</g:link></li>
        </ul>
      </li>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <div>
      <h2 class="ui header">${message(code:'menu.datamanager.changelog', default:'Data Manager Change Log')}</h2>
      <h6 class="ui header">${message(code:'menu.institutions.change_log', default:'Change Log')} <span class="pull-right">${message(code:'datamanager.changeLog.num_changes', args:[num_hl])}</span></h6>

        <semui:filter>
            <g:form action="changeLog" controller="dataManager" method="get" class="ui form">
              <div class="fields">
                  <semui:datepicker label="datamanager.changeLog.from_date" name="startDate" placeholder="default.date.label" value="${params.startDate}" />

                  <semui:datepicker label="datamanager.changeLog.to_date" name="endDate" placeholder="default.date.label" value="${params.endDate}" />

              </div>

        <div class="dropdown">
          <span>${message(code:'datamanager.changeLog.actor')} :<span>
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
        <div><input  class="ui button" type="submit" value="${message(code:'default.button.submit.label', default:'Submit')}"/></div>
      </g:form>
      </semui:filter>
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
              <th></th>
              <th>${message(code:'event.component')}</th>
              <th>${message(code:'event.actor')}</th>
              <th>${message(code:'event.name')}</th>
              <th>${message(code:'event.property')}</th>
              <th>${message(code:'event.old')}</th>
              <th>${message(code:'event.new')}</th>
              <th>${message(code:'event.timestamp')}</th>
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


        <g:if test="${historyLines != null}" >
          <semui:paginate  action="changeLog" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" maxsteps="${max}" total="${num_hl}" />
        </g:if>

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

  </r:script>
  </body>
</html>
