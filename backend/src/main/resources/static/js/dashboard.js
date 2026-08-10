const API_BASE = "/api/metrics";
const INTERVALO = 5000;

const statusEl = document.getElementById("status");
const cpuValueEl = document.getElementById("cpu-value");
const ramValueEl = document.getElementById("ram-value");
const diskValueEl = document.getElementById("disk-value");
const hostSelect = document.getElementById("host-select");

function crearConfig(label, color) {
    return {
        type: "line",
        data: {
            labels: [],
            datasets: [{
                label: label,
                data: [],
                borderColor: color,
                backgroundColor: color + "20",
                borderWidth: 2,
                fill: true,
                tension: 0.3,
                pointRadius: 2
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    min: 0,
                    max: 100,
                    ticks: { color: "#888", callback: v => v + "%" },
                    grid: { color: "#2a2d3a" }
                },
                x: {
                    ticks: { color: "#888", maxTicksLimit: 10 },
                    grid: { color: "#2a2d3a" }
                }
            },
            plugins: {
                legend: { display: false }
            },
            animation: { duration: 300 }
        }
    };
}

const cpuChart = new Chart(
    document.getElementById("cpuChart"),
    crearConfig("CPU", "#38bdf8")
);

const ramChart = new Chart(
    document.getElementById("ramChart"),
    crearConfig("RAM", "#a78bfa")
);

const diskChart = new Chart(
    document.getElementById("diskChart"),
    crearConfig("Disco", "#34d399")
);

function formatearHora(timestamp) {
    const fecha = new Date(timestamp);
    const h = String(fecha.getHours()).padStart(2, "0");
    const m = String(fecha.getMinutes()).padStart(2, "0");
    const s = String(fecha.getSeconds()).padStart(2, "0");
    return h + ":" + m + ":" + s;
}

function actualizarGrafico(chart, labels, datos) {
    chart.data.labels = labels;
    chart.data.datasets[0].data = datos;
    chart.update();
}

async function cargarHosts() {
    try {
        const response = await fetch(API_BASE + "/hosts");
        const hosts = await response.json();

        // limpiamos las opciones menos la primera
        while (hostSelect.options.length > 1) {
            hostSelect.remove(1);
        }

        hosts.forEach(host => {
            const option = document.createElement("option");
            option.value = host;
            option.textContent = host;
            hostSelect.appendChild(option);
        });
    } catch (error) {
        // si falla no pasa nada el selector se queda con la opcion por defecto
    }
}

async function fetchMetricas() {
    try {
        let url = API_BASE;
        const selectedHost = hostSelect.value;
        if (selectedHost) {
            url += "?hostname=" + encodeURIComponent(selectedHost);
        }

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("Status " + response.status);
        }

        const metricas = await response.json();

        if (metricas.length === 0) {
            statusEl.textContent = "Esperando datos del agente...";
            statusEl.className = "";
            return;
        }

        const labels = metricas.map(m => formatearHora(m.timestamp));
        const cpuData = metricas.map(m => m.cpuUsage);
        const ramData = metricas.map(m => m.ramUsage);
        const diskData = metricas.map(m => m.diskUsage);

        actualizarGrafico(cpuChart, labels, cpuData);
        actualizarGrafico(ramChart, labels, ramData);
        actualizarGrafico(diskChart, labels, diskData);

        const ultima = metricas[metricas.length - 1];
        cpuValueEl.textContent = ultima.cpuUsage.toFixed(1) + "%";
        ramValueEl.textContent = ultima.ramUsage.toFixed(1) + "%";
        if (ultima.diskUsage !== null && ultima.diskUsage !== undefined) {
            diskValueEl.textContent = ultima.diskUsage.toFixed(1) + "%";
        } else {
            diskValueEl.textContent = "--%";
        }

        statusEl.textContent = "Conectado";
        statusEl.className = "connected";

    } catch (error) {
        statusEl.textContent = "Error de conexion";
        statusEl.className = "error";
    }
}

// cuando cambia el selector recargamos los datos
hostSelect.addEventListener("change", fetchMetricas);

// primera carga
cargarHosts();
fetchMetricas();

// actualizamos cada 5 segundos
setInterval(() => {
    cargarHosts();
    fetchMetricas();
}, INTERVALO);