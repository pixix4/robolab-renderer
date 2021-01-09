let express = require('express');
let path = require('path');
let fs = require('fs');

let app = express();

app.get('/', function (req, res) {
    res.sendFile(path.join(__dirname, '../distWeb/index.html'));
});

app.use('', express.static(path.join(__dirname, '../distWeb')));
app.use('/src', express.static(path.join(__dirname, '../../src')));

app.listen(3000, function () {
    console.log('robolab-renderer is available on http://localhost:3000!');
});