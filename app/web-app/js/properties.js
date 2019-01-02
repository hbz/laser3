
c3po = {
    loadJsAfterAjax: function () {
        r2d2.go();
    },

    PROP_SEARCH_NATIVE: 'PROP_SEARCH_NATIVE',
    PROP_SEARCH_GROUPED: 'PROP_SEARCH_GROUPED',

    initProperties: function (ajaxurl, cssId, tenantId) {
        // fallback for hardcoded id
        if (!cssId) {
            cssId = "#custom_props_div"
        }
        console.log("c3po.initProperties() " + ajaxurl + " : " + cssId + " : " + tenantId)

        c3po.refdataCatSearch(ajaxurl, cssId)
        c3po.searchProp(c3po.PROP_SEARCH_NATIVE, ajaxurl, cssId, tenantId)
        c3po.showModalOnSelect(cssId)
        c3po.showHideRefData(cssId)
        c3po.hideModalOnSubmit(cssId)
        //Needs to run to make the xEditable visible
        $('.xEditableValue').editable()
        $('.xEditableManyToOne').editable()
    },

    initGroupedProperties: function (ajaxurl, cssId, tenantId) {
        // fallback for hardcoded id
        if (!cssId) {
            cssId = "#custom_props_div"
        }
        console.log("c3po.initGroupedProperties() " + ajaxurl + " : " + cssId + " : " + tenantId)

        c3po.refdataCatSearch(ajaxurl, cssId)
        c3po.searchProp(c3po.PROP_SEARCH_GROUPED, ajaxurl, cssId, tenantId)
        c3po.showModalOnSelect(cssId)
        c3po.showHideRefData(cssId)
        c3po.hideModalOnSubmit(cssId)
        //Needs to run to make the xEditable visible
        $('.xEditableValue').editable()
        $('.xEditableManyToOne').editable()
    },

    refdataCatSearch: function (ajaxurl, cssId) {
        console.log("c3po.refdataCatSearch() " + ajaxurl + " : " + cssId)

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

    searchProp: function (grouped, ajaxurl, cssId, tenantId) {
        console.log("c3po.searchProp() " + ajaxurl + " : " + cssId + " : " + tenantId)

        var desc = $(cssId + " .customPropSelect").attr('data-desc')
        var oid = $(cssId + " .customPropSelect").attr('data-oid')

        var baseClass = 'com.k_int.properties.PropertyDefinition'

        if (grouped == c3po.PROP_SEARCH_GROUPED) {
            baseClass = 'com.k_int.properties.PropertyDefinitionGroup'
        }

        $(cssId + " .customPropSelect").select2({
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
                        oid: oid,
                        page_limit: 10,
                        baseClass: baseClass,
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
    showModalOnSelect: function (cssId) {
        console.log("c3po.showModalOnSelect() " + cssId)

        $(cssId + " .customPropSelect").on("select2-selecting", function (e) {
            if (e.val == -1) {
                var selectedText = e.object.text;
                selectedText = selectedText.replace("Neue Eigenschaft: ", "")
                $("input[name='cust_prop_name']").val(selectedText);
                $('#cust_prop_add_modal').modal('show');

            }
        });
    },

    // TODO -refactoring
    showHideRefData: function (cssId) {
        console.log("c3po.showHideRefData() " + cssId)

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
    hideModalOnSubmit: function (cssId) {
        console.log("c3po.hideModalOnSubmit() " + cssId)

        $("#new_cust_prop_add_btn").click(function () {
            $('#cust_prop_add_modal').modal('hide');
        });
    }
}
