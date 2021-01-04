const { app, BrowserWindow, ipcMain } = require('electron')

const fs = require("fs")
const process = require("process")

function createWindow () {
    const win = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            nodeIntegration: true,
            additionalArguments: ["--##--", ...process.argv.slice(2)]
        }
    })

    if (process.env.NODE_ENV === 'development') {
        win.loadFile('../distElectron/index.html')
    } else {
        win.loadFile('../app/index.html')
    }
}

app.whenReady().then(createWindow)

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
})

app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
        createWindow()
    }
})
