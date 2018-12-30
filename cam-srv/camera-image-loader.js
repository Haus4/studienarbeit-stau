const axios = require('axios');

class CameraImageLoader {
    static loadByCamera(camera) {
        return this.loadById(camera.id);
    }

    static loadById(id) {
        return axios.get(this.getUrl(id), {
            responseType: 'arraybuffer',
            headers: {
                'Referer': this.getReferer()
            },
        })
            .then(this.parseResponse)
            .then(img => {
                img.id = id;
                return img;
            });
    }

    static parseResponse(response) {
        const data = response.data;
        const date = response.headers['last-modified'];

        return {
            data: data,
            date: new Date(date)
        }
    }

    static getUrl(id) {
        return `http://www.svz-bw.de/kamera/ftpdata/${id}/${id}_gross.jpg`;
    }

    static getReferer() {
        return 'https://www.svz-bw.de';
    }
}

module.exports = CameraImageLoader;