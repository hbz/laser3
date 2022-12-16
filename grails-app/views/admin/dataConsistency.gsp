<laser:htmlStart message="menu.admin.dataConsistency" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.dataConsistency" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.dataConsistency" type="admin"/>

<h2 class="ui header">${message(code: "admin.duplicateNames")}</h2>

<div class="ui grid">
    <div class="twelve wide column">

        <g:each in="${duplicates}" var="obj">
            <g:if test="${true}">

                <h3 class="ui header" id="jumpMark_2_${obj.key}">${obj.key}</h3>

                <table class="ui sortable celled la-js-responsive-table la-table compact la-ignore-fixed table">
                    <thead>
                    <tr>
                        <th>Attribut</th>
                        <th>${message(code:'default.value.label')}</th>
                        <th>Vorkommen</th>
                        <th class="la-action-info">${message(code:'default.actions.label')}</th>
                    </tr>
                    </thead>
                    <tbody>
                        <g:each in="${obj.value}" var="row">
                            <g:each in="${row.value}" var="entry">
                                <tr>
                                    <td>${row.key}</td>
                                    <td>${entry[0]}</td>
                                    <td>
                                        ${entry[1]}
                                    </td>
                                    <td class="x">
                                        <button class="ui mini button icon" data-key="${obj.key}" data-key2="${row.key}" data-value="${entry[0]}"><i class="ui icon search"></i></button>
                                    </td>
                                </tr>
                            </g:each>
                        </g:each>
                    </tbody>
                </table>

            </g:if>
        </g:each>

    </div>
    <div class="four wide column">
        <div class="ui sticky">
            <aside>
                <p>${message(code: "admin.duplicateNames")}</p>

                <g:each in="${duplicates}" var="obj">
                    <a href="#jumpMark_2_${obj.key}">${obj.key}</a> <br />
                </g:each>
            </aside>
        </div>
    </div>

</div>

<ui:modal id="modalConsistencyCheck" message="menu.admin.dataConsistency" hideSubmitButton="true">
    <form>
        <h4 class="ui header"></h4>
        <br />
        <div class="ui relaxed divided list">
        </div>
    </form>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.x button').on('click', function(){

        var key = $(this).attr('data-key')
        var key2 = $(this).attr('data-key2')
        var value = $(this).attr('data-value')

        $.ajax({
            url: '<g:createLink controller="ajaxJson" action="consistencyCheck"/>',
            method: 'POST',
            data: {
                key: key,
                key2: key2,
                value: value
            },
            success: function(res, code, xhr) {
                var $h4 = $('#modalConsistencyCheck form h4')
                $h4.empty().append( key + '.' + key2 + ' = ' + value )

                var $html = $('#modalConsistencyCheck form div')
                $html.empty()

                var mergeables = []
                var deletables = []

                $.each( res, function( i, elem) {
                    var markup = '<div class="item">' +
                        '<div class="right floated content">' + elem.created + ' | ' + elem.updated + '</div>' +
                        '<a target="_blank" href="' + elem.link + '">( ' + elem.id + ' ) &nbsp; ' + elem.name

                    if (elem.mergeable) {
                        markup += ' <i class="icon recycle positive"></i> '
                        mergeables.push(elem.id)
                    }
                    if (elem.deletable) {
                        markup += ' <i class="icon trash alternate outline negative"></i> '
                        deletables.push(elem.id)
                    }
                    markup += '</a></div>'
                    $html.append( markup )
                })
                $html.append('<br />')

                if (mergeables.length > 0) {
                    var mergeUrl = "<g:createLink controller="admin" action="dataConsistency" />?task=merge&objType=Org";
                    mergeUrl += '&objId=' + mergeables.join('&objId=')
                    $html.append( '<a href="' + mergeUrl + '" class="ui button positive"><i class="icon recycle"></i> Zusammenführen</a>' )
                }
                if (deletables.length > 0) {
                    var deleteUrl = "<g:createLink controller="admin" action="dataConsistency" />?task=delete&objType=Org";
                    deleteUrl += '&objId=' + deletables.join('&objId=')
                    $html.append( '<a href="' + deleteUrl + '" class="ui button negative"><i class="icon trash alternate outline"></i> Löschen</a>' )
                }
                $('#modalConsistencyCheck').modal('show')
            }
        });
    })
</laser:script>

<laser:htmlEnd />
