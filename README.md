mixare
======

mixare (mix Augmented Reality Engine) is a free open source augmented reality browser, 
which is published under the GPLv3.

This fork has been created in the wake of a Master Project of @MelanieWe and @pokerazor.
It continues to work on the library and app. Some changes compared to the official repository:
* switched build system to gradle and IDE to Android Studio 1.5, bump API version to current 23
* merged master and develop branch and some other commits, pruned stale/orphaned branches
* removed all Google dependencies
* switched mapsforge to current 0.6 version
* cleaned code a bit, started fixing the lint warnings, removed deprecations, renamed artifacts and
heavily refactored internal code structure to be less monolithic, more spread over files/classes,
especially in the marker rendering of Mix/Augmentation Activity with it's structure of layered Views
* fixed some minor bugs in DataSourceList and with marker position rendering
* made some minor improvements in performance
* switched menu to side drawer
* introduced HUD to display status information (also moving Radar and RangeBar formerly known as ZoomBar into it)
* activated arrow rendering configurable per DataSource
* updated german translations

More work to do:
* introduce 3D rendering
* introduce routing capabilities
* continue to clean and refactor
* rework concurrency structure to move work into background worker threads
* maybe introduce more GUI improvements
* maybe introduce caching and offline mode# Mixare-Sensor
