<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Manage ES Sorces</title>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
        <semui:crumb text="ES Sources" class="active" />
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

      <table class="ui celled la-table table">
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
            <th>GOKB ES</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${sources}" var="source">
            <tr>
              <td>${source.identifier}</td>
              <td>${source.name}</td>
              <td>${source.active}</td>
              <td>${source.port}</td>
              <td>${source.index}</td>
              <td>${source.cluster}</td>
              <td>${source.laser_es}</td>
              <td>${source.gokb_es}</td>
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
                    <label>GOKB ES</label>
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
