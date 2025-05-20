<%@ page import="de.laser.ui.Icon;" %>
<laser:htmlStart message="menu.devDocs" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" class="active" />
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.devDocs" type="dev"/>

<div class="ui equal width grid la-clear-before" style="margin:1em 0;">
    <div class="row">
        <div class="column">
            <div class="ui divided relaxed list">
                <div class="item">
                    <i class="icon code la-list-icon"></i>
                    <div class="content">
                        <g:link controller="dev" action="frontend">Frontend</g:link>
                    </div>
                </div>
                <div class="item">
                    <i class="icon code la-list-icon"></i>
                    <div class="content">
                        <g:link controller="dev" action="klodav">klodav</g:link>
                    </div>
                </div>
            </div>
        </div>
        <div class="column"></div>
        <div class="column"></div>
        <div class="column"></div>
    </div>
</div>

<laser:htmlEnd />
