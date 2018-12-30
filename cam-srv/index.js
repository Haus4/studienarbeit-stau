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

function promisedSleep(milliseconds) {
	return new Promise((resolve, reject) => {
		setTimeout(resolve, milliseconds);
	});
}

function saveCamera(c) {
	console.log(`Loading image for camera ${c.id}`);
	return CameraImageLoader.loadByCamera(c)
            .then(saveImage)
            .catch(error => {
                console.error(`Failed to load image for camera  ${c.id}: ${error}`);
            });
}

function saveOnce(cameras) {
	var promiseContainer = {
		p: promisedSleep(1)
	};
	
	var first = true;
	
	cameras.forEach(c => {
		var camCopy = c;
		
		if(!first) {
			
			promiseContainer.p = promiseContainer.p.then(() => {
				return promisedSleep(1000 * 5);
			});
		}
		
		first = false;
		
		promiseContainer.p = promiseContainer.p.then(() => {
			return saveCamera(camCopy);
		});
    });
	
	return promiseContainer.p;
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

/*
CameraLoader.loadCameras().then(c => {
    work(c);
});
*/

work([
	"S071",
	"S131",
	"KA041",
	"KA061",
	"KA091",
	"KA151",
    //"KA021",
    //"KA022",
    /*"TU011",
    "TU012",
    "S211",
    "S091",
    "K8054",
    "KA031",
    "KA032",
    "KA041",
    "KA042",
    "KA061",
    "KA062",
    "KA151",
    "KA152",
    "K8063",
    "FR042",
    "FR041",
    "FR051",
    "FR011",
    "FR021",
    "FR061",
    "FR031",*/
    
    /*"EXT030",
    "KA091",
    "RLP825",
    "EXT047",
    "KA101",*/
].map(c => { return {id: c}}));