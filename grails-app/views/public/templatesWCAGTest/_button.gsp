<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn" %>
<h3 class="ui dividing header">3.1. Link funktioniert als Button mit Tooltip</h3>
<a role="button" class="${Btn.ICON.SIMPLE_TOOLTIP} la-audit-button" href='https://www.w3.org/' data-content="Das ist der Inhalt des Tooltips">
    <i class="${Icon.SIG.INHERITANCE} la-js-editmode-icon"></i>
</a>
<h3 class="ui dividing header">3.2. Link funktioniert als Button ohne Tooltip</h3>
<g:link aria-label="Das ist eine Beschreibung für den Accessibility Tree" controller="public" action="wcagTest" params="" class="${Btn.ICON.POSITIVE}">
    <i aria-hidden="true" class="${Icon.SYM.YES}"></i>
</g:link>


<h3 class="ui dividing header">3.3. Button mit Text und für den Accessibility-Tree verstecktem Icon</h3>
<button class="${Btn.SIMPLE}"> Filter
        <i class="filter icon"></i>
        <span aria-label="Anzahl der gesetzten Filter" class="ui circular label">111</span>
</button>


<h3 class="ui dividing header">3.4. Toggle-Button in einer Beschreibungsliste</h3>
<div class="la-inline-lists">
    <div class="ui card">
        <div class="content">
            <dl>
                <dt class="control-label">Status</dt>
                <dd class="la-js-editmode-container">

                    <g:if test="${params.toggle=="true"}">
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('.meinToggleButton').addClass('green');
                            $('.la-js-editmode-icon').removeClass('slash');
                            $('.la-js-editmode-icon').removeClass('la-thumbtack');
                            $('.la-js-editmode-icon').addClass('thumbtack');
                            $('.meinToggleButton').attr('data-content','Wert wird vererbt');
                        </laser:script>
                    </g:if>
                    <g:else>
                        <g:if test="${params.toggle=="false"}">
                            <laser:script file="${this.getGroovyPageFileName()}">
                                $('.meinToggleButton').removeClass('green');
                                $('.la-js-editmode-icon').addClass('slash');
                                $('.la-js-editmode-icon').addClass('la-thumbtack');
                                $('.la-js-editmode-icon').removeClass('thumbtack');
                                $('.meinToggleButton').attr('data-content','Wert wird nicht vererbt');
                            </laser:script>
                        </g:if>
                    </g:else>
                    <g:set var="test" value='true' />
                    <g:link
                        controller= 'public'
                        action='wcagTest'
                        data-content="Wert wird nicht vererbt"
                        class="${Btn.ICON.SIMPLE_TOOLTIP} mini la-audit-button meinToggleButton"
                        params="['toggle': params.toggle=='true'?false:true]"
                    >
                        <i aria-hidden="true" class="la-js-editmode-icon ${Icon.SIG.INHERITANCE_OFF}"></i>

                    </g:link>


                </dd>
            </dl>

        </div>
    </div>
</div>

<h3 class="ui dividing header">3.5. Button, der Inhalte ein- und ausblendet</h3>
<section aria-label="Filter">
    <laser:render template="templatesWCAGTest/toggle" />
</section>


%{--<h3 class="ui dividing header">Toggle-Button in einer Beschreibungsliste</h3>
<div class="la-inline-lists">
    <div class="ui card">
        <div class="content">
            <dl>
                <dt class="control-label">Status</dt>
                <dd><span><a href="#" id="de.laser.Subscription:11636:status" class="xEditableManyToOne editable editable-click" data-value="de.laser.RefdataValue:103" data-pk="de.laser.Subscription:11636" data-type="select" data-name="status" data-source="/ajax/remoteRefdataSearch/subscription.status?oid=de.laser.Subscription%3A11636&amp;constraint=removeValue_deleted" data-url="/laser/ajax/genericSetData" data-emptytext="Bearbeiten">Aktiv</a></span></dd>
                <dd class="la-js-editmode-container">

                    <g:if test="${params.toggle=="true"}">
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('.meinToggleButton').addClass('green');
                            $('.la-js-editmode-icon').removeClass('slash');
                            $('.la-js-editmode-icon').removeClass('la-thumbtack');
                            $('.la-js-editmode-icon').addClass('thumbtack');
                            $('.meinToggleButton').attr('data-content','Wert wird vererbt');
                        </laser:script>
                    </g:if>
                    <g:else>
                        <g:if test="${params.toggle=="false"}">
                            <laser:script file="${this.getGroovyPageFileName()}">
                                $('.meinToggleButton').removeClass('green');
                                $('.la-js-editmode-icon').addClass('slash');
                                $('.la-js-editmode-icon').addClass('la-thumbtack');
                                $('.la-js-editmode-icon').removeClass('thumbtack');
                                $('.meinToggleButton').attr('data-content','Wert nicht wird vererbt');
                            </laser:script>
                        </g:if>
                    </g:else>
                    <g:set var="test" value='true' />
                    <g:link
                            controller= 'public'
                            action='wcagTest'
                            data-content="Wert wird nicht vererbt"
                            class="${Btn.ICON.SIMPLE_TOOLTIP} mini la-audit-button meinToggleButton"
                            aria-labelledby="wcag_mlbbjc4mb"
                            params="['toggle': params.toggle=='true'?false:true]"
                    >
                        <i aria-hidden="true" class="la-js-editmode-icon ${Icon.SIG.INHERITANCE_OFF}"></i>

                    </g:link>


                </dd>
            </dl>

        </div>
    </div>
</div>--}%

