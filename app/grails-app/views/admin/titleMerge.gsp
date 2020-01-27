<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Admin::Title Merge</title>
  </head>

  <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.admin.dash" controller="admin" action="index" />
            <semui:crumb text="Title Merge" class="active"/>
        </semui:breadcrumbs>
        <br>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Title Merge</h1>

        <semui:messages data="${flash}" />

        <semui:form>
            <g:form action="executeTiCleanup" method="post" class="ui form" data-confirm-id="clearUp_form">
                <table class="ui table">
                    <tbody>
                        <tr>
                            <th>Title duplicates without TIPPs</th>
                        </tr>
                        <g:each in="${dupsWithoutTIPP}" var="duplicate">
                            <tr>
                                <td>${duplicate}</td>
                            </tr>
                        </g:each>
                        <tr>
                            <th>Title duplicates with TIPPs</th>
                        </tr>
                        <g:each in="${dupsWithTIPP}" var="duplicate">
                            <tr>
                                <td>${duplicate}</td>
                            </tr>
                        </g:each>
                        <tr></tr>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td>
                                <g:hiddenField name="id" value="clearUp" />
                                <div class="ui icon negative button js-open-confirm-modal"
                                     data-confirm-tokenMsg="${message(code: "confirm.dialogtriggerCleanup")}"
                                     data-confirm-term-how="clearUp"
                                     data-confirm-id="clearUp" >
                                    <i class="bath icon"></i>
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
