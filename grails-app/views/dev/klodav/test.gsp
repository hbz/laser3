<%@ page import="de.laser.utils.PasswordUtils; de.laser.utils.RandomUtils; org.apache.commons.codec.binary.StringUtils; de.laser.Org; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Test" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="dev"/>

<g:render template="klodav/nav" />

<ui:form controller="dev" action="klodav" method="GET">
    <div class="two fields">
        <div class="field">
            <g:textField name="idsearch" placeholder="${message(code:'default.search.identifier')}"/>
        </div>
        <div class="field">
            <button type="submit" class="${Btn.SIMPLE}">
                ${message(code:'search.button')}
            </button>
        </div>
    </div>

    <input name="cmd" type="hidden" value="idsearch" />
    <input name="id" type="hidden" value="test" />

    <g:if test="${idsearchresult}">
        <g:each in="${idsearchresult}" var="sr">
            <div class="field">
                <span class="ui fluid centered label">
                    Suche nach "${sr[0]}" liefert ${sr[1]} Treffer in ${sr[2]}ms
                </span>
            </div>
            <div class="field">
                <pre>${sr[3]}</pre>
            </div>
        </g:each>
    </g:if>
</ui:form>

<style>
    .ui.segment.grey .form { margin-bottom:0 }
    .ui.segment.grey .form .fields { margin-bottom:0 }
</style>

%{--    <div id="webk-content">--}%
%{--        <iframe src="https://wekb-dev.hbz-nrw.de"></iframe>--}%
%{--    </div>--}%

%{--<style>--}%
%{--    #webk-content {--}%
%{--        width: 100%;--}%
%{--        max-width: 100%;--}%
%{--        padding-top: 56.25%;--}%
%{--        position: relative;--}%
%{--    }--}%
%{--    #webk-content iframe {--}%
%{--        width: 100%;--}%
%{--        height: 100%;--}%
%{--        top: 0;--}%
%{--        left: 0;--}%
%{--        position: absolute;--}%
%{--        border: none;--}%
%{--    }--}%
%{--</style>--}%

<laser:htmlEnd />