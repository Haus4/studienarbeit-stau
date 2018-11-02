const axios = require('axios');
const Camera = require('./camera');

const host = "https://www.svz-bw.de";
const camInfoA = "/kamera/kamera_A.txt";
const camInfoB = "/kamera/kamera_B.txt";

class CameraLoader {

    static parseCameras(info) {
        return info.cameras.map(camera => {
            return new Camera(info.header, camera);
        });
    }

    static parseCameraInfo(response) {
        const data = response.data;
        const lines = data.split(/\r?\n/);
        const table = lines
            .filter(line => line.length > 0)
            .map(line => {
                return line.split('\t');
            });

        return {
            header: table.shift(),
            cameras: table
        };
    }

    static loadCamera(infoUrl) {
        return axios.get(host + infoUrl)
            .then(this.parseCameraInfo)
            .then(this.parseCameras);
    }

    static loadCameras() {
        const camA = this.loadCamera(camInfoA);
        const camB = this.loadCamera(camInfoB);

        return Promise.all([camA, camB])
            .then(cameras => cameras.reduce((a, c) => a.concat(c), []));
    }
}

module.exports = CameraLoader;