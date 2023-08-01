<laser:htmlStart message="workflow.plural" serviceInjection="true" />

    <laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView]}"/>

    <ui:controlButtons>
        <laser:render template="actions" model="${[org:org]}"/>
    </ui:controlButtons>

    <ui:h1HeaderWithIcon text="${orgInstance.name}">
        <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
    </ui:h1HeaderWithIcon>

    <laser:render template="nav" />
    <ui:messages data="${flash}" />

    <laser:render template="/templates/workflow/table" model="${[target:orgInstance, workflows:workflows, checklists:checklists]}"/>

%{--    <laser:render template="/templates/workflow/details" model="${[target:orgInstance, workflows:workflows, checklists:checklists]}"/>--}%

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
