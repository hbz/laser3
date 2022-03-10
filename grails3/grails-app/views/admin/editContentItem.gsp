<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Manage Content Items</title>
  </head>

  <body>

    <div>
        <ul class="breadcrumb">
           <li> <g:link controller="home">${message(code:'default.home.label')}</g:link> <span class="divider">/</span> </li>
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
        <input type="submit" class="ui button"/>
      </g:form>
    </div>
  </body>
</html>
