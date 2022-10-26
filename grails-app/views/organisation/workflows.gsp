<laser:htmlStart message="workflow.plural" serviceInjection="true" />

    <ui:breadcrumbs>
        <g:if test="${!inContextOrg}">
            <ui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:if>
    </ui:breadcrumbs>
    <ui:controlButtons>
        <g:if test="${inContextOrg || isProviderOrAgency}">
            <laser:render template="actions" model="${[org:org]}"/>
        </g:if>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" />
    <ui:messages data="${flash}" />

    <laser:render template="/templates/workflow/table" model="${[target: orgInstance, workflows: workflows]}"/>

    <laser:render template="/templates/workflow/details" model="${[target: orgInstance, workflows: workflows]}"/>

%{--    <div id="wfModal" class="ui modal"></div>--}%

%{--    <laser:script file="${this.getGroovyPageFileName()}">--}%
%{--        $('.wfModalLink').on('click', function(e) {--}%
%{--            e.preventDefault();--}%
%{--            var func = bb8.ajax4SimpleModalFunction("#wfModal", $(e.currentTarget).attr('href'), false);--}%
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
