# Car Racing Game using SunSPOT as Game Controller

> This project was migrated from https://code.google.com/p/sunspot-racing on May 17 2015  
> Members: *mamtasingh05* (owner)  
> Keywords: *SunSPOT*, *racing*, *car*, *wireless*, *sensor*  
> Links: [sunspot-racing.tgz](/sunspot-racing.tgz)  
> License: [GNU GPL v3](http://www.gnu.org/licenses/gpl.html)  

This is a multi-player car racing game in which a player uses a SunSPOT to control her car in a 2-dimensional space. There are two NetBeans project: Project4SPOT runs on the SPOT and Project4BaseStation runs on the base-station.

I did this project as part of my wireless sendor network coursework at University of San Francisco.

## How to Play? ##

First, deploy the SPOT application to all the SPOTs that the players will use.
```
$ cd Project4SPOT
$ ant deploy
```
Then launch the base-station application to show the user interface.
```
$ cd Project4BaseStation
$ ant host-run
```
Now start the SPOTs, so that the user interface shows the player view. Each player is identified by the SPOT address, and is shown as a color in the user interface as well as SPOT LED.

You can play it as a single player test-drive or multiplayer competition.

The user interface allows you to configure the map-type and difficulty level. There are several map-types with different types of obstructions: randomly generated blocks, randomly generated maze, randomly generated moving blocks and open field. There are three difficulty levels: easy, moderate and difficult. Once you have selected the map type and difficulty level, you can click on the "New Map" button to generate a new map. If you notice that in your map you cannot reach from start to finish line because all paths have obstructions, you should re-generate a new map.

The race starts when you click on the "Start Game" button and stops when you click on "Stop Game" button. The user input from SPOT is ignored if the race is not started. A player can start her SPOT after the game has started, and he will be immediately placed in an ongoing game.

The user interface shows a global map, along with start line, finish line, obstructions as well as all players, on the left. Individual player view is shown on the right. The individual player view shows the player's car at a fixed location, and other map items as relative to the car. The player view also shows a grid so that it gives the appearance of motion when your car moves in the map.

The player can control her car using her SPOT. Only the accelerometer tilt readings in x and y direction are used. The x-tilt controls the steering wheel (left and right) angle of the car. The y-tilt controls the speed (gas, brake and reverse) of the car. The SPOT periodically sends the tilt readings to the base station application. The tilt angle is used to update the speed and angle of the player's car. Note that a tilt angle of -90 to +90 is useful with 0 as no change and -90 and +90 indicating maximum change in either direction. Tilting more than 90 degrees causes lower than maximum change. For speed, negative angle reduces the speed and positive angle increases the speed. For direction, negative angle goes left and positive angle goes right.

The map view and the player view also show the obstructions in the map. If the player's car collides with the obstruction or map boundary, then the car's damage count is increased. The increase depends on the speed of collision. If your car's damage count reaches 100, you lose. Collision also causes the car's speed to go to zero, and you will have to re-gain speed. The maximum speed that you can drive a damaged car is lower.

In stationary maps your car cannot cross an obstruction. In a moving map, although your car cannot cross the obstruction, it is possible that some moving obstruction moves over your car. Your car cannot be moved if an obstruction is moving over it.

All cars start at the start line. Once you cross the finish line, your finish time is recorded and displayed in your view. Whoever finishes first in a multi-player game is the winner. The game however continues so that others can finish and observe their finish time. In a single player test drive, you can try improving your finish time.

The map type and difficulty level determine your strategy to complete the race. The change in angle causes a longer turn if your speed is high. In more congested map, it is suggested to keep your speed low, so that you can quickly turn your car, and even if it collides, the damage is not much. For starters, I suggest first try on the open field, then start with easy level of random blocks map. The maze map requires more control and helps in learning how to drive your car.

I have learned in this game that the map view is usually more important for the less congested map, and you can easily drive looking at the direction of your car in the map view. But the player view becomes more important in congested map (at higher difficulty level) to closely navigate your car around obstructions.

## Software Design ##

The checked in source code is well commented. Here I describe the content of each file at high level.

Project4SPOT.java contains the main application for SPOT, which creates other threads from DataSender.java and DataReceiver.java. The data sender thread periodically (every 100 ms) sends the tilt readings in x,y,z to base station. The data receiver thread receives command to set the color of the SPOT depending on the address, after the base station assigns a player color.

The Project4BaseStation.java is the main application for base station. It creates sender and receiver connections, and launches the ControlPanel. The ControlPanel is the main user interface controller. It uses the data model in GameData, and uses other view objects in MapView and PlayerView to show the user interface. The data model further contains the MapData to represent the map and obstructions, and PlayerData to represent player related information. The MapData is derived into individual map types such as MapDataBlocks for randomly generated obstructions, MapDataMaze for randomly generated maze, and MapDataMovingBlocks for randomly generated moving blocks. The PlayerData class is the main class that handles actual car control, position, speed, angle, etc. There are several 2D graphics related methods in MapView and PlayerView to display the views.

In terms of difficulty, I spent most time implementing the 2D graphics transformation. Things like drawing the car in correct direction and mapping the obstructions from data to view co-ordinates took time. Although I have created different difficulty level, I haven't yet finished a race in the difficult level yet. So getting used to driving using tilt on SunSPOT needs some practice.
