(function () {
  "use strict";

  function KafkaMonitor() {
    var React = window.React;
    var data = window.HRMS_KAFKA_MONITOR || {};
    var configured = data.configuredTopics || [];
    var dlt = data.dltTopics || [];
    var missing = data.missingTopics || [];
    var missingSet = {};
    missing.forEach(function (topic) { missingSet[topic] = true; });

    var filterState = React.useState("all");
    var filter = filterState[0];
    var setFilter = filterState[1];

    function topicRows() {
      return configured.map(function (topic) {
        return {
          name: topic,
          type: "Topic",
          ready: !missingSet[topic]
        };
      }).concat(dlt.map(function (topic) {
        return {
          name: topic,
          type: "DLT",
          ready: data.status === "UP"
        };
      })).filter(function (row) {
        if (filter === "ready") return row.ready;
        if (filter === "missing") return !row.ready;
        if (filter === "dlt") return row.type === "DLT";
        return true;
      });
    }

    var rows = topicRows();
    var buttons = [
      ["all", "Tất cả"],
      ["ready", "Sẵn sàng"],
      ["missing", "Thiếu"],
      ["dlt", "DLT"]
    ];

    return React.createElement("div", { className: "kafka-react" }, [
      React.createElement("div", { className: "d-flex flex-wrap gap-2 mb-3", key: "filters" },
        buttons.map(function (item) {
          return React.createElement("button", {
            key: item[0],
            type: "button",
            className: "btn btn-sm " + (filter === item[0] ? "btn-primary" : "btn-outline-secondary"),
            onClick: function () { setFilter(item[0]); }
          }, item[1]);
        })
      ),
      React.createElement("div", { className: "topic-list", key: "topics" },
        rows.length ? rows.map(function (row) {
          return React.createElement("span", {
            key: row.type + row.name,
            className: "topic",
            style: {
              borderColor: row.ready ? "#bbf7d0" : "#fecaca",
              color: row.ready ? "#047857" : "#b91c1c",
              background: row.ready ? "#f0fdf4" : "#fff1f2"
            }
          }, (row.ready ? "✓ " : "! ") + row.name + " · " + row.type);
        }) : React.createElement("span", { className: "muted" }, "Không có topic trong bộ lọc này.")
      )
    ]);
  }

  window.HRMSReactIslands = window.HRMSReactIslands || {};
  window.HRMSReactIslands.KafkaMonitor = KafkaMonitor;
})();
