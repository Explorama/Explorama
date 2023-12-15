const { contextBridge, ipcRenderer } = require('electron');

var electronMessagePort;
var handlerInitialized;
var waitInterval;

ipcRenderer.on('workerPort', e => {
  console.info("[worker communication port] set")
  electronMessagePort = e.ports[0];
});

contextBridge.exposeInMainWorld('electronAPI',
  {
    workerRequestHandler: (handler) => {
      if (!handlerInitialized) {
        handlerInitialized = true;
        waitInterval = setInterval(() => {
          if (electronMessagePort) {
            clearInterval(waitInterval);
            electronMessagePort.onmessage = (e) => {
              try {
                handler(e.data);
              }
              catch (error) {
                console.error("Unhandled message from Worker", e, error);
              }
            }
            ipcRenderer.send("portInitalized", "[null,\"ui\"]");
            electronMessagePort.start()
            console.info("[worker communication port] initialized")
          }
        }
          , 100);
      }
    },

    // Send Messages to worker directly
    sendToWorker: (params) => {
      if (electronMessagePort) {
        electronMessagePort.postMessage(params);
      } else {
        console.warn("Worker communication port not initialized - Retry");
        setTimeout(
          () => { window.electronAPI.sendToWorker(params); },
          100);
      }
    },

    startWorkerHandler: () => electronMessagePort.start(),

    workerConnectionEstablished: (callback) => ipcRenderer.on('workerConnectionEstablished', callback),

    selectDirectory: (callbackVec, path) => ipcRenderer.send('selectDirectory', callbackVec, path),
    directorySelected: (callback) => ipcRenderer.on('directorySelected', callback),
    forceAppCrash: () => ipcRenderer.send("forceAppCrash"),
    quitApp: () => ipcRenderer.send("quitApp"),

    openFile: (callbackVec, path) => ipcRenderer.send("openFile", callbackVec, path),
    openLink: (callbackVec, url) => ipcRenderer.send("openLink", callbackVec, url)
  })