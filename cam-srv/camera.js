const url = require('url');
const proj4 = require('proj4');
const decode = require('decode-html');

class Camera {
    constructor(header, info) {
        this.base = {};

        header.forEach((element, index) => {
            this.base[element] = info[index];
        });

        this.constructWithBase();
    }

    constructWithBase() {
        this.parseId();
        this.parseTitle();
        this.parseLocation();
    }

    parseId() {
        const link = url.parse(this.base.linkextern, true);
        this.id = link.query.id;
    }

    parseTitle() {
        this.title = decode(this.base.title);
    }

    parseLocation() {
        const lon = parseFloat(this.base.lon);
        const lat = parseFloat(this.base.lat);

        const loc = this.convertLocation([lon, lat]);

        this.lon = loc[0];
        this.lat = loc[1];
    }

    convertLocation(loc) {
        // Coordinates are given in EPSG:25832
        // The commonly known system is EPSG:4326

        // http://spatialreference.org/ref/epsg/etrs89-utm-zone-32n/
        var epsg25832 = '+proj=utm +zone=32 +ellps=GRS80 +units=m +no_defs';

        // http://spatialreference.org/ref/epsg/wgs-84/
        var epsg4326 = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";

        var res = proj4(epsg25832, epsg4326, loc);
        return res.reverse(); // lat and lon is inverted
    }
}

module.exports = Camera;