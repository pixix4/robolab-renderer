let express = require('express');
let path = require('path');
let fs = require('fs');

let app = express();

let planetPath = path.join(__dirname, '../../robolab-planets/live');
app.get('/planets', function (req, res) {
    fs.readdir(planetPath, function (err, files) {
        res.json(files)
    });
});
app.get('/planet/:name', function (req, res) {
    let name = req.params.name;
    let file = path.join(planetPath, name);
    if (fs.existsSync(file)) {
        res.sendFile(file);
    } else {
        res.sendStatus(404);
    }
});

app.get('/', function (req, res) {
    res.sendFile(path.join(__dirname, 'website/index.html'));
});

app.use('', express.static(path.join(__dirname, 'website')));
app.use('/src', express.static(path.join(__dirname, '../src')));

app.listen(3000, function () {
    console.log('robolab-renderer is available on http://localhost:3000!');
});
