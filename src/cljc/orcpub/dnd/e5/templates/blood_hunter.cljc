(ns orcpub.dnd.e5.templates.blood-hunter
  (:require [orcpub.dnd.e5.options :as opt5e]
            [orcpub.dnd.e5.modifiers :as mod5e]
            [orcpub.dnd.e5.character :as char5e]
            [orcpub.dnd.e5.units :as units5e]
            [orcpub.template :as t]
            [orcpub.common :as common]
            [orcpub.modifiers :as mod]))

(def crimson-rite-options
  [{:name "Rite of the Flame" :key :rite-of-the-flame :damage-type "fire"}
   {:name "Rite of the Frozen" :key :rite-of-the-frozen :damage-type "cold"}
   {:name "Rite of the Storm" :key :rite-of-the-storm :damage-type "lightning"}])

(def esoteric-rite-options
  [{:name "Rite of the Dead" :key :rite-of-the-dead :damage-type "necrotic"}
   {:name "Rite of the Oracle" :key :rite-of-the-oracle :damage-type "psychic"}
   {:name "Rite of the Roar" :key :rite-of-the-roar :damage-type "thunder"}])

(defn rite-option [{:keys [name key damage-type]}]
  (t/option-cfg
   {:name name
    :modifiers [(mod5e/trait-cfg {:name name :summary (str "Crimson Rite deals " damage-type " damage")})]}))

(def blood-curse-options
  [{:name "Blood Curse of the Anxious" :key :curse-anxious
    :summary "Reaction: disadvantage on target's attack roll"}
   {:name "Blood Curse of Binding" :key :curse-binding
    :summary "Bonus action: restrain Large or smaller creature (Str save)"}
   {:name "Blood Curse of Bloated Agony" :key :curse-bloated-agony
    :summary "Bonus action: disadvantage on Str/Dex checks, damage on movement"}
   {:name "Blood Curse of Exposure" :key :curse-exposure
    :summary "Reaction: remove one damage resistance from target"}
   {:name "Blood Curse of the Eyeless" :key :curse-eyeless
    :summary "Reaction: subtract hemocraft die from attack roll against you"}
   {:name "Blood Curse of the Fallen Puppet" :key :curse-fallen-puppet
    :summary "Reaction: creature at 0 HP makes one attack"}
   {:name "Blood Curse of the Marked" :key :curse-marked
    :summary "Bonus action: extra rite damage die on hits this turn"}
   {:name "Blood Curse of the Muddled" :key :curse-muddled
    :summary "Reaction: subtract hemocraft die from Int/Wis/Cha save"}])

(defn curse-option [{:keys [name key summary]}]
  (t/option-cfg
   {:name name
    :modifiers [(mod5e/trait-cfg {:name name :summary summary})]}))

(defn crimson-rite-selection [class-kw]
  (t/selection-cfg
   {:name "Crimson Rite"
    :key :crimson-rite
    :min 1 :max 1
    :ref [:class class-kw :crimson-rite]
    :multiselect? true
    :options (map rite-option crimson-rite-options)}))

(defn esoteric-rite-selection [class-kw]
  (t/selection-cfg
   {:name "Esoteric Rite"
    :key :esoteric-rite
    :min 1 :max 1
    :ref [:class class-kw :esoteric-rite]
    :multiselect? true
    :options (map rite-option esoteric-rite-options)}))

(defn blood-curse-selection [class-kw]
  (t/selection-cfg
   {:name "Blood Curse"
    :key :blood-curse
    :min 1 :max 1
    :ref [:class class-kw :blood-curse]
    :multiselect? true
    :options (map curse-option blood-curse-options)}))

