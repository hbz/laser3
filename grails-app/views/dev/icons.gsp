<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: New Icons" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="dev" action="klodav" class="item">Various</g:link>
    <g:link controller="dev" action="icons" class="item active">New Icons</g:link>
</nav>

<div class="ui basic segment">
    <p class="ui header">de.laser.helper.Icons</p>
    <div class="ui five cards">
        <g:each in="${Icons.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f" status="i">
            <div class="ui mini card" data-cat="${f.name.split('\\.').last().split('_').first()}">
                <div class="content">
                    <div class="header">
                        <i class="${Icons[f.name]} large"></i>
                        ${f.name.split('\\.').last()}
                    </div>
                    <div class="meta">${Icons[f.name]}</div>
                </div>
            </div>
        </g:each>
    </div>
</div>

<g:each in="${Icons.getDeclaredClasses().findAll{ true }}" var="ic">
    <div class="ui basic segment">
        <p class="ui header">${ic.name}</p>
        <div class="ui five cards">
            <g:each in="${ic.getDeclaredFields().findAll{ ! it.isSynthetic() }}" var="f">
                <div class="ui mini card" data-cat="${f.name.split('\\.').last().split('_').first()}">
                    <div class="content">
                        <div class="header">
                            <i class="${ic[f.name]} large"></i>
                            ${f.name.split('\\.').last()}
                        </div>
                        <div class="meta">${ic[f.name]}</div>
                    </div>
                </div>
            </g:each>
        </div>
    </div>
</g:each>

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
</laser:script>

<laser:htmlEnd />
