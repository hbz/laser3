<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} Admin::Identifier Same-As Upload</title>
  </head>

  <body>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Import Identifier Same-As Relations</h1>
      <g:if test="${hasStarted}">
          <semui:msg id="procesing_alert" class="warning" message="admin.upload.issnL" />
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

  </body>
</html>
