<%@ page import="de.laser.titles.TitleInstance" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Admin::Platform duplicates</title>
  </head>

  <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
            <semui:crumb text="List Platform Duplicates" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Platform Duplicates</h1>

        <semui:messages data="${flash}" />

        <semui:form>
            <g:form action="executePlatformCleanup" method="post" class="ui form" data-confirm-id="clearUp_form">
                <table class="ui table">
                    <tbody>
                        <tr>
                            <th>Platform duplicates without TIPPs</th>
                        </tr>
                        <g:each in="${platformDupsWithoutTIPPs}" var="withoutTIPP">
                            <tr>
                                <td><g:link controller="platform" action="show" id="${withoutTIPP.id}">${withoutTIPP.name}</g:link></td>
                            </tr>
                        </g:each>
                        <tr>
                            <th>Platforms without we:kb ID</th>
                        </tr>
                        <g:each in="${platformsWithoutGOKb}" var="withoutGOKbID">
                            <tr>
                                <td>${withoutGOKbID.name}</td>
                            </tr>
                        </g:each>
                        <tr>
                            <th>Platforms removed from we:kb --> delete</th>
                        </tr>
                        <g:each in="${inexistentPlatforms}" var="inexistent">
                            <tr>
                                <td>${inexistent.name}</td>
                            </tr>
                        </g:each>
                        <tr>
                            <th>Outdated platforms which should be updated</th>
                        </tr>
                        <g:each in="${platformsToUpdate}" var="incorrect">
                            <tr>
                                <td>
                                    ${incorrect.old} to ${incorrect.correct.name}
                                </td>
                            </tr>
                        </g:each>
                        <tr>
                            <th>Incorrect platforms which should be remapped by database query</th>
                        </tr>
                        <g:each in="${database}" var="plat">
                            <tr>
                                <td>
                                    ${plat.id} with Name ${plat.name}
                                </td>
                            </tr>
                        </g:each>
                        <tr>
                            <th>Platform duplicates which should be remapped</th>
                        </tr>
                        <g:each in="${incorrectPlatformDups}" var="incorrect">
                            <tr>
                                <td>
                                    ${incorrect.name}
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td>
                                <g:hiddenField name="id" value="clearUp" />
                                <div class="ui icon negative button js-open-confirm-modal"
                                     data-confirm-tokenMsg="${message(code: "confirm.dialogtriggerCleanup")}"
                                     data-confirm-term-how="clearUp"
                                     data-confirm-id="clearUp">
                                    <g:message code="admin.cleanupTIPP.submit"/>
                                </div>
                            </td>
                        </tr>
                    </tfoot>
                </table>
            </g:form>
        </semui:form>

  </body>
</html>
