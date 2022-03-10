<%@ page import="de.laser.helper.RDStore;" %>
<div class="la-icon-list">

    <semui:listIcon type="${tipp.titleType}"/>
    <g:if test="${ie}">
        <g:link controller="issueEntitlement" id="${ie.id}"
                action="show"><strong>${ie.name}</strong>
        </g:link>
    </g:if>
    <g:else>
        <g:link controller="tipp" id="${tipp.id}"
                action="show"><strong>${tipp.name}</strong>
        </g:link>
    </g:else>

    <g:if test="${tipp.hostPlatformURL}">
        <semui:linkIcon
                href="${tipp.hostPlatformURL.startsWith('http') ? tipp.hostPlatformURL : 'http://' + tipp.hostPlatformURL}"/>
    </g:if>
    <br/>

    <g:if test="${!showCompact}">
        <br/>
    </g:if>

    <g:each in="${tipp.ids.sort { it.ns.ns }}" var="title_id">
        <span class="ui small basic image label" style="background: none">
            ${title_id.ns.ns}: <div class="detail">${title_id.value}</div>
        </span>
    </g:each>
<!--                  ISSN:<strong>${tipp.getIdentifierValue('ISSN') ?: ' - '}</strong>,
                  eISSN:<strong>${tipp.getIdentifierValue('eISSN') ?: ' - '}</strong><br />-->
    <br/>

    <g:if test="${!showCompact}">
        <br/>
    </g:if>

    <g:if test="${ie}">
        <div class="item">
            <i class="grey save icon la-popup-tooltip la-delay"
               data-content="${message(code: 'issueEntitlement.perpetualAccessBySub.label')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'issueEntitlement.perpetualAccessBySub.label') + ':'}

                <g:if test="${participantPerpetualAccessToTitle}">
                    ${RDStore.YN_YES.getI10n('value')}
                </g:if>
                <g:else>
                    ${ie.perpetualAccessBySub ? "${RDStore.YN_YES.getI10n('value')}: ${ie.perpetualAccessBySub.dropdownNamingConvention()}" : RDStore.YN_NO.getI10n('value')}
                </g:else>
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.firstAuthor || showEmptyFields)}">
        <div class="item">
            <i class="grey icon user circle la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.firstAuthor')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.firstAuthor') + ':'} ${tipp.firstAuthor}
            </div>
        </div>
    </g:if>

    <g:if test="${(tipp.titleType == 'Book') && (tipp.firstEditor || showEmptyFields)}">
        <div class="item">
            <i class="grey icon industry circle la-popup-tooltip la-delay"
               data-content="${message(code: 'tipp.firstEditor')}"></i>

            <div class="content">
                ${showCompact ? '' : message(code: 'tipp.firstEditor') + ':'} ${tipp.firstEditor}
            </div>
        </div>
    </g:if>

    <div class="ui grid">
        <div class="right aligned wide column">
            <a class="ui mini button" onclick="JSPC.app.showAllTitleInfos(${tipp.id}, ${ie ? ie.id : null});">
                <g:message code="title.details"/>
            </a>
        </div>
    </div>



<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.showAllTitleInfos = function (tippID, ieID) {
        $.ajax({
            url: '<g:createLink controller="ajaxHtml" action="showAllTitleInfos" params="[showPackage: showPackage, showPlattform: showPlattform, showCompact: showCompact, showEmptyFields: showEmptyFields]"/>&tippID='+tippID+'&ieID='+ieID,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#modalAllTitleInfos").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal({
                        onVisible: function () {
                        r2d2.initDynamicSemuiStuff('#modalAllTitleInfos');
                        }
                    }).modal('show');
                }
            });
        }

</laser:script>




