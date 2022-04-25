<%@ page import="de.laser.Org" %>

<g:set var="surveyConfigOrgs" value="${Org.findAllByIdInList(surveyConfig.orgs.org.id) ?: null}" />

${surveyConfigSubOrgs?.id}
    <laser:render template="/templates/filter/orgFilterTable"
              model="[orgList         : surveyConfigOrgs,
                      tmplConfigShow  : ['sortname', 'name', 'libraryType', 'surveySubInfo'],
                      surveyConfig: surveyConfig
              ]"/>

    <br />
