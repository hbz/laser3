<laser:script>
    JSPC.jsConfirmation = function () {
        if ($("td input[data-action='delete']").is(":checked")) {
            return confirm("${g.message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}")
        }
    }
    // FOR ALL THE OTHER TABLES THEN PROPERTIES
    JSPC.toggleAllCheckboxes = function (source) {
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

<g:if test="${!copyObject}">
    <laser:script>
        // ONLY FOR PROPERIES
        JSPC.takeProperty = $('input[name="copyObject.takeProperty"]');
        JSPC.deleteProperty = $('input[name="copyObject.deleteProperty"]');

        JSPC.selectAllTake = function (source) {
            var table = $(source).closest('table');
            var thisBulkcheck = $(table).find(JSPC.takeProperty);
            $(thisBulkcheck).each(function (index, elem) {
                elem.checked = source.checked;
                JSPC.markAffectedTake($(this));
            })
        }

        JSPC.selectAllDelete = function (source) {
            var table = $(source).closest('table');
            var thisBulkcheck = $(table).find(JSPC.deleteProperty);
            $(thisBulkcheck).each(function (index, elem) {
                elem.checked = source.checked;
                JSPC.markAffectedDelete($(this));
            })
        }

        $(JSPC.takeProperty).change(function () {
            JSPC.markAffectedTake($(this));
        });
        $(JSPC.deleteProperty).change(function () {
            JSPC.markAffectedDelete($(this));
        });

        JSPC.markAffectedTake = function (that) {
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
        JSPC.markAffectedDelete = function (that) {
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


        $(JSPC.takeProperty).each(function (index, elem) {
            if (elem.checked) {
                JSPC.markAffectedTake(elem)
            }
        });
    </laser:script>
</g:if>