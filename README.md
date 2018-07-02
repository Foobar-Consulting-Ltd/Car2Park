# Car2Park Android App

The Car2Park Android app is a navigational aid that locates convenient parking near a user’s destination.

The app:

- Displays several Car2Go parking spaces close to the user’s selected destination

- Indicates the capacity of the displayed parking spaces

-  Ranks the parking spaces based on their capacity and their distance from the selected destination

-  Visualizes parking spots near the destination as points on a map

-  Initiates navigation to the selected parking spot in the Google Maps application

Car2Park is intended to simplify the act of finding parking spots and is limited to features which further that goal. It does not provide any features that conflict with the Car2Go app with regard to locating and renting vehicles, or otherwise interacting with the Car2Go system.

## [Car2Go API](https://github.com/car2go/openAPI) Usage 

Our server minimizes its use of the Car2Go server by storing parking spot data in memory for a short time (around 30 seconds) after each request. In this way, repeat user actions within a short window do not generate redundant traffic to the Car2Go API. The parking spot data is deleted 30 seconds after it was retrieved. Car2Park will only request parking spots when there are active users and the request frequency will never exceed twice per minute.

 

The Android application obtains parking spot information by way of our authenticated endpoint; it does not use the Car2Park API directly. The endpoint provides a list of a few parking spots based on the user’s provided destination with attached information about each spot’s distance from the final destination. The Car2Park endpoint does not offer the same features or format as the Car2Go API and is not intended as nor can it be used as indirect access to the Car2Go API
