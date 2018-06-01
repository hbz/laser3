<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${institution.name} :: ${tip?.title?.title} ${message(code:'default.via', default:'via')} ${tip?.provider?.name}</title>
  </head>

  <body>

  <semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <semui:crumb controller="myInstitution" action="tipview" text="${message(code:'title.plural', default:'Titles')}" />
    <semui:crumb  class="active"      text="${tip?.title?.title} ${message(code:'default.via', default:'via')} ${tip?.provider?.name}"/>
  </semui:breadcrumbs>

        <semui:messages data="${flash}" />

          <h3 class="ui header">${message(code:'myinst.tip.coreDates', default:'Core dates')}</h3>

          <ul>
            <g:each in="${tip.coreDates}" var="cd">
              <li>${cd}</li>
            </g:each>
          </ul>
          <g:if test="${tip.coreDates == null || tip.coreDates.size() == 0}">
            ${message(code:'myinst.tip.no_coreDates', default:'No Core Dates Currently')}
          </g:if>

          <h3 class="ui header">${message(code:'myinst.tip.usageRecords', default:'Usage Records')}</h3>

          <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'default.start.label', default:'Start')}</th>
              <th>${message(code:'default.end.label', default:'End')}</th>
              <th>${message(code:'myinst.tip.reportingYear', default:'Reporting Year')}</th>
              <th>${message(code:'myinst.tip.reportingMonth', default:'Reporting Month')}</th>
              <th>${message(code:'default.type.label', default:'Type')}</th>
              <th>${message(code:'default.value.label', default:'Value')}</th>
            </tr>
          </thead>
          <tbody>
              <g:if test="${usage && usage.size() > 0 }">
                <g:each in="${usage}" var="u">
                  <tr>
                    <td>${u.factFrom}</td>
                    <td>${u.factTo}</td>
                    <td>${u.reportingYear}</td>
                    <td>${u.reportingMonth}</td>
                    <td>${u.factType?.value}</td>
                    <td>${u.factValue}</td>
                  </tr>
                </g:each>
              </g:if>
              <g:else>
                <tr><td colspan="6">${message(code:'myinst.tip.noUsage', default:'No usage currently')}</td></tr>
              </g:else>
            </tbody>
          </table>

          <h4 class="ui header">${message(code:'myinst.tip.addUsage', default:'Add usage information')}</h4>
          <g:form action="tip" id="${params.id}">
            ${message(code:'myinst.tip.usageDate', default:'Usage Date')} : <input type="date" name="usageDate"/><br/>
            ${message(code:'myinst.tip.usageRecord', default:'Usage Record')} : <input type="text" name="usageValue"/><br/>
            ${message(code:'myinst.tip.usageType', default:'Usage Type')} :
            <g:select name='factType'
    from='${com.k_int.kbplus.RefdataValue.executeQuery('select o from RefdataValue as o where o.owner.desc=?',['FactType'])}'
    optionKey="id" optionValue="${{it.getI10n('value')}}"></g:select><br/>

            <button type="submit">${message(code:'default.add.label', args:[message(code:'default.usage.label', default:'Usage')], default:'Add Usage')}</button>
          </g:form>

  </body>
</html>
