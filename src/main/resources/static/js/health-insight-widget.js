(function () {
    function readXsrfCookie() {
        const name = "XSRF-TOKEN=";
        const parts = document.cookie.split(";");
        for (let i = 0; i < parts.length; i++) {
            const c = parts[i].trim();
            if (c.startsWith(name)) return decodeURIComponent(c.substring(name.length));
        }
        return null;
    }

    function csrfHeaders(form) {
        const headers = {};
        const headerName = form?.dataset?.csrfHeader;
        const token = form?.dataset?.csrfToken;
        if (headerName && token) headers[headerName] = token;
        const xsrf = readXsrfCookie();
        if (xsrf) headers["X-XSRF-TOKEN"] = xsrf;
        return headers;
    }

    function number(form, name) {
        const value = form.elements[name]?.value;
        return value === "" || value == null ? null : Number(value);
    }

    function render(result, node) {
        const level = (result.riskLevel || "LOW").toLowerCase();
        const tips = (result.recommendations || []).map((x) => `<li>${x}</li>`).join("");
        const flags = (result.flags || []).length ? ` Dấu hiệu: ${(result.flags || []).join(", ")}.` : "";
        node.innerHTML = `<div><strong class="${level}">${result.wellnessScore}/100 - ${result.riskLevel}</strong>. ${result.summary}${flags}</div>`
            + `<ul style="margin:8px 0 0 18px;padding:0;">${tips}</ul>`
            + `<div style="color:#94a3b8;margin-top:8px;">${result.disclaimer || ""}</div>`;
    }

    function init() {
        document.querySelectorAll("[data-health-form]").forEach((form) => {
            form.addEventListener("submit", async (event) => {
                event.preventDefault();
                const resultNode = form.parentElement.querySelector("[data-health-result]");
                resultNode.textContent = "Đang phân tích...";
                const payload = {
                    sleepHours: number(form, "sleepHours"),
                    stressLevel: number(form, "stressLevel"),
                    steps: number(form, "steps"),
                    waterLiters: number(form, "waterLiters"),
                    overtimeHours: number(form, "overtimeHours")
                };
                try {
                    const response = await fetch("/api/lifestyle/health-insights", {
                        method: "POST",
                        credentials: "same-origin",
                        headers: Object.assign({ "Content-Type": "application/json" }, csrfHeaders(form)),
                        body: JSON.stringify(payload)
                    });
                    if (!response.ok) throw new Error("health_failed");
                    render(await response.json(), resultNode);
                } catch (_) {
                    resultNode.textContent = "Không phân tích được lúc này. Kiểm tra đăng nhập hoặc thử lại sau.";
                }
            });
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();