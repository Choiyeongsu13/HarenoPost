// =========================================================
//  記念日テーマ（背景イラスト・ポストのかざり）
//  ポストの色は赤で固定。記念日ごとに背景とかざりが変わる。
// =========================================================

var THEMES = {
  shogatsu: {
    label: 'お正月', openLabel: '1月1日 0:00（固定）', date: '2027-01-01', fixed: true,
    note: 'お正月は 1月1日 0:00（日本時間）に固定です。',
    decos: ['KAGAMI', 'SHIMEKAZARI', 'KADOMATSU']
  },
  birthday: {
    label: '誕生日', openLabel: '登録した誕生日', date: '2026-11-20', fixed: false,
    note: '登録した誕生日が初期値です。変更できます。',
    decos: ['HAT', 'RIBBON', 'BALLOON']
  },
  christmas: {
    label: 'クリスマス', openLabel: '12月25日 0:00（固定）', date: '2026-12-25', fixed: true,
    note: 'クリスマスは 12月25日 0:00（日本時間）に固定です。',
    decos: ['SANTA', 'WREATH', 'PRESENT']
  }
};

// ---------- ポストのかざり ----------
// slot: top = 屋根の上 / front = ポストの前面 / side = ポストの横（地面）
var DECOS = {
  // お正月
  KAGAMI: { name: '鏡餅', slot: 'top', svg:
    '<svg viewBox="0 0 120 70" width="110" height="64"><rect x="32" y="56" width="56" height="10" rx="2" fill="#C99A5B"/><rect x="40" y="62" width="40" height="8" fill="#A97C40"/>' +
    '<ellipse cx="60" cy="47" rx="30" ry="12" fill="#FFFFFF" stroke="#E5DDD0" stroke-width="2"/><ellipse cx="60" cy="32" rx="22" ry="10" fill="#FFFFFF" stroke="#E5DDD0" stroke-width="2"/>' +
    '<circle cx="60" cy="18" r="9" fill="#F08A24"/><path d="M60 10 q8 -6 12 0 q-6 4 -12 0z" fill="#5E8F4E"/></svg>' },
  SHIMEKAZARI: { name: 'しめ飾り', slot: 'front', svg:
    '<svg viewBox="0 0 120 70" width="80" height="47"><path d="M10 20 Q60 2 110 20" fill="none" stroke="#C9A35B" stroke-width="8" stroke-linecap="round"/>' +
    '<path d="M44 20 l7 11 -7 11 7 11" fill="none" stroke="#FFFFFF" stroke-width="5"/><path d="M76 20 l-7 11 7 11 -7 11" fill="none" stroke="#FFFFFF" stroke-width="5"/>' +
    '<circle cx="60" cy="28" r="11" fill="#F08A24"/><path d="M56 18 q4 -7 8 0" fill="#5E8F4E"/></svg>' },
  KADOMATSU: { name: '門松', slot: 'side', svg:
    '<svg viewBox="0 0 90 170" width="90" height="170"><polygon points="22,62 36,48 36,130 22,130" fill="#5E9B4A"/><polygon points="38,40 52,24 52,130 38,130" fill="#6FAE58"/><polygon points="54,72 68,58 68,130 54,130" fill="#5E9B4A"/>' +
    '<g fill="#2F5E2A"><circle cx="20" cy="118" r="10"/><circle cx="34" cy="112" r="9"/><circle cx="58" cy="114" r="9"/><circle cx="72" cy="120" r="10"/></g>' +
    '<rect x="14" y="122" width="62" height="46" rx="4" fill="#C9A35B"/><path d="M14 136 H76 M14 152 H76" stroke="#8E6B2E" stroke-width="3"/></svg>' },
  // 誕生日
  HAT: { name: 'とんがり帽子', slot: 'top', svg:
    '<svg viewBox="0 0 120 70" width="110" height="64"><path d="M60 6 L84 66 L36 66 Z" fill="#3E8FB0"/><path d="M50 32 L70 32 M44 48 L76 48" stroke="#F2B544" stroke-width="5"/><circle cx="60" cy="7" r="7" fill="#E0567A"/></svg>' },
  RIBBON: { name: 'リボン', slot: 'front', svg:
    '<svg viewBox="0 0 120 60" width="76" height="38"><path d="M60 32 L26 12 Q16 7 19 22 L22 42 Q25 50 35 45 Z" fill="#E0567A"/><path d="M60 32 L94 12 Q104 7 101 22 L98 42 Q95 50 85 45 Z" fill="#E0567A"/><circle cx="60" cy="33" r="10" fill="#B83A5B"/></svg>' },
  BALLOON: { name: '風船', slot: 'side', svg:
    '<svg viewBox="0 0 90 170" width="90" height="170"><g fill="none" stroke="#9A8C98" stroke-width="1.4"><path d="M30 64 Q40 120 84 160"/><path d="M60 50 Q62 110 84 160"/><path d="M18 100 Q50 140 84 160"/></g>' +
    '<ellipse cx="30" cy="40" rx="18" ry="23" fill="#E0567A"/><ellipse cx="60" cy="26" rx="16" ry="20" fill="#F2B544"/><ellipse cx="18" cy="82" rx="14" ry="18" fill="#3E8FB0"/></svg>' },
  // クリスマス
  SANTA: { name: 'サンタ帽', slot: 'top', svg:
    '<svg viewBox="0 0 120 70" width="110" height="64"><path d="M30 56 Q44 10 92 16 Q98 20 96 28 L86 30 Q66 30 88 56 Z" fill="#D23C3C"/>' +
    '<rect x="24" y="52" width="72" height="14" rx="7" fill="#FFFFFF"/><circle cx="96" cy="26" r="8" fill="#FFFFFF"/></svg>' },
  WREATH: { name: 'リース', slot: 'front', svg:
    '<svg viewBox="0 0 64 60" width="58" height="54"><circle cx="32" cy="27" r="18" fill="none" stroke="#2F7A55" stroke-width="10"/>' +
    '<g fill="#D23C3C"><circle cx="18" cy="18" r="3"/><circle cx="46" cy="20" r="3"/><circle cx="44" cy="38" r="3"/><circle cx="22" cy="38" r="3"/></g>' +
    '<path d="M32 44 L22 56 L28 56 L32 50 L36 56 L42 56 Z" fill="#D23C3C"/><circle cx="32" cy="45" r="4" fill="#B02828"/></svg>' },
  PRESENT: { name: 'プレゼント', slot: 'side', svg:
    '<svg viewBox="0 0 90 170" width="90" height="170"><rect x="12" y="116" width="54" height="52" rx="3" fill="#D23C3C"/><rect x="35" y="116" width="8" height="52" fill="#E0B44A"/><rect x="12" y="132" width="54" height="7" fill="#E0B44A"/>' +
    '<path d="M39 116 Q26 100 30 112 Q22 118 39 116 Q52 100 50 112 Q58 118 39 116" fill="#E0B44A"/>' +
    '<rect x="56" y="140" width="30" height="28" rx="3" fill="#2F7A55"/><rect x="68" y="140" width="6" height="28" fill="#F4F1E6"/></svg>' }
};

