(ns orcpub.dnd.e5.compendium.blood-hunter)

(def hemocraft-die-by-level
  {1 4, 2 4, 3 4, 4 4, 5 6, 6 6, 7 6, 8 6, 9 6, 10 6,
   11 8, 12 8, 13 8, 14 8, 15 8, 16 8, 17 10, 18 10, 19 10, 20 10})

(def crimson-rites
  [{:key :rite-of-the-flame :name "Rite of the Flame" :level 2 :damage-type :fire
    :description "As a bonus action, you imbue your weapon with the rite, taking necrotic damage equal to your hemocraft die. While active, attacks deal extra fire damage equal to your hemocraft die."}
   {:key :rite-of-the-frozen :name "Rite of the Frozen" :level 2 :damage-type :cold
    :description "As a bonus action, you imbue your weapon with the rite, taking necrotic damage equal to your hemocraft die. While active, attacks deal extra cold damage equal to your hemocraft die."}
   {:key :rite-of-the-storm :name "Rite of the Storm" :level 2 :damage-type :lightning
    :description "As a bonus action, you imbue your weapon with the rite, taking necrotic damage equal to your hemocraft die. While active, attacks deal extra lightning damage equal to your hemocraft die."}
   {:key :rite-of-the-dead :name "Rite of the Dead" :level 14 :damage-type :necrotic
    :description "As a bonus action, you imbue your weapon with the rite, taking necrotic damage equal to your hemocraft die. While active, attacks deal extra necrotic damage equal to your hemocraft die."}
   {:key :rite-of-the-oracle :name "Rite of the Oracle" :level 14 :damage-type :psychic
    :description "As a bonus action, you imbue your weapon with the rite, taking necrotic damage equal to your hemocraft die. While active, attacks deal extra psychic damage equal to your hemocraft die."}
   {:key :rite-of-the-roar :name "Rite of the Roar" :level 14 :damage-type :thunder
    :description "As a bonus action, you imbue your weapon with the rite, taking necrotic damage equal to your hemocraft die. While active, attacks deal extra thunder damage equal to your hemocraft die."}
   {:key :rite-of-the-dawn :name "Rite of the Dawn" :level 3 :damage-type :radiant :subclass "Ghostslayer"
    :description "Your Crimson Rite deals radiant damage and sheds bright light in a 20-foot radius. While active, you have resistance to necrotic damage."}])

(def blood-curses
  [{:key :blood-curse-of-the-anxious :name "Blood Curse of the Anxious" :level 1
    :description "When a creature you can see within 30 feet makes an attack roll, you can use your reaction to give that attack roll disadvantage. Amplify: The target has disadvantage on the next attack roll before the end of their next turn. You take hemocraft die necrotic damage."}
   {:key :blood-curse-of-binding :name "Blood Curse of Binding" :level 1
    :description "As a bonus action, you attempt to bind a Large or smaller creature within 30 feet. It must make a Strength saving throw or be restrained until the end of its next turn. Amplify: The curse lasts for 1 minute, with repeated saves at end of each turn. You take hemocraft die damage."}
   {:key :blood-curse-of-bloated-agony :name "Blood Curse of Bloated Agony" :level 1
    :description "As a bonus action, you curse a creature within 30 feet. It must make a Constitution save. On a failure, the creature has disadvantage on Strength and Dexterity checks, and takes 1d8 necrotic damage whenever it moves more than 10 feet on a turn. Amplify: Lasts for 1 minute. You take hemocraft die damage."}
   {:key :blood-curse-of-exposure :name "Blood Curse of Exposure" :level 1
    :description "When a creature you can see within 30 feet takes damage, you can use your reaction to remove one damage resistance from that creature until the end of your next turn. Amplify: Remove damage immunity instead, treating it as resistance. You take hemocraft die damage."}
   {:key :blood-curse-of-the-eyeless :name "Blood Curse of the Eyeless" :level 1
    :description "When a creature you can see within 30 feet makes an attack roll, you can use your reaction to roll your hemocraft die and subtract the result from the attack roll. Amplify: All attack rolls against you by that creature have the penalty until end of its turn. You take hemocraft die damage."}
   {:key :blood-curse-of-the-fallen-puppet :name "Blood Curse of the Fallen Puppet" :level 1
    :description "When a creature you can see within 30 feet drops to 0 hit points, you can use your reaction to cause it to make one weapon attack against a target of your choice within its range. Amplify: The attack has advantage and deals extra damage equal to hemocraft die. You take hemocraft die damage."}
   {:key :blood-curse-of-the-marked :name "Blood Curse of the Marked" :level 1
    :description "As a bonus action, you mark a creature within 30 feet. Until the end of your turn, whenever you hit the cursed creature with a weapon for which you have an active Crimson Rite, you roll your rite damage an additional time. Amplify: The mark lasts for 1 minute (concentration). You take hemocraft die damage."}
   {:key :blood-curse-of-the-muddled :name "Blood Curse of the Muddled" :level 1
    :description "When a creature you can see within 30 feet makes an Intelligence, Wisdom, or Charisma saving throw, you can use your reaction to roll your hemocraft die and subtract the result from the save. Amplify: Apply to all saves of those types until end of creature's next turn. You take hemocraft die damage."}
   {:key :blood-curse-of-corrosion :name "Blood Curse of Corrosion" :level 15 :subclass "Mutant"
    :description "As a bonus action, you cause a creature within 30 feet to take 4d6 necrotic damage. It must succeed on a Constitution save or be poisoned. Amplify: The creature is also paralyzed while poisoned this way. You take hemocraft die damage."}
   {:key :blood-curse-of-the-exorcist :name "Blood Curse of the Exorcist" :level 15 :subclass "Ghostslayer"
    :description "As a bonus action, you choose one creature within 30 feet that is charmed, frightened, or possessed. The condition ends. The source creature takes 3d6 psychic damage and must succeed on a Wisdom save or be stunned until end of its next turn. Amplify: All such conditions on the target end. You take hemocraft die damage."}
   {:key :blood-curse-of-the-howl :name "Blood Curse of the Howl" :level 15 :subclass "Lycan"
    :description "As an action, you unleash a blood-curdling howl. Each creature within 30 feet that can hear you must make a Wisdom save or be frightened of you until the end of your next turn. Amplify: Creatures that fail are stunned instead. You take hemocraft die damage."}
   {:key :blood-curse-of-the-souleater :name "Blood Curse of the Souleater" :level 15 :subclass "Profane Soul"
    :description "When a creature within 30 feet is reduced to 0 hit points, you can use your reaction to offer the life energy to your patron. You regain an expended pact magic spell slot and gain resistance to all damage until the end of your next turn. Amplify: You also regain HP equal to hemocraft die + Intelligence modifier. You take hemocraft die damage."}])

