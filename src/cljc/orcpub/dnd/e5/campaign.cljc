(ns orcpub.dnd.e5.campaign)

(defn generate-invite-code []
  (let [chars "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"]
    (apply str (repeatedly 6 #(nth chars (rand-int (count chars)))))))