// ---------- 背景イラスト（viewBox 400x860、下寄せで切り取り） ----------
function seigaiha(id, color) {
  return '<defs><pattern id="' + id + '" width="48" height="24" patternUnits="userSpaceOnUse"><g fill="none" stroke="' + color + '" stroke-opacity=".45" stroke-width="1.6">' +
    '<circle cx="24" cy="24" r="22"/><circle cx="24" cy="24" r="15"/><circle cx="24" cy="24" r="8"/><circle cx="0" cy="12" r="22"/><circle cx="0" cy="12" r="15"/><circle cx="48" cy="12" r="22"/><circle cx="48" cy="12" r="15"/></g></pattern></defs>';
}
function plum(x, y, s) {
  return '<g transform="translate(' + x + ' ' + y + ') scale(' + s + ')"><g fill="#E8687E"><circle cx="0" cy="-11" r="8"/><circle cx="10" cy="-3" r="8"/><circle cx="6" cy="9" r="8"/><circle cx="-6" cy="9" r="8"/><circle cx="-10" cy="-3" r="8"/></g><circle r="3.5" fill="#F2C14E"/></g>';
}
function burst(cx, cy, r, color) {
  var s = '<g stroke="' + color + '" stroke-width="2.4" stroke-linecap="round">';
  for (var i = 0; i < 12; i++) {
    var a = i * Math.PI / 6;
    s += '<line x1="' + (cx + Math.cos(a) * r * .3).toFixed(1) + '" y1="' + (cy + Math.sin(a) * r * .3).toFixed(1) + '" x2="' + (cx + Math.cos(a) * r).toFixed(1) + '" y2="' + (cy + Math.sin(a) * r).toFixed(1) + '"/>';
  }
  return s + '</g><circle cx="' + cx + '" cy="' + cy + '" r="3" fill="' + color + '"/>';
}
function lantern(x, y, s) {
  return '<g transform="translate(' + x + ' ' + y + ') scale(' + s + ')"><line x1="0" y1="-30" x2="0" y2="0" stroke="#C9A97A" stroke-width="1.5"/><rect x="-13" y="0" width="26" height="5" rx="1" fill="#1E1520"/>' +
    '<ellipse cx="0" cy="25" rx="18" ry="21" fill="#E8A33D"/><path d="M-17 17 H17 M-18 25 H18 M-17 33 H17" stroke="#B5651D" stroke-width="1.5"/><rect x="-13" y="45" width="26" height="5" rx="1" fill="#1E1520"/></g>';
}
function tree(x, y, s) {
  return '<g transform="translate(' + x + ' ' + y + ') scale(' + s + ')"><rect x="-8" y="150" width="16" height="22" fill="#6B4A2B"/>' +
    '<polygon points="0,0 46,70 -46,70" fill="#2F7A55"/><polygon points="0,40 58,115 -58,115" fill="#2A6E4C"/><polygon points="0,85 70,155 -70,155" fill="#246344"/>' +
    '<path d="M0 -14 l5 10 11 1 -8 7 3 11 -11 -6 -11 6 3 -11 -8 -7 11 -1z" fill="#E0B44A"/>' +
    '<g><circle cx="-18" cy="60" r="5" fill="#D23C3C"/><circle cx="20" cy="92" r="5" fill="#E0B44A"/><circle cx="-26" cy="120" r="5" fill="#E0B44A"/><circle cx="30" cy="140" r="5" fill="#D23C3C"/><circle cx="-6" cy="138" r="5" fill="#FFFFFF"/></g></g>';
}

