<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Spotlight</title>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb  message="spotlight.addSpotlightPage" class="active"/>
    </semui:breadcrumbs>


   <div>

    <g:if test="${flash.error}">
        <bootstrap:alert class="alert-error">${flash.error}</bootstrap:alert>
    </g:if>
        
    <g:hasErrors bean="${newPage}">
        <bootstrap:alert class="alert-error">
        <ul>
            <g:eachError bean="${newPage}" var="error">
                <li> <g:message error="${error}"/></li>
            </g:eachError>
        </ul>
        </bootstrap:alert>
    </g:hasErrors>

      <div class="span6"> 
          <table class="ui celled striped table">
              <thead>
                <tr>
                  <th>Controller</th>
                  <th>Action</th>
                  <th>Alias</th>
                  <th>Preview</th>
                  <th>Remove</th>
                </tr>
              </thead>
              
              <tbody>
              <g:each in="${pages}" var="page">
                <tr>
                  <td>
                    <g:xEditable owner="${page}" type="text" field="controller"/>
                  </td>
                  <td>
                    <g:xEditable owner="${page}" type="text" field="action"/>
                  </td>
                  <td>
                    <g:xEditable owner="${page}" type="text" field="alias"/>
                  </td>
                  <td><a href="${page.getLink().url}">${page.getLink().linktext}</a></td>
                  <td>
                    <g:link controller="spotlight" action="managePages" id="${page.id}">Remove</g:link>
                  </td>
                </tr>
              </g:each>
              
              </tbody>
          </table>

      <g:form action="managePages" method="POST">
        <label>Controller: </label><input type="text" name="newCtrl"/>
        <label>Action: </label><input type="text" name="newAction"/>
        <label>Alias: </label><input type="text" name="newAlias"/>
        <label></label><input type="submit" value="Add New Page" class="btn-primary"/>
      </g:form>
      </div>
    </div>
      <r:script language="JavaScript">
        $('.xEditableValue').editable();
      </r:script>
  </body>

</html>
