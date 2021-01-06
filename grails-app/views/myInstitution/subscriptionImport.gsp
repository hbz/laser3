<%@ page import="de.laser.helper.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection/>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title><g:message code="laser"/> : <g:message code="myinst.subscriptionImport.pageTitle"/></title>
  </head>

  <body>
  <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
      <semui:crumb message="menu.institutions.subscriptionImport" class="active"/>
  </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="menu.institutions.subscriptionImport"/></h1>

    <semui:messages data="${flash}" />

          <g:message code="myinst.subscriptionImport.headline"/>
          <a href="${resource(dir: 'files', file: 'bulk_load_subscription_records_template_01.csv')}" download="template_bulk_load_subscription_records.csv">
            <p><g:message code="myinst.subscriptionImport.template"/></p>
          </a>
         <table class="ui celled striped table la-table">
           <thead>
             <tr>
                <th><g:message code="myinst.subscriptionImport.tsvColumnName"/></th>
                <th><g:message code="myinst.subscriptionImport.descriptionColumnName"/></th>
                <th><g:message code="myinst.subscriptionImport.necessaryFormat"/></th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${mappingCols}" var="mpg">
                <%
                    List args = []
                    switch(mpg) {
                        case 'status': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS).collect { it -> it.getI10n('value') })
                            break
                        case 'type': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND).collect { it -> it.getI10n('value') })
                            break
                        case 'form': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM).collect { it -> it.getI10n('value') })
                            break
                        case 'resource': args.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE).collect { it -> it.getI10n('value') })
                            break
                    }
                %>
                <tr>
                    <td>${message(code:"myinst.subscriptionImport.${mpg}",args:args ?: '')}</td>
                    <td>${message(code:"myinst.subscriptionImport.description.${mpg}") ?: ''}</td>
                    <td>${message(code:"myinst.subscriptionImport.format.${mpg}",args:[raw("<ul><li>${args.join('</li><li>')}</li></ul>")]) ?: ''}</td>
                </tr>
              </g:each>
            </tbody>
          </table>

          <g:uploadForm action="processSubscriptionImport" method="post">
            <dl>
              <div class="field">
                <dt><g:message code="myinst.subscriptionImport.upload"/></dt>
                <dd>
                  <input type="file" name="tsvFile" />
                </dd>
              </div>
              <button class="ui button" name="load" type="submit" value="Go"><g:message code="myinst.subscriptionImport.upload"/></button>
            </dl>
          </g:uploadForm>
  </body>
</html>
