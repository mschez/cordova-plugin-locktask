(function (window, undefined) {
  'use strict';

  var LockTask = {
    startLockTask: function (successCallback, errorCallback, adminClassName) {
      cordova.exec(successCallback, errorCallback, "LockTask", "START_LOCK_TASK", adminClassName ? [adminClassName] : []);
    },

    stopLockTask: function (successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "LockTask", "STOP_LOCK_TASK", []);
    },

    isLockTaskSupported: function (successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "LockTask", "IS_LOCK_TASK_SUPPORTED", []);
    },

    isInLockTaskMode: function (successCallback, errorCallback) {
      cordova.exec(successCallback, errorCallback, "LockTask", "IS_IN_LOCK_TASK_MODE", []);
    },
  };

  cordova.addConstructor(function() {
    window.LockTask = LockTask;

    return window.LockTask;
  });
})(window);
