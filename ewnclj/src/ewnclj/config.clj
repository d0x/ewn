(ns ewnclj.config)


; todo do is map

; ----- Settings
;(def hostname "vpf.mind-score.de")
(def hostname "localhost")
(def initial-bot-name "cb")

; ----- constants
(def top-player-setup "311 512 113 221 422 631")
(def bot-player-setup "355 554 153 245 444 635")

; ----- Initial state
(def blank "__")
(def initial-board [[blank, blank, blank, blank, blank],
                    [blank, blank, blank, blank, blank],
                    [blank, blank, blank, blank, blank],
                    [blank, blank, blank, blank, blank],
                    [blank, blank, blank, blank, blank]])

(def initial-game-state {:botname       initial-bot-name
                         :opponent-name nil
                         :board         initial-board
                         :own-side      nil
                         :opponent-side nil})
