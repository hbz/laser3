<%@ page import="de.laser.helper.RDConstants; com.k_int.kbplus.RefdataCategory;static de.laser.helper.RDStore.*" %>
<laser:serviceInjection/>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title><g:message code="laser"/> : <g:message code="myinst.subscriptionImport.pageTitle"/></title>
  </head>

  <body>
  <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
    <semui:crumb message="menu.institutions.subscriptionImport" class="active"/>
  </semui:breadcrumbs>
  <br>
    <semui:messages data="${flash}" />

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon /><g:message code="menu.institutions.subscriptionImport"/></h1>

          <g:message code="myinst.subscriptionImport.headline"/>
          <a href="${resource(dir: 'resources/downloadFile', file: 'bulk_load_subscription_records_template_01.csv')}" download="template_bulk_load_subscription_records.csv">
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
                        case 'status': args.addAll(RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SUBSCRIPTION_STATUS).collect { it -> it.getI10n('value') })
                            break
                        case 'instanceOf':
                            List<String> parentSubscriptionType = []
                            if(accessService.checkPerm("ORG_CONSORTIUM"))
                                parentSubscriptionType << SUBSCRIPTION_TYPE_CONSORTIAL.getI10n('value')
                            else if(accessService.checkPerm("ORG_INST_COLLECTIVE"))
                                parentSubscriptionType << SUBSCRIPTION_TYPE_LOCAL.getI10n('value')
                            args.addAll(parentSubscriptionType)
                            break
                        case 'type': args.addAll(RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SUBSCRIPTION_TYPE).collect { it -> it.getI10n('value') })
                            break
                        case 'form': args.addAll(RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SUBSCRIPTION_FORM).collect { it -> it.getI10n('value') })
                            break
                        case 'resource': args.addAll(RefdataCategory.getAllRefdataValues(de.laser.helper.RDConstants.SUBSCRIPTION_RESOURCE).collect { it -> it.getI10n('value') })
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
