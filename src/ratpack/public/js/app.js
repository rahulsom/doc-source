$(function () {
  /*
   * Window configuration
   */
  $('.navsection').css('min-height', $(window).height());

  $(window).on('resize', function () {
    $('.navsection').css('min-height', $(window).height());
  });

});
