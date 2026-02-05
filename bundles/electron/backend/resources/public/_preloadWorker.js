const { ipcRenderer } = require('electron')

var electronMessagePort;
var handlerInitialized;
var waitInterval;

ipcRenderer.on('uiPort', e => {
    console.info("[ui communication port] set", e, e.ports[0]);
    electronMessagePort = e.ports[0];
});

window.setUiRequestHandler = (handler) => {
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
                        console.error("Unhandled message from UI", e, error);
                    }
                }
                electronMessagePort.start()
                ipcRenderer.send("portInitalized", "[null \"worker\"]");
                console.info("[ui communication port] initialized")
            }
        }
            , 100);
    }
}

window.sendToUi = (params) => {
    if (electronMessagePort) {
        electronMessagePort.postMessage(params);
    } else {
        console.warn("UI communication port not initialized - Retry");
        setTimeout(
            () => { window.sendToUi(params); },
            100);
    }
}

window.electronAPI = {
    quitApp: () => ipcRenderer.send("quitApp"),
    forceAppCrash: () => ipcRenderer.send("forceAppCrash"),
    saveDialog: (callbackVec, options) => ipcRenderer.send("saveDialog", callbackVec, options),
    saveDialogDone: (callback) => ipcRenderer.on("saveDialogDone", callback),
    selectDirectory: (callbackVec, path) => ipcRenderer.send('selectDirectory', callbackVec, path),
    directorySelected: (callback) => ipcRenderer.on('directorySelected', callback),
    openFile: (callbackVec, path) => ipcRenderer.send("openFile", callbackVec, path),
    openLink: (callbackVec, url) => ipcRenderer.send("openLink", callbackVec, url)
}