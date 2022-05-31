<laser:script>
    $(function(){
      const formObject = $(".la-pagination-custom-input .ui.form");
      const linkObject = $(".la-pagination-custom-link");
      const inputObject = $(".la-pagination-custom-input input");
      let oldHref = linkObject.attr("href");
      let validFlag;

      inputObject.change(function () {
        formObject.form("validate form");
        let newOffset = ($(this).val() - 1) * ${max};
        let newHref = oldHref + "&offset=" + newOffset;
        linkObject.attr("href", newHref);
      });

      $.fn.form.settings.rules.smallerEqualThanTotal = function (inputValue) {
          let inputValueNumber = parseInt(inputValue);
          return inputValueNumber <= Math.round(Math.ceil($('.la-pagination-custom-input').data('total')/${max}));
      } ;
      $.fn.form.settings.rules.biggerThan = function (inputValue, validationValue) {
          let inputValueNumber = parseInt(inputValue);
          return inputValueNumber > validationValue;
      } ;


      formObject.form({
        inline: true,
        fields: {
          paginationCustomInput: {
            identifier: "paginationCustomInput",
            rules: [
              {
                type: "empty",
                prompt: "${message(code: 'pagination.keyboardInput.validation.integer')}"
              },
              {
                type: "integer",
                prompt: "${message(code: 'pagination.keyboardInput.validation.integer')}"
              },
              {
                type: "smallerEqualThanTotal",
                prompt: "${message(code: 'pagination.keyboardInput.validation.smaller')}"
              },
              {
                type   : "biggerThan[0]",
                prompt : "${message(code: 'pagination.keyboardInput.validation.biggerZero')}"
              }
            ]
          }
        },
        inline: true,
        onInvalid: function () {
          return (validFlag = 0);
        },
        onValid: function () {
          return (validFlag = 1);
        }
      });
      linkObject.click(function (e) {
        formObject.form("validate form");
        if (validFlag == 0) {
            e.preventDefault();
        }
      });
    });


</laser:script>