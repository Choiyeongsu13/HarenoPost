// ハレの日ポスト 共通スクリプト（発表用の画面なので、サーバー通信の部分はダミーです）
$(function () {
  // トースト表示
  window.toast = function (text) {
    var $t = $('.toast');
    if (!$t.length) { $t = $('<div class="toast" role="status"></div>').appendTo('body'); }
    $t.text(text).stop(true, true).fadeIn(150).delay(1800).fadeOut(250);
  };

  // モーダル開閉
  $(document).on('click', '[data-open]', function () { $('#' + $(this).data('open')).addClass('open'); });
  $(document).on('click', '[data-close]', function () { $(this).closest('.modal').removeClass('open'); });
  $(document).on('click', '.modal', function (e) { if (e.target === this) { $(this).removeClass('open'); } });

  // リンクをコピー
  $(document).on('click', '.js-copy', function () {
    var url = $(this).data('url') || location.href;
    if (navigator.clipboard) { navigator.clipboard.writeText(url).catch(function () {}); }
    toast('リンクをコピーしました');
  });

  // 開封までのカウントダウン（data-open-at="2027-01-01T00:00:00+09:00"）
  var $cd = $('.countdown[data-open-at]');
  if ($cd.length) {
    var target = new Date($cd.data('open-at')).getTime();
    var tick = function () {
      var diff = Math.max(0, target - Date.now());
      var d = Math.floor(diff / 86400000);
      var h = Math.floor(diff / 3600000) % 24;
      var m = Math.floor(diff / 60000) % 60;
      $cd.find('.d').text(d);
      $cd.find('.h').text(('0' + h).slice(-2));
      $cd.find('.m').text(('0' + m).slice(-2));
    };
    tick();
    setInterval(tick, 30000);
  }
});

// 郵便ポストの色は赤で固定（記念日ごとのかざりは js/theme-art.js）
