(function () {
  "use strict";

  var ReactCdn = "https://unpkg.com/react@18/umd/react.production.min.js";
  var ReactDomCdn = "https://unpkg.com/react-dom@18/umd/react-dom.production.min.js";

  function loadScript(src, id) {
    return new Promise(function (resolve, reject) {
      if (id && document.getElementById(id)) {
        resolve();
        return;
      }
      var script = document.createElement("script");
      if (id) script.id = id;
      script.src = src;
      script.async = true;
      script.onload = resolve;
      script.onerror = function () { reject(new Error("Cannot load " + src)); };
      document.head.appendChild(script);
    });
  }

  function ensureReact() {
    if (window.React && window.ReactDOM) return Promise.resolve();
    return loadScript(ReactCdn, "hrms-react-cdn")
      .then(function () { return loadScript(ReactDomCdn, "hrms-react-dom-cdn"); });
  }

  function mount(root, component, props) {
    if (!root || !component || !window.React || !window.ReactDOM) return;
    if (root.__hrmsReactMounted) return;
    root.__hrmsReactMounted = true;
    window.ReactDOM.createRoot(root).render(window.React.createElement(component, props || {}));
  }

  function parseProps(root) {
    var raw = root.getAttribute("data-hrms-props");
    if (!raw) return {};
    try {
      return JSON.parse(raw);
    } catch (err) {
      console.warn("[HRMS React] Invalid JSON props", err);
      return {};
    }
  }

  function mountRegisteredIslands() {
    var registry = window.HRMSReactIslands || {};
    var roots = document.querySelectorAll("[data-hrms-react]");
    if (!roots.length) return;

    ensureReact().then(function () {
      Array.prototype.forEach.call(roots, function (root) {
        var name = root.getAttribute("data-hrms-react");
        var component = registry[name];
        if (component) {
          mount(root, component, parseProps(root));
        }
      });
    }).catch(function (err) {
      console.warn("[HRMS React] React island loader failed", err);
    });
  }

  window.HRMSReact = {
    ensureReact: ensureReact,
    mount: mount,
    mountRegisteredIslands: mountRegisteredIslands,
    register: function (name, component) {
      window.HRMSReactIslands = window.HRMSReactIslands || {};
      window.HRMSReactIslands[name] = component;
    }
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", mountRegisteredIslands);
  } else {
    mountRegisteredIslands();
  }
})();
