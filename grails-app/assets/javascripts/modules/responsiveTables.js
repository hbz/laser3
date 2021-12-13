
// modules/responsiveTables.js

responsiveTables = {

  go: function() {
    responsiveTables.init('body')
  },

  init: function(ctxSel) {
    console.log('responsiveTables.init( ' + ctxSel + ' )')

    // smaller then 992px
    if (window.matchMedia('(max-width: 991px)').matches) {
      $('.ui.la-responsive-table tbody>tr').each(function () {
        $('td', this).each(function () {
          let th = $('.ui.la-responsive-table th').eq($(this).index());

          // table header is icon
          if( th.html().includes("icon")) {
            let dataContent = th.find('.la-popup-tooltip').attr("data-content");
            $(this).attr('data-label', dataContent);
          }

          else
            $(this).attr('data-label',th.text());
        });
      });
    }

    // Resize the Window
    $(window).resize(function () {

      // smaller then 992px
      if (window.matchMedia('(max-width: 991px)').matches) {
        $('.ui.la-responsive-table tbody>tr').each(function () {
          $('td', this).each(function () {
            let th = $('.ui.la-responsive-table th').eq($(this).index());

            // table header is icon
            if( th.html().includes("icon")) {
              let dataContent = th.find('.la-popup-tooltip').attr("data-content");
              $(this).attr('data-label', dataContent);
            }

            else
              $(this).attr('data-label',th.text());
          });
        });
      }



    });
  }
}