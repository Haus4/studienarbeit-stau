const fs = require('fs');
const mkdirp = require('mkdirp');
const CameraLoader = require('./camera-loader');

function createDir(dir) {
    return new Promise(function (resolve, reject) {
        mkdirp(dir, function (err) {
            if (err) reject(err);
            else resolve();
        });
    });
}

/*CameraLoader.loadCameras().then(c => {
    //return Promise.all(c.map(cam => analyze(cam.id)));
    return analyze("KA062");
});*/


class CamMasks {
    constructor(blanksPos, blanksNeg) {
        this.blanksPos = blanksPos;
        this.blanksNeg = blanksNeg;
    }

    getForDirection(direction) {
        const x = direction[0];
        const y = direction[1];

        const sx = Math.sign(x);
        const sy = Math.sign(y);

        const ax = Math.abs(x);
        const ay = Math.abs(y);

        if (sx == sy) return this.getForSign(sx);
        else if (sx == 0) return this.getForSign(sy);
        else if (sy == 0) return this.getForSign(sx);
        else return this.getForSign((sx > sy) == (ax > ay) ? 1 : -1);
    }

    getForSign(sign) {
        return sign >= 0 ? this.blanksPos : this.blanksNeg;
    }
}

const camCustomData = {
    "KA061": new CamMasks(
        // -> Frankfurt
        [
            [[0, 0], [0, 354], [518, 62], [578, 58], [592, 106], [580, 153], [343, 479], [639, 479], [639, 0]],
        ],
        // -> Basel
        [
            [[0, 0], [1, 203], [469, 49], [467, 21], [514, 19], [522, 40], [521, 58], [505, 82], [483, 102], [0, 357], [0, 479], [639, 479], [639, 0]],
        ],
    ),

    "KA062": new CamMasks(
        // -> Frankfurt
        [
            [[366, 479], [20, 81], [104, 76], [117, 95], [639, 322], [639, 0], [0, 1], [0, 479]],
        ],
        // -> Basel
        [
            [[639, 311], [115, 93], [114, 71], [195, 64], [211, 81], [639, 165], [639, 0], [1, 1], [0, 479], [639, 479]],
        ],
    ),
}

analyze("KA062");

const cv = require('opencv4nodejs');

function detectVehicle(fg_mask, min_contour_width, min_contour_height) {
    return fg_mask.findContoursAsync(cv.RETR_EXTERNAL, cv.CHAIN_APPROX_TC89_L1)
        .then(contours => {
            return contours.map(c => {
                const rect = c.boundingRect();
                return {
                    x: rect.x,
                    y: rect.y,
                    w: rect.width,
                    h: rect.height
                };
            }).filter(c => {
                return c.w >= min_contour_width && c.h >= min_contour_height;
            });
        })
}

function loadImages(id) {
    return new Promise(function (resolve, reject) {
        fs.readdir(`data/raw/${id}`, (err, files) => {
            if (err) reject(err);
            else resolve(files.map(f => `data/raw/${id}/${f}`));
        });
    });
}

function maskImage(id, imgs) {
    const data = camCustomData[id];

    if (data) {
        const pointsList = [];

        const blanks = data.getForSign(1);

        for (var i = 0; i < blanks.length; ++i) {
            const points = [];
            const blank = blanks[i];

            for (var j = 0; j < blank.length; ++j) {
                const point = blank[j];
                points.push(new cv.Point2(point[0], point[1]));
            }

            pointsList.push(points);
        }

        imgs.forEach(img => {
            img.b.drawFillPoly(pointsList, new cv.Vec3(0, 0, 0));
        });
    }

    return imgs;
}

function parseImage(file) {
    return cv.imreadAsync(file)
        .then(img => {
            return img.gaussianBlurAsync(new cv.Size(5, 5), 1.2)
                .then(blur => {
                    return {
                        o: img,
                        b: blur
                    };
                });
        });
}

function parseImages(imgs) {
    const list = imgs
        .filter(x => x.endsWith(".jpg"))
        .map(parseImage);

    return Promise.all(list);
}

