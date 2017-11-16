<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Manage Content Items</title>
  </head>

  <body>

    <div>
        <ul class="breadcrumb">
           <li> <g:link controller="home">${message(code:'default.home.label', default:'Home')}</g:link> <span class="divider">/</span> </li>
           <li>Content Items</li>
        </ul>
    </div>

  <semui:messages data="${flash}" />


    <div>
      <g:form action="editContentItem" id="${params.id}">
        <dl>
          <dt>Key</dt>
          <dd>${contentItem.key}</dd>
          <dt>Locale</dt>
          <dd>${contentItem.locale}</dd>
          <dt>Content (Markdown)</dt>
          <dd><textarea name="content" rows="5">${contentItem.content}</textarea></dd>
        </dl>
        <input type="submit" class="ui primary button"/>
      </g:form>
    </div>
  </body>
</html>
