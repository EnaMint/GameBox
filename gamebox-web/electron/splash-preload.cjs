const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('gameboxBoot', {
  onProgress: (cb) => ipcRenderer.on('boot:progress', (_e, payload) => cb(payload)),
  onState: (cb) => ipcRenderer.on('boot:state', (_e, payload) => cb(payload)),
  cancel: () => ipcRenderer.send('boot:cancel'),
  retry: () => ipcRenderer.send('boot:retry'),
  openLogs: () => ipcRenderer.send('boot:open-logs'),
  quit: () => ipcRenderer.send('boot:quit'),
});
