<ui:modal id="replacePropertyDefinitionModal" message="propertyDefinition.exchange.label" isEditModal="isEditModal">
    <g:form class="ui form" url="[controller: controllerName, action: action]">
        <input type="hidden" name="cmd" value="replacePropertyDefinition"/>
        <input type="hidden" name="xcgPdFrom" value=""/>

        <div class="ui icon negative message">
            <i class="exclamation icon"></i>
            <div class="content">
                <div class="header"><g:message code="default.warning.emph" /></div>
                <p><g:message code="menu.institutions.replace_prop.header"/></p>
            </div>
        </div>

        <div class="ui info icon message">
            <i class="info icon"></i>
            <div class="content">
                <div class="header"><g:message code="default.notice" /></div>
                <p><g:message code="menu.institutions.replace_prop.refdata"/></p>
                <p><g:message code="menu.institutions.replace_prop.setting"/></p>
            </div>
        </div>

        <div class="field">
            <label for="xcgPdTo">&nbsp;</label>
            <%--suppress XmlDuplicatedId; message is for IntelliJ and because it errs (it never will be duplicated) --%>
            <select id="xcgPdTo"></select>
        </div>

        <div class="field">
            <label for="overwrite"><g:message code="menu.institutions.replace_prop.overwrite"/></label>
            <g:checkBox name="overwrite" id="overwrite" />
        </div>

    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('button[data-xcg-pd]').on('click', function(e){
            e.preventDefault();
            var pd = $(this).attr('data-xcg-pd');
            //var type = $(this).attr('data-xcg-type');
            //var rdc = $(this).attr('data-xcg-rdc');

            $('#replacePropertyDefinitionModal .xcgInfo').text($(this).attr('data-xcg-debug'));
            $('#replacePropertyDefinitionModal input[name=xcgPdFrom]').attr('value', pd);

            $.ajax({
                url: '<g:createLink controller="ajaxJson" action="searchPropertyAlternativesByOID"/>' + '?oid=' + pd,
                    success: function (data) {
                        var select = '<option></option>';
                        for (var index = 0; index < data.length; index++) {
                            var option = data[index];
                            if (option.value != pd) {
                                select += '<option value="' + option.value + '">' + option.text;
                                if(option.isPrivate == true) {
                                    select += ' (priv.)';
                                }
                                select += '</option>';
                            }
                        }
                        select = '<select id="xcgPdTo" name="xcgPdTo" class="ui fluid search selection dropdown la-filterPropDef">' + select + '</select>';

                        $('label[for=xcgPdTo]').next().replaceWith(select);

                        $('#xcgPdTo').dropdown({
                            duration: 150,
                            transition: 'fade'
                        });
                    }
                });
            })
    </laser:script>

</ui:modal>