var BG = {
  shogatsu: {
    svg: '<rect width="400" height="860" fill="#FBF6EC"/>' +
      '<path d="M40 860 L168 676 Q200 640 232 676 L360 860 Z" fill="#A9C3DC" fill-opacity=".55"/>' +
      '<path d="M168 676 Q200 640 232 676 L218 694 L206 682 L196 698 L186 686 Z" fill="#FFFFFF"/>' +
      '<circle cx="330" cy="236" r="44" fill="#E9473F" fill-opacity=".9"/>' +
      '<rect x="250" y="270" width="150" height="14" rx="7" fill="#F0DDB8"/><rect x="290" y="292" width="110" height="12" rx="6" fill="#F0DDB8"/><rect x="0" y="250" width="90" height="12" rx="6" fill="#F0DDB8"/>' +
      plum(46, 150, 1) + plum(84, 196, .7) + plum(30, 224, .55) +
      '<rect x="-20" y="772" width="250" height="18" rx="9" fill="#F0DDB8"/><rect x="190" y="798" width="240" height="18" rx="9" fill="#F0DDB8"/>' +
      '<rect x="0" y="826" width="400" height="34" fill="#F3E6CC"/>' },
  birthday: {
    svg: '<rect width="400" height="860" fill="#FFF4F1"/>' +
      '<g opacity=".45"><path d="M0 120 Q200 160 400 120" fill="none" stroke="#9A8C98" stroke-width="1.5"/>' +
      [[40, 127, '#E0567A'], [80, 133, '#F2B544'], [120, 137, '#3E8FB0'], [160, 139, '#7FB685'], [200, 140, '#E0567A'], [240, 139, '#F2B544'], [280, 137, '#3E8FB0'], [320, 133, '#7FB685'], [360, 127, '#E0567A']]
        .map(function (f) { return '<polygon points="' + (f[0] - 13) + ',' + f[1] + ' ' + (f[0] + 13) + ',' + f[1] + ' ' + f[0] + ',' + (f[1] + 26) + '" fill="' + f[2] + '"/>'; }).join('') + '</g>' +
      '<g><path d="M40 360 Q48 400 38 460" fill="none" stroke="#9A8C98" stroke-width="1.2"/><ellipse cx="40" cy="330" rx="24" ry="30" fill="#E0567A" fill-opacity=".75"/>' +
      '<path d="M360 340 Q352 390 362 450" fill="none" stroke="#9A8C98" stroke-width="1.2"/><ellipse cx="360" cy="312" rx="22" ry="28" fill="#3E8FB0" fill-opacity=".7"/></g>' +
      '<g transform="translate(18 700)"><ellipse cx="50" cy="100" rx="52" ry="8" fill="#E8D5CF"/><rect x="8" y="58" width="84" height="40" rx="6" fill="#F7C9D4"/><rect x="8" y="58" width="84" height="10" rx="5" fill="#FFFFFF"/>' +
      '<rect x="22" y="28" width="56" height="32" rx="6" fill="#FBE3E8"/><rect x="22" y="28" width="56" height="8" rx="4" fill="#FFFFFF"/>' +
      '<g fill="#3E8FB0"><rect x="34" y="10" width="4" height="18"/><rect x="48" y="8" width="4" height="20"/><rect x="62" y="10" width="4" height="18"/></g>' +
      '<g fill="#F2B544"><ellipse cx="36" cy="6" rx="3" ry="5"/><ellipse cx="50" cy="4" rx="3" ry="5"/><ellipse cx="64" cy="6" rx="3" ry="5"/></g></g>' +
      '<g><rect x="24" y="210" width="8" height="12" rx="2" fill="#E0567A" transform="rotate(20 28 216)"/><rect x="330" y="230" width="10" height="7" rx="2" fill="#3E8FB0"/><rect x="370" y="560" width="7" height="12" rx="2" fill="#F2B544"/><rect x="16" y="560" width="9" height="9" rx="4.5" fill="#E0567A"/><rect x="350" y="680" width="10" height="7" rx="2" fill="#7FB685" transform="rotate(-25 355 683)"/></g>' },
  christmas: {
    svg: '<rect width="400" height="860" fill="#1E4A3A"/>' +
      '<g fill="#FFFFFF" fill-opacity=".85"><circle cx="30" cy="160" r="2.2"/><circle cx="90" cy="250" r="1.6"/><circle cx="360" cy="150" r="2.4"/><circle cx="310" cy="260" r="1.6"/><circle cx="20" cy="400" r="2"/><circle cx="380" cy="420" r="2.2"/><circle cx="140" cy="60" r="1.6"/><circle cx="260" cy="40" r="2"/><circle cx="60" cy="520" r="1.8"/><circle cx="350" cy="540" r="1.6"/></g>' +
      '<g stroke="#FFFFFF" stroke-opacity=".7" stroke-width="2" stroke-linecap="round"><path d="M60 330 v20 M50 340 h20 M53 333 l14 14 M67 333 l-14 14"/><path d="M340 320 v16 M332 328 h16 M334 322 l12 12 M346 322 l-12 12"/></g>' +
      tree(62, 610, .9) + tree(350, 650, .7) +
      '<path d="M0 800 Q100 770 200 792 T400 784 V860 H0Z" fill="#F4F1E6"/>' +
      '<path d="M0 826 Q120 808 220 822 T400 816 V860 H0Z" fill="#E3EAE6"/>' }
};

