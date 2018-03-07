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
            <semui:card>

                <g:form action="newContentItem" class="ui form">
                    <div class="field">
                        <label>New Content Item Key</label>
                        <input name="key" type="text"/>
                    </div>

                      <div class="field">
                          <label>New Content Item Locale (Or blank for none)</label>
                            <select name="locale">
                              <option value="">No Locale (Default)</option>
                              <option value="en_GB">British English</option>
                              <option value="es">Español</option>
                              <option value="fr">Français</option>
                              <option value="it">Italiano</option>
                              <option value="de">Deutsch</option>
                              <option value="ja">日本人</option>
                              <option value="zn-CH">中国的</option>
                              <option value="en_US">US English</option>
                            </select>
                      </div>

                      <div class="field">
                        <label>New Content (Markdown)</label>
                        <textarea name="content" rows="5"></textarea>
                      </div>
                      <div class="field">
                        <input type="submit" value="Create" class="ui button"/>
                      </div>
                </g:form>
            </semui:card>
        </div><!-- .four -->
      </div><!-- .grid -->

  </body>
</html>