(def class-features
  [{:key :hunters-bane :name "Hunter's Bane" :level 1
    :description "You have advantage on Survival checks to track fey, fiends, and undead, and on Intelligence checks to recall information about them. You are immune to the curse of vampirism."}
   {:key :hemocraft-die :name "Hemocraft Die" :level 1
    :description "Your hemocraft die starts as a d4 and increases as you gain levels: d6 at 5th, d8 at 11th, d10 at 17th. Used for Crimson Rite damage and Blood Curse costs."}
   {:key :crimson-rite :name "Crimson Rite" :level 2
    :description "As a bonus action, activate a rite on a weapon you're holding. You take necrotic damage equal to one roll of your hemocraft die (can't be reduced). While active, your attacks deal extra damage of the rite's type equal to your hemocraft die. Lasts until you finish a short/long rest, drop the weapon, or are incapacitated."}
   {:key :blood-maledict :name "Blood Maledict" :level 1
    :description "You can invoke blood curses. Uses equal to your hemocraft modifier (minimum 1). Regain all uses on a short or long rest. You learn additional curses as you level."}
   {:key :extra-attack-bh :name "Extra Attack" :level 5
    :description "You can attack twice, instead of once, whenever you take the Attack action on your turn."}
   {:key :brand-of-castigation :name "Brand of Castigation" :level 6
    :description "When you damage a creature with your Crimson Rite, you can brand it (no save). You always know the direction to the branded creature. Each time it deals damage to you or a creature within 5 feet of you, it takes psychic damage equal to your hemocraft die."}
   {:key :grim-psychometry :name "Grim Psychometry" :level 9
    :description "You have advantage on Intelligence (History) checks made to examine an object. You can meditate on an object for 10 minutes to receive a psychic impression of the last creature to hold it."}
   {:key :dark-augmentation :name "Dark Augmentation" :level 10
    :description "Your speed increases by 5 feet. You gain a bonus to Strength, Dexterity, and Constitution saving throws equal to your hemocraft modifier (minimum +1)."}
   {:key :brand-of-tethering :name "Brand of Tethering" :level 13
    :description "Your Brand of Castigation now prevents the creature from teleporting. If it attempts to teleport or leave its current plane, it takes 4d6 psychic damage and must succeed on a Wisdom save or the attempt fails."}
   {:key :hardened-soul :name "Hardened Soul" :level 14
    :description "You have advantage on saving throws against being frightened and charmed."}
   {:key :sanguine-mastery :name "Sanguine Mastery" :level 20
    :description "When you are below half your maximum hit points, your Crimson Rite damage dice are maximized. When you score a critical hit with a weapon with an active Crimson Rite, you regain hit points equal to your hemocraft die + hemocraft modifier."}])