(def ghostslayer-subclass
  {:name "Order of the Ghostslayer"
   :key :ghostslayer
   :levels {3 {:modifiers [(mod5e/trait-cfg {:name "Rite of the Dawn" :summary "Crimson Rite deals radiant damage, sheds bright light 20ft. Gain necrotic resistance while active."})
                           (mod5e/damage-resistance :necrotic)]}
            7 {:modifiers [(mod5e/trait-cfg {:name "Curse Specialist" :summary "+1 Blood Maledict use, +1 curse save DC"})]}
            11 {:modifiers [(mod5e/trait-cfg {:name "Aether Walk" :summary "Move through creatures/objects. 1/long rest."})]}
            15 {:modifiers [(mod5e/trait-cfg {:name "Brand of Sundering" :summary "Brand deals extra hemocraft die radiant, prevents phasing"})]}
            18 {:modifiers [(mod5e/trait-cfg {:name "Rite Revival" :summary "At 0 HP with active rite: end rite, drop to 1 HP. 1/long rest."})]}}})

(def lycan-subclass
  {:name "Order of the Lycan"
   :key :lycan
   :levels {3 {:modifiers [(mod5e/trait-cfg {:name "Hybrid Transformation" :summary "Bonus action: +1 AC, advantage Str checks/saves, 1d6+Str claws, bonus action claw attack, regen 1+Con below half HP. Prof bonus uses/long rest."})
                           (mod5e/trait-cfg {:name "Lycanthropy" :summary "Vulnerable to silver. Immune to polymorph."})
                           (mod5e/natural-ac-bonus 1)
                           (mod5e/attack
                            {:name "Predatory Claws (Hybrid)"
                             :damage-die 6
                             :damage-die-count 1
                             :damage-type "slashing"
                             :ability ::char5e/str
                             :notes "Hybrid form only. Bonus action: extra claw attack."})]}
            7 {:modifiers [(mod5e/trait-cfg {:name "Stalker's Prowess" :summary "+10ft speed in hybrid. Advantage Perception (hearing/smell). Darkvision 60ft."})
                           (mod5e/darkvision 60)
                           (mod5e/speed 10)]}
            11 {:modifiers [(mod5e/trait-cfg {:name "Advanced Transformation" :summary "Magical claws, +1d6 claw damage, resistance to nonmagical non-silver B/P/S in hybrid."})
                            (mod5e/damage-resistance :bludgeoning)
                            (mod5e/damage-resistance :piercing)
                            (mod5e/damage-resistance :slashing)]}
            15 {:modifiers [(mod5e/trait-cfg {:name "Brand of the Voracious" :summary "In hybrid form, Brand psychic damage heals you."})]}
            18 {:modifiers [(mod5e/trait-cfg {:name "Hybrid Transformation Mastery" :summary "Unlimited uses, +2 claw damage, regen above half HP."})]}}})
(def mutagen-options
  [{:name "Mutagen: Aether" :key :mutagen-aether :summary "Fly 20ft. Drawback: disadvantage Str/Dex saves."}
   {:name "Mutagen: Precision" :key :mutagen-precision :summary "+2 attack rolls. Drawback: disadvantage Str saves."}
   {:name "Mutagen: Rapidity" :key :mutagen-rapidity :summary "+10ft speed, no opportunity attacks. Drawback: disadvantage Int saves."}
   {:name "Mutagen: Mobility" :key :mutagen-mobility :summary "Immune grappled/restrained. Drawback: disadvantage Str/Dex saves."}
   {:name "Mutagen: Cruelty" :key :mutagen-cruelty :summary "Extra weapon attack. Drawback: disadvantage Int/Wis/Cha saves." :level 11}
   {:name "Mutagen: Reconstruction" :key :mutagen-reconstruction :summary "Regen prof bonus HP/turn below half. Drawback: -10ft speed." :level 11}])

(defn mutagen-option [{:keys [name key summary]}]
  (t/option-cfg {:name name :modifiers [(mod5e/trait-cfg {:name name :summary summary})]}))

