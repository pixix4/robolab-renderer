var express = require('express');
var path = require('path');
var fs = require('fs');

var app = express();

var planetPath = path.join(__dirname, '../planet');
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
// app.use('/src/main', express.static(path.join(__dirname, '../src/main')));

app.listen(3000, function () {
    console.log('robolab-renderer is available on http://localhost:3000!');
});
