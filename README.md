# dice

## About
This is a simple dice-rolling app, written to get familiar with
ClojureScript and om. It's also pretty good-looking. And, you
know, rolls dice for you. Which is nice.

Check it out online at
[dice.whentheresawill.net](http://dice.whentheresawill.net)

## Development

### Command line

```
$ lein repl

(run)
(browser-repl)
```

Wait a bit, then browse to
[http://localhost:10555](http://localhost:10555).

### Lighttable

Lighttable provides a tighter integration for live coding with an inline browser-tab. Rather than evaluating cljs on the command line with weasel repl, evaluate code and preview pages inside Lighttable.

Steps: After running `(run)`, open a browser tab in Lighttable. Open a cljs file from within a project, go to the end of an s-expression and hit Cmd-ENT. Lighttable will ask you which client to connect. Click 'Connect a client' and select 'Browser'. Browse to [http://localhost:10555](http://localhost:10555)

View LT's console to see a Chrome js console.

Hereafter, you can save a file and see changes or evaluate cljs code (without saving a file). Note that running a weasel server is not required to evaluate code in Lighttable.


## Production
Run ```lein cljsbuild once``` from the terminal.

## License

Copyright © 2015 Will Lee-Wagner

Distributed under the MIT License

## Chestnut

Created with [Chestnut](http://plexus.github.io/chestnut/) 0.7.0.
