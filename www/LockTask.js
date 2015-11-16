var LockTask = {
  _listener: {},
  startLockTask: function (successCallback, errorCallback, opts) {
    cordova.exec(successCallback, errorCallback, "LockTask", "START_LOCK_TASK", []);
  },
  stopLockTask: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "LockTask", "STOP_LOCK_TASK", []);
  },
  isLockTaskSupported: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "LockTask", "IS_LOCK_TASK_SUPPORTED", []);
  },
  on: function (event, callback, scope) {
    if (!this._listener[event]) {
      this._listener[event] = [];
    }
    var item = [callback, scope || window];
    this._listener[event].push(item);
  },
  off: function (event, callback) {
    var listener = this._listener[event];
    if (!listener) return;
    for (var i = 0; i < listener.length; i++) {
      var fn = listener[i][0];
      if (fn == callback) {
        listener.splice(i, 1);
        break;
      }
    }
  },
  fireEvent: function (event) {
    var args = Array.apply(null, arguments).slice(1),
      listener = this._listener[event];

    if (!listener) return;

    for (var i = 0; i < listener.length; i++) {
      var fn = listener[i][0], scope = listener[i][1];
      fn.apply(scope, args);
    }
  }
};

module.exports = LockTask;