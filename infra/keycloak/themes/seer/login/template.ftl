<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false displayWide=false showAnotherWayIfPresent=true>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" lang="${(locale.currentLanguageTag)!'en'}">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="dark">
  <meta name="robots" content="noindex, nofollow">
  <title>${msg("loginTitleHtml", (realm.displayNameHtml!''))!'Sign in'}</title>

  <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" type="image/x-icon" />

  <#if properties.styles?has_content>
    <#list properties.styles?split(' ') as style>
      <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
    </#list>
  </#if>

  <#if properties.scripts?has_content>
    <#list properties.scripts?split(' ') as script>
      <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
    </#list>
  </#if>
</head>
<body class="${properties.kcBodyClass!} login-pf">
<div class="login-pf-page">

  <#-- ── Locale switcher (top-right) ──────────────────────────────────────── -->
  <#if realm.internationalizationEnabled?? && realm.internationalizationEnabled && locale?? && locale.supported?? && locale.supported?size gt 1>
    <div id="kc-locale">
      <#list locale.supported as l>
        <a href="${l.url}" lang="${l.languageTag}">${l.label}</a>
      </#list>
    </div>
  </#if>

  <#-- ── Brand block ─────────────────────────────────────────────────────── -->
  <div class="seer-brand">
    <div class="seer-mark"><span>S</span></div>
    <div class="seer-wordmark">
      <span class="dot"></span>
      <span class="name">SEIÐR STUDIO</span>
      <span class="sub">${msg("loginAccountTitle")!"Sign in"}</span>
    </div>
    <div class="seer-slogan" id="seer-slogan"></div>
    <div class="seer-platform">AIDA · PLATFORM · IDENTITY</div>
  </div>

  <#-- ── Random slogan (EN/RU based on locale) ─────────────────────────── -->
  <script>
    (function() {
      var lang = (document.documentElement.lang || 'en').split('-')[0];
      // Neutral slogans for the platform-wide identity layer.
      // App-specific themes (heimdall, verdandi) override these in their own template.ftl.
      var SLOGANS = {
        en: [
          "Lineage that doesn't lie.",
          "From source to sink — every column accounted for.",
          "Seiðr — the old craft of seeing what flows.",
          "One identity. Every flow.",
          "The fabric beneath the data.",
          "Observe. Trace. Trust."
        ],
        ru: [
          "Lineage, который не лжёт.",
          "От источника до приёмника — каждая колонка под учётом.",
          "Seiðr — древнее ремесло видеть то, что течёт.",
          "Одна личность. Все потоки.",
          "Ткань под данными.",
          "Наблюдай. Проследи. Доверяй."
        ]
      };
      var pool = SLOGANS[lang] || SLOGANS.en;
      var pick = pool[Math.floor(Math.random() * pool.length)];
      var el = document.getElementById('seer-slogan');
      if (el) el.textContent = pick;
    })();
  </script>

  <#-- ── Card body ───────────────────────────────────────────────────────── -->
  <div class="card-pf">

    <#-- Server message banner -->
    <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
      <div class="alert alert-${message.type} pf-c-alert pf-m-${message.type}">
        <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
      </div>
    </#if>

    <#nested "form">

    <#if auth?has_content && auth.showTryAnotherWayLink() && showAnotherWayIfPresent>
      <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
        <div>
          <input type="hidden" name="tryAnotherWay" value="on"/>
          <a href="#" id="try-another-way" onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
        </div>
      </form>
    </#if>

    <#nested "info">
  </div>

  <div class="seer-footer">
    SEIÐR STUDIO · ${.now?string("yyyy")} · ${(realm.name)!''}
  </div>
</div>
</body>
</html>
</#macro>
