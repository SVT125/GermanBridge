![](http://gchan5.github.io/img/feature2.png)

# GermanBridge

From the infamous game of Hearts, to the team-based game of Spades, to the strategically challenging variation of the 
bridge-type games, German Bridge, Card Suite brings you a compilation of all three! With intelligent AI and random card 
distribution among all players, infinite possibilities exist for each game. Play against mischievous friends or brilliant 
bots, or even a combination of both with an incredible customizable bot and player selection system. 

This is an Android app we developed as a summer side-project and, for some of us, a head-first dive into Android development. 
It's a pass-and-play app with which you can play Hearts, Spades, or German Bridge with up to 4 players; you may optionally
choose to play with bots instead, which we created from academia and our own rough shots at making AI. There are other small
features we added in along the wayside e.g. game saves, help overlays, that make for a nice touch to the app.

## Installation

You can download it free from the Google Play store [here](https://play.google.com/store/apps/details?id=com.gtjgroup.cardsuite&hl=en).
Currently, the app is targeted for API levels 19-23 (4.4 to 6.0/KitKat to Marshmallow).

## To-do

* Much of the code can still be refactored/restructured for readability and less clutter.
  * Replace many of the click listeners with lambda expressions for more succinct code (iff Java 8 supported).
  * Replace antiquated hardcoded values for view margins.
  * Hearts and Spades AI can be improved.
  * Move hardcoded strings to strings.xml or as immutable constants.
  * Add comments here and there to clarify some of the methods.
* Add support for API level 24/Nougat.

## License

[GNU GPLv3](https://github.com/gorhill/uBlock/blob/master/LICENSE.txt).
