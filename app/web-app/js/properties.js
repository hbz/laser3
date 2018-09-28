
c3po = {

    initProperties: function (ajaxurl, contextId, tenantId) {
        // fallback for hardcoded id
        if (!contextId) {
            contextId = "#custom_props_div"
        }
        console.log("c3po.initProperties() " + ajaxurl + " : " + contextId + " : " + tenantId)

        c3po.refdataCatSearch(ajaxurl, contextId)
        c3po.searchProp(ajaxurl, contextId, tenantId)
        c3po.showModalOnSelect(contextId)
        c3po.showHideRefData(contextId)
        c3po.hideModalOnSubmit(contextId)
        //Needs to run to make the xEditable visible
        $('.xEditableValue').editable()
        $('.xEditableManyToOne').editable()
    },

    refdataCatSearch: function (ajaxurl, contextId) {
        console.log("c3po.refdataCatSearch() " + ajaxurl + " : " + contextId)

        $("#cust_prop_refdatacatsearch").select2({
            placeholder: "Kategorie angeben ..",
            minimumInputLength: 1,
            ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: ajaxurl,
                dataType: 'json',
                data: function (term, page) {
                    return {
                        q: term, // search term
                        page_limit: 10,
                        baseClass: 'com.k_int.kbplus.RefdataCategory'
                    };
                },
                results: function (data, page) {
                    return {results: data.values};
                }
            }
        });
    },

    searchProp: function (ajaxurl, contextId, tenantId) {
        console.log("c3po.searchProp() " + ajaxurl + " : " + contextId + " : " + tenantId)
        // store
        var desc = $(contextId + " .customPropSelect").attr('desc')

        $(contextId + " .customPropSelect").select2({
            placeholder: "Eigenschaft suchen ..",
            minimumInputLength: 0,
            width: 300,
            ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: ajaxurl,
                dataType: 'json',
                data: function (term, page) {
                    return {
                        q: term, // search term
                        desc: desc,
                        page_limit: 10,
                        baseClass: 'com.k_int.properties.PropertyDefinition',
                        tenant: tenantId
                    };
                },
                results: function (data, page) {
                    return {results: data.values};
                }
            },
            createSearchChoice: function (term, data) {
                return null; // {id: -1, text: "Neue Eigenschaft: " + term};
            }
        });
    },

    // TODO -refactoring
    showModalOnSelect: function (contextId) {
        console.log("c3po.showModalOnSelect() " + contextId)

        $(contextId + " .customPropSelect").on("select2-selecting", function (e) {
            if (e.val == -1) {
                var selectedText = e.object.text;
                selectedText = selectedText.replace("Neue Eigenschaft: ", "")
                $("input[name='cust_prop_name']").val(selectedText);
                $('#cust_prop_add_modal').modal('show');

            }
        });
    },

    // TODO -refactoring
    showHideRefData: function (contextId) {
        console.log("c3po.showHideRefData() " + contextId)

        $('#cust_prop_modal_select').change(function () {
            var selectedText = $("#cust_prop_modal_select option:selected").val();
            if (selectedText == "class com.k_int.kbplus.RefdataValue") {
                $("#cust_prop_ref_data_name").show();
            } else {
                $("#cust_prop_ref_data_name").hide();
            }
        });
    },

    // TODO -refactoring
    hideModalOnSubmit: function (contextId) {
        console.log("c3po.hideModalOnSubmit() " + contextId)

        $("#new_cust_prop_add_btn").click(function () {
            $('#cust_prop_add_modal').modal('hide');
        });
    }
}
