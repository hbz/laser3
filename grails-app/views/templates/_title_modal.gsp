<%@ page import="de.laser.helper.RDStore;" %>
<semui:modal id="modalAllTitleInfos" text="${message(code: 'title.details')}"
             hideSubmitButton="true">

    <g:render template="/templates/title_long"
              model="${[ie: ie, tipp: tipp,
                        showPackage: showPackage, showPlattform: showPlattform, showCompact: showCompact, showEmptyFields: showEmptyFields]}"/>

    <br>
    <br>

</semui:modal>
