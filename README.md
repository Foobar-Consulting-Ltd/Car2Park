# Car2Park Android App

The Car2Park Android app is a navigational aid that locates convenient parking near a user’s destination.

<p align="center">
  <img src="https://github.com/Foobar-Consulting-Ltd/Car2Park/blob/master/readme_pictures/Logo.png" height="600" width="600">
</p>

The app:

- Displays several Car2Go parking spaces close to the user’s selected destination

- Indicates the capacity of the displayed parking spaces

-  Ranks the parking spaces based on their capacity and their distance from the selected destination

-  Visualizes parking spots near the destination as points on a map

-  Initiates navigation to the selected parking spot in the Google Maps application

Car2Park is intended to simplify the act of finding parking spots and is limited to features which further that goal. It does not provide any features that conflict with the Car2Go app with regard to locating and renting vehicles, or otherwise interacting with the Car2Go system.

## [Car2Go API Usage](https://github.com/car2go/openAPI)  

Our server minimizes its use of the Car2Go server by storing parking spot data in memory for a short time (around 30 seconds) after each request. In this way, repeat user actions within a short window do not generate redundant traffic to the Car2Go API. The parking spot data is deleted 30 seconds after it was retrieved. Car2Park will only request parking spots when there are active users and the request frequency will never exceed twice per minute.

 

The Android application obtains parking spot information by way of our authenticated endpoint; it does not use the Car2Park API directly. The endpoint provides a list of a few parking spots based on the user’s provided destination with attached information about each spot’s distance from the final destination. The Car2Park endpoint does not offer the same features or format as the Car2Go API and is not intended as nor can it be used as indirect access to the Car2Go API

## APP Preview
### Login

<p align="center">
  <img src="https://github.com/Foobar-Consulting-Ltd/Car2Park/blob/master/readme_pictures/Login.png" height="600" width="600">
</p>

### Main Map User Interface 

<p align="center">
  <img src="https://github.com/Foobar-Consulting-Ltd/Car2Park/blob/master/readme_pictures/Main%20Map%20Screen.png" height="600" width="600">
</p>

In the main screen of the app, users are able to search for available parking spots by specifying their destination address in the search menu or by simply clicking on any location on the map. The app will then search for any nearby available parking spots and display it on the map.

### Parking Information and GPS 

<p align="center">
  <img src="https://github.com/Foobar-Consulting-Ltd/Car2Park/blob/master/readme_pictures/Parking%20Info%20and%20Map.png" height="600" width="800">
</p>

By clicking on one of the parking spots marker on the map, the app will display the selected
parking spot information that includes the address, distance to destination, a google street view
and its current capacity. Upon clicking the Set Parking Destination button, the app will open the
Google Maps application which will guide the user to the selected parking spot.

### Other Features

<p align="center">
  <img src="https://github.com/Foobar-Consulting-Ltd/Car2Park/blob/master/readme_pictures/Other%20Features.png" height="600" width="800">
</p>

Other features of the app includes: 
- Enable users to change the map type (RoadMap, Satellite, Hybrid, and Terrain) 
- Enable users to filter the number of surounding parking spots to be displayed on the map
- The ability to save users selected destinations 

## High-Level Architecture

<p align="center">
  <img src="https://github.com/Foobar-Consulting-Ltd/Car2Park/blob/master/readme_pictures/High%20Level%20Design.png" height="600" width="800">
</p>

- “Messaging Protocol” contains a mirrored message format for the client and server. Allows for communication via JSON serialization.
- The “Internet” package represents an abstraction of HTTP messaging.
- Google Maps API package is a set of REST API requests over HTTP
- The “Parking Manager” handles requests to the Car2Go API





