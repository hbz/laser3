<%@page import="de.laser.GlobalSourceSyncService; de.laser.config.ConfigMapper" %>

<laser:htmlStart message="menu.yoda.manageGlobalSources" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb message="menu.yoda.manageGlobalSources" class="active" />
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.manageGlobalSources" type="yoda" />

    <ui:messages data="${flash}" />

      <table class="ui celled la-js-responsive-table la-table table">
        <thead>
          <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'default.name.label')}</th>
            <th>${message(code:'default.type.label')}</th>
            <th>URL</th>
            <th>RecType</th>
            <th>Up To</th>
            <th>URL with Up To</th>
            <th>Active</th>
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
                      case GlobalSourceSyncService.RECTYPE_PROVIDER: component = "Org"
                          break
                      case GlobalSourceSyncService.RECTYPE_TIPP: component = "TitleInstancePackagePlatform"
                          break
                      case GlobalSourceSyncService.RECTYPE_VENDOR: component = "Vendor"
                          break
                  }
              %>
            <tr>
              <td>${source.id}</td>
              <td>${source.name}</td>
              <td>${source.type}</td>
              <td>${source.getUri()}</td>
              <td>${component}</td>
              <td><ui:xEditable owner="${source}" field="haveUpTo" type="date"/></td>
              <td>
                  <%--
                      Set<String> requestedStatus = ["Current","Expected","Retired","Deleted",Constants.PERMANENTLY_DELETED,"Removed"]
                      String statusString = ""
                      requestedStatus.each { String status ->
                          statusString += "&status=${status}"
                      }
                  --%>
                  <g:link uri="${source.getUri() + '/searchApi?componentType='+component+'&changedSince=' + formatDate(format: "yyyy-MM-dd HH:mm:ss", date: source.haveUpTo)}&username=${ConfigMapper.getWekbApiUsername()}&password=${ConfigMapper.getWekbApiPassword()}&sort=lastUpdated" target="_blank">Link</g:link>
              </td>
              <td>${source.active}</td>
            </tr>
          </g:each>
        </tbody>
      </table>

<laser:htmlEnd />
