# EinStein Würfelt nicht Clojure Bot

## Usage

Start the first bot with
```
lein run --name ClojureBot --sleep 100
```
It will wait for incomming game requests.

Now start the second bot with
```
lein run --name challanger -sleep 100 --opponent ClojureBot
```

To play on the official server use the `--host vpf.mind-score.de` option.

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
