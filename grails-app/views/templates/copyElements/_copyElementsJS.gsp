<g:if test="${! tmplConfig_propertiesCompare}"> %{-- default --}%

<laser:script file="${this.getGroovyPageFileName()}">
    $(".setDeletionConfirm").click( function() {
        //console.log($('[data-action="delete"]:checked'));
        if($('[data-action="delete"]:checked').length === 0){
            $("#copyElementsSubmit").removeClass("js-open-confirm-modal");
            $("#copyElementsSubmit").off("click");
        }
        else if(!$("#copyElementsSubmit").hasClass("js-open-confirm-modal")) {
            $("#copyElementsSubmit").addClass("js-open-confirm-modal");
            r2d2.initDynamicUiStuff('form');
        }
    });
    // FOR ALL THE OTHER TABLES THEN PROPERTIES
    JSPC.app.toggleAllCheckboxes = function (source) {
        var action = $(source).attr("data-action")
        var checkboxes = document.querySelectorAll('input[data-action="' + action + '"]');
        for (var i = 0; i < checkboxes.length; i++) {
            if (source.checked && !checkboxes[i].checked) {
                $(checkboxes[i]).trigger('click')
            } else if (!source.checked && checkboxes[i].checked) {
                $(checkboxes[i]).trigger('click')
            }
        }
    }
</laser:script>

</g:if>
<g:else> %{--<g:if test="${!copyObject}">--}% %{-- only used @ _copyPropertiesCompare.gsp --}%

    <laser:script file="${this.getGroovyPageFileName()}">
        // ONLY FOR PROPERIES
        JSPC.app.takeProperty = $('input[name="copyObject.takeProperty"]');
        JSPC.app.deleteProperty = $('input[name="copyObject.deleteProperty"]');

        JSPC.app.selectAllTake = function (source) {
            var table = $(source).closest('table');
            var thisBulkcheck = $(table).find(JSPC.app.takeProperty);
            $(thisBulkcheck).each(function (index, elem) {
                elem.checked = source.checked;
                JSPC.app.markAffectedTake($(this));
            })
        }

        JSPC.app.selectAllDelete = function (source) {
            var table = $(source).closest('table');
            var thisBulkcheck = $(table).find(JSPC.app.deleteProperty);
            $(thisBulkcheck).each(function (index, elem) {
                elem.checked = source.checked;
                JSPC.app.markAffectedDelete($(this));
            })
        }

        $(JSPC.app.takeProperty).change(function () {
            JSPC.app.markAffectedTake($(this));
        });
        $(JSPC.app.deleteProperty).change(function () {
            JSPC.app.markAffectedDelete($(this));
        });

        JSPC.app.markAffectedTake = function (that) {
            var indexOfTakeCheckbox = ($(that).closest('.la-replace').index());
            var numberOfCheckedTakeCheckbox = $(that).closest('td').find("[type='checkbox']:checked").length;
            var multiPropertyIndex = $(that).closest('tr').find('.la-copyElements-flex-container').index();
            var sourceElem = $(that).closest('tr').find('.la-colorCode-source');
            var targetElem = $(that).closest('tr').find('.la-colorCode-target');
            //  _
            // |x|
            //
            if ($(that).is(":checked")) {
                // Properties with multipleOccurence do
                // - not receive a deletion mark because they are not overwritten but copied
                // - need to have the specific child of la-copyElements-flex-container
                if ($(that).attr('data-multipleOccurrence') == 'true') {
                    sourceElem = $(that).closest('tr').find('.la-colorCode-source:nth-child(' + (indexOfTakeCheckbox + 1) + ')').addClass('willStay');
                    sourceElem.addClass('willStay');
                    targetElem.addClass('willStay'); // mark all the target elemnts green because they will not be deleted
                } else {
                    sourceElem.addClass('willStay');
                    targetElem.addClass('willBeReplaced');
                }
            }
                    //  _
                    // |_|
            //
            else {
                if ($(that).attr('data-multipleOccurrence') == 'true') {
                    if (numberOfCheckedTakeCheckbox == 0) {
                        targetElem.removeClass('willStay');
                    }
                    sourceElem = $(that).closest('tr').find('.la-colorCode-source:nth-child(' + (indexOfTakeCheckbox + 1) + ')').addClass('willStay');
                    sourceElem.removeClass('willStay');
                } else {
                    sourceElem.removeClass('willStay');
                    if ((that).parents('tr').find('input[name="copyObject.deleteProperty"]').is(':checked')) {
                    } else {
                        targetElem.removeClass('willBeReplaced');
                    }
                }
            }
        }
        JSPC.app.markAffectedDelete = function (that) {
            var indexOfDeleteCheckbox = ($(that).closest('.la-noChange').index());
            var targetElem = $(that).closest('tr').find('.la-colorCode-target');
            //  _
            // |x|
            //
            if ($(that).is(":checked")) {
                if ($(that).attr('data-multipleOccurrence') == 'true') {
                    targetElem = $(that).closest('tr').find('.la-colorCode-target:nth-child(' + (indexOfDeleteCheckbox + 1) + ')').addClass('willBeReplaced');
                    targetElem.addClass('willBeReplaced')
                } else {
                    targetElem.addClass('willBeReplaced')
                }
            }
                    //  _
                    // |_|
            //
            else {
                if ($(that).parents('tr').find('input[name="copyObject.takeProperty"]').is(':checked')) {
                    if ($(that).attr('data-multipleOccurrence') == 'true') {
                        targetElem = $(that).closest('tr').find('.la-colorCode-target:nth-child(' + (indexOfDeleteCheckbox + 1) + ')').addClass('willBeReplaced');
                        targetElem.removeClass('willBeReplaced');
                    } else {
                    }
                } else {
                    targetElem.removeClass('willBeReplaced');
                }
            }
        }


        $(JSPC.app.takeProperty).each(function (index, elem) {
            if (elem.checked) {
                JSPC.app.markAffectedTake(elem)
            }
        });
    </laser:script>
%{--</g:if>--}%
</g:else>