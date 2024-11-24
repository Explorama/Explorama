(ns de.explorama.frontend.woco.configs.paths)

(def configs-root [:woco :configs])

(def share-infos (conj configs-root :share))
(def possible-users (conj share-infos :users))
(def possible-roles (conj share-infos :roles))