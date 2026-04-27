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
</head>
<body class="${properties.kcBodyClass!} login-pf">
<div class="login-pf-page">

  <#if realm.internationalizationEnabled?? && realm.internationalizationEnabled && locale?? && locale.supported?? && locale.supported?size gt 1>
    <div id="kc-locale">
      <#list locale.supported as l>
        <a href="${l.url}" lang="${l.languageTag}">${l.label}</a>
      </#list>
    </div>
  </#if>

  <div class="seer-brand">
    <div class="seer-mark"><span>H</span></div>
    <div class="seer-wordmark">
      <span class="dot"></span>
      <span class="name">HEIMÐALLR</span>
      <span class="sub">Control</span>
    </div>
    <div class="seer-slogan" id="seer-slogan"></div>
    <div class="seer-platform">AIDA · PLATFORM · OBSERVABILITY</div>
  </div>

  <script>
    (function() {
      var lang = (document.documentElement.lang || 'en').split('-')[0];
      var SLOGANS = {
        en: [
          "Watching every heartbeat of the pipeline.",
          "All-seeing. Always on.",
          "Named for the guardian of Bifröst.",
          "The watchtower of AIÐA.",
          "Events never sleep. Neither does Heimðallr."
        ],
        ru: [
          "Каждое биение пульса конвейера — под наблюдением.",
          "Всевидящий. Всегда на посту.",
          "Назван в честь стража Биврёста.",
          "Наблюдательная башня AIÐA.",
          "События не спят. Heimðallr тоже."
        ]
      };
      var pool = SLOGANS[lang] || SLOGANS.en;
      var pick = pool[Math.floor(Math.random() * pool.length)];
      var el = document.getElementById('seer-slogan');
      if (el) el.textContent = pick;
    })();
  </script>

  <div class="card-pf">
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
    HEIMÐALLR · ${.now?string("yyyy")} · ${(realm.name)!''}
  </div>
</div>
</body>
</html>
</#macro>
