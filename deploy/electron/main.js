const {app, BrowserWindow, ipcMain, dialog, Menu} = require('electron')
const windowStateKeeper = require('electron-window-state');
const fs = require("fs")
const process = require("process")

const isMac = process.platform === 'darwin'
const isDev = process.env.NODE_ENV === 'development'

let mainWindow = null;

let initOpenFileQueue = [];

if (app.isPackaged) {
    process.argv.unshift(null)
}

const template = [
    // { role: 'appMenu' }
    ...(isMac ? [{
        label: "robolab-renderer",
        submenu: [
            {role: 'about'},
            {type: 'separator'},
            {role: 'services'},
            {type: 'separator'},
            {role: 'hide'},
            {role: 'hideothers'},
            {role: 'unhide'},
            {type: 'separator'},
            {role: 'quit'}
        ]
    }] : []),
    // { role: 'fileMenu' }
    {
        label: 'File',
        submenu: [
            {
                label: "Open planet file",
                click: async () => {
                    const result = await dialog.showOpenDialog(mainWindow, {
                        properties: ['openFile', 'multiSelections'],
                        filters: [
                            {name: 'Planets', extensions: ['planet']},
                            {name: 'All Files', extensions: ['*']}
                        ]
                    })

                    result.filePaths.forEach((file) => {
                        let text = fs.readFileSync(file).toString()
                        let mtime = fs.statSync(file).mtime.getTime()
                        mainWindow.webContents.send("open-file", {
                            name: file,
                            mtime: mtime,
                            content: text
                        })
                    })
                }
            },
            {
                label: "Open mqtt log file",
                click: async () => {
                    const result = await dialog.showOpenDialog(mainWindow, {
                        properties: ['openFile'],
                        filters: [
                            {name: 'Log file', extensions: ['log']},
                            {name: 'All Files', extensions: ['*']}
                        ]
                    })
                    result.filePaths.forEach((file) => {
                        let text = fs.readFileSync(file).toString()
                        let mtime = fs.statSync(file).mtime.getTime()
                        mainWindow.webContents.send("open-file", {
                            name: file,
                            mtime: mtime,
                            content: text
                        })
                    })
                }
            },
            isMac ? {role: 'close'} : {role: 'quit'}
        ]
    },
    // { role: 'editMenu' }
    {
        label: 'Edit',
        submenu: [
            {role: 'undo'},
            {role: 'redo'},
            {type: 'separator'},
            {role: 'cut'},
            {role: 'copy'},
            {role: 'paste'},
            ...(isMac ? [
                {role: 'pasteAndMatchStyle'},
                {role: 'delete'},
                {role: 'selectAll'},
                {type: 'separator'},
                {
                    label: 'Speech',
                    submenu: [
                        {role: 'startSpeaking'},
                        {role: 'stopSpeaking'}
                    ]
                }
            ] : [
                {role: 'delete'},
                {type: 'separator'},
                {role: 'selectAll'}
            ])
        ]
    },
    ...(isDev ? [{
        label: 'View',
        submenu: [
            {role: 'reload'},
            {role: 'forceReload'},
            {role: 'toggleDevTools'},
            {type: 'separator'},
            {role: 'resetZoom'},
            {role: 'zoomIn'},
            {role: 'zoomOut'},
            {type: 'separator'},
            {role: 'togglefullscreen'}
        ]
    }] : [])
]

const menu = Menu.buildFromTemplate(template)
Menu.setApplicationMenu(menu)

function createWindow() {
    let mainWindowState = windowStateKeeper({
        defaultWidth: 1200,
        defaultHeight: 800
    });

    mainWindow = new BrowserWindow({
        x: mainWindowState.x,
        y: mainWindowState.y,
        width: mainWindowState.width,
        height: mainWindowState.height,
        webPreferences: {
            nodeIntegration: true,
            enableRemoteModule: true,
            contextIsolation: false,
            additionalArguments: ["--##--", ...process.argv.slice(2), ...initOpenFileQueue]
        }
    })

    mainWindowState.manage(mainWindow);

    initOpenFileQueue = [];

    if (isDev) {
        mainWindow.loadFile('../distElectron/index.html')
    } else {
        mainWindow.loadFile('../app/index.html')
    }

    mainWindow.on("rotate-gesture", (event, rotation) => {
        mainWindow.webContents.send("rotate-gesture", {
            rotation: rotation
        })
    })
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

app.on('will-finish-launching', () => {
    app.on("open-file", (event, file) => {
        if (mainWindow) {
            let text = fs.readFileSync(file).toString()
            let mtime = fs.statSync(file).mtime.getTime()
            mainWindow.webContents.send("open-file", {
                name: file,
                mtime: mtime,
                content: text
            })
        } else {
            initOpenFileQueue.push(file);
        }
        ;
        event.preventDefault();
    });
});

ipcMain.on("select-directory", async (event) => {
    const result = await dialog.showOpenDialog(mainWindow, {
        properties: ['openDirectory']
    })
    event.reply("select-directory", [
        result.filePaths[0]
    ])
});
