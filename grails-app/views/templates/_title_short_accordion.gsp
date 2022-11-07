<%@ page import="de.laser.storage.RDStore; de.laser.remote.ApiSource" %>
<div class="item">
    <ui:listIcon type="${tipp.titleType}"/>
    <g:if test="${ie}">
        <g:link controller="issueEntitlement" id="${ie.id}" action="show"><strong>${ie.name}</strong>
        </g:link>
    </g:if>
    <g:else>
        <g:link controller="tipp" id="${tipp.id}" action="show"><strong>${tipp.name}</strong></g:link>
    </g:else>

    <g:if test="${tipp.hostPlatformURL}">
        <ui:linkWithIcon
                href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
    </g:if>
</div>

<div class="item">
    <g:if test="${controllerName != 'tipp' && tipp.id}">
        <g:link class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
                data-content="${message(code: 'laser')}"
                target="_blank"
                controller="tipp" action="show"
                id="${tipp.id}">
            <i class="book icon"></i>
        </g:link>
    </g:if>
    <g:each in="${ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}" var="gokbAPI">
        <g:if test="${tipp.gokbId}">
            <a role="button" class="ui icon tiny blue button la-js-dont-hide-button la-popup-tooltip la-delay"
               data-content="${message(code: 'wekb')}"
               href="${gokbAPI.editUrl ? gokbAPI.editUrl + '/public/tippContent/?id=' + tipp.gokbId : '#'}"
               target="_blank"><i class="la-gokb  icon"></i>
            </a>
        </g:if>
    </g:each>
</div>
<a class="ui mini button" data-ajaxTippId="${tipp.id}"  onclick="JSPC.app.showAllTitleInfos2(${tipp.id}, ${ie ? ie.id : null});">
    <g:message code="title.details"/>
</a>

<laser:script file="${this.getGroovyPageFileName()}">

//für alle Elemente mit Attribut data-xyz
// ajaxTippID
// hänge Klickevent an
// this
    JSPC.app.showAllTitleInfos2 = function (tippID, ieID) {
        console.log("------------------");
    console.log( this );
            console.log("------------------");
        $.ajax({
            url: '<g:createLink controller="ajaxHtml" action="showAllTitleInfos2" params="[showPackage: showPackage, showPlattform: showPlattform, showCompact: showCompact, showEmptyFields: showEmptyFields]"/>&tippID='+tippID+'&ieID='+ieID,
                success: function(result){
                    $("#dynamicAccordionContainer").empty();
                    $("#accordionAllTitleInfos").remove();

                    //$(this).html(result);
%{--                    $("#dynamicAccordionContainer .ui.modal").modal({
                        onVisible: function () {
                            r2d2.initDynamicUiStuff('#accordionAllTitleInfos');
                        }
                    }).modal('show');--}%
                }
            });
        }

</laser:script>