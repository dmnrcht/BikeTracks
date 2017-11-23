# Android project : BikeTracks

*October 2017 - January 2018*

Authors
- Antoine Drabble <antoine.drabble@master.hes-so.ch>
- Damien Rochat <damien.rochat@master.hes-so.ch>
- Sébastien Richoz <sebastien.richoz@master.hes-so.ch>

## Objective
The goal of this project is to develop an Android mobile app as part of the master course T-Mobop. The project is developped in Java and is called "BikeTracks"

This application let users discover mountain bike tracks by exploring them on a map and gives the following details on each track : the track itself drawn on the map, the altimetry profile and distance.

In addition, they would also be able to start their own mountain bike activity, tracked by the location of their smartphone letting them know if they are followoing or not the desired track. Typical data is also exposed like distance, time, current elevation, cumulate elevation and so on.

If the biker has any problem, an emergency call button will let him send his location to his predefined secure contacts.

All tracks are locally registered in the smartphone and users are able to consult them any time. They also may be shared with users' contacts (by email or another messaging app).

## Conception

TODO schema architecture

### GPX file format
The tracks are sent to the API in their .gpx format. Here is an example.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="Creator_name">
  <trk>
    <name>Name_of_the_track</name>
    <type>Downhill|Freeride|Enduro|Xcountry</type>
    <trkseg>
      <trkpt lat="46.78888733" lon="6.74366133">
        <ele>449.0</ele>
        <time>2017-09-22T10:11:35.000Z</time>
      </trkpt>
      ...
    </trkseg>
  </trk>
</gpx>
```
Then the API transforms it and stores in a postGIS database.

### API Endpoints
The API contains a simple endpoint to retrieve the tracks in a given region defined by a circle:
- Center [lat,long] : The GPS coordinates corresponding to the center of the currently seen map on the smartphone
- Radius [m] : The biggest distance between the width and height of the currently seen map on the smartphone
