let exerciseProgressChart;
let personalProgressChart;

const userUnit = window.userStatsConfig?.unit || 'kg';
const personalLabels = window.userStatsConfig?.personalLabels || [];
const personalData = window.userStatsConfig?.personalData || [];

const activeExercises = new Map();

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    setupThemeObserver();
});

/* -------------------- THEME HELPERS -------------------- */

function cssVar(name) {
    return getComputedStyle(document.documentElement)
        .getPropertyValue(name)
        .trim();
}

function isDark() {
    return document.documentElement.classList.contains('dark');
}

/* Convert hex → rgba with alpha */
function withAlpha(hex, alpha) {
    const h = hex.replace('#', '');
    const bigint = parseInt(h, 16);
    const r = (bigint >> 16) & 255;
    const g = (bigint >> 8) & 255;
    const b = bigint & 255;
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/* Slightly shift brightness for multiple datasets */
function shiftGray(hex, step) {
    const h = hex.replace('#', '');
    let r = parseInt(h.substr(0, 2), 16);
    let g = parseInt(h.substr(2, 2), 16);
    let b = parseInt(h.substr(4, 2), 16);

    r = Math.min(255, Math.max(0, r + step));
    g = Math.min(255, Math.max(0, g + step));
    b = Math.min(255, Math.max(0, b + step));

    return `rgb(${r}, ${g}, ${b})`;
}

/* -------------------- CHART COLORS -------------------- */

function getBaseLineColor(index = 0) {
    const base = cssVar('--text-main');
    const step = isDark() ? -20 * index : 20 * index;
    return shiftGray(base, step);
}

function getFillColor(lineColor) {
    return withAlpha(rgbToHex(lineColor), isDark() ? 0.18 : 0.12);
}

/* Convert rgb() to hex */
function rgbToHex(rgb) {
    const nums = rgb.match(/\d+/g).map(Number);
    return (
        '#' +
        nums
            .map(v => v.toString(16).padStart(2, '0'))
            .join('')
    );
}

/* -------------------- SCALES -------------------- */

function getScales(data, label) {
    const text = cssVar('--text-main');
    const grid = cssVar('--border-soft');

    let min = null, max = null;
    if (data.length) {
        min = Math.floor((Math.min(...data) * 0.85) / 10) * 10;
        max = Math.ceil((Math.max(...data) * 1.15) / 10) * 10;
    }

    return {
        x: {
            grid: { color: grid, lineWidth: 0.6 },
            ticks: { color: text },
            title: { display: true, text: 'Date', color: text }
        },
        y: {
            min,
            max,
            grid: { color: grid, lineWidth: 0.6 },
            ticks: { color: text },
            title: {
                display: true,
                text: label,
                color: text,
                font: { weight: 'bold' }
            }
        }
    };
}

/* -------------------- OPTIONS -------------------- */

function chartOptions(scales) {
    const text = cssVar('--text-main');
    const bg = cssVar('--bg-card');
    const border = cssVar('--border-soft');

    return {
        responsive: true,
        maintainAspectRatio: false,
        scales,
        plugins: {
            legend: {
                labels: {
                    color: text,
                    boxWidth: 10,
                    boxHeight: 10
                }
            },
            tooltip: {
                backgroundColor: bg,
                titleColor: text,
                bodyColor: text,
                borderColor: border,
                borderWidth: 1
            }
        }
    };
}

/* -------------------- INIT -------------------- */

function initCharts() {
    const primary = cssVar('--text-main');
    const primaryFill = withAlpha(primary, isDark() ? 0.22 : 0.15);

    // Personal
    personalProgressChart = new Chart(
        document.getElementById('personalProgressChart').getContext('2d'),
        {
            type: 'line',
            data: {
                labels: personalLabels,
                datasets: [{
                    label: `Body Weight (${userUnit})`,
                    data: personalData,
                    borderColor: primary,
                    backgroundColor: primaryFill,
                    fill: true,
                    tension: 0.35,
                    pointRadius: 3
                }]
            },
            options: chartOptions(
                getScales(personalData, `Weight (${userUnit})`)
            )
        }
    );

    // Exercise
    exerciseProgressChart = new Chart(
        document.getElementById('exerciseProgressChart').getContext('2d'),
        {
            type: 'line',
            data: { labels: [], datasets: [] },
            options: chartOptions(
                getScales([], `Weight (${userUnit})`)
            )
        }
    );
}

/* -------------------- TOGGLE EXERCISE -------------------- */

function toggleExercise(card) {
    const parent = card.closest('.exercise-item');
    const id = parent.dataset.id;
    const name = parent.dataset.name;

    if (activeExercises.has(id)) {
        activeExercises.delete(id);
        card.classList.remove('selected-active');
        updateExerciseChart();
        return;
    }

    fetch(`/stats/exercise/${id}`)
        .then(r => r.json())
        .then(data => {
            if (!data.labels?.length) return;

            activeExercises.set(id, {
                label: name,
                labels: data.labels,
                values: data.values
            });

            card.classList.add('selected-active');
            updateExerciseChart();
        });
}

/* -------------------- UPDATE -------------------- */

function updateExerciseChart() {
    const values = [];
    let index = 0;

    const datasets = [];

    activeExercises.forEach(ex => {
        ex.values.forEach(v => values.push(v));

        const line = getBaseLineColor(index);
        const fill = withAlpha(rgbToHex(line), isDark() ? 0.16 : 0.12);

        datasets.push({
            label: ex.label,
            data: ex.labels.map((d, i) => ({ x: d, y: ex.values[i] })),
            borderColor: line,
            backgroundColor: fill,
            fill: true,
            tension: 0.35,
            pointRadius: 3
        });

        index++;
    });

    exerciseProgressChart.data.datasets = datasets;
    exerciseProgressChart.options.scales =
        getScales(values, `Weight (${userUnit})`);

    exerciseProgressChart.update();

    const counter = document.getElementById('selectedCounter');
    if (counter) counter.innerText = `${activeExercises.size} selected`;
}

/* -------------------- THEME SWITCH -------------------- */

function setupThemeObserver() {
    new MutationObserver(() => {
        exerciseProgressChart?.destroy();
        personalProgressChart?.destroy();
        initCharts();
        updateExerciseChart();
    }).observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['class']
    });
}