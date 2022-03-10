<div class="ui card">
    <div class="content">
        <dl>
            <dt class="control-label">Status</dt>

            <dd>
                <a href="#" class="myXeditable"   id="status" data-type="select" data-pk="1" data-url="" data-title="Select status"></a>
                <laser:script file="${this.getGroovyPageFileName()}">
                        $('#status').editable({
                            value: 2,
                            source: [
                                {value: 1, text: 'Active'},
                                {value: 2, text: 'Bestellt'},
                                {value: 3, text: 'Entscheidung steht aus'},
                                {value: 4, text: 'Erscheinen eingestellt'},
                                {value: 5, text: 'Geplant'},
                                {value: 6, text: 'In Verhandlung'}
                            ]
                        });
                </laser:script>
            </dd>
        </dl>
    </div>
</div>