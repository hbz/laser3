<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Admin::Identifier Same-As Upload</title>
  </head>

  <body>
    <div>
      <div class="span12">
        <h1 class="ui header">Import Identifier Same-As Relations</h1>
      <g:if test="${hasStarted}">
        <div>
            <bootstrap:alert id="procesing_alert" class="alert-info"><g:message code="admin.upload.issnL" default="admin.upload.issnL=CSV Processing has started in the background and will not require intervention"></g:message> </bootstrap:alert>
        </div>
      </g:if>
        <p>Upload a file of tab separated equivalent identifiers. By default, the assumption is ISSN -&gt; ISSNL mappings</p>
           
        <g:form action="uploadIssnL" method="post" enctype="multipart/form-data">
          <dl>
            <div class="control-group">
              <dt>Upload ISSN to ISSNL mapping file</dt>
              <dd>
                <input type="file" name="sameasfile" />
              </dd>
            </div>
            <button name="load" type="submit" value="Go">Upload...</button>
          </dl>
        </g:form>
      </div>
    </div>
  </body>
</html>
