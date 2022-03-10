<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Manage ES Sorces</title>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
        <semui:crumb text="ES Sources" class="active" />
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
            <th>Identifier</th>
            <th>Name</th>
            <th>Host</th>
            <th>Active</th>
            <th>Port</th>
            <th>Index</th>
            <th>Cluster</th>
            <th>Laser ES</th>
            <th>we:kb ES</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${sources}" var="source">
            <tr>
              <td>${source.identifier}</td>
              <td>
                  <semui:xEditable owner="${source}" field="name"/>
              </td>
              <td>
                  <semui:xEditable owner="${source}" field="host"/>
              </td>
              <td>
                  <semui:xEditableBoolean owner="${source}" field="active" />
              </td>
              <td>
                  <semui:xEditable owner="${source}" field="port"/>
              </td>
              <td>
                  <semui:xEditable owner="${source}" field="index"/>
              </td>
              <td>
                    <semui:xEditable owner="${source}" field="cluster"/>
              </td>
              <td>
                  <semui:xEditableBoolean owner="${source}" field="laser_es" />
              </td>
              <td>
                  <semui:xEditableBoolean owner="${source}" field="gokb_es" />
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <br />

    <semui:form>
        <g:form action="newESSource" class="ui form">

            <div class="field">
                <label>ES Source Host</label>
                <input type="text" name="host" placeholder="" value=""/>
            </div>

            <div class="fields two">
                <div class="field">
                    <label>ES Source Identifier</label>
                    <input type="text" name="identifier" placeholder=""/>
                </div>
                <div class="field">
                    <label>ES Source Name</label>
                    <input type="text" name="name" placeholder=""/>
                </div>
            </div>

            <div class="fields two">
                <div class="field">
                    <label>ES Index</label>
                    <input type="text" name="index" placeholder=""/>
                </div>
                <div class="field">
                    <label>ES Cluster</label>
                    <input type="text" name="cluster" placeholder=""/>
                </div>
            </div>

            <div class="fields two">
                <div class="field">
                    <label>LAS:eR ES</label>
                    <select name="laser_es">
                        <option value="0">No</option>
                        <option value="1">Yes</option>
                    </select>
                </div>
                <div class="field">
                    <label>we:kb ES</label>
                    <select name="gokb_es">
                        <option value="0">No</option>
                        <option value="1">Yes</option>
                    </select>
                </div>
            </div>

            <div class="field">
                <label>&nbsp;</label>
                <input type="submit" value="Submit" class="ui button"/>
            </div>

        </g:form>
    </semui:form>
  </body>
</html>