(def ghostslayer-features
  [{:key :gs-rite-of-the-dawn :name "Rite of the Dawn" :level 3 :subclass "Ghostslayer"
    :description "Your Crimson Rite damage type becomes radiant. While active, you shed bright light in a 20-foot radius and have resistance to necrotic damage."}
   {:key :gs-curse-specialist :name "Curse Specialist" :level 7 :subclass "Ghostslayer"
    :description "You gain an additional use of your Blood Maledict feature. Your blood curse save DC increases by 1."}
   {:key :gs-aether-walk :name "Aether Walk" :level 11 :subclass "Ghostslayer"
    :description "At the start of your turn, you can magically step into the veil between planes. You can move through creatures and objects as if they were difficult terrain. You take 1d10 force damage if you end your turn inside an object. Once per long rest."}
   {:key :gs-brand-of-sundering :name "Brand of Sundering" :level 15 :subclass "Ghostslayer"
    :description "Your Brand of Castigation deals an additional hemocraft die of radiant damage when you hit the branded creature. The branded creature cannot move through creatures or objects."}
   {:key :gs-rite-revival :name "Rite Revival" :level 18 :subclass "Ghostslayer"
    :description "When you are reduced to 0 HP while you have an active Crimson Rite, you can end the rite and drop to 1 HP instead. Once per long rest."}])

(def lycan-features
  [{:key :ly-hybrid-transformation :name "Hybrid Transformation" :level 3 :subclass "Lycan"
    :description "As a bonus action, you transform into a hybrid form. You gain: +1 AC, advantage on Strength checks and saves, unarmed strikes deal 1d6+Str slashing damage, bonus action unarmed strike, regeneration of 1+Con modifier HP per turn (if below half HP and not taking silver damage). Lasts for 10 minutes or until you revert (bonus action). Uses: proficiency bonus per long rest."}
   {:key :ly-stalkers-prowess :name "Stalker's Prowess" :level 7 :subclass "Lycan"
    :description "Your speed increases by 10 feet while in hybrid form. You have advantage on Wisdom (Perception) checks that rely on hearing or smell. You gain darkvision 60 feet (or +30 if you already have it)."}
   {:key :ly-advanced-transformation :name "Advanced Transformation" :level 11 :subclass "Lycan"
    :description "In hybrid form: unarmed strikes count as magical, you gain an additional 1d6 to unarmed damage, and you have resistance to bludgeoning, piercing, and slashing damage from nonmagical, non-silver weapons."}
   {:key :ly-brand-of-the-voracious :name "Brand of the Voracious" :level 15 :subclass "Lycan"
    :description "While in hybrid form, your Brand of Castigation now also heals you for the psychic damage dealt to the branded creature."}
   {:key :ly-hybrid-mastery :name "Hybrid Transformation Mastery" :level 18 :subclass "Lycan"
    :description "You can use Hybrid Transformation an unlimited number of times. While in hybrid form, you gain +2 to unarmed damage rolls and regenerate even above half HP."}])

