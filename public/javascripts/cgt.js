$(document).ready($(function () {

  $('*[data-hidden]').each(function () {

    var $self = $(this);
    var $hidden = $('#hidden')
    var $input = $self.find('input');

    if ($input.val() === 'Yes' && $input.prop('checked')) {
      $hidden.show();
    } else {
      $hidden.hide();
    }

    $input.change(function () {

      var $this = $(this);

      if ($this.val() === 'Yes') {
        $hidden.show();
      } else if ($this.val() === 'No') {
        $hidden.hide();
      }
    });
  });

  var radioOptions = $('input[type="radio"]');

  radioOptions.each(function () {
    var o = $(this).parent().next('.additional-option-block');
    if ($(this).prop('checked')) {
      o.show();
    } else {
      o.hide();
    }
  });

  radioOptions.on('click', function (e) {
    var o = $(this).parent().next('.additional-option-block');
    if (o.index() == 1) {
      $('.additional-option-block').hide();
      o.show();
    }
  });

  $('[data-metrics]').each(function () {
    var metrics = $(this).attr('data-metrics');
    var parts = metrics.split(':');
    ga('send', 'event', parts[0], parts[1], parts[2]);
  });

    var reportLink = $('#get-help-action');
    var reportLocation = window.location.pathname;
    reportLink.on('click', function () {
    ga('send', 'event','resident-shares-get-help', 'Get help' , reportLocation);
    });

}));
