# Photog

A web interface for your Mac Photos library

## Dependencies

* OpenJDK 1.8 or equivalent
* Gradle
* SassC
* make
* inotifywait required if using `make watch_sass` recipe

## Getting Started

* `git clone` or download this repository
* Import project into IntelliJ
* Run Gradle to install dependencies
* Type `make` to compile Sass
* Run the project to start the server

## Export Apple Photos Database

* Setup the project so `src/main/export.kt` is the main class
* When executing, pass the path to the directory containing you Apple Photos databases (most likely `Database/apdb` inside your Photos library)
* The SQL to import the database into [photog-phoenix](https://github.com/allen-garvey/photog-phoenix) will be printed to stdout (You can setup Intellij to save console output to a file).

## License

Photog is released under the MIT License. See license.txt for more details.