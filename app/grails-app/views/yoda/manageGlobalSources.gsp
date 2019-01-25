<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Manage Global Sorces</title>
  </head>

  <body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
        <semui:crumb text="Global Sources" class="active" />
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

      <table class="ui celled la-table table">
        <thead>
          <tr>
            <th>Identifier</th>
            <th>Name</th>
            <th>Type</th>
            <th>Up To</th>
            <th>URL</th>
            <th>URL with Up To</th>
            <th>List Prefix</th>
            <th>Full Prefix</th>
            <th>Principal</th>
            <th>Credentials</th>
            <th>RecType</th>
            <th>${message(code:'sidewide.number')} Local Copies</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${sources}" var="source">
            <tr>
              <td>${source.identifier}</td>
              <td>${source.name}</td>
              <td>${source.type}</td>
              <td>${source.haveUpTo}</td>
              <td>${source.uri}</td>
              <td><g:link
                      uri="${source.uri + '?verb=ListRecords&metadataPrefix=' + source.fullPrefix + '&from=' + formatDate(format: "yyyy-MM-dd'T'HH:mm:ss'Z'", date: source.haveUpTo)}"
                      target="_blank">Link</g:link></td>
              <td>${source.listPrefix}</td>
              <td>${source.fullPrefix}</td>
              <td>${source.principal}</td>
              <td>${source.credentials}</td>
              <td>${source.rectype==0?'Package':'Title'}</td>
              <td>${source.getNumberLocalPackages()}</td>
              <td>
                <g:link class="ui button"
                        controller="yoda"
                        onclick="return confirm('Deleting this package will remove all tracking info and unlink any local packages - Are you sure?')"
                        action="deleteGlobalSource"
                        id="${source.id}">${message('code':'default.button.delete.label')}</g:link>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <br />

    <semui:form>
        <g:form action="newGlobalSource" class="ui form">

            <div class="field">
                <label>Global Source URI</label>
                <input type="text" name="uri" placeholder="eg https://gokb.kuali.org/gokb/oai/packages" value="https://some.host/gokb/oai/packages"/>
            </div>

            <div class="fields two">
                <div class="field">
                    <label>Global Source Identifier</label>
                    <input type="text" name="identifier" placeholder="eg GOKbLive"/>
                </div>
                <div class="field">
                    <label>Global Source Name</label>
                    <input type="text" name="name" placeholder="eg GOKb Live Server"/>
                </div>
            </div>

            <div class="fields two">
                <div class="field">
                    <label>Global Source Type</label>
                    <select name="type"><option value="OAI">GOKb OAI Source</option></select>
                </div>
                <div class="field">
                    <label>Record Type</label>
                    <select name="rectype">
                        <option value="0">Package</option>
                        <option value="1">Title</option>
                    </select>
                </div>
            </div>

            <div class="fields four">
                <div class="field">
                    <label>List Records Prefix</label>
                    <input type="text" name="listPrefix" placeholder="oai_dc" value="oai_dc"/>
                </div>
                <div class="field">
                    <label>Full Record Prefix</label>
                    <input type="text" name="fullPrefix" placeholder="gokb" value="gokb"/>
                </div>
                <div class="field">
                    <label>Principal (Username)</label>
                    <input type="text" name="principal" placeholder=""/>
                </div>
                <div class="field">
                    <label>Credentials (Password)</label>
                    <input type="text" name="credentials" placeholder=""/>
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
