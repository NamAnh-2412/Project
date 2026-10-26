(function () {
  var KEY = 'qlbh_offline_orders';

  function readQueue() {
    try {
      return JSON.parse(localStorage.getItem(KEY) || '[]');
    } catch (error) {
      return [];
    }
  }

  function writeQueue(queue) {
    localStorage.setItem(KEY, JSON.stringify(queue));
  }

  function save(body) {
    var queue = readQueue();
    queue.push(body);
    writeQueue(queue);
  }

  async function flush() {
    if (!navigator.onLine) {
      return;
    }
    var queue = readQueue();
    if (!queue.length) {
      return;
    }
    var base = window.APP_BASE_PATH || '/';
    if (!base.endsWith('/')) {
      base += '/';
    }
    var pending = [];
    for (var i = 0; i < queue.length; i++) {
      try {
        var response = await fetch(base + 'api/don-hang/create', {
          method: 'POST',
          credentials: 'same-origin',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            'Accept': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
          },
          body: queue[i]
        });
        var data = await response.json();
        if (!response.ok || !data.success) {
          pending.push(queue[i]);
        }
      } catch (error) {
        pending.push(queue[i]);
      }
    }
    writeQueue(pending);
  }

  window.qlbhOffline = { save: save, flush: flush };
  window.addEventListener('online', flush);
  document.addEventListener('DOMContentLoaded', flush);
})();