(def mutant-features
  [{:key :mu-mutagencraft :name "Mutagencraft" :level 3 :subclass "Mutant"
    :description "You can create mutagens — dangerous elixirs that temporarily enhance your abilities. You know 3 mutagen formulas and learn more at higher levels. You can imbibe a mutagen as a bonus action, gaining its benefit and drawback until your next short or long rest. Only one mutagen active at a time."}
   {:key :mu-mutagen-aether :name "Mutagen: Aether" :level 3 :subclass "Mutant"
    :description "You gain a flying speed of 20 feet. Drawback: You have disadvantage on Strength and Dexterity saving throws."}
   {:key :mu-mutagen-precision :name "Mutagen: Precision" :level 3 :subclass "Mutant"
    :description "You gain +2 to attack rolls. Drawback: You have disadvantage on Strength saving throws."}
   {:key :mu-mutagen-rapidity :name "Mutagen: Rapidity" :level 3 :subclass "Mutant"
    :description "Your walking speed increases by 10 feet and your movement doesn't provoke opportunity attacks. Drawback: You have disadvantage on Intelligence saving throws."}
   {:key :mu-mutagen-mobility :name "Mutagen: Mobility" :level 3 :subclass "Mutant"
    :description "You are immune to the grappled and restrained conditions. Drawback: You have disadvantage on Strength and Dexterity saving throws."}
   {:key :mu-mutagen-cruelty :name "Mutagen: Cruelty" :level 11 :subclass "Mutant"
    :description "When you use the Attack action, you can make one additional weapon attack as part of that action. Drawback: You have disadvantage on Intelligence, Wisdom, and Charisma saving throws."}
   {:key :mu-mutagen-reconstruction :name "Mutagen: Reconstruction" :level 11 :subclass "Mutant"
    :description "At the start of each turn, if you are below half HP, you regain HP equal to your proficiency bonus. Drawback: Your speed is reduced by 10 feet."}
   {:key :mu-strange-metabolism :name "Strange Metabolism" :level 7 :subclass "Mutant"
    :description "You gain resistance to poison damage and the poisoned condition. You can imbibe a mutagen as an action during combat, and when you do, you can end one active mutagen to start a new one."}
   {:key :mu-brand-of-axiom :name "Brand of Axiom" :level 15 :subclass "Mutant"
    :description "Your Brand of Castigation prevents the branded creature from being hidden from you (invisibility, illusion). If it is shape-changed, it must succeed on a Wisdom save or revert to true form."}
   {:key :mu-exalted-mutation :name "Exalted Mutation" :level 18 :subclass "Mutant"
    :description "You can have two mutagens active simultaneously. You gain immunity to poison damage and the poisoned condition."}])

(def profane-soul-features
  [{:key :ps-otherworldly-patron :name "Otherworldly Patron" :level 3 :subclass "Profane Soul"
    :description "You forge a pact with a lesser fiend or dark entity. Choose a patron (Archfey, Celestial, Fiend, Great Old One, Hexblade, or Undying). You gain Pact Magic: two cantrips and spell slots that recharge on short rest. Intelligence is your spellcasting ability."}
   {:key :ps-rite-focus :name "Rite Focus" :level 3 :subclass "Profane Soul"
    :description "While you have an active Crimson Rite, your weapon is a spellcasting focus. You gain a benefit based on patron: Archfey (illuminates targets), Celestial (bonus action heal from curse uses), Fiend (+bonus fire damage with Rite of the Flame), Great Old One (frighten on curse use), Hexblade (proficiency bonus damage on curse), Undying (heal on enemy death)."}
   {:key :ps-mystic-frenzy :name "Mystic Frenzy" :level 7 :subclass "Profane Soul"
    :description "When you use your action to cast a cantrip, you can immediately make one weapon attack as a bonus action."}
   {:key :ps-revealed-arcana :name "Revealed Arcana" :level 7 :subclass "Profane Soul"
    :description "You learn an additional spell based on your patron: Archfey (Blur), Celestial (Lesser Restoration), Fiend (Scorching Ray), Great Old One (Detect Thoughts), Hexblade (Branding Smite), Undying (Blindness/Deafness). These don't count against spells known."}
   {:key :ps-brand-of-the-sapping :name "Brand of the Sapping" :level 15 :subclass "Profane Soul"
    :description "Your Brand of Castigation reduces the branded creature's speed by 10 feet and prevents it from taking reactions while branded."}
   {:key :ps-unsealed-arcana :name "Unsealed Arcana" :level 18 :subclass "Profane Soul"
    :description "You gain a higher-level patron spell: Archfey (Slow), Celestial (Revivify), Fiend (Fireball), Great Old One (Haste), Hexblade (Blink), Undying (Speak with Dead). Once per long rest without using a spell slot."}])

(def all-entries
  (concat
   (map #(assoc % :type :class-feature :class "Blood Hunter" :source "Blood Hunter (Matt Mercer 2022)") class-features)
   (map #(assoc % :type :class-feature :class "Blood Hunter" :subtype "Crimson Rite" :source "Blood Hunter (Matt Mercer 2022)") crimson-rites)
   (map #(assoc % :type :class-feature :class "Blood Hunter" :subtype "Blood Curse" :source "Blood Hunter (Matt Mercer 2022)") blood-curses)
   (map #(assoc % :type :class-feature :class "Blood Hunter" :source "Blood Hunter (Matt Mercer 2022)") ghostslayer-features)
   (map #(assoc % :type :class-feature :class "Blood Hunter" :source "Blood Hunter (Matt Mercer 2022)") lycan-features)
   (map #(assoc % :type :class-feature :class "Blood Hunter" :source "Blood Hunter (Matt Mercer 2022)") mutant-features)
   (map #(assoc % :type :class-feature :class "Blood Hunter" :source "Blood Hunter (Matt Mercer 2022)") profane-soul-features)))
