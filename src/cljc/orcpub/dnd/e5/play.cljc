(ns orcpub.dnd.e5.play
  (:require [orcpub.dice :as dice]
            [orcpub.dnd.e5.character :as char5e]))

(def standard-conditions
  #{:blinded :charmed :deafened :frightened :grappled
    :incapacitated :invisible :paralyzed :petrified
    :poisoned :prone :restrained :stunned :unconscious
    :exhaustion-1 :exhaustion-2 :exhaustion-3
    :exhaustion-4 :exhaustion-5 :exhaustion-6})

(defn effective-max-hp [play-state built-char]
  (+ (or (::char5e/max-hit-points built-char) 0)
     (or (::max-hp-modifier play-state) 0)))

(defn clamp-hp [play-state built-char]
  (let [max-hp (effective-max-hp play-state built-char)]
    (update play-state ::current-hp #(max 0 (min (or % max-hp) max-hp)))))

(defn init-play-state [built-char]
  {::current-hp (or (::char5e/max-hit-points built-char) 0)
   ::temp-hp 0
   ::max-hp-modifier 0
   ::spell-slots-used {}
   ::pact-slots-used 0
   ::resources-used []
   ::conditions #{}
   ::custom-conditions []
   ::death-save-successes 0
   ::death-save-failures 0
   ::equipped-items #{}
   ::attuned-items #{}
   ::active-rites []
   ::blood-curses-used 0
   ::prepared-spells []
   ::hit-dice-used []
   ::mode :play})

(defn take-damage [play-state built-char amount]
  (let [temp (or (::temp-hp play-state) 0)
        absorbed (min temp amount)
        remaining (- amount absorbed)
        new-state (-> play-state
                      (assoc ::temp-hp (- temp absorbed))
                      (update ::current-hp #(max 0 (- (or % 0) remaining))))]
    (if (zero? (::current-hp new-state))
      (-> new-state
          (update ::conditions conj :unconscious))
      new-state)))

(defn heal [play-state built-char amount]
  (let [was-zero? (zero? (or (::current-hp play-state) 0))
        max-hp (effective-max-hp play-state built-char)
        new-state (update play-state ::current-hp
                          #(min max-hp (+ (or % 0) amount)))]
    (if was-zero?
      (-> new-state
          (assoc ::death-save-successes 0
                 ::death-save-failures 0)
          (update ::conditions disj :unconscious))
      new-state)))

(defn set-temp-hp [play-state amount]
  (assoc play-state ::temp-hp (max amount (or (::temp-hp play-state) 0))))

(defn use-spell-slot [play-state level max-slots]
  (let [used (get-in play-state [::spell-slots-used level] 0)]
    (if (< used (get max-slots level 0))
      (assoc-in play-state [::spell-slots-used level] (inc used))
      play-state)))

(defn restore-spell-slot [play-state level]
  (let [used (get-in play-state [::spell-slots-used level] 0)]
    (if (pos? used)
      (assoc-in play-state [::spell-slots-used level] (dec used))
      play-state)))

(defn use-resource [play-state resource-key max-val]
  (let [resources (or (::resources-used play-state) [])
        existing (first (filter #(= (:key %) resource-key) resources))
        used (or (:used existing) 0)]
    (if (< used max-val)
      (assoc play-state ::resources-used
             (if existing
               (mapv #(if (= (:key %) resource-key)
                        (update % :used inc) %)
                     resources)
               (conj resources {:key resource-key :used 1})))
      play-state)))

(defn set-condition [play-state condition active?]
  (if active?
    (update play-state ::conditions conj condition)
    (update play-state ::conditions disj condition)))

(defn add-custom-condition [play-state label]
  (update play-state ::custom-conditions conj label))

(defn record-death-save [play-state roll-value]
  (cond
    (= roll-value 20)
    (-> play-state
        (assoc ::death-save-successes 0
               ::death-save-failures 0
               ::current-hp 1)
        (update ::conditions disj :unconscious))

    (= roll-value 1)
    (update play-state ::death-save-failures #(min 3 (+ (or % 0) 2)))

    (>= roll-value 10)
    (update play-state ::death-save-successes #(min 3 (inc (or % 0))))

    :else
    (update play-state ::death-save-failures #(min 3 (inc (or % 0))))))

(defn death-saves-terminal? [play-state]
  (or (>= (or (::death-save-successes play-state) 0) 3)
      (>= (or (::death-save-failures play-state) 0) 3)))

(defn short-rest
  [play-state built-char hit-dice-to-spend resource-defs]
  (let [con-mod (or (::char5e/con-mod built-char) 0)
        state-after-heal
        (reduce (fn [state {:keys [die-size]}]
                  (let [roll (+ (dice/die-roll die-size) con-mod)
                        healing (max 1 roll)]
                    (-> state
                        (heal built-char healing)
                        (update ::hit-dice-used
                                (fn [hd]
                                  (let [existing (first (filter #(= (:die-size %) die-size) (or hd [])))
                                        others (remove #(= (:die-size %) die-size) (or hd []))]
                                    (vec (conj others {:die-size die-size
                                                       :used (inc (or (:used existing) 0))}))))))))
                play-state
                hit-dice-to-spend)
        state-after-resources
        (-> state-after-heal
            (assoc ::pact-slots-used 0)
            (assoc ::blood-curses-used 0)
            (update ::resources-used
                    (fn [resources]
                      (vec (map (fn [r]
                                  (let [def (first (filter #(= (:key %) (:key r)) resource-defs))]
                                    (if (= :short-rest (:reset-on def))
                                      (assoc r :used 0)
                                      r)))
                                (or resources []))))))]
    state-after-resources))

(defn long-rest [play-state built-char resource-defs]
  (let [max-hp (effective-max-hp play-state built-char)
        total-hit-dice (or (::char5e/total-hit-dice built-char) 1)
        recovery (max 1 (quot total-hit-dice 2))]
    (-> play-state
        (assoc ::current-hp max-hp
               ::temp-hp 0
               ::spell-slots-used {}
               ::pact-slots-used 0
               ::blood-curses-used 0
               ::death-save-successes 0
               ::death-save-failures 0)
        (update ::resources-used (fn [rs] (vec (map #(assoc % :used 0) (or rs [])))))
        (update ::hit-dice-used
                (fn [hd]
                  (vec (map (fn [h]
                              (update h :used #(max 0 (- (or % 0) recovery))))
                            (or hd []))))))))

(defn activate-rite [play-state weapon-key rite-key damage-type hp-cost]
  (-> play-state
      (update ::active-rites conj {:weapon-key weapon-key
                                    :rite-key rite-key
                                    :damage-type damage-type
                                    :hp-cost hp-cost})
      (update ::max-hp-modifier - hp-cost)))

(defn deactivate-rite [play-state weapon-key]
  (let [rite (first (filter #(= (:weapon-key %) weapon-key) (::active-rites play-state)))
        hp-cost (or (:hp-cost rite) 0)]
    (-> play-state
        (update ::active-rites (fn [rites] (vec (remove #(= (:weapon-key %) weapon-key) rites))))
        (update ::max-hp-modifier + hp-cost))))

(defn use-blood-curse [play-state max-uses amplify? hp-cost]
  (let [used (or (::blood-curses-used play-state) 0)]
    (if (< used max-uses)
      (cond-> play-state
        true (update ::blood-curses-used inc)
        amplify? (update ::current-hp #(max 0 (- (or % 0) hp-cost))))
      play-state)))

(defn toggle-equipment [play-state item-key equip?]
  (if equip?
    (update play-state ::equipped-items conj item-key)
    (update play-state ::equipped-items disj item-key)))

(defn toggle-attunement [play-state item-key attune?]
  (if attune?
    (if (< (count (::attuned-items play-state)) 3)
      (update play-state ::attuned-items conj item-key)
      play-state)
    (-> play-state
        (update ::attuned-items disj item-key)
        (update ::equipped-items disj item-key))))

(defn set-prepared-spells [play-state class-key spell-keys max-prepared]
  (if (<= (count spell-keys) max-prepared)
    (let [others (remove #(= (:class-key %) class-key) (::prepared-spells play-state))]
      (assoc play-state ::prepared-spells
             (vec (conj others {:class-key class-key :spell-keys (vec spell-keys)}))))
    play-state))
