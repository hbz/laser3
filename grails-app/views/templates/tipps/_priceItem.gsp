<laser:serviceInjection/>
<div class="item" data-object="${genericOIDService.getOID(priceItem)}">
    <i class="money grey icon la-popup-tooltip la-delay"></i>

    <div class="content">
        <div class="header"><g:message
                code="tipp.price.localPrice"/>:</div>

        <div class="description">
            <ui:xEditable field="localPrice"
                          owner="${priceItem}"/>
            <ui:xEditableRefData
                    field="localCurrency"
                    owner="${priceItem}"
                    config="Currency"/>
            <g:if test="${editable}">
                <span class="right floated">
                    <button class="ui compact icon button tiny removeObject" data-objType="priceItem" data-objId="${priceItem.id}" data-trigger="${genericOIDService.getOID(priceItem)}">
                        <i class="ui icon minus" data-content="Preis entfernen"></i>
                    </button>
                </span>
            </g:if>
        </div>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $(".removeObject").on('click', function(e) {
        e.preventDefault();
        let objType = $(this).attr('data-objType');
        let objId = $(this).attr('data-objId');
        let trigger = $(this).attr('data-trigger');
        $.ajax({
            url: '<g:createLink controller="ajaxJson" action="removeObject"/>',
            data: {
                object: objType,
                objId: objId
            }
        }).done(function(result) {
            if(result.success === true) {
                $('[data-object="'+trigger+'"]').remove();
            }
        });
    });
</laser:script>