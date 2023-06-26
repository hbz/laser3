<%@page import="de.laser.GlobalSourceSyncService; de.laser.config.ConfigMapper" %>

<laser:htmlStart text="Manage Global Sources" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb text="Global Sources" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon text="Global Sources" type="yoda" />

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
          </tr>
        </thead>
        <tbody>
          <g:each in="${sources}" var="source">
              <%
                  String component
                  switch(source.rectype) {
                      case GlobalSourceSyncService.RECTYPE_PACKAGE: component = "Package"
                          break
                      case GlobalSourceSyncService.RECTYPE_PLATFORM: component = "Platform"
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
                  <%--
                      Set<String> requestedStatus = ["Current","Expected","Retired","Deleted",Constants.PERMANENTLY_DELETED,"Removed"]
                      String statusString = ""
                      requestedStatus.each { String status ->
                          statusString += "&status=${status}"
                      }
                  --%>
                  <g:link uri="${source.uri + 'searchApi?componentType='+component+'&changedSince=' + formatDate(format: "yyyy-MM-dd HH:mm:ss", date: source.haveUpTo)}&username=${ConfigMapper.getWekbApiUsername()}&password=${ConfigMapper.getWekbApiPassword()}" target="_blank">Link</g:link>
              </td>
              <td>${source.listPrefix}</td>
              <td>${source.fullPrefix}</td>
              <td>${source.principal}</td>
              <td>${source.credentials}</td>
              <td>
                  ${component}
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>

<laser:htmlEnd />
