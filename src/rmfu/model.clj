(ns rmfu.model)

(defrecord User [;; ommit ObjectId auto-created by mongodb as _id on insert
                 first
                 last
                 email
                 password])