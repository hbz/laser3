<%@ page import="de.laser.storage.RDStore;" %>
<laser:htmlStart message="workflow.plural" serviceInjection="true" />

    <laser:render template="breadcrumb" model="${[ license:license, params:params ]}"/>

    <ui:controlButtons>
        <laser:render template="actions" />
    </ui:controlButtons>

    <g:set var="visibleOrgRelationsJoin" value="${visibleOrgRelations.findAll{it.roleType != RDStore.OR_SUBSCRIPTION_CONSORTIA}.sort{it.org.sortname}.collect{it.org}.join(' – ')}"/>
    <ui:h1HeaderWithIcon visibleOrgRelationsJoin="${visibleOrgRelationsJoin}">
        <ui:xEditable owner="${license}" field="reference" id="reference"/>
    </ui:h1HeaderWithIcon>
    <ui:anualRings object="${license}" controller="license" action="workflows" navNext="${navNextLicense}" navPrev="${navPrevLicense}"/>

    <laser:render template="nav" />
    <ui:messages data="${flash}" />

    <laser:render template="/templates/workflow/table" model="${[target:license, workflows:workflows, checklists:checklists]}"/>

%{--    <laser:render template="/templates/workflow/details" model="${[target:license, workflows:workflows, checklists:checklists]}"/>--}%

%{--    <div id="wfModal" class="ui modal"></div>--}%

%{--    <laser:script file="${this.getGroovyPageFileName()}">--}%
%{--        $('.wfModalLink').on('click', function(e) {--}%
%{--            e.preventDefault();--}%
%{--            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'));--}%
%{--            func();--}%
%{--        });--}%
%{--        $('button[data-wfId]').on('click', function(e) {--}%
%{--            var trigger = $(this).hasClass('la-modern-button');--}%
%{--            $('div[data-wfId]').hide();--}%
%{--            $('button[data-wfId]').addClass('la-modern-button');--}%
%{--            if (trigger) {--}%
%{--                $('div[data-wfId=' + $(this).removeClass('la-modern-button').attr('data-wfId') + ']').show();--}%
%{--            }--}%
%{--        });--}%

%{--        <g:if test="${info}">--}%
%{--            $('button[data-wfId=' + '${info}'.split(':')[3] + ']').trigger('click');--}%
%{--        </g:if>--}%
%{--        <g:else>--}%
%{--            if ($('button[data-wfId]').length == 1) {--}%
%{--                $('button[data-wfId]').trigger('click');--}%
%{--            }--}%
%{--        </g:else>--}%
%{--    </laser:script>--}%

<laser:htmlEnd />
