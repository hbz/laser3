<%@ page import="de.laser.annotations.UIDoc; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: New Icons" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="klodav" id="icons" class="item active"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Icons</g:link>
    <g:link controller="dev" action="klodav" id="buttons" class="item"><i class="${Icon.SIG.NEW_OBJECT} yellow"></i> New Buttons</g:link>
    <g:link controller="dev" action="klodav" id="markdown" class="item"><i class="${Icon.SIG.NEW_OBJECT} orange"></i> Markdown</g:link>
    <g:link controller="dev" action="klodav" id="security" class="item"><i class="${Icon.SIG.NEW_OBJECT} red"></i> Security</g:link>
</nav>

<div class="ui info message">
    <p class="ui header">
        Usage
        <button class="${Btn.ICON.PRIMARY}" id="metaToggle1" style="float: right"><i class="${Icon.UI.INFO}"></i></button>
        <button class="${Btn.ICON.SECONDARY}" id="metaToggle2" style="float: right"><i class="${Icon.UI.HELP}"></i></button>
    </p>
    <pre>&lt;button class=&quot;&dollar;{Btn.ICON.SECONDARY}&quot; id=&quot;metaToggle2&quot;&gt;&lt;i class=&quot;&dollar;{Icon.UI.HELP}&quot;&gt;&lt;/i&gt;&lt;/button&gt;</pre>
    <pre>&lt;button class=&quot;&dollar;{Btn.ICON.PRIMARY}&quot; id=&quot;metaToggle1&quot;&gt;&lt;i class=&quot;&dollar;{Icon.UI.INFO}&quot;&gt;&lt;/i&gt;&lt;/button&gt;</pre>
</div>

<div class="ui basic segment">
    <p class="ui header">de.laser.ui.Icon</p>
    <div class="ui five cards">
        <g:each in="${Icon.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f" status="i">
            <div class="ui mini card" data-cat="${f.name.split('\\.').last().split('_').first()}">
                <div class="content">
                    <div class="header">
                        <i class="${Icon[f.name]} large"></i>
                        ${f.name.split('\\.').last()}
                    </div>
                    <div class="meta hidden">${Icon[f.name]}</div>
                    <g:if test="${f.getAnnotation(de.laser.annotations.UIDoc)}">
                        <div class="description hidden">${f.getAnnotation(UIDoc).usage()}</div>
                    </g:if>
                </div>
            </div>
        </g:each>
    </div>
</div>

<g:each in="${Icon.getDeclaredClasses().findAll{ true }}" var="ic">
    <div class="ui basic segment">
        <p class="ui header">${ic.name.replace(Icon.name + '$', 'Icon.')}</p>
        <div class="ui five cards">
            <g:each in="${ic.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f">
                <div class="ui mini card" data-cat="${f.name.split('\\.').last().split('_').first()}">
                    <div class="content">
                        <div class="header">
                            <i class="${ic[f.name]} large"></i>
                            ${f.name.split('\\.').last()}
                        </div>
                        <div class="meta hidden">${ic[f.name]}</div>
                        <g:if test="${f.getAnnotation(UIDoc)}">
                            <div class="description hidden">${f.getAnnotation(UIDoc).usage()}</div>
                        </g:if>
                    </div>
                </div>
            </g:each>
        </div>
    </div>
</g:each>

%{--<div class="ui segment">--}%
%{--    <p class="ui header">de.laser.ui.Icon</p>--}%
%{--    <table class="ui very basic compact celled table">--}%
%{--        <tbody>--}%
%{--            <g:each in="${Icon.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f" status="i">--}%
%{--                <tr data-cat="${f.name.split('\\.').last().split('_').first()}">--}%
%{--                    <td>--}%
%{--                        <i class="${Icon[f.name]} large"></i>--}%
%{--                    </td>--}%
%{--                    <td>--}%
%{--                        ${f.name.split('\\.').last()}--}%
%{--                    </td>--}%
%{--                    <td class="meta">--}%
%{--                        ${Icon[f.name]}--}%
%{--                    </td>--}%
%{--                </tr>--}%
%{--            </g:each>--}%
%{--        </tbody>--}%
%{--    </table>--}%
%{--</div>--}%

%{--<g:each in="${Icon.getDeclaredClasses().findAll{ true }}" var="ic">--}%
%{--    <div class="ui segment">--}%
%{--        <p class="ui header">${ic.name.replace(Icon.name + '$', 'Icon.')}</p>--}%
%{--        <table class="ui very basic compact celled table">--}%
%{--            <tbody>--}%
%{--                <g:each in="${ic.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f">--}%
%{--                    <tr data-cat="${f.name.split('\\.').last().split('_').first()}">--}%
%{--                        <td>--}%
%{--                            <i class="${ic[f.name]} large"></i>--}%
%{--                        </td>--}%
%{--                        <td>--}%
%{--                            ${f.name.split('\\.').last()}--}%
%{--                        </td>--}%
%{--                        <td class="meta">--}%
%{--                            ${ic[f.name]}--}%
%{--                        </td>--}%
%{--                    </tr>--}%
%{--                </g:each>--}%
%{--            </tbody>--}%
%{--        </table>--}%
%{--    </div>--}%
%{--</g:each>--}%

<style>
    #mainContent .ui.segment > .ui.header   { padding-bottom: 1em; }
    #mainContent .ui.card .content .header  { color: #3b3b3b; }
    #mainContent .ui.card.hover             { background-color: #f3f3f3; border-color: #cc2711; }
</style>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#mainContent .ui.card').hover(
        function() {
            let dc = $(this).attr('data-cat')
            if (dc) {
                $('#mainContent .ui.card[data-cat=' + dc + ']').addClass('hover')
            }
        },
        function() {
            $('#mainContent .ui.card').removeClass('hover')
        }
    );
    $('#metaToggle1').click(function() { $('#mainContent .ui.card .description').toggleClass('hidden'); });
    $('#metaToggle2').click(function() { $('#mainContent .ui.card .meta').toggleClass('hidden'); });
</laser:script>

<laser:htmlEnd />