function trainAsync(id, imgs, subtractor) {
    const trainImgCount = 10; // Use the last 10 images to train the subtractor

    imgs.sort();

    return new Promise(function (resolve) {
        setTimeout(() => {
            console.log(`Training ${id}`);
            imgs.forEach((x, index) => {
                //if (index < (imgs.length - trainImgCount)) return;

                subtractor.apply(x.b);
            });
            resolve();
        }, 1);
    });
}

function removeShadows(img) {
    for (var y = 0; y < img.cols; ++y) {
        for (var x = 0; x < img.rows; ++x) {
            if (img.at(x, y) == 127) {
                img.set(x, y, 0);
            }
        }
    }
}

function drawJamDectection(img, jam) {
    const text = jam ? "Stau" : "Fluss";
    const color = jam ? cv.Vec3(0, 0, 255) : cv.Vec3(0, 255, 0);

    const fontFace = cv.FONT_HERSHEY_COMPLEX;
    const fontScale = 1;
    const thickness = 3;

    const origin = new cv.Point2(0, 0);
    const dim = cv.getTextSize(text, fontFace, fontScale, thickness);

    const width = dim.size.width;
    const height = dim.size.height;

    img.drawFillPoly([
        [
            new cv.Point2(origin.x, origin.y),
            new cv.Point2(origin.x + width, origin.y),
            new cv.Point2(origin.x + width, origin.y + height + 10),
            new cv.Point2(origin.x, origin.y + height + 10)
        ]
    ], new cv.Vec3(0, 0, 0));

    img.putText(text, new cv.Point2(origin.x, origin.y + height), fontFace, fontScale, color, thickness);
}

function detectJam(img, contours, car_jam_threshold) {
    const jam = contours.length >= car_jam_threshold;
    drawJamDectection(img, jam);
}

function saveAsync(id, imgs, subtractor) {
    console.log(`Writing ${id}`);
    var count = 0;
    return imgs.map(x => {
        const number = count++;
        var obj = subtractor.apply(x.b).copy();

        removeShadows(obj);

        const kernel = cv.getStructuringElement(cv.MORPH_ELLIPSE, new cv.Size(2, 2));
        var closing = obj.morphologyEx(kernel, cv.MORPH_CLOSE);
        var opening = closing.morphologyEx(kernel, cv.MORPH_OPEN);
        obj = opening.erode(kernel, new cv.Point2(-1, -1), 2);

        closing = obj.morphologyEx(kernel, cv.MORPH_CLOSE);
        opening = closing.morphologyEx(kernel, cv.MORPH_OPEN);
        obj = obj.dilate(kernel, new cv.Point2(-1, -1), 2);

        return detectVehicle(obj, 12, 12).then(vehicles => {
            vehicles.forEach(v => {
                x.o.drawRectangle(new cv.Point2(v.x, v.y), new cv.Point2(v.x + v.w, v.y + v.h),
                    new cv.Vec3(0, 255, 0));
            });

            detectJam(x.o, vehicles, 10);

            return Promise.all([
                createDir(`data/blur/${id}`),
                createDir(`data/obj/${id}`),
                createDir(`data/res/${id}`)
            ]).then(() => {
                return Promise.all([
                    cv.imwriteAsync(`data/blur/${id}/${number}.jpg`, x.b),
                    cv.imwriteAsync(`data/obj/${id}/${number}.jpg`, obj),
                    cv.imwriteAsync(`data/res/${id}/${number}.jpg`, x.o)
                ])
            });
        });
    });
}

function doMagic(id, imgs) {
    const subtractor = new cv.BackgroundSubtractorMOG2();

    return trainAsync(id, imgs, subtractor).then(() => {
        return saveAsync(id, imgs, subtractor);
    });
}

function analyze(id) {
    return loadImages(id)
        .then(parseImages)
        .then(maskImage.bind(this, id))
        .then(doMagic.bind(this, id));
}
