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

// TODO: investigate why this js is currently not working and if required - links to UR Banner implementation
//  $('[data-metrics]').each(function () {
//    var metrics = $(this).attr('data-metrics');
//    var parts = metrics.split(':');
//    ga('send', 'event', parts[0], parts[1], parts[2]);
//  });

    var reportLink = $('#get-help-action');
    var reportLocation = window.location.pathname;
    reportLink.on('click', function () {
    ga('send', 'event','resident-shares-get-help', 'Get help' , reportLocation);
    });

// =====================================================
// Handle the CGT UR panel dismiss link functionality
// =====================================================
    function setCookie (name, value, duration, domain) {
        var secure = window.location.protocol.indexOf('https') ? '' : '; secure'

        var cookieDomain = ""
        if (domain) {
          cookieDomain = '; domain=' + domain
        }

        var expires = ""
        if (duration) {
          var date = new Date()
          date.setTime(date.getTime() + (duration * 24 * 60 * 60 * 1000))
          expires = '; expires=' + date.toGMTString()
        }

        document.cookie = name + '=' + value + expires + cookieDomain + '; path=/' + secure
    }

    var cookieData=GOVUK.getCookie("mdtpurr");
    if (cookieData==null) {
        $("#ur-panel").addClass("banner-panel--show");
    }

    $(".banner-panel__close").on("click", function(e) {
        e.preventDefault();
        setCookie("mdtpurr", "suppress_for_all_services", 28);
         $("#ur-panel").removeClass("banner-panel--show");
    });

}));
