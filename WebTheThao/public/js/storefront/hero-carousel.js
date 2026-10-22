(function () {
    var DEFAULT_INTERVAL = 5000;

    function createState(length) {
        return { index: 0, paused: false, length: length, timer: null };
    }

    function render(root, state) {
        var slides = root.querySelectorAll('[data-hero-slide]');
        var dots = root.querySelectorAll('[data-hero-dot]');
        var counter = root.querySelector('[data-hero-counter]');

        slides.forEach(function (slide, slideIndex) {
            var active = slideIndex === state.index;
            slide.classList.toggle('is-active', active);
            slide.setAttribute('aria-hidden', active ? 'false' : 'true');
        });
        dots.forEach(function (dot, dotIndex) {
            var active = dotIndex === state.index;
            dot.classList.toggle('is-active', active);
            dot.setAttribute('aria-selected', active ? 'true' : 'false');
        });
        if (counter) {
            counter.textContent = pad(state.index + 1) + ' / ' + pad(state.length);
        }
        root.dataset.heroIndex = String(state.index);
    }

    function renderPlayback(root, state) {
        var toggle = root.querySelector('[data-hero-toggle]');
        var icon = root.querySelector('[data-hero-toggle-icon]');
        var label = root.querySelector('[data-hero-toggle-label]');
        if (!toggle) return;
        toggle.setAttribute('aria-pressed', state.paused ? 'true' : 'false');
        toggle.setAttribute('aria-label', state.paused ? 'Phát trình chiếu' : 'Tạm dừng trình chiếu');
        if (icon) icon.textContent = state.paused ? '▶' : 'Ⅱ';
        if (label) label.textContent = state.paused ? 'Phát' : 'Tạm dừng';
        root.dataset.heroPaused = state.paused ? 'true' : 'false';
    }

    function stopTimer(state) {
        if (state.timer !== null) window.clearTimeout(state.timer);
        state.timer = null;
    }

    function schedule(root, state, interval) {
        stopTimer(state);
        if (state.paused || state.length < 2 || document.visibilityState !== 'visible') return;
        state.timer = window.setTimeout(function () {
            goTo(root, state, state.index + 1, interval);
        }, interval);
    }

    function goTo(root, state, nextIndex, interval) {
        state.index = (nextIndex + state.length) % state.length;
        render(root, state);
        schedule(root, state, interval);
    }

    function setPaused(root, state, paused, interval) {
        state.paused = paused;
        if (paused) stopTimer(state);
        else schedule(root, state, interval);
        renderPlayback(root, state);
    }

    function bind(root, state, interval) {
        var prev = root.querySelector('[data-hero-prev]');
        var next = root.querySelector('[data-hero-next]');
        var toggle = root.querySelector('[data-hero-toggle]');

        if (prev) prev.addEventListener('click', function () { goTo(root, state, state.index - 1, interval); });
        if (next) next.addEventListener('click', function () { goTo(root, state, state.index + 1, interval); });
        root.querySelectorAll('[data-hero-dot]').forEach(function (dot) {
            dot.addEventListener('click', function () { goTo(root, state, Number(dot.dataset.heroDot), interval); });
        });
        if (toggle) toggle.addEventListener('click', function () { setPaused(root, state, !state.paused, interval); });
        document.addEventListener('visibilitychange', function () {
            if (document.visibilityState === 'visible') schedule(root, state, interval);
            else stopTimer(state);
        });
    }

    function pad(value) {
        return String(value).padStart(2, '0');
    }

    function init(root) {
        var slides = root.querySelectorAll('[data-hero-slide]');
        if (slides.length === 0) return;
        var interval = Number(root.dataset.heroInterval) || DEFAULT_INTERVAL;
        var state = createState(slides.length);
        render(root, state);
        renderPlayback(root, state);
        bind(root, state, interval);
        schedule(root, state, interval);
    }

    document.querySelectorAll('[data-hero-carousel]').forEach(init);
})();
