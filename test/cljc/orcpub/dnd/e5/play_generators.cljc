(ns orcpub.dnd.e5.play-generators
  (:require [clojure.test.check.generators :as gen]
            [orcpub.dnd.e5.play :as play]))

(def gen-hp (gen/choose 0 200))

(def gen-hp-delta (gen/choose 1 100))

(def gen-spell-level (gen/choose 1 9))

(def gen-condition (gen/elements (vec play/standard-conditions)))

(def gen-die-size (gen/elements [4 6 8 10 12 20]))

(def gen-resource-key (gen/elements [:ki :rage :sorcery-points :superiority-dice :channel-divinity :wild-shape]))

(def gen-weapon-key (gen/elements [:longsword :shortsword :greatsword :rapier :scimitar]))

(def gen-rite-key (gen/elements [:rite-of-the-flame :rite-of-the-frozen :rite-of-the-storm :rite-of-the-dead]))

(def gen-play-state
  (gen/let [hp gen-hp
            temp-hp (gen/choose 0 30)
            max-mod (gen/choose -20 0)
            curses-used (gen/choose 0 5)
            ds-success (gen/choose 0 3)
            ds-fail (gen/choose 0 3)]
    {::play/current-hp hp
     ::play/temp-hp temp-hp
     ::play/max-hp-modifier max-mod
     ::play/spell-slots-used {}
     ::play/pact-slots-used 0
     ::play/resources-used []
     ::play/conditions #{}
     ::play/custom-conditions []
     ::play/death-save-successes ds-success
     ::play/death-save-failures ds-fail
     ::play/equipped-items #{}
     ::play/attuned-items #{}
     ::play/active-rites []
     ::play/blood-curses-used curses-used
     ::play/prepared-spells []
     ::play/hit-dice-used []
     ::play/mode :play}))
