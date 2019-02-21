package com.momo5502.stauanalyse.camera;

import android.location.Location;

import com.momo5502.stauanalyse.position.Position;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Camera {
    private Position location;

    private String title;
    private String description;
    private String link;

    private String id;

    private int iconOffset[];

    public Camera(Map<String, String> data) {
        this.title = parseField(data, "title");
        this.description = parseField(data, "description");
        this.link = parseField(data, "linkextern");

        parseId(data);
        parseIcon(data);
        parseLocation(data);
    }

    private void parseLocation(Map<String, String> data) {
        String lon = parseField(data, "lon");
        String lat = parseField(data, "lat");

        ProjCoordinate coordinate = new ProjCoordinate();

        coordinate.x = Double.parseDouble(lon);
        coordinate.y = Double.parseDouble(lat);

        coordinate = transformLocation(coordinate);

        this.location = new Position(coordinate.y, coordinate.x);
    }

    private ProjCoordinate transformLocation(ProjCoordinate coordinates) {
        CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
        CRSFactory csFactory = new CRSFactory();

        final String GRS80_PARAM = "+proj=utm +zone=32 +ellps=GRS80 +units=m +no_defs";
        CoordinateReferenceSystem epsg25832 = csFactory.createFromParameters("GRS80", GRS80_PARAM);

        final String WGS84_PARAM = "+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs";
        CoordinateReferenceSystem epsg4326 = csFactory.createFromParameters("WGS84", WGS84_PARAM);

        CoordinateTransform trans = ctFactory.createTransform(epsg25832, epsg4326);

        ProjCoordinate result = new ProjCoordinate();
        trans.transform(coordinates, result);

        return result;
    }

    private void parseId(Map<String, String> data) {
        if (this.link == null) return;

        Pattern pattern = Pattern.compile("[&?]id=(.+)&?");
        Matcher matcher = pattern.matcher(this.link);
        if (matcher.find() && matcher.groupCount() >= 1) {
            this.id = matcher.group(1);
        }
    }

    private void parseIcon(Map<String, String> data) {
        String offset = parseField(data, "iconOffset");
        String offsets[] = offset.split(",");

        if (offsets.length == 2) {
            this.iconOffset = new int[2];
            this.iconOffset[0] = Integer.parseInt(offsets[0]);
            this.iconOffset[1] = Integer.parseInt(offsets[1]);
        }
    }

    private String parseField(Map<String, String> data, String name) {
        if (!data.containsKey(name)) return null;
        return data.get(name);
    }

    public Position getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getId() {
        return id;
    }

    public int[] getIconOffset() {
        return iconOffset;
    }

    @Override
    public String toString() {
        return this.id + " - " + this.title;
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
