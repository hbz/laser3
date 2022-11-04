<%@page import="de.laser.GlobalSourceSyncService" %>

<laser:htmlStart text="Manage Global Sources" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="Global Sources" class="active" />
    </ui:breadcrumbs>

    <ui:messages data="${flash}" />

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
              <th>${message(code:'default.identifier.label')}</th>
              <th>${message(code:'default.name.label')}</th>
              <th>${message(code:'default.type.label')}</th>
            <th>Up To</th>
            <th>URL</th>
            <th>URL to editable instance</th>
            <th>URL with Up To</th>
            <th>List Prefix</th>
            <th>Full Prefix</th>
            <th>Principal</th>
            <th>Credentials</th>
            <th>RecType</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${sources}" var="source">
              <%
                  String component
                  switch(source.rectype) {
                      case GlobalSourceSyncService.RECTYPE_PACKAGE: component = "Package"
                          break
                      case GlobalSourceSyncService.RECTYPE_TITLE: component = "Title"
                          break
                      case GlobalSourceSyncService.RECTYPE_ORG: component = "Org"
                          break
                      case GlobalSourceSyncService.RECTYPE_TIPP: component = "TitleInstancePackagePlatform"
                          break
                  }
              %>
            <tr>
              <td>${source.identifier}</td>
              <td>${source.name}</td>
              <td>${source.type}</td>
              <td>${source.haveUpTo}</td>
              <td>${source.uri}</td>
              <td>${source.editUri}</td>
              <td>
                  <g:if test="${source.type == "OAI"}">
                      <g:link uri="${source.uri + '?verb=ListRecords&metadataPrefix=' + source.fullPrefix + '&from=' + formatDate(format: "yyyy-MM-dd'T'HH:mm:ss'Z'", date: source.haveUpTo)}" target="_blank">Link</g:link>
                  </g:if>
                  <g:elseif test="${source.type == "JSON"}">
                      <%
                          Set<String> requestedStatus = ["Current","Expected","Retired","Deleted",GlobalSourceSyncService.PERMANENTLY_DELETED,"Removed"]
                          String statusString = ""
                          requestedStatus.each { String status ->
                              statusString += "&status=${status}"
                          }
                      %>
                      <g:link uri="${source.uri + 'find?componentType='+component+'&changedSince=' + formatDate(format: "yyyy-MM-dd HH:mm:ss", date: source.haveUpTo)}${statusString}" target="_blank">Link</g:link>
                  </g:elseif>
              </td>
              <td>${source.listPrefix}</td>
              <td>${source.fullPrefix}</td>
              <td>${source.principal}</td>
              <td>${source.credentials}</td>
              <td>
                  ${component}
              </td>
              <td>
                <%--<g:link class="ui button"
                        controller="yoda"
                        onclick="return confirm('Deleting this package will remove all tracking info and unlink any local packages - Are you sure?')"
                        action="deleteGlobalSource"
                        id="${source.id}">${message('code':'default.button.delete.label')}</g:link>--%>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <br />

    <ui:form controller="yoda" action="newGlobalSource">

            <div class="fields two">
                <div class="field">
                    <label for="uri">Global Source URI</label>
                    <input type="text" id="uri" name="uri" placeholder="eg https://gokb.kuali.org/gokb/oai/packages" value="https://some.host/gokb/oai/packages"/>
                </div>
                <div class="field">
                    <label for="editUri">Global Source Edit URI</label>
                    <input type="text" id="editUri" name="editUri" placeholder="eg https://gokb.kuali.org/gokb/oai/packages" value="https://some.host/gokb/oai/packages"/>
                </div>
            </div>


            <div class="fields two">
                <div class="field">
                    <label for="identifier">Global Source Identifier</label>
                    <input type="text" id="identifier" name="identifier" placeholder="eg GOKbLive"/>
                </div>
                <div class="field">
                    <label for="name">Global Source Name</label>
                    <input type="text" id="name" name="name" placeholder="eg we:kb Live Server"/>
                </div>
            </div>

            <div class="fields two">
                <div class="field">
                    <label for="type">Global Source Type</label>
                    <select id="type" name="type"><option value="OAI">we:kb OAI Source</option></select>
                </div>
                <div class="field">
                    <label for="rectype">Record Type</label>
                    <select id="rectype" name="rectype">
                        <option value="${GlobalSourceSyncService.RECTYPE_PACKAGE}">Package</option>
                        <option value="${GlobalSourceSyncService.RECTYPE_TITLE}">Title</option>
                        <option value="${GlobalSourceSyncService.RECTYPE_ORG}">Org</option>
                        <option value="${GlobalSourceSyncService.RECTYPE_TIPP}">TIPP</option>
                    </select>
                </div>
            </div>

            <div class="fields four">
                <div class="field">
                    <label for="listPrefix">List Records Prefix</label>
                    <input type="text" id="listPrefix" name="listPrefix" placeholder="oai_dc" value="oai_dc"/>
                </div>
                <div class="field">
                    <label for="fullPrefix">Full Record Prefix</label>
                    <input type="text" id="fullPrefix" name="fullPrefix" placeholder="gokb" value="gokb"/>
                </div>
                <div class="field">
                    <label for="principal">Principal (Username)</label>
                    <input type="text" id="principal" name="principal" placeholder=""/>
                </div>
                <div class="field">
                    <label for="credentials">Credentials (Password)</label>
                    <input type="text" id="credentials" name="credentials" placeholder=""/>
                </div>
            </div>

            <div class="field">
                <label>&nbsp;</label>
                <input type="submit" value="Submit" class="ui button"/>
            </div>

    </ui:form>

<laser:htmlEnd />