// 背景を描く（$box の中に絶対配置の SVG を入れる）
function drawBackground($box, key) {
  var b = BG[key] || BG.shogatsu;
  $box.html('<svg viewBox="0 0 400 860" preserveAspectRatio="xMidYMax slice" style="position:absolute;inset:0;width:100%;height:100%" aria-hidden="true">' + b.svg + '</svg>');
}

// ポストにかざりを付ける（codes = ['KAGAMI', ...]）
function drawDecos($post, codes) {
  $post.find('.deco-top, .deco-front, .deco-side').empty();
  $.each(codes || [], function (_, code) {
    var d = DECOS[code];
    if (d) { $post.find('.deco-' + d.slot).html(d.svg); }
  });
}

// 記念日アイコン（記念日を選ぶボタン用）
var THEME_ICONS = {
  shogatsu: '<svg viewBox="0 0 60 50" width="56" height="46"><rect x="14" y="40" width="32" height="7" rx="2" fill="#C99A5B"/><ellipse cx="30" cy="33" rx="18" ry="8" fill="#FFFFFF" stroke="#D9CDB8" stroke-width="1.5"/><ellipse cx="30" cy="23" rx="13" ry="7" fill="#FFFFFF" stroke="#D9CDB8" stroke-width="1.5"/><circle cx="30" cy="13" r="6.5" fill="#F08A24"/><path d="M30 7 q6 -5 9 0 q-5 3 -9 0z" fill="#5E8F4E"/></svg>',
  birthday: '<svg viewBox="0 0 60 50" width="56" height="46"><rect x="10" y="26" width="40" height="18" rx="4" fill="#F7C9D4"/><rect x="10" y="26" width="40" height="6" rx="3" fill="#FFFFFF"/><rect x="5" y="44" width="50" height="4" rx="2" fill="#E8D5CF"/><g fill="#3E8FB0"><rect x="20" y="13" width="3" height="13"/><rect x="29" y="11" width="3" height="15"/><rect x="38" y="13" width="3" height="13"/></g><g fill="#F2B544"><ellipse cx="21.5" cy="9" rx="2.5" ry="4"/><ellipse cx="30.5" cy="7" rx="2.5" ry="4"/><ellipse cx="39.5" cy="9" rx="2.5" ry="4"/></g></svg>',
  christmas: '<svg viewBox="0 0 60 50" width="56" height="46"><rect x="27" y="40" width="6" height="8" fill="#6B4A2B"/><polygon points="30,6 44,24 16,24" fill="#2F7A55"/><polygon points="30,16 48,40 12,40" fill="#246344"/><path d="M30 0 l2.2 4.5 5 .5 -3.7 3.3 1.1 4.9 -4.6 -2.6 -4.6 2.6 1.1 -4.9 -3.7 -3.3 5 -.5z" fill="#E0B44A"/><circle cx="24" cy="30" r="2.5" fill="#D23C3C"/><circle cx="35" cy="34" r="2.5" fill="#E0B44A"/><circle cx="31" cy="22" r="2" fill="#D23C3C"/></svg>'
};
