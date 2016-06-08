# MarkovBot98
Open-source and non-Facebook-connected version of [TextpostBot 98](https://www.facebook.com/TextpostBot-98-512340822260257/), an automated and crowd-fed Markov chain generating bot.

To be honest, this code is somewhat messy and wasn't written entirely for interactive usage, but I figured some fans of the page would be interested in having it, so here it is and thanks for the love!

## Compiling
You'll need the Java SDK installed.  Just run the included build script to build the program from its sources.

## Usage
Use the included run script to start the program.

### Commands
* generate, g - Generate a random result from the current database
  * Alternatively, use "generate from [file]" to use a file as input for generation.
* db - Access various database commands.  This is something I started but never quite finished and was intended to be a way to manage the ever-growing database that TextpostBot is using.  Unfortunately at this point that database is far beyond any possible management I could add here lol
* dbl [file] - Shortcut for loading a database from file
  * If another database is currently open, you can either close it and open the new one, or merge them.
* dbs [file] - Shortcut for saving the current database to a file
* read [file], r [file] - Read and learn from a file
* teach, t - Teach the bot from stdin
* quit, q - Exits the program
