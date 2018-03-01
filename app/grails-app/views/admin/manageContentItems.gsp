<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Manage Content Items</title>
  </head>

  <body>

      <semui:breadcrumbs>
          <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
          <semui:crumb text="Content Items" class="active"/>
      </semui:breadcrumbs>

      <semui:messages data="${flash}" />


    <div class="ui grid">
      <div class="twelve wide column">
          <table class="ui celled la-table table">
                <thead>
                    <tr>
                        <th>Key</th>
                        <th>Locale</th>
                        <th>Content</th>
                        <th></th>
                    </tr>
                </thead>
            <tbody>
              <g:each in="${items}" var="item">
                <tr>
                  <td>${item.key}</td>
                  <td>${item.locale}</td>
                  <td>${item.content}</td>
                  <td><g:link action="editContentItem" id="${item.key}:${item.locale?:''}">${message('code':'default.button.edit.label')}</g:link></td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </div><!-- .twelve -->
        <div class="four wide column">
            <semui:card class="card-grey">
                <div class="content">
          <g:form action="newContentItem">
            <dl>
              <dt>New Content Item Key</dt>
              <dd><input name="key" type="text"/></dd>

              <dt>New Content Item Locale (Or blank for none)</dt>
              <dd><select name="locale">
                    <option value="">No Locale (Default)</option>
                    <option value="en_GB">British English</option>
                    <option value="es">Español</option>
                    <option value="fr">Français</option>
                    <option value="it">Italiano</option>
                    <option value="de">Deutsch</option>
                    <option value="ja">日本人</option>
                    <option value="zn-CH">中国的</option>
                    <option value="en_US">US English</option>
                  </select></dd>
              <dt>New Content (Markdown)</dt>
              <dd><textarea name="content" rows="5"></textarea></dd>
            </dl>
            <input type="submit" value="Create" class="ui button"/>
          </g:form>
                </div>
            </semui:card>
        </div><!-- .four -->
      </div><!-- .grid -->

  </body>
</html>
