<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : ${institution.name} :: ${tip?.title?.title} ${message(code:'default.via')} ${tip?.provider?.name}</title>
  </head>

  <body>

  <semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
    <semui:crumb controller="myInstitution" action="tipview" text="${message(code:'title.plural')}" />
    <semui:crumb  class="active"      text="${tip?.title?.title} ${message(code:'default.via')} ${tip?.provider?.name}"/>
  </semui:breadcrumbs>

        <semui:messages data="${flash}" />

          <h3 class="ui header">${message(code:'myinst.tip.coreDates')}</h3>

          <ul>
            <g:each in="${tip.coreDates}" var="cd">
              <li>${cd}</li>
            </g:each>
          </ul>
          <g:if test="${tip.coreDates == null || tip.coreDates.size() == 0}">
            ${message(code:'myinst.tip.no_coreDates')}
          </g:if>

          <h3 class="ui header">${message(code:'myinst.tip.usageRecords')}</h3>

          <table class="ui celled la-table table">
          <thead>
            <tr>
              <th>${message(code:'default.start.label')}</th>
              <th>${message(code:'default.end.label')}</th>
              <th>${message(code:'myinst.tip.reportingYear')}</th>
              <th>${message(code:'myinst.tip.reportingMonth')}</th>
              <th>${message(code:'default.type.label')}</th>
              <th>${message(code:'default.value.label')}</th>
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
                <tr><td colspan="6">${message(code:'myinst.tip.noUsage')}</td></tr>
              </g:else>
            </tbody>
          </table>

          <h4 class="ui header">${message(code:'myinst.tip.addUsage')}</h4>
          <g:form action="tip" id="${params.id}">
            ${message(code:'myinst.tip.usageDate')} : <input type="date" name="usageDate"/><br/>
            ${message(code:'myinst.tip.usageRecord')} : <input type="text" name="usageValue"/><br/>
            ${message(code:'myinst.tip.usageType')} :
            <g:select name='factType'
    from='${com.k_int.kbplus.RefdataValue.executeQuery('select o from RefdataValue as o where o.owner.desc=?',[de.laser.helper.RDConstants.FACT_TYPE])}'
    optionKey="id" optionValue="${{it.getI10n('value')}}"></g:select><br/>

            <button type="submit">${message(code:'default.add.label', args:[message(code:'default.usage.label')])}</button>
          </g:form>

  </body>
</html>
