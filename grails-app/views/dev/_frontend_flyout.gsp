<h2 class="ui dividing header">Flyout<a class="anchor" id="flyout"></a>
</h2>


<div class="ui flyout test">
    <i class="close icon"></i>
    <div class="ui header">
        <i class="question icon"></i>
        <div class="content">
            Archive Old Messages
        </div>
    </div>
    <div class="content">
        <p>Your inbox is getting full, would you like us to enable automatic archiving of old messages?</p>
    </div>
    <div class="actions">
        <div class="ui red cancel button">
            <i class="remove icon"></i>
            No
        </div>
        <div class="ui green ok button">
            <i class="checkmark icon"></i>
            Yes
        </div>
    </div>
</div>

<laser:script file="">
    $('#flyout-trigger').on ('click', function(e) {
        e.preventDefault()
        $('.ui.flyout.test').flyout('toggle')
    });
</laser:script>

<a class="ui button" id="flyout-trigger">Trigger Flyout</a>
<div class="html ui top attached segment example">
    <div class="ui top attached label">Neues Feature in Fomantic UI 2.9: Flyouts ist die Vereinigung von einem Modal und einer Sidebar</div>
</div>

<div class="annotation transition visible">
    <div class="ui instructive bottom attached segment">
        <pre aria-hidden="true">
            &lt;div class="ui flyout"&gt;
                &lt;i class="close icon">&lt;/i&gt;
            &lt;div class="ui header">
            &lt;i class="question icon">&lt;/i&gt;
            &lt;div class="content"&gt;
                        Archive Old Messages
            &lt;/div&gt;
                &lt;/div&gt;
            &lt;div class="content"&gt;
            &lt;p>Your inbox is getting full, would you like us to enable automatic archiving of old messages?&lt;/p&gt;
                &lt;/div&gt;
                &lt;div class="actions"&gt;
                &lt;div class="ui red cancel button"&gt;
            &lt;i class="remove icon">&lt;/i&gt;
                        No
                        &lt;/div>
                        &lt;div class="ui green ok button"&gt;
                        &lt;i class="checkmark icon">&lt;/i&gt;
                        Yes
                    &lt;/div&gt;
                        &lt;/div&gt;
                        &lt;/div&gt;
        </pre>
    </div>
</div>



