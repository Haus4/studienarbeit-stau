const fs = require('fs');
const mkdirp = require('mkdirp');
const CameraLoader = require('./camera-loader');
const CameraImageLoader = require('./camera-image-loader');

function createDir(dir) {
    return new Promise(function (resolve, reject) {
        mkdirp(dir, function (err) {
            if (err) reject(err);
            else resolve();
        });
    });
}

function saveImage(image) {
    return createDir(`data/raw/${image.id}`)
        .then(() => {
            fs.writeFile(`data/raw/${image.id}/${image.date.getTime()}.jpg`, image.data, 'binary', function (err) {
                if (err) return Promise.reject(err);
            });
        })
}

function saveOnce(cameras) {
    var operations = cameras.map(c => {
        console.log(`Loading image for camera ${c.id}`);
        return CameraImageLoader.loadByCamera(c)
            .then(saveImage)
            .catch(error => {
                console.error(`Failed to load image for camera  ${c.id}: ${error}`);
            });
    });

    return Promise.all(operations);
}

function work(cameras) {
    console.log(`Saving data for ${cameras.length} cameras`);

    const start = new Date();

    saveOnce(cameras).then(() => {
        const minute = 1000 * 60;

        const end = new Date();
        const diff = end.getTime() - start.getTime();

        var remaining = minute - diff;
        if (remaining < 0) remaining = 1;

        setTimeout(() => {
            work(cameras);
        }, remaining);
    });
}

CameraLoader.loadCameras().then(c => {
    work(c);
});