(defn mutagen-selection [class-kw]
  (t/selection-cfg
   {:name "Mutagen Formula"
    :key :mutagen-formula
    :min 1 :max 1
    :ref [:class class-kw :mutagen-formula]
    :multiselect? true
    :options (map mutagen-option (filter #(nil? (:level %)) mutagen-options))}))

(defn advanced-mutagen-selection [class-kw]
  (t/selection-cfg
   {:name "Advanced Mutagen Formula"
    :key :advanced-mutagen
    :min 1 :max 1
    :ref [:class class-kw :advanced-mutagen]
    :multiselect? true
    :options (map mutagen-option mutagen-options)}))

(def mutant-subclass
  {:name "Order of the Mutant"
   :key :mutant
   :levels {3 {:selections [(mutagen-selection :blood-hunter)
               (mutagen-selection :blood-hunter)
               (mutagen-selection :blood-hunter)]
            :modifiers [(mod5e/trait-cfg {:name "Mutagencraft" :summary "Bonus action to imbibe. One active at a time. Lasts until short/long rest."})]}
            7 {:modifiers [(mod5e/trait-cfg {:name "Strange Metabolism" :summary "Resistance to poison/poisoned. Swap mutagens as action in combat."})
                           (mod5e/damage-resistance :poison)]}
            11 {:selections [(advanced-mutagen-selection :blood-hunter)
                (advanced-mutagen-selection :blood-hunter)]
             :modifiers [(mod5e/trait-cfg {:name "Advanced Mutagencraft" :summary "Learn 2 advanced formulas."})]}
            15 {:modifiers [(mod5e/trait-cfg {:name "Brand of Axiom" :summary "Brand prevents invisibility/illusion. Shapechangers must save or revert."})]}
            18 {:modifiers [(mod5e/trait-cfg {:name "Exalted Mutation" :summary "Two mutagens active simultaneously. Immune to poison/poisoned."})
                            (mod5e/condition-immunity :poisoned)]}}})

(def profane-soul-spells-known
  {3 2, 4 1, 5 1, 7 1, 8 1, 9 1, 11 1, 13 1, 15 1, 17 1, 19 1})

(def profane-soul-subclass
  {:name "Order of the Profane Soul"
   :key :profane-soul
   :spellcasting {:cantrips-known {3 2, 10 1}
                  :spells-known profane-soul-spells-known
                  :slot-schedule {3 [1] 4 [1] 5 [2] 6 [2] 7 [0 2] 8 [0 2] 9 [0 0 2]
                                  10 [0 0 2] 11 [0 0 0 2] 12 [0 0 0 2] 13 [0 0 0 0 2]
                                  14 [0 0 0 0 2] 15 [0 0 0 0 2] 16 [0 0 0 0 2]
                                  17 [0 0 0 0 2] 18 [0 0 0 0 2] 19 [0 0 0 0 2] 20 [0 0 0 0 2]}
                  :known-mode :schedule
                  :pact-magic? true
                  :ability ::char5e/int}
   :levels {3 {:modifiers [(mod5e/trait-cfg {:name "Otherworldly Patron" :summary "Choose patron. Gain Pact Magic (Int-based). Cantrips + spell slots recharge on short rest."})
                           (mod5e/trait-cfg {:name "Rite Focus" :summary "Weapon is spellcasting focus with active rite. Patron-specific bonus."})]}
            7 {:modifiers [(mod5e/trait-cfg {:name "Mystic Frenzy" :summary "Cast cantrip → bonus action weapon attack."})
                           (mod5e/trait-cfg {:name "Revealed Arcana" :summary "Patron-specific 2nd level spell (free)."})]}
            11 {:modifiers [(mod5e/trait-cfg {:name "Improved Pact Magic" :summary "Pact slots become 3rd level."})]}
            15 {:modifiers [(mod5e/trait-cfg {:name "Brand of the Sapping" :summary "Branded creature -10ft speed, can't take reactions."})]}
            18 {:modifiers [(mod5e/trait-cfg {:name "Unsealed Arcana" :summary "3rd level patron spell, 1/long rest free."})]}}})

(defn blood-hunter-option [spells spells-map plugin-subclasses-map language-map weapon-map]
  (opt5e/class-option
   spells
   spells-map
   plugin-subclasses-map
   language-map
   weapon-map
   {:name "Blood Hunter"
    :key :blood-hunter
    :hit-die 10
    :ability-increase-levels [4 8 12 16 19]
    :subclass-title "Blood Hunter Order"
    :subclass-level 3
    :source :blood-hunter
    :profs {:armor {:light true :medium true :shields true}
            :weapon {:simple true :martial true}
            :save {::char5e/dex true ::char5e/int true}
            :skill-options {:choose 3 :options {:acrobatics true :arcana true :athletics true
                                                :history true :insight true :investigation true
                                                :religion true :survival true}}}
    :multiclass-prereqs [(opt5e/ability-prereq ::char5e/str 13)
                         (opt5e/ability-prereq ::char5e/int 13)]
    :weapon-choices [{:name "Martial Weapon"
                      :options {:martial 1}}
                     {:name "Simple Weapon"
                      :options {:simple 1}}]
    :equipment {:explorers-pack 1}
    :subclasses [ghostslayer-subclass lycan-subclass mutant-subclass profane-soul-subclass]
    :levels {1 {:modifiers [(mod5e/trait-cfg {:name "Hunter's Bane" :summary "Advantage Survival to track fey/fiends/undead. Advantage Int checks to recall info. Immune to vampirism."})
                            (mod5e/trait-cfg {:name "Blood Maledict" :summary "Invoke blood curses. Uses = Int mod (min 1). Regain on short/long rest."})
                            (mod5e/saving-throw-advantage [:fey :fiend :undead])]
                :selections [(blood-curse-selection :blood-hunter)]}
             2 {:modifiers [(mod5e/trait-cfg {:name "Crimson Rite" :summary "Bonus action: activate rite. Take hemocraft die necrotic (can't reduce). Attacks deal extra rite damage = hemocraft die. d4 (1-4), d6 (5-10), d8 (11-16), d10 (17-20)."})
                            (mod5e/attack
                             {:name "Crimson Rite"
                              :damage-die 4
                              :damage-die-count 1
                              :damage-type "rite element"
                              :notes "Bonus action to activate. Scales: d4/d6/d8/d10."})]
                :selections [(crimson-rite-selection :blood-hunter)
                             (opt5e/fighting-style-selection :blood-hunter #{:archery :dueling :great-weapon-fighting :two-weapon-fighting})]}
             5 {:modifiers [(mod5e/trait-cfg {:name "Extra Attack" :summary "Attack twice with Attack action."})]}
             6 {:modifiers [(mod5e/trait-cfg {:name "Brand of Castigation" :summary "Brand damaged creature. Know direction. Takes hemocraft die psychic when it damages you/allies within 5ft."})]
                :selections [(blood-curse-selection :blood-hunter)]}
             7 {:selections [(crimson-rite-selection :blood-hunter)]}
             9 {:modifiers [(mod5e/trait-cfg {:name "Grim Psychometry" :summary "Advantage History checks on objects. 10 min meditation for psychic impression."})]}
             10 {:modifiers [(mod5e/trait-cfg {:name "Dark Augmentation" :summary "+5ft speed. Bonus to Str/Dex/Con saves = Int mod (min +1)."})
                             (mod5e/speed 5)
                             (mod5e/saving-throw-bonus ::char5e/str 1)
                             (mod5e/saving-throw-bonus ::char5e/dex 1)
                             (mod5e/saving-throw-bonus ::char5e/con 1)]
                 :selections [(blood-curse-selection :blood-hunter)]}
             13 {:modifiers [(mod5e/trait-cfg {:name "Brand of Tethering" :summary "Branded creature can't teleport. Wis save or fail + 4d6 psychic."})]}
             14 {:modifiers [(mod5e/trait-cfg {:name "Hardened Soul" :summary "Advantage on saves vs frightened and charmed."})
                             (mod5e/saving-throw-advantage [:frightened :charmed])]
                 :selections [(blood-curse-selection :blood-hunter)
                              (esoteric-rite-selection :blood-hunter)]}
             20 {:modifiers [(mod5e/trait-cfg {:name "Sanguine Mastery" :summary "Below half HP: rite dice maximized. On crit: heal hemocraft die + Int mod."})]}}}))
