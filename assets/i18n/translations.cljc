(ns i18n.translations)

(defn translations-for-lang [translations lang]
  (reduce (fn [acc [lang-key tl]]
            (if-let [tl (get tl lang)]
              (assoc acc lang-key tl)
              acc))
          {}
          translations))

(def translations
  {:< {:de-DE "<"
       :en-GB "<"}
   :no-protocol-sync {:de-DE "Protokoll ist deaktiviert während jemand anderes auch das Projekt geladen hat."
                      :en-GB "The protocol is deactivated while another user has also loaded the project."}
   :project-sync-cursortoggle {:de-DE "Cursor anzeigen/ausblenden"
                               :en-GB "show/hide cursor"}
   :aarima {:de-DE "Auto ARIMA"
            :en-GB "Auto ARIMA"}
   :aarima-desc {:de-DE "Der Algorithmus Auto-Regressive Integrated Moving Average (ARIMA) ist in der Ökonometrie, Statistik und Zeitreihenanalyse bekannt. Die Auto Variante versucht die wichtigsten Parameter anhand der Daten selbst zu bestimmen."
                 :en-GB "The auto regressive integrated moving average (ARIMA) algorithm is famous in econometrics, statistics and time series analysis. The Auto variant tries to determine important parameters on its own."}
   :abac-dialog-name {:de-DE "Rolle hinzufügen"
                      :en-GB "Add role"}
   :abac-dialog-save-action-name {:de-DE "Speichern"
                                  :en-GB "Save"}
   :abac-dialog-title {:de-DE "Fügt eine neue Rolle hinzu mit dem Namen ..."
                       :en-GB "Adds a new role with name…"}
   :abac-dialog-title-title {:de-DE "Rolle hinzufügen"
                             :en-GB "Add role"}
   :abac-groups-main-tab {:de-DE "Rollen"
                          :en-GB "Groups"}
   :abac-groups-role-name-name {:de-DE "Rollenname"
                                :en-GB "Role name"}
   :abac-groups-role-name-single-values-label {:de-DE "Rollenname"
                                               :en-GB "Role name"}
   :abac-groups-role-name-single-values-value {:de-DE "Rollenname"
                                               :en-GB "Role name"}
   :abac-user-main-tab {:de-DE "Benutzer"
                        :en-GB "User"}
   :arrange-dataset {:de-DE "Stelle deine Datenbasis zusammen"
                     :en-GB "Arrange your dataset"}
   :choose-search-method {:de-DE "Wähle eine Suchmethode"
                          :en-GB "Choose a search method"}
   :different-attribute-label-hint {:de-DE "Das Attribut existiert bereits, es wird angezeigt als: %s. Wenn du ein neues Attribut erstellen möchtest, benenne das Attribut um."
                                    :en-GB "The attribute already exists, it will be shown as %s. If you want to create a new attribute change the name."}
   :access {:de-DE "Zugriff"
            :en-GB "Access"}
   :accessibility-label {:de-DE "Erklärung zur Barrierefreiheit"
                         :en-GB "Declaration of accessibility"}
   :and {:de-DE "und"
         :en-GB "and"}
   :or {:de-DE "oder"
        :en-GB "or"}
   :user-joined-project {:de-DE "Der Nutzer %s hat das Projekt geladen und sieht die Änderungen."
                         :en-GB "The user %s loaded the project and can see the changes live."}
   :user-left-project {:de-DE "Der Nutzer %s hat das Projekt bei sich geschlossen."
                       :en-GB "The user %s closed the project."}
   :no-sync-hint {:de-DE "Es kann nichts angezeigt werden. Der Hauptbenutzer nimmt hier einige Änderungen vor."
                  :en-GB "Nothing able to display. Main User is making some changes here."}
   :aria-carousel-next {:de-DE "Weiter"
                        :en-GB "next"}
   :aria-carousel-previous {:de-DE "Zurück"
                            :en-GB "previous"}
   :aria-clear {:de-DE "Eingabe entfernen"
                :en-GB "clear input"}
   :aria-copy {:de-DE "Kopieren"
               :en-GB "copy"}
   :aria-select-open {:de-DE "Drop-down öffnern"
                      :en-GB "open drop-down"}
   :aria-select-clear {:de-DE "Auswahl entfernen"
                       :en-GB "clear selection"}
   :aria-select-remove {:de-DE "Entfernen"
                        :en-GB "remove"}
   :aria-select-search {:de-DE "Optionen durchsuchen"
                        :en-GB "search options"}
   :aria-slider-input {:de-DE "Schieber Wert"
                       :en-GB "slider value"}
   :aria-slider-left-input {:de-DE "Schieber Anfangswret"
                            :en-GB "slider start value"}
   :aria-slider-right-input {:de-DE "Schieber Endwert"
                             :en-GB "slider end value"}
   :aria-product-tour-cancel {:de-DE "Produkt-Tour abbrechen"
                              :en-GB "cancel product tour"}
   :aria-label-card-menu {:de-DE "Projekt Menü öffnen"
                          :en-GB "open project menu"}
   :aria-label-slide-up {:de-DE "Folie nach oben bewegen"
                         :en-GB "Move slide up"}
   :aria-label-slide-down {:de-DE "Folie nach unten bewegen"
                           :en-GB "Move slide down"}
   :aria-label-slide-remove {:de-DE "Folie löschen"
                             :en-GB "Remove slide"}
   :aria-label-drag-slide {:de-DE "Folie ziehen"
                           :en-GB "Drag slide"}
   :aria-label-edit-slide-title {:de-DE "Folie umbennen"
                                 :en-GB "Rename slide"}
   :aria-label-select-checkbox {:de-DE "Der Auswahl hinzufügen"
                                :en-GB "Add to selection"}
   :aria-copy-project-link {:de-DE "Projektlink kopieren"
                            :en-GB "Copy project link"}
   :aria-add-new-row {:de-DE "Neue Reihe hinzufügen"
                      :en-GB "Add new row"}
   :aria-label-current-page {:de-DE "Aktuelle Seite"
                             :en-GB "Current Page"}
   :aria-add-attribute {:de-DE "Weiteres Attribut hinzufügen"
                        :en-GB "Add another attribute"}
   :aria-detail-view-open {:de-DE "Füge das Event der Übersicht hinzu"
                           :en-GB "Add the event to the overview"}
   :aria-detail-view-focus {:de-DE "Fokussiere das Ursprungsfenster"
                            :en-GB "Focus the frame the event originates from"}
   :author-label {:de-DE "Erstellt von"
                  :en-GB "Created by"}
   :multiselect-arrange-horizontal {:de-DE "Selektierte Fenster horizontal anordnen"
                                    :en-GB "Arrange selected windows horizontally"}
   :multiselect-arrange-vertical {:de-DE "Selektierte Fenster vertikal anordnen"
                                  :en-GB "Arrange selected windows vertically"}
   :multiselect-arrange-dynamic {:de-DE "Selektierte Fenster gruppiert nach ihrer Quelle anordnen"
                                 :en-GB "Arrange selected windows grouped by their source"}
   :multiselect-close {:de-DE "Selektierte Fenster schließen"
                       :en-GB "Close selected windows"}
   :topic-datasource-category {:de-DE "Thema/Datenquelle"
                               :en-GB "Topic/Datasource"}
   :topic-category {:de-DE "Thema"
                    :en-GB "Topic"}
   :topic-category-desc {:de-DE "Auf Basis von Themen relevante Datenquellen finden"
                         :en-GB "Based on topics you find relevant datasources"}
   :hover-info-datasource-tooltip {:de-DE "Wähle eine oder mehrere Datenquellen aus. Bewege die Maus über das Info-Symbol, um eine Beschreibung anzuzeigen"
                                   :en-GB "Select one or more datasources. Move the mouse over the info icon to display a description."}
   :hover-info-tooltip {:de-DE "Bewege die Maus über das Info-Symbol, um eine Beschreibung anzuzeigen"
                        :en-GB "Move the mouse over the info icon to display a description."}

   :hover-info-topic-tooltip {:de-DE "Wähle ein oder mehrere Themen aus. Bewege die Maus über das Info-Symbol, um eine Beschreibung anzuzeigen"
                              :en-GB "Select one or more topics. Move the mouse over the info icon to display a description."}
   :no-description-available {:de-DE "Keine Beschreibung vorhanden"
                              :en-GB "No description available"}
   :no-options {:de-DE "Keine Optionen"
                :en-GB "No options"}
   :datasources {:de-DE "Datenquellen"
                 :en-GB "Datasources"}
   :temp-datasources {:de-DE "Temporäre Datenquellen"
                      :en-GB "Temporary Datasources"}
   :topics {:de-DE "Themen"
            :en-GB "Topics"}
   :apply-changes {:de-DE "Änderungen anwenden"
                   :en-GB "Apply changes"}
   :create-snapshot-title {:de-DE "Neuen Snapshot erstellen"
                           :en-GB "Create Snapshot"}
   :edit-snapshot-title {:de-DE "Snapshot bearbeiten"
                         :en-GB "Edit Snapshot"}
   :topics->datasources-desc {:de-DE "Datenquellen, die mit dem Thema verbunden sind"
                              :en-GB "Datasources that are connected to the topic."}
   :datsource->topics-desc {:de-DE "Themen, die mit der Datenquelle verbunden sind"
                            :en-GB "Topics that are connected to the datasource."}
   :datasource-category {:de-DE "Datenquelle"
                         :en-GB "Datasource"}
   :project-already-loaded {:de-DE "Das Projekt wird gerade von einem anderen Nutzer bearbeitet, Projekt kann schreibgeschützt geladen werden."
                            :en-GB "Another user is working on the project, you can load it read-only."}
   :datasource-category-desc {:de-DE "Überblicke und wähle vorhandene Datenquellen aus"
                              :en-GB "Get an overview of existing datasources"}
   :data-section-title-displayed {:de-DE "%s von "
                                  :en-GB "%s of "}

   :geografic-category {:de-DE "Geografisch"
                        :en-GB "Geographic"}
   :geographic-selection-subtitle {:de-DE "Für eine geografische Einschränkung füge geografische Attribute (z.B. Land) und deren Ausprägungen hinzu."
                                   :en-GB "For a geographical restriction, add geographical attributes (e.g., country) and their characteristics."}
   :geografic-category-desc {:de-DE "Wähle den für dich interessanten geografischen Raum"
                             :en-GB "Choose the geographical area that interests you"}
   :selection-characteristics-title {:de-DE "Ausprägungen"
                                     :en-GB "Characteristics"}
   :selection-characteristics-subtitle {:de-DE "Die Selektion kann die nachfolgenden Möglichkeiten einschränken."
                                        :en-GB "The selection can limit the following possibilities."}

   :timeperiod-category {:de-DE "Zeitraum"
                         :en-GB "Time period"}
   :timeperiod-selection-subtitle {:de-DE "Für eine zeitliche Einschränkung füge zeitliche Attribute (z.B. Jahr) und deren Ausprägungen hinzu."
                                   :en-GB "For a temporal restriction, add temporal attributes (e.g., year) and their characteristics."}
   :timeperiod-category-desc {:de-DE "Schränke den Zeitraum deiner Datenauswahl ein"
                              :en-GB "Limit the period of your data selection"}
   :attribute-category {:de-DE "Weitere Attribute"
                        :en-GB "Other attributes"}
   :attribute-selection-subtitle {:de-DE "Für eine genauere Definition der Daten wähle ein Attribut und dazu eine oder mehrere Ausprägungen."
                                  :en-GB "For a more precise definition of the data, select an attribute and one or more values for it."}
   :attribute-category-desc {:de-DE "Verfeinere deine Datenauswahl mit weiteren Attributen (nicht in den vorherigen Kategorien enthalten)"
                             :en-GB "Refine your data selection with additional attributes (not included in the previous categories)"}
   :add-attribute-placeholder {:de-DE "Attribut hinzufügen..."
                               :en-GB "Add attribute..."}
   :no-matches {:de-DE "Keine Übereinstimmung"
                :en-GB "No matches"}
   :switch-to-datasource {:de-DE "Datenquelle auswählen"
                          :en-GB "Choose datasource"}
   :switch-to-topic {:de-DE "Thema auswählen"
                     :en-GB "Choose topic"}
   :free-search-mode {:de-DE "Frei"
                      :en-GB "Free"}
   :free-search {:de-DE "Freie Suche"
                 :en-GB "Free search"}
   :free-search-shortdesc {:de-DE "Geeignet für Nutzer, die schnell und zielgerichtet vorgehen möchten"
                           :en-GB "Suitable for users who want to proceed quickly and purposefully"}
   :free-search-desc {:de-DE "In einer einfachen Übersicht sind alle Kategorien und Attribute direkt auswählbar und die Zusammenstellung deiner Datenbasis für die Analyse ist frei."
                      :en-GB "All categories and attributes can be selected directly in a simple overview and you are free to compile your data basis for the analysis."}
   :incomplete-definition {:de-DE "unvollständig"
                           :en-GB "incomplete"}
   :invalid-search-row-message {:de-DE "Attribut ist nicht mehr verfügbar. Entfernen oder erneut importieren, um fortzufahren"
                                :en-GB "Attribute is not available anymore. Delete or re import to proceed"}
   :guided-search-mode {:de-DE "Geführt"
                        :en-GB "Guided"}
   :guided-search {:de-DE "Geführte Suche"
                   :en-GB "Guided search"}
   :guided-search-shortdesc {:de-DE "Optimal für die Erstnutzung und Nutzer, die schrittweise begleitet werden möchten"
                             :en-GB "Optimal for first-time users and users who want to be guided step-by-step"}
   :guided-search-desc {:de-DE "Du wirst Schritt für Schritt durch die Auswahl der Daten für die Analyse geführt. Dabei werden zusätzliche Hinweise angezeigt."
                        :en-GB "You are guided step by step through the selection of data for analysis. Additional notes are displayed along the way."}
   :save-search-mode-decision {:de-DE "Meine Entscheidung für die nächste Suche speichern"
                               :en-GB "Save my decision for next search"}
   :included-datasources {:de-DE "Enthaltene Datenquellen"
                          :en-GB "Included datasources"}
   :active-item-tooltip {:de-DE "Klicken um die Auswahl aufzuheben"
                         :en-GB "Click to deselect"}
   :active-label {:de-DE "Aktiv"
                  :en-GB "Active"}
   :add-custom-attribute-label {:de-DE "Erweiterte Attributdefinition"
                                :en-GB "Add custom attribute"}
   :add-indicator {:de-DE "Indikator hinzufügen"
                   :en-GB "Add indicator"}
   :add-layout-button-text {:de-DE "Layout hinzufügen"
                            :en-GB "Add layout"}
   :add-layout-label {:de-DE "Layout hinzufügen"
                      :en-GB "Add Layout"}
   :add-overlayer-button-text {:de-DE "Overlayer hinzufügen"
                               :en-GB "Add Overlayer"}
   :add-row {:de-DE "Reihe hinzufügen"
             :en-GB "Add row"}
   :additional-attributes-header {:de-DE "Zusätzliche Attribute"
                                  :en-GB "Additional Attributes"}
   :additional-attributes-hint {:de-DE "Zusätzliche Attribute können entweder als Kontext (Checkbox Kontext) oder normal (Property) eingeordnet werden. Kontexte sind Attribute, bei denen sich die Ausprägungen öfter wiederholen, Länder, Kategorien etc. Bei normalen Attributen (Properties) sind die Werte spezifischer pro Datenpunkt, wie zum Beispiel bei numerischen Werten oder Titel (quasi unendlich viele Ausprägungen möglich). Kontexte können gruppiert und sortiert werden, normale Attribute (Properties) können nur sortiert werden."
                                :en-GB "Additional attributes can be classified either as context or as property. Contexts are attributes with a smaller number of repeatedly occurring values like countries, categories, etc. Properties can have values specific for a data point like numeric values or title (an infinite number of values are possible). Contexts can be grouped and sorted, properties can only be sorted."}
   :additional-infos {:de-DE "Zusätzliche Informationen"
                      :en-GB "Additional Information"}
   :adv-add-condition-label {:de-DE "Abgleich hinzufügen"
                             :en-GB "Add condition"}
   :adv-add-row-label {:de-DE "Zuweisung hinzufügen"
                       :en-GB "Add lookup"}
   :adv-aggregate-columns-init-label {:de-DE "Startwert"
                                      :en-GB "Initial value"}
   :adv-aggregate-columns-init-placeholder {:de-DE "..."
                                            :en-GB "..."}
   :adv-aggregate-columns-op-label {:de-DE "Spalten aggregieren mit"
                                    :en-GB "Aggregate columns by"}
   :adv-aggregate-columns-op-placeholder {:de-DE "..."
                                          :en-GB "..."}
   :adv-aggregate-row-by-id-init-label {:de-DE "Startwert"
                                        :en-GB "Initial value"}
   :adv-aggregate-row-by-id-init-placeholder {:de-DE "..."
                                              :en-GB "..."}
   :adv-aggregate-row-by-id-op-label {:de-DE "Zeilen aggregieren mit"
                                      :en-GB "Aggregate rows by"}
   :adv-aggregate-row-by-id-op-placeholder {:de-DE "..."
                                            :en-GB "..."}
   :adv-col-lookup-col-label {:de-DE "Spaltenname"
                              :en-GB "Column name"}
   :adv-col-lookup-col-placeholder {:de-DE "Spaltenname"
                                    :en-GB "Column name"}
   :adv-col-lookup-input-label {:de-DE "Spaltenwert"
                                :en-GB "Column value"}
   :adv-col-lookup-input-placeholder {:de-DE "Spaltenwert"
                                      :en-GB "Column value"}
   :adv-col-lookup-result-label {:de-DE "Ergebniswert"
                                 :en-GB "Result value"}
   :adv-col-lookup-result-placeholder {:de-DE "Ergebniswert"
                                       :en-GB "Result value"}
   :adv-columns-label {:de-DE "Spaltenauswahl (Datei)"
                       :en-GB "Column selection (File)"}
   :adv-columns-placeholder {:de-DE "Spaltenname"
                             :en-GB "Column name"}
   :adv-conditions-label {:de-DE "Abgleichen"
                          :en-GB "Conditions"}
   :adv-input-mode-label {:de-DE "Fort. Methoden"
                          :en-GB "Adv. methods"}
   :adv-input-mode-placeholder {:de-DE "Auswahl"
                                :en-GB "..."}
   :adv-or-label {:de-DE "Fallback-Wert"
                  :en-GB "Fallback value"}
   :adv-or-placeholder {:de-DE "Fallback-Wert"
                        :en-GB "Fallback value"}
   :adv-remove-condition-label {:de-DE "Abgleich entfernen"
                                :en-GB "Remove condition"}
   :adv-remove-row-label {:de-DE "Zuweisung entfernen"
                          :en-GB "Remove lookup"}
   :adv-result-name-label {:de-DE "Attributname"
                           :en-GB "Attribute name"}
   :adv-result-name-placeholder {:de-DE "Attributname"
                                 :en-GB "Attribute name"}
   :adv-save-button-label {:de-DE "Attribut hinzufügen"
                           :en-GB "Add attribute"}
   :adv-select-mode {:de-DE "ggf. fort. Methoden auswählen"
                     :en-GB "possibly add adv. methods"}
   :adv-split-adv-or-placeholderchar-label {:de-DE ""
                                            :en-GB ""}
   :adv-split-char-label {:de-DE "Aufteilen nach"
                          :en-GB "Split by"}
   :adv-split-char-placeholder {:de-DE "..."
                                :en-GB "..."}
   :adv-toggle-label {:de-DE "Erweiterte Funktionen"
                      :en-GB "Adv. functions"}
   :adv-type-input-label {:de-DE "Typ (Custom)"
                          :en-GB "Type (Custom)"}
   :adv-type-input-placeholder {:de-DE "Typ"
                                :en-GB "Type"}
   :adv-type-label {:de-DE "Typauswahl"
                    :en-GB "Type selection"}
   :adv-type-placeholder {:de-DE "Typ"
                          :en-GB "..."}
   :advanced-edit-property-label {:de-DE "Bearbeiten"
                                  :en-GB "Edit"}
   :advanced-label {:de-DE "Erweitert"
                    :en-GB "Advanced"}
   :advanced-ops-label {:de-DE "Erweiterte Optionen"
                        :en-GB "Advanced Options"}
   :advanced-options-label {:de-DE "Erweiterte Optionen"
                            :en-GB "Advanced Options"}
   :advanced-selection-no-valid {:de-DE "(keine valide Auswahl)"
                                 :en-GB "(no valid selection)"}
   :advanced-settings-div {:de-DE "Erweiterte Einstellungen"
                           :en-GB "Advanced Settings"}
   :advancedmode-label {:de-DE "Erweiterter Modus"
                        :en-GB "Advanced mode"}
   :affect-future-only {:de-DE "Betrifft"
                        :en-GB "Affects"}
   :aggregate-attributes {:de-DE "Allgemein"
                          :en-GB "General"}
   :aggregate-by-attribute {:de-DE "Aggregieren nach"
                            :en-GB "Aggregate by"}
   :aggregate-dates-error {:de-DE "Durchschnitt und Summe sind keine validen Optionen für Aggregationen mit Dates"
                           :en-GB "Average and sum are not a valid options for date aggregations"}
   :aggregation-method-label {:de-DE "Aggregation"
                              :en-GB "Aggregation"}
   :aic {:de-DE "AIC"
         :en-GB "AIC"}
   :alert-delete-failed {:de-DE "Löschen des Alarms fehlgeschlagen. Bitte versuche es später erneut"
                         :en-GB "Failed to delete alert. Please try again later"}
   :alert-desc-invalid {:de-DE "Der Alarm ist unvollständig oder invalide"
                        :en-GB "The given alert is incomplete or invalid"}
   :alert-label {:de-DE "Alarm"
                 :en-GB "Alert"}
   :alert-list-failed {:de-DE "Abfragen von Alarmen fehlgeschlagen. Bitte lade die Seite neu und versuche es erneut"
                       :en-GB "Failed to fetch alerts. Please reload the Page and try again"}
   :alert-paused {:de-DE "Alarm pausiert"
                  :en-GB "alert paused"}
   :alert-sidebar-title {:de-DE "Alarme verwalten"
                         :en-GB "Manage Alerts"}
   :alert-update-failed {:de-DE "Speichern des Alarms fehlgeschlagen. Bitte versuche es später erneut"
                         :en-GB "Failed to save alert. Please try again later"}
   :alerts-title {:de-DE "Alarme"
                  :en-GB "Alerts"}
   :algorithm {:de-DE "Algorithmus"
               :en-GB "Algorithm"}
   :algorithms {:de-DE "Algorithmus"
                :en-GB "Algorithms"}
   :all {:de-DE "Alle"
         :en-GB "All"}
   :all-filter-tooltip-text {:de-DE "Events ungefiltert"
                             :en-GB "Events unfiltered"}
   :all-label {:de-DE "Alle"
               :en-GB "all"}
   :alpha {:de-DE "Alpha"
           :en-GB "Alpha"}
   :alternating {:de-DE "Alternating"
                 :en-GB "Alternating"}
   :annotation-footer {:de-DE "Text wird automatisch gespeichert"
                       :en-GB "Text saved automatically"}
   :annotation-label {:de-DE "Projektnotizen"
                      :en-GB "Project Annotation"}
   :any-empty-values-label {:de-DE "Jeder leere Wert"
                            :en-GB "Any empty value"}
   :any-non-empty-values-label {:de-DE "Jeder nicht leere Wert"
                                :en-GB "Any non-empty value"}
   :apply-for-frame-label {:de-DE "Im Fenster nutzen"
                           :en-GB "Apply for window"}
   :arima {:de-DE "ARIMA"
           :en-GB "ARIMA"}
   :arima-desc {:de-DE "Der Algorithmus Auto-Regressive Integrated Moving Average (ARIMA) ist in der Ökonometrie, Statistik und Zeitreihenanalyse bekannt."
                :en-GB "The auto regressive integrated moving average (ARIMA) algorithm is famous in econometrics, statistics and time series analysis."}
   :arima-multiple-not-valid {:de-DE "Mehrfache Werte mit dem selben Datum verursachen Fehler bei der Ausführung von dem ARIMA-Algorithmus"
                              :en-GB "Multiple values with the same date cause errors in the execution of the Arima algorithm"}
   :arima-not-enough-data {:de-DE "Nicht genug Daten - Dieser Algorithmus erfordert mehr Beobachtungen als derzeit bereitgestellt"
                           :en-GB "Not enough data - this algorithm requires more observations than is currently provided"}
   :arima-not-enough-data-50 {:de-DE "Nicht genug Daten - Dieser Algorithmus erfordert mehr Beobachtungen als derzeit bereitgestellt (mindestens 50)."
                              :en-GB "Not enough data - this algorithm requires more observations than is currently provided (at least 50)."}
   :arima-retrospective-forecast-warning {:de-DE "Rückwirkende Vorhersagelinie ist nicht vorhanden."
                                          :en-GB "Retrospective forecast line is not available."}
   :arrangement-tooltip-text {:de-DE "Anordnung optimieren"
                              :en-GB "Optimize arrangement"}
   :assignments-label {:de-DE "Zuweisungen"
                       :en-GB "Assignments"}
   :attribute-config-aggregation {:de-DE "Aggregation"
                                  :en-GB "Aggregation"}
   :attribute-config-categoric-encoding {:de-DE "Encoding"
                                         :en-GB "Encoding"}
   :attribute-config-date-granularity {:de-DE "Granularität"
                                       :en-GB "Granularity"}
   :attribute-config-div {:de-DE "Konfiguration für "
                          :en-GB "Configuration for "}
   :attribute-config-enum? {:de-DE "Kategorisches Feature"
                            :en-GB "Categorical feature"}
   :attribute-config-merge-policy {:de-DE "Unvollständige Zeilen"
                                   :en-GB "Incomplete rows"}
   :attribute-config-multiple-values-per-event {:de-DE "Mehrfache Ausprägung pro Attribut und Event"
                                                :en-GB "Handling of multiple characteristics per Attributes and Event"}
   :attribute-detail-label {:de-DE "Attribut"
                            :en-GB "Attribute"}
   :attribute-label {:de-DE "Attribut"
                     :en-GB "Attribute"}
   :attribute-list-label {:de-DE "Attribute"
                          :en-GB "Attributes"}
   :attribute-name-label {:de-DE "Attributname"
                          :en-GB "Attribute Name"}
   :attribute-names-invalid {:de-DE "Name oder Typ dürfen nicht leer sein oder\neine der folgenden Bezeichnungen haben: "
                             :en-GB "Name or type cannot be empty or\nhave one of the following terms: "}
   :attribute-select-hint {:de-DE "Wähle ein vorhandenes Attribut aus, um die Suchparameter für dieses Attribut einzustellen."
                           :en-GB "Choose from available attributes to adjust search parameters"}
   :attribute-selection {:de-DE "Attributselektion"
                         :en-GB "Attribute selection"}
   :attribute-settings-group {:de-DE "Gewichtete Attribute"
                              :en-GB "Weighted Attributes"}
   :attribute-type-label {:de-DE "Attributtyp"
                          :en-GB "Attribute Type"}
   :attribute-type-tooltip {:de-DE "Für Kontextattribute auswählen. Kontexte sind Attribute, bei denen sich die Ausprägungen öfter wiederholen, Länder, Kategorien etc.. Bei normalen Attributen (Properties) sind die Werte spezifischer pro Datenpunkt, wie zum Beispiel numerische Attribute oder Titel (quasi unendlich viele Ausprägungen möglich). Kontexte können gruppiert und sortiert werden, normale Attribute (Properties) können nur sortiert werden."
                            :en-GB "Tick to define a context attribute. Contexts are attributes with a smaller number of repeatedly occurring values like countries, categories, etc. Properties can have values specific for a data point like numeric attributes or title (an infinite number of values are possible). Context can be grouped and sorted, properties can only be sorted."}
   :attributes-filter-placeholder {:de-DE "Attribute filtern"
                                   :en-GB "Filter attributes"}
   :attributes-label {:de-DE "Attribute"
                      :en-GB "Attributes"}
   :attributes-title {:de-DE "Attribute"
                      :en-GB "Attributes"}
   :auto {:de-DE "Auto"
          :en-GB "Auto"}
   :autogenerated-info {:de-DE "Wird automatisch generiert."
                        :en-GB "Automatically generated."}
   :automatic {:de-DE "Automatisch"
               :en-GB "Automatic"}
   :auxiliary-normailty {:de-DE "Auxiliary Normality"
                         :en-GB "Auxiliary Normality"}
   :auxiliary-normality {:de-DE "Normalverteilung testen"
                         :en-GB "Auxiliary normality test"}
   :available-when-grouped-tooltip {:de-DE "Nur wenn gruppiert"
                                    :en-GB "Only when grouped"}
   :average {:de-DE "Mittelwert"
             :en-GB "Average"}
   :average-label {:de-DE "Durchschnitt"
                   :en-GB "Average"}
   :search-back-to-introduction {:de-DE "Start"
                                 :en-GB "Start"}
   :back {:de-DE "Zurück"
          :en-GB "Back"}
   :background-color {:de-DE "Hintergrundfarbe"
                      :en-GB "Background color"}
   :back-label {:de-DE "Zurück"
                :en-GB "Back"}
   :back-to-overview-label {:de-DE "Zurück zur Übersicht"
                            :en-GB "Back to overview"}
   :backward {:de-DE "Rückwärts"
              :en-GB "Backward"}
   :bar-chart-label {:de-DE "Balkendiagramm"
                     :en-GB "Bar chart"}
   :base-app-label {:de-DE "Zur Anwendung"
                    :en-GB "Open Application"}
   :base-map-select-label {:de-DE "Basiskarte"
                           :en-GB "Base map"}
   :base-multiple {:de-DE "$num Events."
                   :en-GB "$num events."}
   :base-multiple-conds {:de-DE "$num Events mit $conditions."
                         :en-GB "$num events with $conditions."}
   :base-single {:de-DE "$num Event."
                 :en-GB "$num event."}
   :base-single-conds {:de-DE "$num Event mit $conditions."
                       :en-GB "$num event with $conditions."}
   :bat-grp-rights-and-roles {:de-DE "Alarmierung"
                              :en-GB "Alerting"}
   :bic {:de-DE "BIC"
         :en-GB "BIC"}
   :binary {:de-DE "Binary"
            :en-GB "Binary"}
   :bottom-label {:de-DE "Unten"
                  :en-GB "Bottom"}
   :bring-to-search-disabled-hint {:de-DE "Selektiere einen eindeutigen Suchparameter, eventuell ist es notwendig, ein Attribut zusätzlich auszuwählen."
                                   :en-GB "Select an unambiguous search parameter, maybe it's necessary to select an additional attribute."}
   :bring-to-search-label {:de-DE "In die Suche"
                           :en-GB "Bring to search"}
   :btn-add-condition {:de-DE "Bedingung hinzufügen"
                       :en-GB "Add Condition"}
   :bubble-chart-label {:de-DE "Blasendiagramm"
                        :en-GB "Bubble chart"}
   :button-add {:de-DE "Hinzufügen"
                :en-GB "Add"}
   :button-download-mapping {:de-DE "Lade Zuweisungen herunter"
                             :en-GB "Download Mapping"}
   :button-download-xml {:de-DE "Lade XML herunter"
                         :en-GB "Download XML"}
   :button-generate-xml {:de-DE "Generiere XML"
                         :en-GB "Generate XML"}
   :button-test-import {:de-DE "Testimport"
                        :en-GB "Test Import"}
   :calculate-preview-button {:de-DE "Datenvorschau berechnen"
                              :en-GB "Run preview calculation"}
   :cancel-label {:de-DE "Abbrechen"
                  :en-GB "Cancel"}
   :cancel-settings-button {:de-DE "Abbrechen"
                            :en-GB "Cancel"}
   :canvas-tooltip-grp-sort-average {:de-DE "Gruppen sortiert nach dem Mittelwert des Attibutes"
                                     :en-GB "Groups sorted by the average value of Attribute"}
   :canvas-tooltip-grp-sort-max {:de-DE "Gruppen sortiert nach dem maximalen Wert des Attibutes"
                                 :en-GB "Groups sorted by the maximal value of Attribute"}
   :canvas-tooltip-grp-sort-min {:de-DE "Gruppen sortiert nach dem minimalen Wert des Attibutes"
                                 :en-GB "Groups sorted by the minimal value of Attribute"}
   :canvas-tooltip-grp-sort-sum {:de-DE "Gruppen sortiert nach der Summe des Attibutes"
                                 :en-GB "Groups sorted by the sum of Attribute"}
   :cardeditor-discardbutton-label {:de-DE "Abbrechen"
                                    :en-GB "Discard"}
   :cardeditor-savebutton-label {:de-DE "Speichern"
                                 :en-GB "Save"}
   :categoric {:de-DE "kategorisch"
               :en-GB "categorical"}
   :categoric-variable {:de-DE "Kategorische Variable"
                        :en-GB "Categorical variable"}
   :category-col {:de-DE "Kategoriespalte"
                  :en-GB "Category column"}
   :category-weights {:de-DE "Kategoriegewichtung"
                      :en-GB "Category weigths"}
   :century-label {:de-DE "Jhdt."
                   :en-GB "cent."}
   :century-placeholder {:de-DE "z.B. 2000"
                         :en-GB "e.g., 2000"}
   :ch-sig-level {:de-DE "Canova-Hansen Signifikanzlevel"
                  :en-GB "Canova-Hansen significance level"}
   :characteristic-detail-label {:de-DE "Ausprägung"
                                 :en-GB "Characteristic"}
   :characteristic-list-label {:de-DE "Ausprägungen "
                               :en-GB "Characteristics "}
   :characteristic-max-list-label {:de-DE "(max. 10.000)"
                                   :en-GB "(max. 10,000)"}
   :characteristics-info {:de-DE "Wähle die Gruppen, welche im Diagramm angezeigt werden"
                          :en-GB "Choose which groups to include in the chart"}
   :chart-add-button {:de-DE "Diagramm hinzufügen"
                      :en-GB "Add chart"}
   :chart-disabled-hint {:de-DE "Dieser Typ kann nicht mit mehrer Diagrammen kombiniert werden."
                         :en-GB "You can't use this type in combination with another chart."}
   :chart-remove-idx-chart {:de-DE "Diagramm $num entfernen"
                            :en-GB "Remove chart $num"}
   :chart-remove-button {:de-DE "Diagramm entfernen"
                         :en-GB "Remove chart"}
   :chart-attr-label {:de-DE "Attribut"
                      :en-GB "Attribute"}
   :chart-component-label {:de-DE "Diagramm"
                           :en-GB "Chart"}
   :chart-loading-message {:de-DE "Diagramm wird generiert"
                           :en-GB "Chart is generated"}
   :chart-options-loading-message {:de-DE "Diagrammauswahlen werden berechnet"
                                   :en-GB "Chart Options Data are calculating"}
   :chart-type-label {:de-DE "Diagrammtyp"
                      :en-GB "Chart Type"}
   :chart-type-placeholder {:de-DE "Diagrammtyp auswählen"
                            :en-GB "Select chart type"}
   :chart-colors  {:de-DE "Farben"
                   :en-GB "Colors"}
   :charts-protocol-action-bar {:de-DE "Balkendiagramm"
                                :en-GB "Bar chart"}
   :charts-protocol-action-bubble {:de-DE "Blasendiagramm"
                                   :en-GB "Bubble chart"}
   :charts-protocol-action-config-changed {:de-DE "Einstellungen geändert"
                                           :en-GB "settings changed"}
   :charts-protocol-action-line {:de-DE "Liniendiagramm"
                                 :en-GB "Line chart"}
   :charts-protocol-action-pie {:de-DE "Tortendiagramm"
                                :en-GB "Pie chart"}
   :charts-protocol-action-scatter {:de-DE "Streudiagramm"
                                    :en-GB "Scatter plot"}
   :charts-protocol-action-wordcloud {:de-DE "Wortwolke"
                                      :en-GB "Wordcloud"}
   :charts-tooltip-search {:de-DE "Visualisiere die Daten im Diagramm"
                           :en-GB "Visualize the data in the chart"}
   :chebyshev {:de-DE "Chebyshev-Distanz"
               :en-GB "Chebyshev distance"}
   :cholesky-decomposition {:de-DE "Cholesky decomposition"
                            :en-GB "Cholesky decomposition"}
   :choose-algorithm-action {:de-DE "Algorithmusauswahl (Erweiterer Modus)"
                             :en-GB "Choose Algorithm (Advanced Mode)"}
   :choose-algorithm-div {:de-DE "Algorithmusauswahl"
                          :en-GB "Choose Algorithm"}
   :choose-chart-type-header {:de-DE "Wähle einen Diagrammtyp aus."
                              :en-GB "Select a chart type."}
   :choose-problem-type-action {:de-DE "Problemtypauswahl"
                                :en-GB "Choose Problem Type"}
   :clean-before-new-project {:de-DE "Ein Projekt ist schon offen. Räume die Arbeitsfläche auf, um ein neues zu erstellen."
                              :en-GB "A project is already loaded. To create a new project, clean the workspace first."}
   :close {:de-DE "Schließen"
           :en-GB "Close"}
   :close-sidebar {:de-DE "Schließen"
                   :en-GB "Close"}
   :close-tooltip {:de-DE "Schließen"
                   :en-GB "Close"}
   :cluster-marker-label {:de-DE "Marker Clustern?"
                          :en-GB "Cluster Marker?"}
   :color-base-label {:de-DE "Färbung basierend auf "
                      :en-GB "Coloring based on "}
   :color-picker {:de-DE "Farbwähler"
                  :en-GB "Color picker"}
   :color-picker-1 {:de-DE "Farbe 1"
                    :en-GB "Color 1"}
   :color-picker-10 {:de-DE "Farbe 10"
                     :en-GB "Color 10"}
   :color-picker-2 {:de-DE "Farbe 2"
                    :en-GB "Color 2"}
   :color-picker-3 {:de-DE "Farbe 3"
                    :en-GB "Color 3"}
   :color-picker-4 {:de-DE "Farbe 4"
                    :en-GB "Color 4"}
   :color-picker-5 {:de-DE "Farbe 5"
                    :en-GB "Color 5"}
   :color-picker-6 {:de-DE "Farbe 6"
                    :en-GB "Color 6"}
   :color-picker-7 {:de-DE "Farbe 7"
                    :en-GB "Color 7"}
   :color-picker-8 {:de-DE "Farbe 8"
                    :en-GB "Color 8"}
   :color-picker-9 {:de-DE "Farbe 9"
                    :en-GB "Color 9"}
   :color-scale-add {:de-DE "Farbskala hinzufügen"
                     :en-GB "Add new Color Scale"}
   :color-scale-names {:de-DE "Farbskalanamen"
                       :en-GB "Color scale names"}
   :color-scale-names-name {:de-DE "Farbskalaname"
                            :en-GB "Color scale name"}
   :color-scale-picker {:de-DE "Farben Auswhal"
                        :en-GB "Color Pickers"}
   :color-settings-group {:de-DE "Färbung"
                          :en-GB "Coloring"}
   :colorscale-size-not-in-range {:de-DE "Anzahl Farben ist nicht gültig."
                                  :en-GB "Invalid number of colors."}
   :color-scale-custom-coloring-label {:de-DE "Benutzerdefinierte Einfärbung"
                                       :en-GB "Custom coloring"}
   :color-scale-custom-coloring-tooltip {:de-DE "Benutzerdefinierte Einfärbung"
                                         :en-GB "Custom coloring"}
   :color-scale-label {:de-DE "Farbskala"
                       :en-GB "Color Scale"}
   :column-label {:de-DE "Spalte"
                  :en-GB "Column"}
   :column-select-ph {:de-DE "Wähle Spalte"
                      :en-GB "Select Column"}
   :columns-label {:de-DE "Spalte(n)"
                   :en-GB "Column(s)"}
   :columns-select-ph {:de-DE "Wähle Spalten"
                       :en-GB "Select Columns"}
   :comment-author-label {:de-DE "Erstellt von"
                          :en-GB "Created by"}
   :comment-editor-label {:de-DE "Zuletzt geändert von"
                          :en-GB "Last edited by"}
   :compare-raw-data-label {:de-DE "Vergleiche Rohdaten"
                            :en-GB "Compare raw data"}
   :cond-attribute-label {:de-DE "Attribut"
                          :en-GB "attribute"}
   :cond-condition-label {:de-DE "Bedingung"
                          :en-GB "condition"}
   :cond-value-label {:de-DE "Wert"
                      :en-GB "value"}
   :condition-placeholder {:de-DE "Bedingung.."
                           :en-GB "Condition.."}
   :condition-skeleton {:de-DE "$attribute $relation $value"
                        :en-GB "$attribute $relation $value"}
   :config-copy {:de-DE "Kopieren"
                 :en-GB "Copy"}
   :config-create-topic {:de-DE "Thema erstellen"
                         :en-GB "Create Topic"}

   :config-datasources-tab-name {:de-DE "Datenquellen"
                                 :en-GB "Datasources"}
   :config-default-datasource-label {:de-DE "Datenquelle"
                                     :en-GB "Datasource"}
   :config-default-datasources-label {:de-DE "Datenquellen"
                                      :en-GB "Datasources"}
   :continue-anyway {:de-DE "Trotzdem fortfahren"
                     :en-GB "Continue anyway"}
   :existing-topics-label {:de-DE "Existierende Themen"
                           :en-GB "Existing Topics"}
   :config-add-attribute-label {:de-DE "Attribut hinzufügen"
                                :en-GB "Add attribute"}
   :config-contained-in-ds-label {:de-DE "In folgenden Datenquellen"
                                  :en-GB "Contained in Datasources"}
   :config-datasources-save-failed-msg {:de-DE "Es ist ein Fehler beim Speichern der Datenquellen aufgetreten"
                                        :en-GB "An error occured while saving the datasources"}
   :config-datasources-save-success-msg {:de-DE "Die Datenquellen wurden gespeichert"
                                         :en-GB "The datasources were saved"}
   :config-desc-label {:de-DE "Beschreibung"
                       :en-GB "Description"}
   :config-geo-attr-save-failed-msg {:de-DE "Es ist ein Fehler beim Speichern der geografischen Attribute aufgetreten"
                                     :en-GB "An error occured while saving the geographic attributes"}
   :config-geo-attr-tab-name {:de-DE "Geografische Attribute"
                              :en-GB "Geographic Attributes"}
   :config-geo-attributes-saved-msg {:de-DE "Die geografischen Attribute wurden gespeichert"
                                     :en-GB "Geographic attributes were saved"}
   :config-geomap-marker {:de-DE "Attribute"
                          :en-GB "Attributes"}
   :config-geomap-marker-general {:de-DE "Selektierte Attribute"
                                  :en-GB "Selected attributes"}
   :config-geomap-marker-tab-name {:de-DE "Marker-Informationen"
                                   :en-GB "Marker Information"}
   :config-mosaic-constraints-name {:de-DE "Streudiagramm & Einschränkungsfenster"
                                    :en-GB "Scatter plot & Constraints view"}
   :config-mosaic-groupby-name {:de-DE "Gruppierung"
                                :en-GB "Group by"}
   :config-mosaic-intersect-name {:de-DE "Schnittmenge"
                                  :en-GB "Intersection"}
   :config-mosaic-tab-name {:de-DE "Mosaik Warnungen"
                            :en-GB "Mosaic Warnings"}
   :config-mosaic-thresholds-stop {:de-DE "Ausführungsabbruch"
                                   :en-GB "Stop execution"}
   :config-mosaic-thresholds-warn {:de-DE "Benutzerwarnung"
                                   :en-GB "Warn user"}
   :config-mosaic-tooltip-label {:de-DE "Standardwert"
                                 :en-GB "Default value"}
   :config-language-label {:de-DE "Sprache"
                           :en-GB "Language"}
   :config-mapped-to-datasources-label {:de-DE "Gemappt auf folgende Datenquellen"
                                        :en-GB "Mapped to datasources"}
   :config-mapped-topics-label {:de-DE "Gemappte Themen"
                                :en-GB "Mapped Topics"}
   :config-save-topic {:de-DE "Thema sichern"
                       :en-GB "Save Topic"}
   :config-saved-topic-msg {:de-DE "Das Thema wurde gesichert"
                            :en-GB "Topic was saved"}
   :config-temp-datasources-label {:de-DE "Temporäre Datenquellen"
                                   :en-GB "Temporary Datasources"}
   :config-temp-datasource-label {:de-DE "Temporäre Datenquelle"
                                  :en-GB "Temporary Datasource"}
   :config-title-label {:de-DE "Titel"
                        :en-GB "Title"}
   :config-topic-label {:de-DE "Thema"
                        :en-GB "Topic"}
   :config-topic-already-exists {:de-DE "Thema mit dem Namen existiert schon."
                                 :en-GB "Topic with this name already exist."}
   :config-topic-save-failed-msg {:de-DE "Es ist ein Fehler beim es Themas aufgetreten"
                                  :en-GB "An error occured while saving the topic"}
   :config-topics-tab-name {:de-DE "Themen"
                            :en-GB "Topics"}
   :config-vis-edge-name {:de-DE "Kanten"
                          :en-GB "Edges"}
   :config-vis-node-name {:de-DE "Knoten"
                          :en-GB "Nodes"}
   :config-vis-tab-name {:de-DE "Visualization Warnungen"
                         :en-GB "Visualization Warnings"}
   :config-vis-thresholds-stop {:de-DE "Ausführungsabbruch"
                                :en-GB "Stop execution"}
   :config-vis-thresholds-warn {:de-DE "Benutzerwarnung"
                                :en-GB "Warn user"}
   :config-woco-general {:de-DE "Allgemeine Konfiguration"
                         :en-GB "General configuration"}
   :config-woco-overlay {:de-DE "Banner"
                         :en-GB "Overlay"}
   :config-woco-tab-name {:de-DE "Arbeitskontext"
                          :en-GB "Working Context"}
   :confimrm-del-details {:de-DE "Dies entfernt auch alle dazugehörigen Benachrichtigungen."
                          :en-GB "This will also remove all notifications based on this alert."}
   :confirm-back-message {:de-DE "Sollen die Änderungen verworfen werden?"
                          :en-GB "Discard your changes?"}
   :confirm-back-title {:de-DE "Änderungen verwerfen"
                        :en-GB "Discard Changes"}
   :confirm-back-yes {:de-DE "Verwerfen"
                      :en-GB "Discard"}
   :confirm-del-message {:de-DE "Alarm entfernen?"
                         :en-GB "Delete this alert?"}
   :confirm-del-title {:de-DE "Alarm löschen"
                       :en-GB "Delete Alert"}
   :confirm-delete-dialog-infos {:de-DE "Das Projekt wird für alle Nutzer dauerhaft gelöscht."
                                 :en-GB "The project will be deleted for all users permanently."}
   :confirm-delete-dialog-question-config {:de-DE "Die Konfiguration löschen?"
                                           :en-GB "Do you want to delete the current configuration?"}
   :confirm-delete-dialog-title-config {:de-DE "Konfiguration löschen?"
                                        :en-GB "Delete config?"}
   :confirm-delete-dialog-question-project {:de-DE "Das Projekt löschen?"
                                            :en-GB "Do you want to delete the project?"}
   :confirm-delete-dialog-title-project {:de-DE "Projekt löschen?"
                                         :en-GB "Delete project?"}
   :confirm-delete-dialog-question-indicator {:de-DE "Den Indikator löschen?"
                                              :en-GB "Do you want to delete the indicator?"}
   :confirm-delete-dialog-title-indicator {:de-DE "Indikator löschen?"
                                           :en-GB "Delete indicator?"}
   :confirm-description-dialog-infos {:de-DE "Das Umbenennen der Beschreibung ist nur möglich, wenn das Projekt nicht geladen ist."
                                      :en-GB "Changing the description is only possible if the project is not loaded or if it is opened by you."}
   :confirm-description-dialog-question {:de-DE "Du kannst die Beschreibung nicht umbenennen, wenn es geöffnet ist."
                                         :en-GB "You cannot change the project's description while it is open."}
   :confirm-description-dialog-title {:de-DE "Beschreibung bearbeiten"
                                      :en-GB "Change description"}
   :confirm-dialog-load-project {:de-DE "Du hast gerade gearbeitet.\nMöchtest du es im Projekt speichern?"
                                 :en-GB "You have some work in progress.\nWould you like to save it as a project?"}
   :confirm-dialog-no {:de-DE "Nein"
                       :en-GB "No"}
   :confirm-dialog-yes {:de-DE "Ja"
                        :en-GB "Yes"}
   :confirm-remove-dialog-infos {:de-DE "Anschließend kannst du das Projekt über die Eingabe des Titels (mind. 3 Buchstaben) in Projektsuchleiste wieder öffnen und der Projektübersicht hinzufügen."
                                 :en-GB "You can then open the project again by entering the title (min. 3 letters) in the project search bar and add it to the project overview."}
   :confirm-remove-dialog-question {:de-DE "Möchtest du das Projekt wirklich aus der Projektübersicht entfernen?"
                                    :en-GB "Do you really want to remove the project from the project overview?"}
   :confirm-remove-dialog-title {:de-DE "Projekt entfernen"
                                 :en-GB "Remove project"}
   :confirm-title-dialog-infos {:de-DE "Das Umbenennen des Projekts ist nur möglich, wenn das Projekt nicht geladen ist."
                                :en-GB "Renaming the project is only possible if the project is not loaded."}
   :confirm-title-dialog-question {:de-DE "Du kannst das Projekt nicht umbenennen, wenn es geöffnet ist."
                                   :en-GB "You cannot rename the project while it is open."}
   :confirm-title-dialog-title {:de-DE "Titel umbenennen"
                                :en-GB "Rename title"}
   :constant-value-error {:de-DE "Werte eines Attributs müssen sich unterscheiden. Die Nutzung konstanter Werte ist nicht valide."
                          :en-GB "Values of one attribute must differ. The usage of constant values is not valid."}
   :constraints-apply-button-label {:de-DE "Anwenden"
                                    :en-GB "Apply"}
   :constraints-attribute-select-hint {:de-DE "Wähle aus den vorhandenen Attributen aus, um Beschränkungen für dieses Attributes einzustellen"
                                       :en-GB "Choose from available attributes to adjust attribute constraints."}
   :constraints-attributes-title {:de-DE "Attribute"
                                  :en-GB "Attributes"}
   :constraints-close-button-label {:de-DE "Schließen"
                                    :en-GB "Close"}
   :constraints-inactive-tooltip {:de-DE "Deaktiviert (siehe Einstellungen)"
                                  :en-GB "Deactivated (see settings)"}
   :constraints-invalid-message-general {:de-DE "Die gewählten Einschränkungen sind nicht in den Daten vorhanden."
                                         :en-GB "Data does not contain constraints."}
   :constraints-invalid-message-num-1 {:de-DE "Der eingeschränkte Wert "
                                       :en-GB "Filter value "}
   :constraints-invalid-message-num-2 {:de-DE " ist außerhalb der Grenzen.\n"
                                       :en-GB " is out of bounds.\n"}
   :constraints-invalid-message-num-3 {:de-DE "Die Einschränkungen sollte nicht "
                                       :en-GB "The filter should not exceed "}
   :constraints-invalid-message-num-4 {:de-DE "\nüberschreiten, beträgt allerdings "
                                       :en-GB ",\nbut is "}
   :constraints-invalid-message-resolve {:de-DE "Beheb dieses Problem,\nindem du die Einschränkungen entfernst\n und erneut hinzufügst oder\n einen geeigneten Datensatz auswählst."
                                         :en-GB "\nResolve this issue by removing and\nadding the filter again\nor select a suitable data set."}
   :constraints-invalid-tooltip {:de-DE "Invalider Filter"
                                 :en-GB "Invalid constraints"}
   :constraints-limit-reached-tooltip {:de-DE "Zu viele Daten für Filterung"
                                       :en-GB "Too much data prevents using the filter"}
   :constraints-remove-button-label {:de-DE "Filter entfernen"
                                     :en-GB "Remove filter"}
   :constraints-year {:de-DE "Jahr"
                      :en-GB "year"}
   :contains-no-seasonality {:de-DE "Die Daten weisen keine Saisonalität auf."
                             :en-GB "The data contains no seasonality."}
   :contains-no-trend {:de-DE "Die Daten enthalten keinen Trend."
                       :en-GB "No trend was found within the data."}
   :contains-no-white-noise {:de-DE "Die Daten enthalten kein weißes Rauschen."
                             :en-GB "The data contains no white noise."}
   :contains-seasonality {:de-DE "Die Daten weisen eine Saisonalität auf."
                          :en-GB "The data contains seasonality."}
   :contains-trend {:de-DE "Die Daten enthalten einen Trend. "
                    :en-GB "The data contains a trend."}
   :contains-white-noise {:de-DE "Die Daten enthalten weißes Rauschen."
                          :en-GB "The data contains white noise."}
   :context-menu-tooltip {:de-DE "Kontextmenü"
                          :en-GB "Context menu"}
   :context-missmatch {:de-DE "Reihe <p> hat den falschen Typ"
                       :en-GB "Row <p> has a wrong type"}
   :contextmenu-canvas-comment {:de-DE "Kommentieren"
                                :en-GB "Comment"}
   :contextmenu-grid {:de-DE "Raster"
                      :en-GB "Grid"}
   :contextmenu-operations-aggregate-average {:de-DE "Mittelwert"
                                              :en-GB "Average"}
   :contextmenu-operations-aggregate-max {:de-DE "Maximum"
                                          :en-GB "Max"}
   :contextmenu-operations-aggregate-min {:de-DE "Minimum"
                                          :en-GB "Min"}
   :contextmenu-operations-aggregate-sum {:de-DE "Summe"
                                          :en-GB "Sum"}
   :contextmenu-operations-couple-by {:de-DE "Zeitleisten koppeln nach"
                                      :en-GB "Couple timelines by"}
   :contextmenu-operations-difference {:de-DE "Differenz"
                                       :en-GB "Difference"}
   :contextmenu-operations-group-label {:de-DE "Operationen"
                                        :en-GB "Operations"}
   :contextmenu-operations-intersect {:de-DE "Schnittmenge"
                                      :en-GB "Intersect"}
   :contextmenu-operations-move {:de-DE "Bewegen"
                                 :en-GB "Move"}
   :contextmenu-operations-override {:de-DE "Daten ersetzen"
                                     :en-GB "Replace data"}
   :contextmenu-operations-replace-data {:de-DE "Daten ersetzen"
                                         :en-GB "Replace data"}
   :contextmenu-operations-sort-by-event-count {:de-DE "Eventanzahl"
                                                :en-GB "No. of Events"}
   :contextmenu-operations-sort-by-group-name {:de-DE "Gruppenname"
                                               :en-GB "Group Name"}
   :contextmenu-operations-symdifference {:de-DE "Symmetrische Differenz"
                                          :en-GB "Symmetric Difference"}
   :contextmenu-operations-union {:de-DE "Vereinigung"
                                  :en-GB "Union"}
   :contextmenu-scatter-plot {:de-DE "Streudiagramm"
                              :en-GB "Scatter plot"}
   :contextmenu-top-level-close {:de-DE "Schließen"
                                 :en-GB "Close"}
   :contextmenu-top-level-copy {:de-DE "Kopieren"
                                :en-GB "Copy"}
   :contextmenu-top-level-decouple {:de-DE "Entkoppeln"
                                    :en-GB "Decouple"}
   :contextmenu-top-level-filter {:de-DE "Filtern"
                                  :en-GB "Filter"}
   :contextmenu-top-level-groupby {:de-DE "Gruppieren"
                                   :en-GB "Group"}
   :contextmenu-top-level-groupby-1 {:de-DE "Untergruppieren"
                                     :en-GB "Subgroup"}
   :contextmenu-top-level-remove {:de-DE "Entfernen"
                                  :en-GB "Remove"}
   :contextmenu-top-level-sort-group {:de-DE "Gruppen sortieren"
                                      :en-GB "Sort groups"}
   :contextmenu-top-level-sortby {:de-DE "Sortieren"
                                  :en-GB "Sort"}
   :contextmenu-top-level-sub-sort-group {:de-DE "Untergruppen sortieren"
                                          :en-GB "Sort subgroups"}
   :contextmenu-top-level-ungroup {:de-DE "Gruppierung aufheben"
                                   :en-GB "Ungroup"}
   :contextmenu-top-level-ungroup-1 {:de-DE "Untergruppierung aufheben"
                                     :en-GB "Ungroup subgroups"}
   :contextmenu-view-group-label {:de-DE "Darstellung"
                                  :en-GB "View"}
   :continue-btn-title {:de-DE "OK"
                        :en-GB "Continue"}
   :continues-max {:de-DE "Maximaler kontinuierlicher Wert"
                   :en-GB "Continues max value"}
   :continues-method {:de-DE "Methode kontinuierliche Werte"
                      :en-GB "Continues method"}
   :continues-min {:de-DE "Minimaler kontinuierlicher Wert"
                   :en-GB "Continues min value"}
   :continues-value {:de-DE "Kontinuierlicher Wert"
                     :en-GB "Continues value"}
   :copy-config-not-possible-dialog {:de-DE "Kopieren erst möglich nach dem speichern der Konfig %s."
                                     :en-GB "Copy is not possible until the new config %s is saved."}
   :copy-config-not-possible-dialog-title {:de-DE "Kopieren nicht möglich"
                                           :en-GB "Copy is not possible"}
   :copy-label {:de-DE "Kopieren"
                :en-GB "Copy"}
   :copy-link-to-clipboard-button {:de-DE "Kopieren"
                                   :en-GB "Copy"}
   :cosine {:de-DE "Cosine-Distanz"
            :en-GB "Cosine distance"}
   :countries-dropdown-placeholder {:de-DE "Land auswählen"
                                    :en-GB "Select country"}
   :countries-label {:de-DE "Länder"
                     :en-GB "Countries"}
   :country {:de-DE "Country"
             :en-GB "Country"}
   :country-mapped-enable-label {:de-DE "Aktivieren"
                                 :en-GB "Enable"}
   :country-mapped-name {:de-DE "Land (zugeordnet)"
                         :en-GB "Country (Mapped)"}
   :country-orig-name {:de-DE "Land (Upload)"
                       :en-GB "Country (Upload)"}
   :country-required {:de-DE "Ein Kontext vom Typ country ist erforderlich"
                      :en-GB "Context with type country is required"}
   :create-dashboard-label {:de-DE "Dashboard erstellen"
                            :en-GB "Create Dashboard"}
   :create-label {:de-DE "Neuer Alarm"
                  :en-GB "New Alert"}
   :create-new-indicator {:de-DE "Neuer Indikator"
                          :en-GB "Create New Indicator"}
   :create-project-cancel-button {:de-DE "Abbrechen"
                                  :en-GB "Cancel"}
   :create-project-create-button {:de-DE "Projekt erstellen"
                                  :en-GB "Create project"}
   :create-project-description {:de-DE "Beschreibung:"
                                :en-GB "Description:"}
   :create-project-dialog-title {:de-DE "Projekttitel und Beschreibung"
                                 :en-GB "Project title and description"}
   :create-project-new-project {:de-DE "Neues Projekt"
                                :en-GB "New project"}
   :create-project-selection-no-results {:de-DE "Keine Fenster"
                                         :en-GB "No windows"}
   :create-project-selection-placeholder {:de-DE "Selektierte Fenster"
                                          :en-GB "Selected windows"}
   :create-project-title {:de-DE "Titel:"
                          :en-GB "Title:"}
   :create-project-title-placeholder {:de-DE "Erforderlich"
                                      :en-GB "Required"}
   :create-project-windows {:de-DE "Fenster:"
                            :en-GB "Windows:"}
   :create-report-label {:de-DE "Report erstellen"
                         :en-GB "Create Report"}
   :created-by-me-label {:de-DE "Von Mir Erstellt"
                         :en-GB "Created By Me"}
   :creates-new-ir-tooltip {:de-DE "Ersetzt initialen Informationsraum, Analyse-Informationsräume bleiben erhalten"
                            :en-GB "Replaces initial information room, analysis information rooms remain unchanged"}
   :creation-error-tip {:de-DE "Wenn dieses Problem weiterhin bestehen bleibt, wende dich an einen Administrator. Wahrscheinlich ist ein Dienst nicht verfügbar."
                        :en-GB "If this issue continues, please contact an administrator. A service is probably not available."}
   :credits-created-by {:de-DE "Erstellt von: "
                        :en-GB "Created by: "}
   :created-by-me {:de-DE "Erstellt von mir"
                   :en-GB "Created by me"}
   :shared-with-me {:de-DE "Geteilt mit mir"
                    :en-GB "Shared with me"}
   :all-projects-label {:de-DE "Alle Projekte"
                        :en-GB "All projects"}
   :credits-shared-from {:de-DE " | Geteilt von: %s"
                         :en-GB " | Shared by: %s"}
   :shared-by {:de-DE "Geteilt von: %s"
               :en-GB "Shared by: %s"}
   :css {:de-DE "CSS"
         :en-GB "CSS"}
   :custom-attribute {:de-DE "erweitertes Attribut"
                      :en-GB "custom attribute"}
   :custom-def-info {:de-DE "Als erweitertes Attribut definiert."
                     :en-GB "Defined as a custom attribute."}
   :custom-type-label {:de-DE "Eigener Typ?"
                       :en-GB "Custom type?"}
   :custom-value-label {:de-DE "Fester Wert?"
                        :en-GB "Custom value?"}
   :cyclical-coordinate-descent {:de-DE "Cyclical coordinate descent"
                                 :en-GB "Cyclical coordinate descent"}
   :d {:de-DE "D"
       :en-GB "D"}
   :d-info {:de-DE "Negativ: automatische Berechnung\nRest: Wert wird für Berechnung genutzt"
            :en-GB "Negative: calculated automatic\nOther: use as first-differencing order"}
   :explorama-platform {:de-DE "In der Anwendung"
                        :en-GB "Within the application"}
   :daily {:de-DE "Täglich"
           :en-GB "Daily"}
   :danger-close-tooltip {:de-DE "Fenster kann erst geschlossen werden, wenn die Operation vollständig durchgeführt wurde!"
                          :en-GB "Closing the window may lead to a data loss, while an operation is still running!"}
   :dashboard-label {:de-DE "Dashboard"
                     :en-GB "Dashboard"}
   :dashboards-label {:de-DE "Dashboards"
                      :en-GB "Dashboards"}
   :dashboard-name-exists {:de-DE "Ein Dashboard mit dem Namen existiert schon."
                           :en-GB "A dashboard with this name already exists."}
   :data-atlas-grp-rights-and-roles {:de-DE "Datenatlas"
                                     :en-GB "Data Atlas"}
   :data-atlas-name {:de-DE "Datenatlas"
                     :en-GB "Data Atlas"}
   :data-atlas-rights-and-roles {:de-DE "Rechte und Rollenverwaltung"
                                 :en-GB "Rights & roles Management"}
   :data-attributes {:de-DE "Datenattribute"
                     :en-GB "Data attributes"}
   :data-management-label {:de-DE "Datenmanagement"
                           :en-GB "Data Management"}
   :data-manager-label {:de-DE "Datenmanagement"
                        :en-GB "Data Management"}
   :data-preview-missing-data-text {:de-DE "Lade eine Datei hoch, um eine Vorschau deiner Daten zu bekommen."
                                    :en-GB "Upload a file to see a preview of your data."}
   :data-provisioning {:de-DE "Temporäre Datenimport"
                       :en-GB "Temporary Data Import"}
   :data-provisioning-grp-rights-and-roles {:de-DE "Temporärer Datenimport"
                                            :en-GB "Temporary Data Import"}
   :data-provisioning-rights-and-roles {:de-DE "Rechte und Rollenverwaltung"
                                        :en-GB "Rights & roles Management"}
   :data-section-title {:de-DE "Daten"
                        :en-GB "Data"}
   :data-source-detail-label {:de-DE "Datenquelle"
                              :en-GB "Datasource"}
   :data-source-frame-title {:de-DE "Ursprungsfenster"
                             :en-GB "Source window"}
   :data-source-list-label {:de-DE "Datenquellen"
                            :en-GB "Datasources"}
   :data-test-button {:de-DE "Datentest ausführen"
                      :en-GB "Run Test"}
   :data-tests {:de-DE "Datentests"
                :en-GB "Data tests"}
   :data-tests-desc {:de-DE "Testet die Eingabedaten auf verschiedene Eigenschaften."
                     :en-GB "Tests whether or not the input data hat various attributes."}
   :data-type-label {:de-DE "Datentyp"
                     :en-GB "Datatype"}
   :datasource-dropdown-placeholder {:de-DE "Datenquelle auswählen"
                                     :en-GB "Select datasource"}
   :datasource-label {:de-DE "Datenquelle"
                      :en-GB "Datasource"}
   :datasource-name-input-hint {:de-DE "Ist der Name einzigartig über alle bereits importierten temporären Daten, so wird eine neue Datenquelle angelegt. Wenn der Name bereits existiert, dann werden bereits importierte temporäre Daten ergänzt oder verändert. Bereits importierte temporäre Datenquellen können im Bereich Datenquellenverwaltung eingesehen und gelöscht werden."
                                :en-GB "If a temporary datasource with this name already exists, then the existing data will be extended or modified, otherwise a new datasource will be created. Existing temporary datasources can be found and deleted in the Datasource Management section."}
   :datasource-url-input-hint {:de-DE "Eine Referenz zum Datenquellenursprung. In der Regel eine URL (z.B. bei offenen Datenquellen). Falls keine URL vorliegt, kann hier auch ein kleiner textueller Vermerk zum Ursprung gemacht werden."
                               :en-GB "A reference to the origin of the data. This is usually a URL (e.g., in the case of open data). If no URL is available, a short description can be used."}
   :date {:de-DE "Datum"
          :en-GB "date"}
   :date-format-missmatch {:de-DE "Reihe <p> Format ist fehlerhaft"
                           :en-GB "Row <p> has a wrong format"}
   :date-input-hint {:de-DE "Unter Erweiterte Attributdefinitionen über die Checkbox „aus Spalten bilden“ kann jeweils eine Spalte für das Jahr, Monat und/oder Tag angegeben werden. Das Format ist dann nicht mehr notwendig."
                     :en-GB "Under custom attributes by ticking the checkbox \"Build from columns\", separate columns can be specified for year, month, and/or day. In this case, the date format does not have to be specified."}
   :date-pattern-label {:de-DE "Muster"
                        :en-GB "Pattern"}
   :date-pattern-placeholder {:de-DE "z.B. DD.MM.YYYY"
                              :en-GB "e.g., YYYY-MM-DD"}
   :day {:de-DE "Tag"
         :en-GB "Day"}
   :de-DE {:de-DE "Deutsch"
           :en-GB "German"}
   :deactivate-advancedmode-label {:de-DE "Normaler Modus"
                                   :en-GB "Normal mode"}
   :decimal-separator {:de-DE ","
                       :en-GB "."}
   :default-mail-text {:de-DE "Hallo,\n\nlade die PDF für %s '%s' unter folgendem Link herunter:\n%s\n\nLiebe Grüße,\n%s"
                       :en-GB "Hello,\n\ndownload the pdf for %s '%s' under:\n%s\n\nKind Regards,\n%s"}
   :degree {:de-DE "Grad (bei polynomiellem Kernel)"
            :en-GB "Degree (using a polynm. kernel)"}
   :delete-dashboard-info-text {:de-DE "Das Dashboard konnte nicht gelöscht werden."
                                :en-GB "The dashboard could not be deleted."}
   :delete-dashboard-text {:de-DE "Möchtest du das Dashboard löschen?"
                           :en-GB "Do you want to delete the dashboard?"}
   :delete-dashboard-title {:de-DE "Dashboard löschen"
                            :en-GB "Delete Dashboard"}
   :delete-done-info {:de-DE "Löschen der Datenquelle abgeschlossen"
                      :en-GB "Delete datasource done"}
   :delete-failed-message {:de-DE "Es gab einen Fehler beim Löschen der Datenquelle."
                           :en-GB "Datasource deletion failed."}
   :delete-failed-tip {:de-DE "Versuch es erneut oder lass dir von einem Admin helfen."
                       :en-GB "Try again or ask an admin to help you."}
   :delete-label {:de-DE "Löschen"
                  :en-GB "Delete"}
   :delete-layout-question {:de-DE "Möchtest du das Layout löschen?"
                            :en-GB "Do you want to delete the layout?"}
   :delete-layout-title {:de-DE "Layout Löschen"
                         :en-GB "Delete layout"}
   :delete-topic-question {:de-DE "Möchtest du das Thema löschen?"
                           :en-GB "Do you want to delete the topic?"}
   :delete-topic-title {:de-DE "Thema Löschen"
                        :en-GB "Delete topic"}
   :delete-datasource-question {:de-DE "Möchtest du die Datenquelle löschen?"
                                :en-GB "Do you want to delete the data source?"}
   :delete-datasource-title {:de-DE "Datenquelle Löschen"
                             :en-GB "Delete data source"}
   :delete-loading-message {:de-DE "Die Datenquelle wird gelöscht. Bitte warten..."
                            :en-GB "The datasource is currently being deleted. Please wait..."}
   :delete-loading-tip {:de-DE "Die Dauer ist abhängig von der Datenmenge der Datenquelle"
                        :en-GB "The duration depends on the amount of data"}
   :delete-not-possible-dialog-info {:de-DE "Das löschen dieser Konfiguration ist nicht möglich, weil es die letzte Konfiguration ist."
                                     :en-GB "You can't delete this configuration, because it is the last one."}
   :delete-not-possible-dialog-title {:de-DE "Löschen nicht möglich"
                                      :en-GB "Deletion not possible"}
   :delete-overlayer-question {:de-DE "Möchtest du den Overlayer löschen?"
                               :en-GB "Do you want to delete the overlayer?"}
   :delete-overlayer-title {:de-DE "Overlayer Löschen"
                            :en-GB "Delete overlayer"}
   :delete-report-info-text {:de-DE "Der Report konnte nicht gelöscht werden."
                             :en-GB "The report could not be deleted."}
   :delete-report-text {:de-DE "Möchtest du den Report löschen??"
                        :en-GB "Do you want to delete the report?"}
   :delete-report-title {:de-DE "Report löschen"
                         :en-GB "Delete Report"}
   :delete-role {:de-DE "Rolle löschen"
                 :en-GB "Delete role"}
   :delete-role-message {:de-DE "Möchtest du die Rolle löschen?"
                         :en-GB "Do you want to delete the role?"}
   :delete-tooltip-title {:de-DE "Löschen"
                          :en-GB "Delete"}
   :deleted-di-used-for-attribute {:de-DE "Attribut(e) aus einem entfernten Dataset verwendet. Selektiere die Attribut(e) neu."
                                   :en-GB "Used attribute(s) from removed dataset. Reselect the attributes."}
   :delimiter-label {:de-DE "Trennzeichen"
                     :en-GB "Delimiter"}
   :dependent-variable {:de-DE "Abhängige Variable"
                        :en-GB "Dependent variable"}
   :dependent-variables {:de-DE "Abhängige Variablen"
                         :en-GB "Dependent variables"}
   :desc-title {:de-DE "Name des Alarms"
                :en-GB "Alert Title"}
   :designer-add-colorscale {:de-DE "Farbskala hinzufügen"
                             :en-GB "Add color scale"}
   :designer-add-layer {:de-DE "Overlay hinzufügen"
                        :en-GB "Add Overlay"}
   :designer-add-layout {:de-DE "Layout hinzufügen"
                         :en-GB "Add layout"}
   :designer-attribute-tree {:de-DE "Attribut Referenz"
                             :en-GB "Attribute reference"}
   :designer-available-cards {:de-DE "Kartendesigns"
                              :en-GB "Card layouts"}
   :designer-available-layers {:de-DE "Verfügbare Overlayers"
                               :en-GB "Available overlayers"}
   :designer-card-layout {:de-DE "Objektkarten-Layout"
                          :en-GB "Card layout"}
   :designer-card-names {:de-DE "Kartenlayoutbezeichnung"
                         :en-GB "Card layout name"}
   :designer-card-names-countries {:de-DE "Länder"
                                   :en-GB "Countries"}
   :designer-card-names-datasource {:de-DE "Datenquelle"
                                    :en-GB "Datasource"}
   :designer-card-names-name {:de-DE "Layoutname"
                              :en-GB "Layout name"}
   :designer-card-names-orders {:de-DE "Titelreihenfolge"
                                :en-GB "Title order"}
   :designer-card-names-years {:de-DE "Jahre"
                               :en-GB "Years"}
   :designer-color-coding {:de-DE "Farbkodierung"
                           :en-GB "Color coding"}
   :designer-color-coding-aggregate-method {:de-DE "Aggregation"
                                            :en-GB "Aggregation"}
   :designer-color-coding-attribute {:de-DE "Attribut"
                                     :en-GB "Attribute"}
   :designer-color-coding-color-schema {:de-DE "Farbskala"
                                        :en-GB "Color scale"}
   :designer-colors-definition {:de-DE "Farbdefinition"
                                :en-GB "Colors"}
   :designer-colors-definition-num {:de-DE "Farbe %d"
                                    :en-GB "Color %d"}
   :designer-colorscale-color-nums {:de-DE "Anzahl an Farben"
                                    :en-GB "Number of colors"}
   :designer-colorscale-names {:de-DE "Farbskala Name"
                               :en-GB "Color scale name"}
   :designer-colorscale-names-name {:de-DE "Name"
                                    :en-GB "Name"}
   :designer-colorscale-nums {:de-DE "Farben"
                              :en-GB "Colors"}
   :designer-colorscale-save {:de-DE "Farbskala speichern"
                              :en-GB "Save color scale"}
   :designer-colorscale-title {:de-DE "Farbskala"
                               :en-GB "Color scale"}
   :designer-discard-changes {:de-DE "Änderungen zurücksetzen"
                              :en-GB "Revert changes"}
   :designer-discard-not-possible-no-change {:de-DE "Keine Änderung vorgenommen."
                                             :en-GB "No changes were made."}
   :designer-discart-not-possible-colorscale-changed {:de-DE "Das Farbskala hat die Anzahl an Farben geändert. Speichern erforderlich."
                                                      :en-GB "Color scale changed the number of colors. You need to save."}
   :designer-field-assignment {:de-DE "Feldzuweisung"
                               :en-GB "Field assignment"}
   :designer-field-assignment-num {:de-DE "Feld %d"
                                   :en-GB "Field %d"}
   :designer-layer-heatmap {:de-DE "Heatmap"
                            :en-GB "Heatmap"}
   :designer-layer-heatmap-drop-down-type {:de-DE "Heatmaptyp"
                                           :en-GB "Heatmap type"}
   :designer-layer-heatmap-point-density {:de-DE "Punktdichte"
                                          :en-GB "Point-density"}
   :designer-layer-heatmap-weighted {:de-DE "Gewichtet"
                                     :en-GB "Weighted"}
   :designer-layer-heatmap-weighting-attribute {:de-DE "Gewichtungsattribut "
                                                :en-GB "Weighted attribute"}
   :designer-layer-movement {:de-DE "Strömung"
                             :en-GB "Movement"}
   :designer-layer-movement-source {:de-DE "Quelle"
                                    :en-GB "source"}
   :designer-layer-movement-target {:de-DE "Ziel"
                                    :en-GB "target"}
   :designer-layer-names {:de-DE "Overlay Name"
                          :en-GB "Overlay name"}
   :designer-layer-names-name {:de-DE "Overlay Name"
                               :en-GB "Overlay name"}
   :designer-layer-type {:de-DE "Layertyp"
                         :en-GB "Layer Type"}
   :designer-layer-type-feature {:de-DE "Fläche"
                                 :en-GB "Area"}
   :designer-layer-type-heatmap {:de-DE "Heatmap"
                                 :en-GB "Heatmap"}
   :designer-layer-type-marker {:de-DE "Marker"
                                :en-GB "Marker"}
   :designer-layer-type-movement {:de-DE "Strömung"
                                  :en-GB "Movement"}
   :designer-layer-types {:de-DE "Layertyp"
                          :en-GB "Layer Type"}
   :designer-layouter-method-average {:de-DE "Durchschnitt"
                                      :en-GB "Average"}
   :designer-layouter-method-first-color {:de-DE "Erste gefundene Farbe"
                                          :en-GB "First matching color"}
   :designer-layouter-method-last-color {:de-DE "Letzte gefundene Farbe"
                                         :en-GB "Last matching color"}
   :designer-layouter-method-max {:de-DE "Maximum"
                                  :en-GB "Maximum"}
   :designer-layouter-method-max-color {:de-DE "Meiste Events"
                                        :en-GB "Most events"}
   :designer-layouter-method-min {:de-DE "Minimum"
                                  :en-GB "Minimum"}
   :designer-layouter-method-min-color {:de-DE "Wenigste Events"
                                        :en-GB "Least events"}
   :designer-layouter-method-sum {:de-DE "Summieren"
                                  :en-GB "Sum"}
   :designer-layouter-save {:de-DE "Layout speichern"
                            :en-GB "Save Layout"}
   :designer-map-save {:de-DE "Overlay speichern"
                       :en-GB "Save Overlay"}
   :designer-remove-colorscale {:de-DE "Lösche Farbskala"
                                :en-GB "Remove color scale"}
   :designer-remove-layer {:de-DE "Overlay löschen"
                           :en-GB "Remove Overlay"}
   :designer-remove-layout {:de-DE "Layout löschen"
                            :en-GB "Remove layout"}
   :designer-title {:de-DE "Objektkarten-Designer"
                    :en-GB "Entity Card Designer"}
   :designer-value-assingment {:de-DE "Wert-/Farbzuweisung"
                               :en-GB "Value assignment"}
   :desinger-color-coding-aggregate-methode {:de-DE "Aggregierungs Methode"
                                             :en-GB "Aggregate method"}
   :details-view-loading-external-ref {:de-DE "Externe Referenz wird geladen"
                                       :en-GB "loading external reference"}
   :details-view-notes {:de-DE "Text"
                        :en-GB "Text"}
   :details-view-title {:de-DE "Detailansicht"
                        :en-GB "Details view"}
   :remove-all {:de-DE "Alle entfernen"
                :en-GB "Remove all"}
   :details-view-limit-message {:de-DE "Limit erreicht"
                                :en-GB "Limit reached"}
   :details-view-add-comparison {:de-DE "Zum Vergleich hinzufügen"
                                 :en-GB "Add to comparison"}
   :details-view-remove {:de-DE "Entfernen aus der Detailansicht"
                         :en-GB "Remove from details view"}
   :details-view-remove-comparison {:de-DE "Vom Vergleich entfernen"
                                    :en-GB "Remove from comparison"}
   :di-creation-error {:de-DE "Suchen fehlgeschlagen"
                       :en-GB "Search creation failed"}
   :difference-sign {:de-DE "Difference Sign Test"
                     :en-GB "Difference Sign Test"}
   :direct-search {:de-DE "Direktsuche"
                   :en-GB "Direct search"}
   :direct-search-attributes {:de-DE "Attribute"
                              :en-GB "Attributes"}
   :direct-search-blacklist-type {:de-DE "Negativliste"
                                  :en-GB "Blacklist"}
   :direct-search-country {:de-DE "Direktsuche: Country"
                           :en-GB "Directsearch: Country"}
   :direct-search-datasource {:de-DE "Direktsuche: Datasource"
                              :en-GB "Directsearch: Datasource"}
   :direct-search-empty {:de-DE "Nichts gefunden."
                         :en-GB "Nothing found."}
   :direct-search-filter-type {:de-DE "Auflistungstyp"
                               :en-GB "List type"}
   :direct-search-organisation {:de-DE "Direktsuche: Organisation"
                                :en-GB "Directsearch: Organisation"}
   :direct-search-selection {:de-DE "Attributselektion"
                             :en-GB "Selection of attributes"}
   :direct-search-show-all {:de-DE "Alles auswählen"
                            :en-GB "Select all"}
   :direct-search-unified {:de-DE "Direktsuche"
                           :en-GB "Direct search"}
   :direct-search-warning {:de-DE "Zu viele Selektionen"
                           :en-GB "Too many selections"}
   :direct-search-whitelist-type {:de-DE "Positivliste"
                                  :en-GB "Whitelist"}
   :direct-search-year {:de-DE "Direktsuche: Year"
                        :en-GB "Directsearch: Year"}
   :direct-visualization-desc {:de-DE "Visualisieren in:"
                               :en-GB "Visualize in:"}
   :direct-visualization-toggle-addition {:de-DE " beim Klick auf Suchen"
                                          :en-GB " by clicking search"}
   :disable-label {:de-DE "Pausieren"
                   :en-GB "Pause"}
   :disabled-number-of-events {:de-DE "Deaktiviert für Eventanzahl"
                               :en-GB "Disabled for Number of Events"}
   :disabled-cluster-checkbox-hint {:de-DE "Deaktivierung des Clustering nicht möglich aufgrund zu vieler Daten. Maximale Datenmenge: %s"
                                    :en-GB "Deactivating clustering not possible due to too much data. Maximum data: %s"}
   :distance-level {:de-DE "Distanzmethode"
                    :en-GB "Distance level"}
   :dnd-card-top {:de-DE "Positioniere deine neuen Fenster"
                  :en-GB "Position your new window(s)"}
   :dnd-card-bottom {:de-DE "Abbruch durch ESC"
                     :en-GB "Abort this process by pressing ESC"}
   :dont-show-again {:de-DE "Nicht mehr anzeigen."
                     :en-GB "Don't show again."}
   :double {:de-DE "Gleitkomma"
            :en-GB "floating point"}
   :download-tooltip {:de-DE "Download als Text"
                      :en-GB "Download as text"}
   :drag-hint {:de-DE "Ziehe eine Visualisierung hierher"
               :en-GB "Drag a visualization here"}
   :drag-info {:de-DE "Ziehe eine Suche auf dieses Fenster, um Daten zu visualisieren."
               :en-GB "Drag a search here to visualize data."}
   :drag-or-info {:de-DE " ... oder wähle eine vorheriges Modell aus:"
                  :en-GB " ... or select existing model:"}
   :drop-area-text {:de-DE "Ziehe ein Such- oder Visualisierungsfenster mit Daten hierher"
                    :en-GB "Drag and drop a search or exploration window with data here"}
   :dropdown-noresult {:de-DE "Keine Ergebnisse gefunden"
                       :en-GB "No results found"}
   :duplicate-colorscale-name {:de-DE "Farbskalaname existiert schon"
                               :en-GB "Color scale name already exists"}
   :duplicate-frame {:de-DE "Fenster kopieren"
                     :en-GB "Duplicate window"}
   :duplicate-id-message {:de-DE "Reihe <p> enthält eine doppelte ID"
                          :en-GB "Row <p> contains a duplicate ID"}
   :duplicate-indicator-name {:de-DE "Indikatorname schon vorhanden."
                              :en-GB "Duplicate Indicator Name"}
   :duplicate-layout-name {:de-DE "Layoutname existiert schon"
                           :en-GB "Overlay name already exists"}
   :duplicate-tooltip-title {:de-DE "Duplizieren"
                             :en-GB "Duplicate"}
   :eb {:de-DE "Empirical Bayes"
        :en-GB "Empirical Bayes"}
   :edge-message {:de-DE "Kantenlimit überschritten: "
                  :en-GB "Edge limit exceeded: "}
   :edges-str {:de-DE "Kanten"
               :en-GB "edges"}
   :edit-dashboard-label {:de-DE "Dashboard Bearbeiten"
                          :en-GB "Edit Dashboard"}
   :edit-label {:de-DE "Bearbeiten"
                :en-GB "Edit"}
   :edit-layout-label {:de-DE "Layout bearbeiten"
                       :en-GB "Edit Layout"}
   :edit-overlayer-label {:de-DE "Overlayer bearbeiten"
                          :en-GB "Edit Overlayer"}
   :edit-report-label {:de-DE "Report Bearbeiten"
                       :en-GB "Edit Report"}
   :email-platform {:de-DE "per E-Mail"
                    :en-GB "via e-mail"}
   :empty-data-hint {:de-DE "Es sind keine Daten verfügbar\nÄndere deine Suchauswahl oder ziehe eine andere Suche auf dieses Fenster"
                     :en-GB "No data available\nChange your search selection or drag another search here"}
   :en-GB {:de-DE "Englisch"
           :en-GB "English"}
   :enable-advancedmode-label {:de-DE "Erweiterter Modus"
                               :en-GB "Advanced mode"}
   :enable-label {:de-DE "Aktivieren"
                  :en-GB "Resume"}
   :er {:de-DE "Exponentielle Regression"
        :en-GB "Exponential Regression"}
   :er-desc {:de-DE "Die exponentielle Regression ist ein exponentielles Modell. Es erlaubt die Modellierung der exponentiellen Beziehnugen zwischen einer Variable (abhängigige Variable) und einer anderen Variable (unabhängige Variable)."
             :en-GB "Exponential regression is a exponential model. It allows to model exponential relationships between one variable (dependent variable) and one other variable (independent variable)."}
   :er-value-not-greater-than-zero {:de-DE "Es können keine Werte gleich Null und kleiner in dem Algorithmus Exponentielle Regression verwendet werden."
                                    :en-GB "Values equal to zero or smaller are not valid for the algorithm Exponential Regression"}
   :error-help-tooltip {:de-DE "Fehler sind kritisch und müssen behoben werden, um diese Daten zu importieren"
                        :en-GB "Errors are critical and must be fixed in order to import your data"}
   :error-section {:de-DE "Fehler"
                   :en-GB "Error"}
   :estimator {:de-DE "Schätzfunktion"
               :en-GB "Estimator"}
   :et {:de-DE "ET"
        :en-GB "ET"}
   :euclidean {:de-DE "Euclidean-Distanz"
               :en-GB "Euclidean distance"}
   :example-label {:de-DE "Inputdaten"
                   :en-GB "Input Data"}
   :excl.-value-label {:de-DE "bis exkl."
                       :en-GB "to excl."}
   :exclusive {:de-DE "exklusive"
               :en-GB "exclusive"}
   :exhaust {:de-DE "erschöpfend"
             :en-GB "exhaust"}
   :exit-threshold {:de-DE "Abburchschwellwert"
                    :en-GB "Exit Threshold"}
   :expire {:de-DE "Läuft $unit ab"
            :en-GB "Expires $unit"}
   :expire-day {:de-DE "Tag"
                :en-GB "day"}
   :expire-days {:de-DE "Tagen"
                 :en-GB "days"}
   :expire-today {:de-DE "im Laufe des Tages"
                  :en-GB "during the day"}
   :expires-in {:de-DE "Läuft in $num $unit ab"
                :en-GB "Expires in $num $unit"}
   :explanation-depending {:de-DE "hängt ab von "
                           :en-GB "is depending on "}
   :explanation-depending-on {:de-DE "Abhängig von "
                              :en-GB "Depending on "}
   :explanation-depending-valid {:de-DE "Nur gültig für "
                                 :en-GB "Only valid for "}
   :explanation-div {:de-DE "Erklärung"
                     :en-GB "Explanation"}
   :explanation-type {:de-DE "Typ "
                      :en-GB "Type "}
   :explanation-valid-range {:de-DE "Gültiger Wertbereich ist: "
                             :en-GB "Valid range is: "}
   :explanation-value {:de-DE " Wert "
                       :en-GB " Value "}
   :explore-explorama {:de-DE "Explorama erforschen"
                       :en-GB "Explore Explorama"}
   :exponential-model {:de-DE "Exponentielle Wertvorhersage"
                       :en-GB "Exponential Value Forecast"}
   :exponential-model-desc {:de-DE "Sagt einen exponentiellen Wert basierend auf historischen Daten voraus."
                            :en-GB "Forecast one exponential value based on historic data."}
   :export-label {:de-DE "Exportieren"
                  :en-GB "Export"}
   :expost-flag {:de-DE "Expost-Flag"
                 :en-GB "Expost flag"}
   :extrapolation {:de-DE "Extrapolation"
                   :en-GB "Extrapolation"}
   :expdb-settings-all-buckets {:de-DE "Alle Daten "
                                :en-GB "All Data"}
   :expdb-settings-export {:de-DE "Export"
                           :en-GB "Export"}
   :expdb-settings-group {:de-DE "Export/Import Daten"
                          :en-GB "Export/Import Data"}
   :expdb-settings-import {:de-DE "Import"
                           :en-GB "Import"}
   :expdb-settings-parts {:de-DE "Spezfische Daten"
                          :en-GB "Specific data"}
   :expdb-import-dialog-intro {:de-DE "Damit die Daten importiert können werden müssen die folgenden Spalten automatisch hinzugefügt werden:"
                               :en-GB "In order to fulfill the requirments followings columns are added automatically:"}
   :expdb-import-dialog-id {:de-DE "Eine ID wird für jede Zeile generiert."
                            :en-GB "The ID for each row will be automatically generated."}
   :expdb-import-dialog-date {:de-DE "Das Datum wird für jede Zeile auf das aktuelle Datum gesetzt."
                              :en-GB "The date of each row will be set to the current date."}
   :expdb-import-dialog-country {:de-DE "Das Land wird als \"unspecified\" hinzugefügt."
                                 :en-GB "The country will be set to unspecified."}
   :expdb-import-dialog-outro {:de-DE "Falls diese verhalten dem gewünschten verhalten nicht entspricht muss ein Benutzerdefiniertes Mapping verwendet werden (https://github.com/Explorama/Explorama)."
                               :en-GB "If you want to alter these columns you have to define a custom mapping (https://github.com/Explorama/Explorama)."}
   :expdb-import-dialog-title {:de-DE "Warnung"
                               :en-GB "Warning"}
   :expdb-import-dialog-lat-lon {:de-DE "Latitude und Longitude sind nicht korrekt angegeben und werden ignoriert."
                                 :en-GB "Latitude and longitude values are not provided correctly and will be ignored."}
   :expdb-import-summary-title-success {:de-DE "Import erfolgreich"
                                        :en-GB "Import successful"}
   :expdb-import-summary-title-failed {:de-DE "Import fehlgeschlagen"
                                       :en-GB "Import failed"}
   :expdb-import-summary-success {:de-DE "Der Import und die Umwandlung sind erfolgreich"
                                  :en-GB "The mapping and import was successful"}
   :expdb-import-summary-warning {:de-DE "Import erfolgreich mit Warnungen"
                                  :en-GB "Import successful with warnings"}
   :expdb-import-summary-error {:de-DE "Die Umwandlung und der import ist fehlgeschlagen."
                                :en-GB "The mapping and import failed."}
   :expdb-import-summary-import-report-new {:de-DE "Neue Events"
                                            :en-GB "New Events"}
   :expdb-import-summary-mapping-report-ignored {:de-DE "Bei der umwandlung wurde Events ignoriert"
                                                 :en-GB "The mapping ignored events"}
   :expdb-import-summary-mapping-report-ignored-download {:de-DE "Download ignorierte Events"
                                                          :en-GB "Downoad mapping error log"}
   :expdb-import-summary-procced {:de-DE "Möchten sie fortfahren?"
                                  :en-GB "Do you want to proceed?"}
   :expdb-import-table-column-name {:de-DE "Spaltenname"
                                    :en-GB "Column-name"}
   :expdb-import-table-include {:de-DE "Inkludieren"
                                :en-GB "Include"}
   :expdb-import-table-label {:de-DE "Label"
                              :en-GB "Label"}
   :expdb-import-table-context {:de-DE "Kontext"
                                :en-GB "Context"}
   :expdb-import-table-fact {:de-DE "Fakt" ;Fact
                             :en-GB "Fact"}
   :expdb-import-table-date {:de-DE "Datum"
                             :en-GB "Date"}
   :expdb-import-table-text {:de-DE "Text"
                             :en-GB "Text"}
   :expdb-import-table-location {:de-DE "Standort"
                                 :en-GB "Location"}
   :expdb-import-table-global-id {:de-DE "ID"
                                  :en-GB "ID"}
   :expdb-import-table-attribute {:de-DE "Attributname"
                                  :en-GB "Attribute Name"}
   :expdb-import-table-string {:de-DE "String"
                               :en-GB "String"}
   :expdb-import-table-integer {:de-DE "Integer"
                                :en-GB "Integer"}
   :expdb-import-table-decimal {:de-DE "Decimal"
                                :en-GB "Decimal"}
   :expdb-import-table-lat {:de-DE "Breitengrad"
                            :en-GB "Latitude"}
   :expdb-import-table-lon {:de-DE "Längengrad"
                            :en-GB "Longitude"}
   :expdb-import-table-position {:de-DE "Position"
                                 :en-GB "Position"}
   :expdb-import-table-type {:de-DE "Typ"
                             :en-GB "Type"}
   :expdb-import-misc-download-mapping {:de-DE "Download generiertes Mapping"
                                        :en-GB "Download generated mapping"}
   :expdb-import-misc-upload-mapping {:de-DE "Mapping Hochladen"
                                      :en-GB "Upload mapping"}
   :expdb-import-misc-datasource {:de-DE "Datenquellenname"
                                  :en-GB "Datasource name"}
   :expdb-import-misc-import {:de-DE "Importieren"
                              :en-GB "Import"}
   :expdb-import-misc-cancel {:de-DE "Abbrechen"
                              :en-GB "Cancel"}
   :expdb-import-done-title {:de-DE "Import abgeschlossen"
                             :en-GB "Import done"}
   :expdb-import-done-success {:de-DE "Import erfolgreich - Neue Events:"
                               :en-GB "Import Successful - New Events:"}
   :expdb-import-done-error {:de-DE "Ein unbekannter Fehler ist während des Imports aufgetreten, überprüfe die Logs für mehr informationen."
                             :en-GB "Unknown error occured during import, check the logs for more information."}
   :expdb-import-uploaded-hint {:de-DE "Du hast ein Mapping hochgeladen. Hochgeladene Mappings können nicht visualisiert werden."
                                :en-GB "You have uploaded a mapping. Uploaded mappings can not be visualized."}
   :feature {:de-DE "Merkmal"
             :en-GB "Feature"}
   :feature-configuration {:de-DE "Merkmalstyp"
                           :en-GB "Feature configuration"}
   :feature-grouping-select {:de-DE "Datenattribut"
                             :en-GB "Data attribute"}
   :feature-layer-group {:de-DE "Farbgrenzen"
                         :en-GB "Area color Settings"}
   :feature-layer-select {:de-DE "Grenzen"
                          :en-GB "Border"}
   :field-label {:de-DE "Feld"
                 :en-GB "Field"}
   :field-settings-group {:de-DE "Felder"
                          :en-GB "Fields"}
   :file-size-error {:de-DE "Hochladen gescheitert"
                     :en-GB "Upload failed"}
   :file-size-error-tip {:de-DE "Maximale zulässige Dateigröße für diesen Dateityp ist"
                         :en-GB "Maximum file size for this file type is"}
   :file-type-label {:de-DE "Dateityp"
                     :en-GB "File Type"}
   :file-type-select-ph {:de-DE "Wähle Dateityp"
                         :en-GB "Select File Type"}
   :filtered-tooltip-text {:de-DE "Events gefiltert"
                           :en-GB "Events filtered"}
   :fist-k {:de-DE "Ersten k Beobachtungen"
            :en-GB "First k observations"}
   :fit-intercept {:de-DE "Achsenabschnitt"
                   :en-GB "Fit Intercept"}
   :fitting {:de-DE "Passend zu"
             :en-GB "Fitting"}
   :fixed-value-label {:de-DE "Fester Wert"
                       :en-GB "Fixed Value"}
   :flush-failed-message {:de-DE "Fertigstellen fehlgeschlagen. Bitte überprüfe dein Datenmapping und versuche es erneut."
                          :en-GB "Finalize failed. Please check your data mapping and try again."}
   :flush-loading-message {:de-DE "Finalisiere. Bitte warten..."
                           :en-GB "Finalize. Please wait..."}
   :flush-loading-tip {:de-DE "Die Dauer ist von der Datenmenge abhängig"
                       :en-GB "The duration depends on the amount of data"}
   :flush-waiting-message {:de-DE "Finalisiere. Bitte warten..."
                           :en-GB "Finalize. Please wait..."}
   :flush-waiting-tip {:de-DE "Es kann eine kurze Zeit dauern, bis die Daten nach diesem Schritt aktualisiert werden"
                       :en-GB "It may take a short time until data is updated after this step"}
   :flyout-filter-placeholder {:de-DE "Modell suchen..."
                               :en-GB "Find..."}
   :flyout-title {:de-DE "Gespeicherte Modelle"
                  :en-GB "Saved Models"}
   :forecast {:de-DE "Vorhersage"
              :en-GB "Forecast"}
   :format-select-ph {:de-DE "Wähle Format"
                      :en-GB "Select Format"}
   :forward {:de-DE "Vorwärts"
             :en-GB "Forward"}
   :frame-annotation {:de-DE "Fensternotizen"
                      :en-GB "Window annotation"}
   :frame-closed {:de-DE "Fenster wurde geschlossen"
                  :en-GB "Window closed"}
   :frame-creation-alert {:de-DE "Von dieser Komponente ist nur ein Fenster erlaubt"
                          :en-GB "Only one window of this component is allowed"}
   :fridays {:de-DE "Freitags"
             :en-GB "Fridays"}
   :from-column-label {:de-DE "Ab Spalte"
                       :en-GB "from column"}
   :from-count {:de-DE "von"
                :en-GB "from"}
   :from-to-chars-label {:de-DE "Ab - bis Zeichen"
                         :en-GB "from - to characters"}
   :full-access-share {:de-DE "Vollzugriff"
                       :en-GB "Full access"}
   :full-access-share-groups {:de-DE "Vollzugriff (Gruppe)"
                              :en-GB "Full access (group)"}
   :full-access-share-user {:de-DE "Vollzugriff (Nutzer)"
                            :en-GB "Full access (user)"}
   :fulltext-hint {:de-DE "Wähle eine Zeile aus, um dessen Volltext anzuzeigen"
                   :en-GB "Select a row to view the fulltext"}
   :fulltext-label {:de-DE "Volltext"
                    :en-GB "Fulltext"}
   :function {:de-DE "Funktion"
              :en-GB "Function"}
   :future-data-auto-hint {:de-DE "Werte werden mittels linearer Regression ermittelt"
                           :en-GB "Values will be determined through linear regression"}
   :future-data-hint {:de-DE "Trage die Werte im Abschnitt Zukünftige Werte ein"
                      :en-GB "Have a look at the independent data window"}
   :future-data-manual-hint {:de-DE "Bitte trage die gewünschten Eerte in dem Zukünftige Werte (Manuelle Eingabe) abschnitt ein."
                             :en-GB "Please fill out the values in the Forecast values (Manual Input) section."}
   :future-data-section {:de-DE "Zukünftige Werte (Manuelle Eingabe)"
                         :en-GB "Forecast values (Manual Input)"}
   :future-max {:de-DE "Prädiktionstart"
                :en-GB "Forecast start"}
   :future-only {:de-DE "Nur Zukunft"
                 :en-GB "Future only"}
   :future-values {:de-DE "Prädiktionswerte"
                   :en-GB "Forecast values"}
   :general-missmatch-message {:de-DE "Reihe <p> ist fehlerhaft"
                               :en-GB "Row <p> has an error"}
   :general-settings-group {:de-DE "Allgemein"
                            :en-GB "General"}
   :generating-training-data-running {:de-DE "Trainingsdaten werden berechnet."
                                      :en-GB "Calculating input data."}
   :geomap-color-coding-attribute {:de-DE "Attribut"
                                   :en-GB "Attribute"}
   :geomap-grp-rights-and-roles {:de-DE "Geomap"
                                 :en-GB "Geomap"}
   :geomap-layer-heatmap-drop-down-type {:de-DE "Heatmaptyp"
                                         :en-GB "Heatmap type"}
   :geomap-layer-heatmap-point-density {:de-DE "Punktdichte"
                                        :en-GB "Point-density"}
   :geomap-layer-heatmap-weighted {:de-DE "Gewichtet"
                                   :en-GB "Weighted"}
   :geomap-layer-movement-source {:de-DE "Quelle"
                                  :en-GB "source"}
   :geomap-layer-movement-target {:de-DE "Ziel"
                                  :en-GB "target"}
   :geomap-layouts-grp-rights-and-roles {:de-DE "Geomap Overlayer"
                                         :en-GB "Geomap Overlay"}
   :geomap-protocol-action-base-layer-change {:de-DE "Aktives Basis Layout"
                                              :en-GB "Active base layer"}
   :geomap-protocol-action-marker-settings {:de-DE "Aktive Marker Layouts"
                                            :en-GB "Active marker layouts"}
   :geomap-protocol-action-overlayer-active {:de-DE "Aktive Formen"
                                             :en-GB "Active shapes"}
   :geomap-protocol-action-feature-layers-active {:de-DE "Aktive Overlayer"
                                                  :en-GB "Active Overlayer"}
   :geomap-protocol-action-load-data {:de-DE "Neue Daten"
                                      :en-GB "New data"}
   :geomap-protocol-action-copy-frame {:de-DE "Fenster kopiert"
                                       :en-GB "Copied window"}
   :geomap-rights-and-roles {:de-DE "Rechte- und Rollenverwaltung"
                             :en-GB "Rights and roles management"}
   :geomap-too-much-data-message {:de-DE "Es wurden zu viele Daten ausgewählt, es werden keine Events angezeigt. Overlays können trotzdem aktiviert werden."
                                  :en-GB "Too much data selected, no markers will be shown. Layer can still be activated."}
   :geomap-too-much-data-title {:de-DE "Zu viele Daten!"
                                :en-GB "Too much data selected!"}
   :geor {:de-DE "Geometrische Regression"
          :en-GB "Geometric Regression"}
   :geor-desc {:de-DE "Die geometrische Regression ist ein Ansatz zur Modellierung der Beziehung zwischen einer Variable (abhängige Variable) und einer anderen Variable (unabhängige Variable).
                     Bei der geometrischen Regression werden Daten mithilfe geometrischer Funktionen modelliert und unbekannte Modellparameter aus den Daten geschätzt."
               :en-GB "Geometric regression is an approach used to model the relationship between one variable (dependent variable) and one variable (independent variable).
                     In geometric regression, data is modeled using geometric functions, and unknown model parameters are estimated from the data."}
   :global-loadingscreen-msg {:de-DE "Bitte warten..."
                              :en-GB "Please wait..."}
   :global-loadingscreen-tip {:de-DE "Screenshot wird erstellt"
                              :en-GB "Creating your Screenshot"}
   :global-redo-not-possible-multi {:de-DE "Einige Operationen können nicht erneut ausgeführt werden"
                                    :en-GB "Some operations cannot be performed again"}
   :global-redo-not-possible-single {:de-DE "Eine Operationen kann nicht erneut ausgeführt werden"
                                     :en-GB "An operation cannot be performed again"}
   :mosaic-color-scale {:de-DE "Mosaik Farbskala"
                        :en-GB "Mosaic Color Scale"}
   :mosaic-features-group-by {:de-DE "Gruppieren nach"
                              :en-GB "Group by"}
   :mosaic-features-intersect-by {:de-DE "Schnittmenge nach"
                                  :en-GB "Intersect by"}
   :mosaic-features-sort-by {:de-DE "Sortieren nach"
                             :en-GB "Sort by"}
   :mosaic-grp-rights-and-roles {:de-DE "Mosaik"
                                 :en-GB "Mosaic"}
   :mosaic-layouts-grp-rights-and-roles {:de-DE "Mosaik Layouts"
                                         :en-GB "Mosaic Layouts"}
   :mosaic-operation-group-by {:de-DE "Gruppieren nach"
                               :en-GB "Group by"}
   :mosaic-operation-sort-by {:de-DE "Sortieren nach"
                              :en-GB "Sort by"}
   :mosaic-protocol-action-activate-scatter {:de-DE "Streudiagramm"
                                             :en-GB "Scatterplot"}
   :mosaic-protocol-action-canvas-state {:de-DE "Zoom/Pan"
                                         :en-GB "Zoom/Pan"}
   :mosaic-protocol-action-change-layout {:de-DE "Aktuelle Layouts:"
                                          :en-GB "Current layouts:"}
   :mosaic-protocol-action-close {:de-DE "Informationsraum Schließen"
                                  :en-GB "Close information space"}
   :mosaic-protocol-action-constraint-clear {:de-DE "Lokalen Filter entfernen"
                                             :en-GB "Clear local filter"}
   :mosaic-protocol-action-copy {:de-DE "Informationsraum kopieren"
                                 :en-GB "copy information space"}
   :mosaic-protocol-action-copy-card {:de-DE "Event kopieren"
                                      :en-GB "copy card"}
   :mosaic-protocol-action-copy-group {:de-DE "Gruppe Kopieren"
                                       :en-GB "copy group"}
   :mosaic-protocol-action-couple-by {:de-DE "Zeitleisten koppeln"
                                      :en-GB "Couple timelines"}
   :mosaic-protocol-action-decouple {:de-DE "Zeitleisten entkoppeln"
                                     :en-GB "decouple timelines"}
   :mosaic-protocol-action-group-by {:de-DE "Gruppieren nach"
                                     :en-GB "group-by"}
   :mosaic-protocol-action-operation {:de-DE "Mengenoperation"
                                      :en-GB "set operation"}
   :mosaic-protocol-action-remove-group {:de-DE "Gruppe löschen"
                                         :en-GB "remove group"}
   :mosaic-protocol-action-reset {:de-DE "Anordnung optimieren"
                                  :en-GB "Optimize arrangement"}
   :mosaic-protocol-action-scatter-change {:de-DE "Streudiagramm achse geändert"
                                           :en-GB "scatter axis changed"}
   :mosaic-protocol-action-scatter-change-x {:de-DE "X Achse geändert zu"
                                             :en-GB "X axis changed to"}
   :mosaic-protocol-action-scatter-change-y {:de-DE "Y Achse geändert zu"
                                             :en-GB "Y axis changed to"}
   :mosaic-protocol-action-set-grid {:de-DE "Raster"
                                     :en-GB "Grid"}
   :mosaic-protocol-action-sort-by {:de-DE "Sortieren nach"
                                    :en-GB "sort-by"}
   :mosaic-protocol-action-sort-group {:de-DE "Gruppe sortieren nach"
                                       :en-GB "sort groups by"}
   :mosaic-protocol-action-sub-group-by {:de-DE "Untergruppieren nach"
                                         :en-GB "subgroup by"}
   :mosaic-protocol-action-sub-sort-group {:de-DE "Untergruppen sortieren nach"
                                           :en-GB "sort subgroups by"}
   :mosaic-protocol-action-ungroup {:de-DE "Gruppierung aufheben"
                                    :en-GB "Ungroup"}
   :mosaic-protocol-action-ungroup-sub-group {:de-DE "Untergruppierung aufheben"
                                              :en-GB "Ungroup subgroups"}
   :mosaic-protocol-action-with-direction {:de-DE "mit der Sortierrichtung:"
                                           :en-GB "with direction"}
   :mosaic-protocol-action-with-direction-asc {:de-DE "aufsteigend"
                                               :en-GB "ascending"}
   :mosaic-protocol-action-with-direction-desc {:de-DE "absteigend"
                                                :en-GB "descending"}
   :mosaic-protocol-action-agg-event-count {:de-DE "der Eventanzahl"
                                            :en-GB "number of events"}
   :mosaic-protocol-action-agg-name {:de-DE "dem Gruppennamen"
                                     :en-GB "group name"}
   :mosaic-protocol-action-agg-layout {:de-DE "dem Layout"
                                       :en-GB "layout"}
   :mosaic-protocol-action-agg-aggregate-sum {:de-DE "Summe von"
                                              :en-GB "the sum of"}
   :mosaic-protocol-action-agg-aggregate-min {:de-DE "dem minimalen Wert von"
                                              :en-GB "the minimal value of"}
   :mosaic-protocol-action-agg-aggregate-max {:de-DE "dem maximalen Wert von"
                                              :en-GB "the maximal value of"}
   :mosaic-protocol-action-agg-aggregate-avg {:de-DE "dem Durchschnittswert von"
                                              :en-GB "the average value of"}
   :mosaic-protocol-action-set-op {:de-DE "Mengen operation"
                                   :en-GB "Set operation"}
   :mosaic-protocol-action-overwrite {:de-DE "Daten ersetzt"
                                      :en-GB "Replaced data"}
   :mosaic-protocol-action-load-data {:de-DE "Neue Daten"
                                      :en-GB "New data"}
   :mosaic-rights-and-roles {:de-DE "Rechte- und Rollenverwaltung"
                             :en-GB "Rights and roles management"}
   :mosaic-settings-choose-layout {:de-DE "Aktives Layout"
                                   :en-GB "Active layout"}
   :mosaic-settings-ok {:de-DE "ok"
                        :en-GB "OK"}
   :mosaic-tooltip-search {:de-DE "Visualisiere die Daten in Mosaik"
                           :en-GB "Visualize the data in mosaic"}
   :graph-circle-layout {:de-DE "Kreis"
                         :en-GB "Circle"}
   :graph-component-label {:de-DE "Graph"
                           :en-GB "Graph"}
   :graph-concentric-layout {:de-DE "Konzentrisch"
                             :en-GB "Concentric"}
   :graph-detail-view-label {:de-DE "Detailansicht"
                             :en-GB "Detail View"}
   :graph-edge-attribute-label {:de-DE "Kantenattribut"
                                :en-GB "Edge Attribute"}
   :graph-edges {:de-DE "Kanten: "
                 :en-GB "Edges: "}
   :graph-events {:de-DE "Events: "
                  :en-GB "Events: "}
   :graph-force-layout {:de-DE "Physikalisch"
                        :en-GB "Force-directed"}
   :graph-grid-layout {:de-DE "Raster"
                       :en-GB "Grid"}
   :graph-loading-message {:de-DE "Graph wird generiert"
                           :en-GB "Graph is generating"}
   :graph-node-attribute-label {:de-DE "Knotenattribut"
                                :en-GB "Node Attribute"}
   :graph-nodes {:de-DE "Knoten: "
                 :en-GB "Nodes: "}
   :graph-protocol-action-circle {:de-DE "Kreis"
                                  :en-GB "Circle"}
   :graph-protocol-action-concentric {:de-DE "Konzentrisch"
                                      :en-GB "Concentric"}
   :graph-protocol-action-force {:de-DE "Physikalisch"
                                 :en-GB "Force-directed"}
   :graph-protocol-action-grid {:de-DE "Raster"
                                :en-GB "Grid"}
   :graph-type-label {:de-DE "Layout"
                      :en-GB "Layout"}
   :greedy {:de-DE "Greedy"
            :en-GB "Greedy"}
   :group-by-country {:de-DE "Daten aufteilen nach Land"
                      :en-GB "Group by country"}
   :group-number {:de-DE "Gruppenanzahl"
                  :en-GB "Group number"}
   :group-number-max {:de-DE "Maximale Gruppenanzahl"
                      :en-GB "Group number maximum"}
   :group-number-min {:de-DE "Minimale Gruppenanzahl"
                      :en-GB "Group number minimum"}
   :groups-label {:de-DE "Gruppen"
                  :en-GB "Groups"}
   :guess-states {:de-DE "Werte abschätzen"
                  :en-GB "Guess states"}
   :handling-missing-div {:de-DE "Handling missing data for "}
   :handling-missing-label {:de-DE "Fehlende Werte"
                            :en-GB "Missing values"}
   :hashing {:de-DE "Hashing"
             :en-GB "Hashing"}
   :header-required {:de-DE "Mehr als eine Spalte ist erforderlich"
                     :en-GB "More than one column is required"}
   :heatmap-type-hint {:de-DE "Eine Punktdichte-Heatmap stellt die geografische Dichte von Punkten auf einer Karte dar. Eine gewichtete Heatmap wird auf Grundlage der Anzahl an Events, die das Gewichtungsattribut enthalten, und der Höhe numerischer Messwerte des Gewichtsattributs in den Daten berechnet und angezeigt, darüber hinaus hängt die Anzeige vom jeweiligen Kartenausschnitt ab."
                       :en-GB "A point density heatmap represents the geographic density of points on a map. A weighted heatmap is based on weighted attribute event count and its value, displayed relative to the other sections in the visible area."}
   :help-title {:de-DE "Hilfe"
                :en-GB "Help"}
   :hide-message-label {:de-DE "Text verstecken"
                        :en-GB "Hide message"}
   :hide-prediction-error-message-1 {:de-DE "Ein Fehler ist bei dem Löschen des Modells mit ID"
                                     :en-GB "There was an error when deleting the model with id"}
   :hide-prediction-error-message-2 {:de-DE "aufgetreten."
                                     :en-GB "was not successful."}
   :hide-prediction-error-title {:de-DE "Fehler!"
                                 :en-GB "Error!"}
   :hide-prediction-message {:de-DE "Willst du das Modell wirklich löschen?"
                             :en-GB "Do you want to delete this model?"}
   :hide-prediction-success-message-1 {:de-DE "Es wurde das Modell"
                                       :en-GB "Deletion of"}
   :hide-prediction-success-message-2 {:de-DE "erfolgreich gelöscht"
                                       :en-GB "was successful."}
   :hide-prediction-success-title {:de-DE "Erfolg gelöscht!"
                                   :en-GB "Deletion successful!"}
   :hide-prediction-title {:de-DE "Warnung!"
                           :en-GB "Warning!"}
   :hint-text-pan {:de-DE "Verschieben der Arbeitsfläche mit der mittleren oder rechten Maustase."
                   :en-GB "Pan with middle or right mouse button"}
   :hint-img-alt-pan {:de-DE "Die rechte und linke Mausetaste erlauben das Verschieben auf der Arbeitsfläche."
                      :en-GB "The right or middle mouse button allows panning on the workspace."}
   :hint-text-drag {:de-DE "Fenster können mittels Drag und Drop der linken Maustaste verschoben werden."
                    :en-GB "Drag and drop windows with left mouse button"}
   :hint-img-alt-drag {:de-DE "Die linke Maustaste erlaubt das verschieben von Fenstern."
                       :en-GB "The left mouse button allows to drag and drop windows."}
   :hint-text-notes {:de-DE "Notizen können an Fenstern haften."
                     :en-GB "Notes attach to windows"}
   :hint-img-alt-notes {:de-DE "Ablegen einer Notiz über einem Fenster sort dafür, dass das Notiz am Fenster haftet."
                        :en-GB "Positioning a note over a window makes it stick to the window"}
   :hint-text-custom {:de-DE "Das Verhalten kann in den Benutzereinstellungen angepasst werden"
                      :en-GB "You can customize the behavior in the user settings"} ;not break whitespace alt+255
   :hint-img-alt-custom {:de-DE "Die Konfiguration für das Verhalten in den Benutzereinstellungen oben rechts in der Ecke."
                         :en-GB "The configuration for the behavior is in the user setting in the top right corner."}
   :hint-text-checkbox {:de-DE "Nicht mehr anzeigen"
                        :en-GB "Do not show again"}
   :hint-text-close {:de-DE "Schließen"
                     :en-GB "Close"}
   :delete-query-title {:de-DE "Warnung!"
                        :en-GB "Warning!"}
   :delete-query-message {:de-DE "Willst du die gespeicherte Suche wirklich löschen?"
                          :en-GB "Do you want to delete this query?"}
   :icon-tooltip-organisation {:de-DE "Organisation"
                               :en-GB "Organization"}
   :icon-tooltip-location {:de-DE "Ort"
                           :en-GB "Location"}
   :icon-tooltip-notes {:de-DE "Notiz"
                        :en-GB "Note"}
   :icon-tooltip-else {:de-DE "Info"
                       :en-GB "Info"}
   :icon-tooltip-calendar {:de-DE "Kalender"
                           :en-GB "Calendar"}
   :icon-tooltip-clock {:de-DE "Uhr"
                        :en-GB "Clock"}
   :icon-tooltip-drop {:de-DE "Tropfen"
                       :en-GB "Waterdrop"}
   :icon-tooltip-globe2 {:de-DE "Globus 2"
                         :en-GB "Globe 2"}
   :icon-tooltip-health {:de-DE ""
                         :en-GB "Health"}
   :icon-tooltip-map {:de-DE "Karte"
                      :en-GB "Map"}
   :icon-tooltip-percentage {:de-DE "Prozent"
                             :en-GB "Percentage"}
   :icon-tooltip-rain {:de-DE "Regen"
                       :en-GB "Rain"}
   :icon-tooltip-star {:de-DE "Stern"
                       :en-GB "Star"}
   :icon-tooltip-transfer {:de-DE "Transfer"
                           :en-GB "Transfer"}
   :icon-tooltip-charts {:de-DE "Diagramm"
                         :en-GB "Chart"}
   :icon-tooltip-city {:de-DE "Stadt"
                       :en-GB "City"}
   :icon-tooltip-flame {:de-DE "Flamme"
                        :en-GB "Flame"}
   :icon-tooltip-coin {:de-DE "Münze"
                       :en-GB "Coin"}
   :icon-tooltip-euro {:de-DE "Euro"
                       :en-GB "Euro"}
   :icon-tooltip-globe {:de-DE "Globus"
                        :en-GB "Globe"}
   :icon-tooltip-heart {:de-DE "Herz"
                        :en-GB "Heart"}
   :icon-tooltip-leaf {:de-DE "Blatt"
                       :en-GB "Leaf"}
   :icon-tooltip-sun {:de-DE "Sonne"
                      :en-GB "Sun"}
   :icon-tooltip-search {:de-DE "Lupe"
                         :en-GB "Magnifying glass"}
   :icon-tooltip-speech-bubble {:de-DE "Sprechblase"
                                :en-GB "Speech bubble"}
   :icon-tooltip-circle {:de-DE "Kreis"
                         :en-GB "Circle"}
   :ignore {:de-DE "Ignorieren"
            :en-GB "Ignore"}
   :ignore-incomplete {:de-DE "ignorieren"
                       :en-GB "ignore"}
   :ignore-zero {:de-DE "Nullen ignorieren"
                 :en-GB "Ignore zeros"}
   :import-aborted-message {:de-DE "Einige Daten wurden nicht importiert. Du kannst die Teilmenge nutzen oder löschen und einen neuen Importversuch starten. "
                            :en-GB "Some data was not imported. You can use the subset or delete the subset and try again."}
   :import-aborting-message {:de-DE "Import wird abgebrochen. Bitte warten..."
                             :en-GB "Import is being canceled. Please wait..."}
   :import-aborting-title {:de-DE "Import wurde abgebrochen."
                           :en-GB "Import was canceled."}
   :import-done-info {:de-DE "Import abgeschlossen"
                      :en-GB "Import done"}
   :import-failed-message {:de-DE "Einige Daten konnten nicht importiert werden. Du kannst die Teilmenge nutzen oder löschen und einen neuen Importversuch starten. "
                           :en-GB "Some data could not be imported. You can use the subset or delete the subset and try again."}
   :import-failed-title {:de-DE "Es gab einen Fehler beim Import, bitte überprüfe dein Datenmapping."
                         :en-GB "There was an error while importing, please check your data mapping"}
   :import-no {:de-DE "Nein, ich behebe den inkosistenten Stand selbst"
               :en-GB "No, I will fix the inconsistent state myself"}
   :import-not-enought-space {:de-DE "Nicht genügend Speicher, um diese Daten zu importieren"
                              :en-GB "Not enough memory to import this data"}
   :import-not-enought-space-tip {:de-DE "Ändere dein Mapping, reduziere die Datenmenge oder lösche einige Datenquellen, um Daten zu importieren."
                                  :en-GB "Change your mapping, reduce your data or delete some datasources to import more data."}
   :import-progress-message {:de-DE "Importiere XML-Dateien. Bitte warten..."
                             :en-GB "Import XML Files. Please wait..."}
   :import-yes {:de-DE "Ja"
                :en-GB "Yes"}
   :impressum-label {:de-DE "Impressum"
                     :en-GB "Legal Notice"}
   :incl-value-label {:de-DE "Inkl."
                      :en-GB "Incl."}
   :inclusive {:de-DE "inklusive"
               :en-GB "inclusive"}
   :incomplete-mapping-info {:de-DE "Für ein Beispiel der gemappten Daten definiere alle notwendigen Attribute und mindestens ein zusätzliches normales Attribut (kein Kontextattribut)"
                             :en-GB "To get a preview of the mapped data, define all required attributes and at least one additional non-context attribute."}
   :incorrect-entries-exist {:de-DE "Fehlerhafte Zuweisungen vorhanden"
                             :en-GB "Incorrect entries exist"}
   :independent-variable {:de-DE "Unabhängige Variable"
                          :en-GB "Independent variable"}
   :independent-variables {:de-DE "Unabhängige Variablen"
                           :en-GB "Independent variables"}
   :indicator-additional-attributes-label {:de-DE "Zusätzliche Attribute"
                                           :en-GB "Additional Attributes"}
   :indicator-addon-row-hint {:de-DE "Auswahl der zu übernehmenden Attribute aus den Eingangsdaten"
                              :en-GB "Include additional attributes from your input data in the indicator"}
   :indicator-aggregation-label {:de-DE "Aggregation"
                                 :en-GB "Aggregation"}
   :indicator-attribute-a-label {:de-DE "Attribut A"
                                 :en-GB "Attribute A"}
   :indicator-attribute-b-label {:de-DE "Attribut B"
                                 :en-GB "Attribute B"}
   :indicator-attribute-label {:de-DE "Attribut"
                               :en-GB "Attribute"}
   :indicator-attribute-selection {:de-DE "Attribut"
                                   :en-GB "Attribute"}
   :indicator-average {:de-DE "Durchschnitt"
                       :en-GB "Average"}
   :indicator-average-info {:de-DE "Erstelle einen Indikator, mit dem immer der Durchschnitt eines Attributs pro Jahr/Monat oder Tag und evtl. weiterer Gruppierung, z.B. Country, dargestellt wird."
                            :en-GB "Create an indicator that always shows the average value of the attribute per defined time granularity and further grouping."}
   :indicator-custom {:de-DE "Eigene Beschreibung"
                      :en-GB "Custom Description"}
   :indicator-custom-description {:de-DE "Beschreibung"
                                  :en-GB "Description"}
   :indicator-custom-info {:de-DE "Für die Erstellung einer custom description kontaktieren Sie Ihre Administratoren."
                           :en-GB "Custom indicator"}
   :indicator-desc {:de-DE "Beschreibung"
                    :en-GB "Description"}
   :indicator-desc-placeholder {:de-DE "Beschreibung hinzufügen"
                                :en-GB "Describe the indicator"}
   :indicator-discard-changes {:de-DE "Verwerfen"
                               :en-GB "Discard"}
   :indicator-distinct {:de-DE "Einzigartig"
                        :en-GB "Distinct"}
   :indicator-distinct-info {:de-DE "Sammelt einzigartige Ausprägungen eines Attribute."
                             :en-GB "Collects unique expressions of an attribute."}
   :indicator-division {:de-DE "Proportional"
                        :en-GB "Ratio"}
   :indicator-division-info {:de-DE "Setze zwei Attribute ins Verhältnis um z.B. die Konflikttoten ins Verhältnis zur Bevölkerungsgröße des Landes zu setzen."
                             :en-GB "Ratio of two attributes' values."}
   :indicator-grouping-hint {:de-DE "Wähle ein weiteres Attribut, nach welchem der Indikator gruppiert wird. Beispiel: Wenn du einen Indikator pro Jahr und Land erstellen möchtest, wähle bei time granularity year und hier country."
                             :en-GB "Select another attribute by which the indicator will be grouped. Example: If you want to create one indicator per year and country, select year for time granularity and country here."}
   :indicator-grouping-label {:de-DE "Optionale Gruppierungen"
                              :en-GB "Optional groupings"}
   :indicator-grp-rights-and-roles {:de-DE "Indikator"
                                    :en-GB "Indicator"}
   :indicator-max {:de-DE "Maximum"
                   :en-GB "Maximum"}
   :indicator-max-info {:de-DE "Erstelle einen Indikator, mit dem immer das Maximum eines Attributs pro Jahr/Monat oder Tag und evtl. weiterer Gruppierung, z.B. Country, dargestellt wird."
                        :en-GB "Create an indicator that always shows the maximum value of the attribute per defined time granularity and further grouping."}
   :indicator-min {:de-DE "Minimum"
                   :en-GB "Minimum"}
   :indicator-min-info {:de-DE "Erstelle einen Indikator, mit dem immer das Minimum eines Attributs pro Jahr/Monat oder Tag und evtl. weiterer Gruppierung, z.B. Country, dargestellt wird."
                        :en-GB "Create an indicator that always shows the minimum value of the attribute per defined time granularity and further grouping."}
   :indicator-name {:de-DE "Name"
                    :en-GB "Name"}
   :indicator-normalize {:de-DE "Normalisierung"
                         :en-GB "Normalization"}
   :indicator-normalize-info {:de-DE "Wandle die Werte eines Attributs in einen Zahlenbereich von 0-100 um."
                              :en-GB "Convert the values of one attribute to a scale, e.g., a range of numbers 0 to 100."}
   :indicator-normalize-range {:de-DE "Spektrum"
                               :en-GB "Range"}
   :indicator-range-max-label {:de-DE "Max Range"
                               :en-GB "Max Range"}
   :indicator-range-min-label {:de-DE "Min Range"
                               :en-GB "Min Range"}
   :indicator-rank {:de-DE "Gewichtete Normalisierung"
                    :en-GB "Weighted normalization"}
   :indicator-rank-info {:de-DE "Erstelle dir deinen eigenen Indikator von 0-100, z.B. einen Risikoindikator, der mehrere Risiko-Attribute zusammenführt."
                         :en-GB "Convert multiple attribute values to a scale."}
   :indicator-rights-and-roles {:de-DE "Rechte und Rollenverwaltung"
                                :en-GB "Rights & Roles Management"}
   :indicator-save {:de-DE "Speichern"
                    :en-GB "Save"}
   :indicator-select-attribute {:de-DE "Attribut auswählen"
                                :en-GB "Select attribute"}
   :indicator-settings-label {:de-DE "Indikator Einstellungen"
                              :en-GB "Settings"}
   :indicator-sum {:de-DE "Summe"
                   :en-GB "Sum"}
   :indicator-sum-info {:de-DE "Aggregiere die Werte eines Attributs über die Zeit und weitere Gruppierungen mit einer Summierung zu einem eigenen Indikator, z.B. die No. of Events pro Jahr und Land."
                        :en-GB "Sum of the values of the attibute over a period of time, grouped by another attribute."}
   :indicator-time-granularity-label {:de-DE "Zeitliche Granularität"
                                      :en-GB "Time Granularity"}
   :indicator-type {:de-DE "Indikatortyp"
                    :en-GB "Calculation Type"}
   :indicator-type-hint {:de-DE "Berechnungsmethode, nach der der Indikator berechnet wird"
                         :en-GB "Type of calculation of the indicator"}
   :info-at-least-chars {:de-DE "Muss mindestens %s Zeichen lang sein"
                         :en-GB "Must have at least %s characters"}
   :info-detail-label {:de-DE "Info"
                       :en-GB "Info"}
   :info-div {:de-DE "Beschreibung"
              :en-GB "Description"}
   :info-fallback-layout-hint {:de-DE "Die Datenmenge enthält mind. einen Datenpunkt dem keine Farbe zugewiesen werden konnte."
                               :en-GB "Your data contains at least one event with no appliable layout."}
   :info-grouped-by {:de-DE "Gruppiert nach "
                     :en-GB "Grouped by "}
   :info-groups-sorted-by {:de-DE "Gruppen sortiert nach "
                           :en-GB "Groups sorted by "}
   :info-sorted-by {:de-DE "Sortiert nach "
                    :en-GB "Sorted by "}
   :info-subgrouped-by {:de-DE "Untergruppiert nach "
                        :en-GB "Subgrouped by "}
   :info-subgroups-sorted-by {:de-DE "Untergruppen sortiert nach "
                              :en-GB "Subgroups sorted by "}
   :info-tooltip-text {:de-DE "Einstellungen & Info"
                       :en-GB "Settings & info"}
   :info-treemap-type {:de-DE "Tree Map Typ"
                       :en-GB "Treemap type"}
   :info-treemap-squared {:de-DE "Quadratisch"
                          :en-GB "Squarified"}
   :info-treemap-binary {:de-DE "Binärbaum"
                         :en-GB "Binary tree"}
   :info-treemap-slice {:de-DE "Schneiden und Würfeln"
                        :en-GB "Slice and Dice"}
   :init-type {:de-DE "Initialisierungstyp der Zentren"
               :en-GB "Init type"}
   :initial-p {:de-DE "initiales P"
               :en-GB "initial P"}
   :initial-q {:de-DE "initiales Q"
               :en-GB "initial Q"}
   :initial-seasonal-p {:de-DE "initiales P saisonal"
                        :en-GB "initial seasonal P"}
   :initial-seasonal-q {:de-DE "initiales Q saisonal"
                        :en-GB "initial seasonal Q"}
   :input-data-section {:de-DE "Datenvorschau"
                        :en-GB "Data Preview"}
   :input-data-section-hint {:de-DE "Grobe Strukturvorschau der Daten"
                             :en-GB "Preview of the generated indicator data"}
   :input-invalid {:de-DE "Eingabe ist nicht valide."
                   :en-GB "Input is not valid."}
   :instantly {:de-DE "Sofort"
               :en-GB "Instantly"}
   :integer {:de-DE "Ganzzahl"
             :en-GB "Integer"}
   :interaction-input-hint {:de-DE "Das Attribut Interaction bietet eine Möglichkeit, die Daten zu kategorisieren. So kann beispielweise ausgedrückt werden, ob es sich um ein Event, Indikator oder etwas anderes handelt. Unter Erweiterte Attributdefinitionen kann auch ein eigener Typ, z.B. mit einem festen Wert definiert werden (wenn alle Datenpunkte dort den gleichen Wert haben sollen)."
                            :en-GB "The interaction attribute can be used for categorizing data. For example, it allows specifying whether the given data represents an event or an indicator. Under custom attributes a new type with a fixed value can be specified too (when all data points should have the same value)."}
   :invalid-option-hint {:de-DE "Dieser Eintrag ist nicht gültig"
                         :en-GB "This entry is invalid"}
   :invalid-selection-or-missing-attribute-error {:de-DE "Fehlerhafte oder fehlende Auswahl"
                                                  :en-GB "Invalid selection or missing attribute error"}
   :invalid-time-input {:de-DE "Zeitraumeingabe ist nicht gültig."
                        :en-GB "Time input is invalid."}
   :is-required {:de-DE "ist erforderlich"
                 :en-GB "is required"}
   :json-not-supported-hint {:de-DE "Für diesen Dateityp nicht verfügbar"
                             :en-GB "Not available for this file type"}
   :k-means-desc {:de-DE "Clustering für einen Wert basiert auf historischen Daten. K-means Clustering ist eine Methode zur Clusteranalyse. Der K-Means-Algorithmus partitioniert Werte in Cluster, bei der jede Beobachtung zu dem nächstem Clusterzentrum gehört."
                  :en-GB "Creates groups (clusters) based on similar characteristics. K-means clustering is a method of cluster analysis. The k-means algorithm partitions a value into clusters in which each observation belongs to the cluster with the nearest center."}
   :k-means-model {:de-DE "K-Means Modell"
                   :en-GB "K-Means Model"}
   :k-means-model-desc {:de-DE "Bildet Gruppen (Cluster) basierend auf ähnlichen Merkmalen."
                        :en-GB "Creates groups (clusters) based on similar characteristics."}
   :k-means-pal {:de-DE "K-Means Clustering"
                 :en-GB "K-Means Clustering"}
   :k-means-pal-ignored-normalization {:de-DE "Kategorische Attribute können nur mit Min Max normalisiert werden."
                                       :en-GB "Categorical attributes can only be normalized with Min Max."}
   :keep {:de-DE "Behalten als ein Event"
          :en-GB "Keep as one event"}
   :keep-colinear {:de-DE "Kollinear behalten"
                   :en-GB "Keep collinear"}
   :kernel {:de-DE "Kernel"
            :en-GB "Kernel"}
   :kernel-ridge {:de-DE "Kernel Ridge Regression"
                  :en-GB "Kernel Ridge Regression"}
   :kernel-ridge-desc {:de-DE "Die Kernel Methode kombiniert die l2-regularisierte lineare Regression mit dem Kernel-Trick, um nicht lineare Funktionen zu modellieren."
                       :en-GB "Kernel Ridge Regression combines an l2-regularized regression with the kernel method to fit non-linear models."}
   :algorithms-grp-rights-and-roles {:de-DE "Prädiktion"
                                     :en-GB "Prediction"}
   :algorithms-model-window {:de-DE "Prädiktion"
                             :en-GB "Prediction"}
   :algorithms-protocol-action-submit-task {:de-DE "Prädiktion ausführen"
                                            :en-GB "Execute prediction"}
   :algorithms-protocol-action-connect {:de-DE "Neue Daten"
                                        :en-GB "New data"}
   :algorithms-rights-and-roles {:de-DE "Rechte und Rollenverwaltung"
                                 :en-GB "Rights & Roles Management"}
   :algorithms-simple-title {:de-DE "Prädiktion"
                             :en-GB "Prediction"}
   :algorithms-tooltip-search {:de-DE "Nutze die Daten in einer Prädiktion"
                               :en-GB "Use the data in a prediction"}
   :kpss-sig-level {:de-DE "KPSS Signifikanzlevel"
                    :en-GB "KPSS significance level"}
   :label-create-layout {:de-DE "Layout erstellen"
                         :en-GB "Create Layout"}
   :label-create-overlayer {:de-DE "Overlayer erstellen"
                            :en-GB "Create Overlayer"}
   :label-default-layouts {:de-DE "Standardlayouts"
                           :en-GB "Default Layouts"}
   :label-default-overlayers {:de-DE "Standardoverlayers"
                              :en-GB "Default Overlayers"}
   :label-desc {:de-DE "Attributnamen werden für die Datenbank normalisiert (Kleinschreibung; Sonderzeichen entfernen), neue Attributnamen müssen daher auch nach der Normalisierung einzigartig sein. In der Software wird das Attribut immer so zu sehen sein wie es hier eingegeben wird."
                :en-GB "The attribute name given here will be normalized within the database only (removal of special character and written in lower case), therefore it also needs to be unique in its normalized form. It will always be displayed within our software as it is written here."}
   :label-error-attribute {:de-DE "Es gibt bereits ein Attribut mit einer anderen Schreibweise '%s'. Verwende die gleiche Schreibweise oder einen anderen Attributsname"
                           :en-GB "There is already an attribute with different wording '%s'\nUse the same or another attribute name instead"}
   :label-error-label {:de-DE "Attributname wird bereits verwendet."
                       :en-GB "Attribute name is already used."}
   :label-label {:de-DE "Label"
                 :en-GB "Label"}
   :label-layouts {:de-DE "Layouts"
                   :en-GB "Layouts"}
   :label-match-all {:de-DE "erfüllen alle Bedingungen"
                     :en-GB "match all conditions"}
   :label-match-any {:de-DE "erfüllen mindestens eine Bedingung"
                     :en-GB "match any condition"}
   :label-no-access-geomap {:de-DE "Keine Zugriff auf Geomap / Overlayer sind nicht editierbar"
                            :en-GB "No Geomap access / Overlayers are not editable"}
   :label-my-layouts {:de-DE "Meine Layouts"
                      :en-GB "My Layouts"}
   :label-my-overlayers {:de-DE "Meine Overlayers"
                         :en-GB "My Overlayers"}
   :label-overlayers {:de-DE "Overlayers"
                      :en-GB "Overlayers"}
   :lag {:de-DE "Zeitabstand"
         :en-GB "Lag"}
   :lag-info {:de-DE "Empfohlen wird, die Hälfte der Datenpunkte zu wählen."
              :en-GB "Recommended is half the sample size."}
   :lang-de-DE {:de-DE "Deutsch"
                :en-GB "Deutsch"}
   :lang-en-GB {:de-DE "English"
                :en-GB "English"}
   :langfile {:langfile nil}
   :langloc-language {:de-DE "Sprache"
                      :en-GB "Language"}
   :langloc-language-drop-down {:de-DE "Sprache"
                                :en-GB "Language"}
   :langloc-language-group {:de-DE "Sprachauswahl"
                            :en-GB "Language selection"}
   :lasso {:de-DE "Lasso"
           :en-GB "Lasso"}
   :last-changed-label {:de-DE "Zuletzt bearbeitet"
                        :en-GB "Last changed"}
   :last-triggered {:de-DE "Zuletzt ausgelöst:"
                    :en-GB "Last triggered:"}
   :last-modified-label {:de-DE "Zuletzt geändert"
                         :en-GB "Last modified"}
   :title-asc {:de-DE "Titel (A-Z)"
               :en-GB "Title (A-Z)"}
   :title-des {:de-DE "Titel (Z-A)"
               :en-GB "Title (Z-A)"}
   :latitude-label {:de-DE "Breitengrad"
                    :en-GB "Latitude"}
   :layer-type-feature {:de-DE "Fläche"
                        :en-GB "area"}
   :layer-type-heatmap {:de-DE "Heatmap"
                        :en-GB "heatmap"}
   :layer-type-marker {:de-DE "Marker"
                       :en-GB "marker"}
   :layer-type-movement {:de-DE "Strömung"
                         :en-GB "movement"}
   :layout-management-label {:de-DE "Layout Management"
                             :en-GB "Layout Management"}
   :layout-manager-label {:de-DE "Layout Management"
                          :en-GB "Layout Management"}
   :layout-section-title {:de-DE "Layouts"
                          :en-GB "Layouts"}
   :layout-attribute-hint {:de-DE "Es können mehrere Attribute vom gleichen Typen ausgewählt werden, auf welche das Layout angewendet wird. Wird ein Attribut mit einem anderen Typen ausgewählt, überschreibt es die bisherige Auswahl."
                           :en-GB "You can select multiple attributes of the same type the layout is applied to. Selecting an attribute with a different type will overwrite the prior selections."}
   :layout-title-revert {:de-DE "Titel zurücksetzen"
                         :en-GB "Reset title"}
   :legend-general {:de-DE "Allgemein"
                    :en-GB "General"}
   :legend-add-color {:de-DE "Farbe hinzufügen"
                      :en-GB "Add color"}
   :legend-all-data {:de-DE "Alle Daten"
                     :en-GB "All Data"}
   :legend-all-matching {:de-DE "Alle nicht Übereinstimmenden"
                         :en-GB "All non matching"}
   :legend-attribute {:de-DE "Attribut"
                      :en-GB "Attribute"}
   :legend-colored-by {:de-DE "Gefärbt nach"
                       :en-GB "Colored by"}
   :legend-countries-label {:de-DE "Land"
                            :en-GB "Country"}
   :legend-current-data {:de-DE "Aktuelle Daten im Fenster"
                         :en-GB "Current data from window"}
   :legend-datasources-label {:de-DE "Datenquelle"
                              :en-GB "Datasource"}
   :legend-general-attributes {:de-DE "Allgemein"
                               :en-GB "General"}
   :legend-icomplete-filter-explanation {:de-DE "Es können weitere Einschränkungen vorliegen aufgrund von Mengenoperationen."
                                         :en-GB "There might be additional constraints due to set operations."}
   :legend-icon-tooltip {:de-DE "Legende"
                         :en-GB "Legend"}
   :legend-layout-name {:de-DE "Layout Name"
                        :en-GB "Layout name"}
   :legend-not-operator {:de-DE "nicht"
                         :en-GB "not"}
   :legend-missing-value-error {:de-DE "Es ist kein Wert ausgewählt, bitte wählen Sie mindestens einen Wert aus."
                                :en-GB "No value is selected, please select at least one value."}
   :legend-pos-label {:de-DE "Position der Legende"
                      :en-GB "Legend Position"}
   :legend-range-separator {:de-DE " bis "
                            :en-GB " to "}
   :legend-section-title {:de-DE "Legende"
                          :en-GB "Legend"}
   :legend-send-copy {:de-DE "Kopie senden"
                      :en-GB "Send copy"}
   :legend-show-less {:de-DE "Weniger anzeigen"
                      :en-GB "Show less"}
   :legend-show-more {:de-DE "Mehr anzeigen"
                      :en-GB "Show more"}
   :legend-years-label {:de-DE "Jahr"
                        :en-GB "Year"}
   :max-paste-characters-message {:de-DE "Zu viele Zeichen zum Einfügen. Bitte reduziere die Auswahl auf maximal $num Zeichen"
                                  :en-GB "Too much characters to paste. Please reduce your selection to a maximum of $num characters"}
   :legend-interval-overlap-error {:de-DE "Du hast mindestens einen Wert mehreren Farben zugewiesen, bitte weise jedem Wert nur eine Farbe zu."
                                   :en-GB "You have assigned at least one value to several colors, please assign each value to one color only."}
   :legend-illegal-interval-error {:de-DE "Das untere Limit der Intervalle muss kleiner sein das obere Limit."
                                   :en-GB "The lower limit of the intervals must be smaller than the upper limit."}
   :legend-interval-single-gap-warning {:de-DE "Es existiert eine Lücke zwischen 2 Intervallen."
                                        :en-GB "There is a gap between 2 intervals."}
   :legend-interval-gaps-warning {:de-DE "Zwischen den Intervallen gibt es mehrere Lücken."
                                  :en-GB "There are several gaps between intervals."}
   :legend-negative-infinity {:de-DE "min"
                              :en-GB "min"}
   :legend-infinity {:de-DE "max"
                     :en-GB "max"}
   :length {:de-DE "Vorhersagelänge"
            :en-GB "Forecast length"}
   :line-chart-label {:de-DE "Liniendiagramm"
                      :en-GB "Line chart"}
   :linear {:de-DE "Linear"
            :en-GB "Linear"}
   :linear-regression {:de-DE "Lineare Regression"
                       :en-GB "Linear Regression"}
   :linear-model {:de-DE "Wertvorhersage"
                  :en-GB "Value Forecast"}
   :linear-model-desc {:de-DE "Sagt einen Wert basierend auf historischen Daten voraus."
                       :en-GB "Forecast one value based on historic data."}
   :link-share-hint {:de-DE "Um den Link zu öffnen, benötigt ein Nutzer entweder explizite Zugriffsrechte oder der Link muss 'Öffentlich' gesetzt sein."
                     :en-GB "The link can only be accessed by other users after sharing with them or using the 'Public' setting."}
   :link-share-label {:de-DE "Link zum Teilen"
                      :en-GB "Link to share"}
   :lmmr {:de-DE "Multiple lineare Regression"
          :en-GB "Multiple Linear Regression"}
   :lmmr-desc {:de-DE "Die multiple lineare Regression ist ein lineares Modell. Es erlaubt die Modellierung linearer Beziehnugen zwischen einer Variable (abhängige Variable) und mehrerer anderer Variablen (unabhängige Variablen)."
               :en-GB "Multiple linear regression is a linear model. It allows to model linear relationships between one variable (dependent variable) and multiple other variables (independent variable)."}
   :lmt-gaps {:de-DE "Die Trainingsdaten beinhalten fehlende Werte, der Algorithmus liefert unter Umständen weniger Ergebniswerte als erwartet. Du kannst die fehlenden Werte oben ersetzen."
              :en-GB "The training data contains missing values, the algorithm cannot calculate a reliable forecast. You can replace the missing values under advanced options."}
   :lmt-multiple-not-valid {:de-DE "Mehrfache Werte mit demselben Datum verursachen Fehler bei der Ausführung von dem Lineare Regression mit gedämpften Trends und saisonalen Anpassungen-Algorithmus"
                            :en-GB "Multiple values with the same date cause errors when running Linear Regression with Damped Trend and Seasonal Adjust algorithm."}
   :lmtts {:de-DE "Lineare Regression mit gedämpften Trends und saisonalen Anpassungen"
           :en-GB "Linear Regression with Damped Trend and Seasonal Adjust"}
   :lmtts-desc {:de-DE "Die lineare Regression mit gedämpften Trends und saisonalen Anpassungen ist ein lineares Modell. Es erlaubt die Modellierung linearer Beziehnugen zwischen zwei Variablen (abhängige und unabhängige Variable).
                      Das Modell versucht, die Unter- und Überanpassung zu verringern, indem saisonale Trends abgeschwächt werden. Außerdem bietet es Parameter an, um den Einfluss von Trends zu beeinflussen."
                :en-GB "Linear regression with damped trend and seasonal adjust is a linear model. It allows to model linear relationships between two variables (dependent and independent variable).
                      The model tries to prevent over- and underfitting by dampening seasonal trends, while providing multiple parameters to adjust the influence of detected trends."}
   :load-hide-prediction {:de-DE "Modell löschen"
                          :en-GB "Delete model"}
   :load-mapping-failed {:de-DE "Laden der Zuweisungen gescheitert"
                         :en-GB "Loading Mapping failed"}
   :load-mapping-failed-tip {:de-DE "Die Datei ist ungültig. Es könnte sein, dass diese Datei manuell verändert wurde."
                             :en-GB "The file is invalid. It looks like the file has been changed manually."}
   :load-prediction {:de-DE "Modell"
                     :en-GB "Load model settings"}
   :load-prediction-button {:de-DE "Laden"
                            :en-GB "Load"}
   :load-prediction-name {:de-DE "Modellname"
                          :en-GB "Model name"}
   :load-prediction-section {:de-DE "Modelleinstellungen laden"
                             :en-GB "Load model settings"}
   :load-project-directly {:de-DE "Projekt direkt laden"
                           :en-GB "Load project directly"}
   :load-stop-screen-follow-recommendation {:de-DE "Abbrechen"
                                            :en-GB "Discard"}
   :load-stop-screen-message-part-1 {:de-DE "Die ausgewählte Datenmenge ist zu groß."
                                     :en-GB "The selected amount of data is too large to proceed."}
   :load-stop-screen-message-part-1-graph {:de-DE "Die Anzahl von Knoten und/oder Kanten ist zu groß, um fortzufahren."
                                           :en-GB "The number of nodes and/or edges is too huge to proceed."}
   :load-stop-screen-message-part-2 {:de-DE "Bitte wähle eine kleinere Datenmenge aus."
                                     :en-GB "Please reduce the data selected."}
   :load-stop-screen-message-part-2-graph {:de-DE "Bitte wähle eine kleinere Datenmenge aus oder reduziere die Anzahl an Knoten und Kanten durch Ändern der Auswahl."
                                           :en-GB "Please reduce the selected data or reduce the amount of nodes and edges by changing the selection."}
   :load-stop-screen-title {:de-DE "Stopp"
                            :en-GB "Stop"}
   :load-warning-screen-follow-recommendation {:de-DE "Abbrechen"
                                               :en-GB "Discard"}
   :load-warning-screen-message-part-1 {:de-DE "Diese Operation ist sehr rechenintensiv und kann bis zu 15 Sek dauern."
                                        :en-GB "This operation is computationally costly and might last up to 15 seconds."}
   :load-warning-screen-message-part-1-graph {:de-DE "Diese Operation ist sehr rechenintensiv und kann bis zu 15 Sek dauern."
                                              :en-GB "This operation is computationally costly and might last up to 15 seconds."}
   :load-warning-screen-message-part-2 {:de-DE "In dieser Zeit wird dein Browser nicht auf Nutzereingaben reagieren."
                                        :en-GB "During this time the browser will remain non-responsive."}
   :load-warning-screen-message-part-2-graph {:de-DE "In dieser Zeit wird dein Browser nicht auf Nutzereingaben reagieren."
                                              :en-GB "During this time the browser will remain non-responsive."}
   :load-warning-screen-not-follow-recommendation {:de-DE "Trotzdem fortfahren"
                                                   :en-GB "Continue anyway"}
   :load-warning-screen-recommendation {:de-DE "Wir empfehlen, die Datenmenge einzuschränken."
                                        :en-GB "We recommend to reduce the selected data."}
   :load-warning-screen-recommendation-graph {:de-DE "Wir empfehlen, die Datenmenge einzuschränken oder die Anzahl an Knoten und Kanten durch Ändern der Auswahl zu reduzieren."
                                              :en-GB "We recommend to reduce the selected data or to reduce the amount of nodes and edges by changing the selection."}
   :load-warning-screen-title {:de-DE "Warnung"
                               :en-GB "Warning"}
   :loading-config-failed {:de-DE "Laden der Configuration fehlgeschlagen"
                           :en-GB "Loading of configuration failed"}
   :loading-label {:de-DE "Lade..."
                   :en-GB "Loading..."}
   :loading-langloc-failed {:de-DE "Laden von Sprachdaten fehlgeschlagen"
                            :en-GB "Loading of language data failed"}
   :loading-screen-message {:de-DE "Daten werden analysiert und geladen"
                            :en-GB "We are loading your data…"}
   :loading-screen-tip {:de-DE "Du kannst die Daten im Vorfeld durch entsprechende Suchkriterien einschränken"
                        :en-GB "Use search criteria for data reduction"}
   :loading-screen-tip-titel {:de-DE "Tipp:"
                              :en-GB "Tip:"}
   :loading-screen-tip-title {:de-DE "Wird geladen"
                              :en-GB "Loading"}
   :loading-step-dialog-confirm-button {:de-DE "OK"
                                        :en-GB "OK"}
   :loading-step-dialog-discard-button {:de-DE "Abbrechen"
                                        :en-GB "Cancel"}
   :loading-step-dialog-read-question {:de-DE "Wenn der Schritt im schreibgeschützten Modus geladen ist, kannst du nichts ändern."
                                       :en-GB "If the step is loaded in read-only mode, you cannot change anything."}
   :loading-step-dialog-title {:de-DE "Schritt %d laden"
                               :en-GB "Step %d loading"}
   :loading-step-dialog-write-question {:de-DE "Wenn du an diesem Zustand arbeiten willst, werden alle neueren Schritte gelöscht."
                                        :en-GB "If you work on this state, all newer steps will be deleted."}
   :loading-text {:de-DE "Lade Daten..."
                  :en-GB "Loading data..."}
   :location-attribute-missing {:de-DE "Bitte wähle eine Spalte für den ~A oder entferne den ~A, andernfalls wird die Position ignoriert."
                                :en-GB "Please select a column for ~A or remove ~A, otherwise location will be ignored."}
   :location-input-hint {:de-DE "Die Angabe der Breitengrade (latitude) und Längengrade (longitude) werden zusammengefasst zu dem Attribut location, dieses bestimmt die Position der Events auf der Karte."
                         :en-GB "The latitude and longitude settings are combined into the location attribute which determines the position of the events on the map."}
   :log-message-label {:de-DE "Nachricht"
                       :en-GB "Message"}
   :log-out-tooltip {:de-DE "Ausloggen"
                     :en-GB "Log Out"}
   :warning-logout-open-project-message {:de-DE "Möchtest du dich ausloggen und das Projekt schließen? Deine Arbeit wird im Projekt gespeichert."
                                         :en-GB "Do you want to log out and close the project? Your progress is saved in the project."}
   :warning-logout-message {:de-DE "Möchtest du dich ausloggen ohne deine Arbeit als Projekt zu speichern? Fortschritt geht verloren."
                            :en-GB "Do you want to log out without saving your work as a project? You will lose your progress."}
   :log-section-label {:de-DE "Log"
                       :en-GB "Log"}
   :login-form-keep-logged-in {:de-DE "Eingeloggt bleiben"
                               :en-GB "Remember me"}
   :login-form-loginbutton-label {:de-DE "Anmelden"
                                  :en-GB "Sign in"}
   :login-form-password-placeholder {:de-DE "Passwort"
                                     :en-GB "Password"}
   :login-form-usage-hint {:de-DE "Hinweis zur Benutzung"
                           :en-GB "Usage hint"}
   :login-form-username-placeholder {:de-DE "Benutzername"
                                     :en-GB "Username"}
   :login-header {:de-DE "Explorama"
                  :en-GB "Explorama"}
   :logout-label {:de-DE "Als %s ausloggen"
                  :en-GB "Logout %s"}
   :longitude-label {:de-DE "Längengrad"
                     :en-GB "Longitude"}
   :lr-apache {:de-DE "Lineare Regression"
               :en-GB "Linear Regression"}
   :lr-apache-desc {:de-DE "Die lineare Regression ist ein lineares Modell. Es erlaubt die Modellierung linearer Beziehnugen zwischen einer Variable (abhängige Variable) und einer anderen Variable (unabhängige Variable)."
                    :en-GB "Multiple linear regression is a linear model. It allows to model linear relationships between one variable (dependent variable) and another (independent variable)."}
   :linear-regression-desc {:de-DE "Die lineare Regression ist ein lineares Modell. Es erlaubt die Modellierung linearer Beziehnugen zwischen einer Variable (abhängige Variable) und einer anderen Variable (unabhängige Variable)."
                            :en-GB "Multiple linear regression is a linear model. It allows to model linear relationships between one variable (dependent variable) and another (independent variable)."}
   :lu {:de-DE "Doolittle decomposition"
        :en-GB "Doolittle decomposition"}
   :m5 {:de-DE "M5"
        :en-GB "M5"}
   :mad {:de-DE "MAD"
         :en-GB "MAD"}
   :mail-title-label {:de-DE "Link per E-Mail versenden"
                      :en-GB "Send link via E-Mail"}
   :manage-alerts {:de-DE "Alarme verwalten"
                   :en-GB "Manage alerts"}
   :manhattan {:de-DE "Manhattan-Distanz"
               :en-GB "Manhattan distance"}
   :manual {:de-DE "Manuell"
            :en-GB "Manual"}
   :map-designer-title {:de-DE "Kartenoverlayer-Designer"
                        :en-GB "Map Overlayer Designer"}
   :map-tooltip-search {:de-DE "Visualisiere die Daten in der Karte"
                        :en-GB "Visualize the data in the map"}
   :mape {:de-DE "MAPE"
          :en-GB "MAPE"}
   :mapping-changed {:de-DE "(verändert)"
                     :en-GB "(modified)"}
   :mark-as-read {:de-DE "Alle als gelesen markieren"
                  :en-GB "Mark all as read"}
   :marker-cluster-settings {:de-DE "Cluster Einstellungen"
                             :en-GB "Cluster settings"}
   :marker-cluster-settings-toggle {:de-DE "Clustering aktiv?"
                                    :en-GB "Enable clustering?"}
   :marker-cluster-settings-zoom {:de-DE "Cluster Zoom-Level"
                                  :en-GB "Cluster zoom level"}
   :marker-look-opacity {:de-DE "Deckkraft"
                         :en-GB "Opacity"}
   :marker-look-radius {:de-DE "Radius"
                        :en-GB "Radius"}
   :marker-look-settings {:de-DE "Marker aussehen"
                          :en-GB "Marker look"}
   :marker-not-displayable-tooltip {:de-DE "Einige Events haben eventuell keine Position oder Landzuweisung, die auf der Karte dargestellt werden kann."
                                    :en-GB "Some events may not have a location or country mapping which can be displayed on the map, e.g., unknown."}
   :marker-section-title {:de-DE "Marker"
                          :en-GB "Marker"}
   :marker-select-label {:de-DE "Marker"
                         :en-GB "Marker"}
   :mase {:de-DE "MASE"
          :en-GB "MASE"}
   :max {:de-DE "Max"
         :en-GB "Max"}
   :max-d {:de-DE "Max D"
           :en-GB "Max D"}
   :max-iteration {:de-DE "Maximale Iterationen"
                   :en-GB "Maximum iterations"}
   :max-iterations {:de-DE "Max Iterationen"
                    :en-GB "Max iterations"}
   :max-label {:de-DE "Maximum"
               :en-GB "Maximum"}
   :max-order {:de-DE "Max Grad"
               :en-GB "Max order"}
   :max-p {:de-DE "Max P"
           :en-GB "Max P"}
   :max-q {:de-DE "Max Q"
           :en-GB "Max Q"}
   :max-seasonal-d {:de-DE "Max D saisonal"
                    :en-GB "Max seasonal D"}
   :max-seasonal-p {:de-DE "Max P saisonal"
                    :en-GB "Max seasonal P"}
   :max-seasonal-q {:de-DE "Max Q saisonal"
                    :en-GB "Max seasonal Q"}
   :max-selected-characters {:de-DE "Maximale Selektion erreicht"
                             :en-GB "Maximum selections reached"}
   :maximize-tooltip {:de-DE "Maximieren"
                      :en-GB "Maximize"}
   :measure-name {:de-DE "Algorithmusname"
                  :en-GB "Measure name"}
   :median {:de-DE "Mittelwert"
            :en-GB "Average"}
   :median-label {:de-DE "Median"
                  :en-GB "Median"}
   :menusection-annotation {:de-DE "Projektnotizen"
                            :en-GB "Project annotation"}
   :menusection-close-project {:de-DE "Schließen"
                               :en-GB "Close"}
   :menusection-dashboard {:de-DE "Dashboard"
                           :en-GB "Dashboard"}
   :menusection-data-atlas {:de-DE "Datenatlas"
                            :en-GB "Data Atlas"}
   :menusection-export {:de-DE "Exportieren"
                        :en-GB "Export"}
   :menusection-help {:de-DE "Release 12 Hilfe"
                      :en-GB "Release 12 Help"}
   :menusection-logo {:de-DE "Gehe zurück zur Willkommensseite"
                      :en-GB "Go back to welcome page"}
   :menusection-projects {:de-DE "Projekte"
                          :en-GB "Projects"}
   :menusection-protocol {:de-DE "Protokoll"
                          :en-GB "Protocol"}
   :menusection-settings {:de-DE "Einstellungen"
                          :en-GB "Settings"}
   :menusection-snapshots-clean-workspace {:de-DE "Aufräumen"
                                           :en-GB "Clean"}
   :menusection-snapshots-close-project {:de-DE "Schließen"
                                         :en-GB "Close"}
   :menusection-snapshots-save-project {:de-DE "Projekt speichern"
                                        :en-GB "Save project"}
   :merge-and-multiple-values-error {:de-DE "Es wurde keine Aggregation ausgewählt auf dessen Basis die Events zusammengeführt werden können"
                                     :en-GB "No aggregation was selected as a basis to merge the events"}
   :merge-incomplete {:de-DE "zusammenführen"
                      :en-GB "merge"}
   :message-remove-all-slides {:de-DE "Sollen alle Folien gelöscht werden?"
                               :en-GB "Do you want to delete all slides?"}
   :method {:de-DE "Methode"
            :en-GB "Method"}
   :method-label {:de-DE "Fort. Methode"
                  :en-GB "Adv. Methods"}
   :min {:de-DE "Min"
         :en-GB "Min"}
   :min-label {:de-DE "Minimum"
               :en-GB "Minimum"}
   :min-max {:de-DE "Min Max"
             :en-GB "Min Max"}
   :minimize-tooltip {:de-DE "Minimieren"
                      :en-GB "Minimize"}
   :minkowski {:de-DE "Minkowski-Distanz"
               :en-GB "Minkowski distance"}
   :minkowski-power {:de-DE "Minkowski-Power"
                     :en-GB "Minkowski power"}
   :missing-value {:de-DE "Fehlende Werte"
                   :en-GB "Missing value"}
   :missing-value-message {:de-DE "Reihe <p> Wert fehlt"
                           :en-GB "Row <p> value is missing"}
   :missing-value-replacement {:de-DE "Ersatz"
                               :en-GB "Replacement"}
   :missing-value-replacement-value {:de-DE "Ersatzwert"
                                     :en-GB "Replacement value"}
   :mk {:de-DE "Mann-Kendall Test"
        :en-GB "Mann-Kendall Test"}
   :mle {:de-DE "MLE"
         :en-GB "MLE"}
   :model-last-used {:de-DE "zuletzt vor %s benutzt"
                     :en-GB "last used %s ago"}
   :model-loading-tooltip {:de-DE "Gespeicherte Modelle"
                           :en-GB "Saved models"}
   :model-metrics {:de-DE "Modellmetriken"
                   :en-GB "Model Metrics"}
   :model-settings {:de-DE "Modelleinstellungen"
                    :en-GB "Model Settings"}
   :mondays {:de-DE "Montags"
             :en-GB "Mondays"}
   :month {:de-DE "Monat"
           :en-GB "Month"}
   :monthly {:de-DE "Monatlich"
             :en-GB "Monthly"}
   :most-frequent {:de-DE "Häufigste"
                   :en-GB "Most frequent"}
   :mouse-button-assignment-illegal {:de-DE "Sie können nicht allen drei Maustasten die gleiche Aktion zuweisen."
                                     :en-GB "You cannot assign the same action to all three mouse buttons."}
   :mouse-left {:de-DE "Linke Maustaste"
                :en-GB "Left mouse button"}
   :mouse-right {:de-DE "Rechte Maustaste"
                 :en-GB "Right mouse button"}
   :mouse-middle {:de-DE "Mittlere Maustaste"
                  :en-GB "Middle mouse button"}
   :mouse-pan {:de-DE "Pan"
               :en-GB "Pan"}
   :mouse-select {:de-DE "Drag / Auswählen"
                  :en-GB "Drag / Select"}
   :mouse-label {:de-DE "Arbeitsflächen- und Fensterinteraktionen"
                 :en-GB "Window Handling"}
   :mouse-tip-drag-first {:de-DE "Drag + Select"
                          :en-GB "Drag + Select"}
   :mouse-tip-drag-rest {:de-DE "Fenster bewegen und auswählen"
                         :en-GB "move and select windows"}
   :mouse-tip-pan-rest {:de-DE "Arbeitsfläche navigieren"
                        :en-GB "navigate through the workspace"}
   :mpe {:de-DE "MPE"
         :en-GB "MPE"}
   :mse {:de-DE "MSE"
         :en-GB "MSE"}
   :multi {:de-DE "Mehrfach"
           :en-GB "Multi"}
   :multi-row-entry-label {:de-DE "Spalte"
                           :en-GB "Column"}
   :multiple {:de-DE "Mehrfache Werte"
              :en-GB "Multiple"}
   :multiply {:de-DE "Aufteilen in mehrere Events"
              :en-GB "Split into multiple events"}
   :multirow-label {:de-DE "Multiple Zeilen"
                    :en-GB "Multirow"}
   :multirow-select-label {:de-DE "Spalte Multiple Zeilen"
                           :en-GB "Multirow-Column"}
   :multirow-settings-label {:de-DE "Einstellungen für multiple Zeilen"
                             :en-GB "Multirow-Settings"}
   :my-dashboards-label {:de-DE "Meine Dashboards"
                         :en-GB "My Dashboards"}
   :my-reports-label {:de-DE "Meine Reports"
                      :en-GB "My Reports"}
   :navigate-to-frame {:de-DE "Fenster fokussieren"
                       :en-GB "Focus window"}
   :navigation-exit-fullscreen {:de-DE "Vollbild beenden"
                                :en-GB "Exit fullscreen"}
   :navigation-fit-to-content {:de-DE "An den Inhalt anpassen"
                               :en-GB "Fit to content"}
   :navigation-framelist {:de-DE "Fensterliste"
                          :en-GB "Window List"}
   :navigation-framelist-preview-download-tooltip {:de-DE "Als png herunterladen"
                                                   :en-GB "Download as png"}
   :navigation-framelist-preview-refresh-tooltip {:de-DE "Fensterinhalte aktualisieren"
                                                  :en-GB "Refresh previews"}
   :navigation-framelist-preview-toggle-label {:de-DE "Fensterinhalt?"
                                               :en-GB "Preview?"}
   :navigation-framelist-preview-toggle-tooltip {:de-DE "Zeigt Fensterinhalte an"
                                                 :en-GB "Shows previews from window content"}
   :navigation-fullscreen {:de-DE "Vollbild"
                           :en-GB "Fullscreen"}
   :navigation-minimap {:de-DE "Übersichtskarte"
                        :en-GB "Minimap"}
   :navigation-reset {:de-DE "Position und Zoom zurücksetzen"
                      :en-GB "Reset zoom and position"}
   :navigation-snapping {:de-DE "Snapping"
                         :en-GB "Snapping"}
   :window-snapping {:de-DE "Fenstersnapping"
                     :en-GB "Window Snapping"}
   :grid-snapping {:de-DE "Gittersnapping"
                   :en-GB "Grid Snapping"}
   :navigation-zoom-in {:de-DE "Reinzoomen"
                        :en-GB "Zoom in"}
   :navigation-zoom-out {:de-DE "Herauszoomen"
                         :en-GB "Zoom out"}
   :nested {:de-DE "Optimales hierarchisches Modell"
            :en-GB "Optimal nested model"}
   :new-adv-attribute-type-label {:de-DE "Attribut Typ"
                                  :en-GB "Attribute Type"}
   :new-adv-context-label {:de-DE "Kontext erstellen"
                           :en-GB "Create context"}
   :new-adv-location-label {:de-DE "Position (Lat, Lon)"
                            :en-GB "Location (Lat, Lon)"}
   :new-adv-property-label {:de-DE "neues fort. Attribut"
                            :en-GB "new adv. Property"}
   :new-context-label {:de-DE "Neuer Kontext"
                       :en-GB "New Context"}
   :new-dashboard-label {:de-DE "Neues Dashboard"
                         :en-GB "New Dashboard"}
   :new-event-matching {:de-DE "neues Event erfüllt die definierten Bedingungen"
                        :en-GB "new event matches the conditions"}
   :new-events-matching {:de-DE "neue Events erfüllen die definierten Bedingungen"
                         :en-GB "new events matching the conditions"}
   :new-location-label {:de-DE "Positionsabbildung"
                        :en-GB "Location Mapping"}
   :new-project {:de-DE "Neues Projekt"
                 :en-GB "New project"}
   :new-report-label {:de-DE "Neuer Report"
                      :en-GB "New Report"}
   :new-role-info-text {:de-DE "Bitte gib einen Namen für die Rolle ein."
                        :en-GB "Please enter a name for your role."}
   :nlr {:de-DE "Logarithmische Regression"
         :en-GB "Logarithmic Regression"}
   :nlr-desc {:de-DE "Die logarithmische Regression ist ein logarithmisches Modell. Es erlaubt die Modellierung der logarithmischen Beziehnugen zwischen einer Variable (abhängigige Variable) und einer anderen Variable (unabhängige Variable)."
              :en-GB "Logarithmic regression is a logarithmic model. It allows to model logarithmic relationships between a variable (dependent variable) and another variable (independent variable)."}
   :nlr-value-not-greater-than-zero {:de-DE "In dem Algorithmus Logarithmische Regression können nur positive Werte für die unabhängige Variable verwendet werden."
                                     :en-GB "Values for the independent variable have to be positive for the algorithm Logarithmic Regression"}
   :no {:de-DE "Nein"
        :en-GB "No"}
   :no-data {:de-DE "Keine Vorschaudaten"
             :en-GB "No preview data"}
   :no-data-tooltip {:de-DE "Diese Funktion steht erst zur Verfügung wenn Daten angezeigt werden"
                     :en-GB "Only available with data"}
   :no-demo-data-hint {:de-DE "Ohne Beispieldaten ist etwas unvollständig oder fehlerhaft.
                             Überprüfe deine Daten und das Datenmapping"
                       :en-GB "Sample data is missing, there is something wrong.
                             Check your data and data mapping"}
   :no-element-at {:de-DE "Keine Element für "
                   :en-GB "No element at"}
   :no-free-space {:de-DE "Du hast keinen freien Speicherplatz mehr.
                         Lösche Datenquellen, um mehr Daten importieren zu können"
                   :en-GB "You are running out of free space.
                         Delete datasources to import more data"}
   :no-n {:de-DE "Nein"
          :en-GB "No"}
   :no-notifications {:de-DE "Du hast keine Benachrichtigungen."
                      :en-GB "You have no notifications."}
   :no-operations-hint {:de-DE "Die Mosaik-Funktionen sind unvollständig oder fehlerhaft.
                              Überprüfe deine Daten und das Datenmapping"
                        :en-GB "Mosaic features are missing, there is something wrong.
                              Check your data and data mapping"}
   :no-or-wrong-aggregation-base-defined-error {:de-DE "Aggregieren nach ist nicht definiert"
                                                :en-GB "Aggregate by is not defined"}
   :no-seasonality {:de-DE "Keine Saisonalität"
                    :en-GB "No seasonality"}
   :node-edge-message {:de-DE "Knoten- und Kantenlimit überschritten: "
                       :en-GB "Node and edge limit exceeded: "}
   :node-message {:de-DE "Knotenlimit überschritten: "
                  :en-GB "Node limit exceeded: "}
   :nodes-str {:de-DE "Knoten"
               :en-GB "nodes"}
   :non-empty-colorscale-name {:de-DE "Farbskalaname muss ausgefüllt sein"
                               :en-GB "You need to define color scale name"}
   :non-empty-indicator-name {:de-DE "Indikatorname muss
                                    ausgefüllt sein"
                              :en-GB "You need to define an
                                    indicator name"}
   :none {:de-DE "Keine"
          :en-GB "None"}
   :none-rights {:de-DE "Keine Rechte"
                 :en-GB "no rights"}
   :normal-ops-label {:de-DE "Normale Optionen"
                      :en-GB "Normal Options"}
   :normalization {:de-DE "Normalisation"
                   :en-GB "Normalization"}
   :normalize-tooltip {:de-DE "Normalisieren"
                       :en-GB "Normalize"}
   :not-enough-data-error {:de-DE "Nicht genug Daten - Eine Auswahl resultiert in keinem oder nur einem Datenpunkt."
                           :en-GB "Not enough data - nothing selected or only one data point selected."}
   :not-much-free-space {:de-DE "Du hast nur noch wenig freien Speicherplatz zur Verfügung"
                         :en-GB "You are running out of free space"}
   :not-triggered {:de-DE "Noch nicht ausgelöst"
                   :en-GB "not yet triggered"}
   :notes-input-hint {:de-DE "Dies ist der einzige Text, der aus bis zu 2000 Zeichen bestehen kann, er kann auch aus mehreren Spalten zusammengesetzt werden. Die Informationen werden in der Detailansicht als erstes angezeigt."
                      :en-GB "This is the only text that can be up to 2000 characters long. It may be composed of multiple columns. This information is shown first in the detail view."}
   :notes {:de-DE "Notiz"
           :en-GB "Note"}
   :note-bold {:de-DE "Fett"
               :en-GB "Bold"}
   :note-italic {:de-DE "Kursiv"
                 :en-GB "Italic"}
   :note-unterline {:de-DE "Unterstreichen"
                    :en-GB "Underline"}
   :note-strike {:de-DE "Durchstreichen"
                 :en-GB "Strike"}
   :note-font-color-group {:de-DE "Farbe"
                           :en-GB "Color"}
   :note-highlight-color-group {:de-DE "Hervorheben"
                                :en-GB "Highlight"}
   :note-background-color-group {:de-DE "Füllfarbe"
                                 :en-GB "Fill color"}
   :note-size-group {:de-DE "Schriftgröße"
                     :en-GB "Font size"}
   :note-font-small {:de-DE "Klein"
                     :en-GB "Small"}
   :note-font-normal {:de-DE "Normal"
                      :en-GB "Normal"}
   :note-font-large {:de-DE "Groß"
                     :en-GB "Large"}
   :note-font-huge {:de-DE "Riesig"
                    :en-GB "Huge"}
   :note-list-group {:de-DE "Liste"
                     :en-GB "List"}
   :note-bulletpoints {:de-DE "Aufzählungspunkte"
                       :en-GB "Bulletpoints"}
   :note-numbering {:de-DE "Nummerierung"
                    :en-GB "Numbering"}
   :note-align-group {:de-DE "Textausrichtung"
                      :en-GB "Align"}
   :note-align-right {:de-DE "Rechtsbündig"
                      :en-GB "Right-aligned"}
   :note-align-center {:de-DE "Zentriert"
                       :en-GB "Centered"}
   :note-align-left {:de-DE "Linksbündig"
                     :en-GB "Left-aligned"}
   :note-align-justify {:de-DE "Blocksatz"
                        :en-GB "Justified"}
   :note-remove-formatting {:de-DE "Formatierung entfernen"
                            :en-GB "Remove formatting"}
   :note-white {:de-DE "Weiß"
                :en-GB "White"}
   :note-gray {:de-DE "Grau"
               :en-GB "Gray"}
   :note-black {:de-DE "Schwarz"
                :en-GB "Black"}
   :note-red {:de-DE "Rot"
              :en-GB "Red"}
   :note-orange {:de-DE "Orange"
                 :en-GB "Orange"}
   :note-yellow {:de-DE "Gelb"
                 :en-GB "Yellow"}
   :note-green {:de-DE "Grün"
                :en-GB "Green"}
   :note-teal {:de-DE "Türkis"
               :en-GB "Teal"}
   :note-blue {:de-DE "Blau"
               :en-GB "Blue"}
   :notification-title {:de-DE "Alarm \"$title\" ausgelöst"
                        :en-GB "Alert \"$title\" triggered"}
   :notifications-title {:de-DE "Benachrichtigungen"
                         :en-GB "Notifications"}
   :notify-frequency-text {:de-DE "Benachrichtige mich..."
                           :en-GB "Notify me..."}
   :notify-multi-cond {:de-DE "Benachrichtige mich über neue Events, die folgende Bedingungen erfüllen:"
                       :en-GB "Notify me when a new event matches the following conditions:"}
   :notify-no-cond {:de-DE "Benachrichtige mich über neue Events."
                    :en-GB "Notify me when a new event is added."}
   :notify-single-cond {:de-DE "Benachrichtige mich über neue Events, die die folgende Bedingung erfüllen:"
                        :en-GB "Notify me when a new event matches the following condition:"}
   :number {:de-DE "Zahl"
            :en-GB "Number"}
   :number-of-colors {:de-DE "Anzahl Farben"
                      :en-GB "Number of colors"}
   :number-of-events {:de-DE "Eventanzahl"
                      :en-GB "Number of Events"}
   :number-of-events-label {:de-DE "Anzahl an Events"
                            :en-GB "Number of events"}
   :number-of-events-multiply {:de-DE "Multiply und Eventanzahl resultiert in nicht verständlichen Ergebnissen"
                               :en-GB "Multiply and number of events will result not comprehensible results"}
   :numeric {:de-DE "numerisch"
             :en-GB "numeric"}
   :of-label {:de-DE "von"
              :en-GB "of"}
   :ok {:de-DE "OK"
        :en-GB "OK"}
   :ols {:de-DE "gewöhnliche kleinste Quadrate"
         :en-GB "Oridnary Least Square"}
   :olsc {:de-DE "gewöhnliche kleinste Quadrate mit Schwellwert"
          :en-GB "Ordinary least squares subst selector with a treshold"}
   :one-hot {:de-DE "One-Hot"
             :en-GB "One hot"}
   :open {:de-DE "Öffnen"
          :en-GB "Open"}
   :open-project-step {:de-DE "Projektschritt öffnen"
                       :en-GB "Open project step"}
   :open-project-at-step {:de-DE "Wähle einen Schritt"
                          :en-GB "Select a step"}
   :locked {:de-DE "Gesperrt"
            :en-GB "Locked"}
   :open-in-new-tab {:de-DE "In neuem Tab öffnen"
                     :en-GB "Open in new tab"}
   :open-new-tab-label {:de-DE "Als neuen Tab öffnen"
                        :en-GB "Open as new tab"}
   :operation-skeleton {:de-DE "$condition1 $operator $condition2"
                        :en-GB "$condition1 $operator $condition2"}
   :operator-and {:de-DE "und"
                  :en-GB "and"}
   :operator-or {:de-DE "oder"
                 :en-GB "or"}
   :optional-attributes-label {:de-DE "Besondere Attribute"
                               :en-GB "Optional attributes"}
   :optional-grouping-not-valid {:de-DE "Optionale Gruppierung ist nicht valide, ändere oder entferne die Auswahl."
                                 :en-GB "Optional grouping not valid, either change or remove the grouping."}
   :ordinal {:de-DE "Ordinal / Label"
             :en-GB "Ordinal / Label"}
   :overlay-label {:de-DE "Formen"
                   :en-GB "Shapes"}
   :overlayer-section-title {:de-DE "Overlayer"
                             :en-GB "Overlayer"}
   :overview-description {:de-DE "Die zur Zeit markierten Fenster werden als Basis genommen"
                          :en-GB "Adds all currently selected windows to the project"}
   :overview-description-deactivated {:de-DE "Räume die Arbeitsfläche auf, um ein neues Projekt zu erstellen"
                                      :en-GB "To create a new project, clean the workspace first."}
   :overview-label {:de-DE "Zur Übersicht"
                    :en-GB "Overview"}
   :overview-steps {:de-DE " (%s Schritte)"
                    :en-GB " (%s steps)"}
   :overview-title {:de-DE "Projektübersicht"
                    :en-GB "Project Overview"}
   :overview-title-deactivated {:de-DE "Bereits ein Projekt geladen"
                                :en-GB "A project is already loaded"}
   :overview-toolip-add-project {:de-DE "Projekt hinzufügen"
                                 :en-GB "Add project"}
   :overview-toolip-close {:de-DE "Dieses Projekt löschen"
                           :en-GB "Delete project"}
   :overview-toolip-focus {:de-DE "Projekt auf der Arbeitsfläche anzeigen"
                           :en-GB "Show project on workspace"}
   :overview-toolip-hide {:de-DE "Projekt von der Arbeitsfläche entfernen"
                          :en-GB "Remove project from workspace"}
   :overview-toolip-share {:de-DE "Dieses Projekt teilen"
                           :en-GB "Share project"}
   :overview-tooltip-copy {:de-DE "Projekt kopieren"
                           :en-GB "Copy project"}
   :overview-tooltip-delete-locked {:de-DE "Das Projekt kann nicht gelöscht werden da es derzeit geöffnet ist von: "
                                    :en-GB "The project can't be deleted because it is opened by: "}
   :overview-tooltip-open-locked {:de-DE "Das Projekt ist derzeit geöffnet von: "
                                  :en-GB "Project is currently opened by: "}
   :overview-tooltip-overview-hide {:de-DE "Projekt aus der Übersicht entfernen"
                                    :en-GB "Remove project from project overview"}
   :overview-tooltip-read-only {:de-DE "Das Projekt ist schreibgeschützt, Du kannst es nicht löschen"
                                :en-GB "You cannot delete a read-only project"}
   :overwrite-label {:de-DE "Überschreiben"
                     :en-GB "Overwrite"}
   :overwrite-layout-question {:de-DE "Möchtest du das bestehende Layout überschreiben?"
                               :en-GB "Do you want to overwrite the existing layout?"}
   :overwrite-layout-title {:de-DE "Layout überschreiben"
                            :en-GB "Overwrite layout"}
   :overwrite-overlayer-question {:de-DE "Möchtest du den bestehenden Overlayer überschreiben?"
                                  :en-GB "Do you want to overwrite the existing overlayer?"}
   :overwrite-overlayer-title {:de-DE "Overlayer überschreiben"
                               :en-GB "Overwrite overlayer"}
   :own-indicators-list-label {:de-DE "Deine Indikatoren"
                               :en-GB "Your Indicators"}
   :p {:de-DE "P"
       :en-GB "P"}
   :p-value-backward {:de-DE "P-Wert Rückwärts"
                      :en-GB "P-Value backward"}
   :p-value-forward {:de-DE "P-Wert Vorwärts"
                     :en-GB "P-Value forward"}
   :pace2 {:de-DE "PACE2"
           :en-GB "PACE2"}
   :pace4 {:de-DE "PACE4"
           :en-GB "PACE4"}
   :pace6 {:de-DE "PACE6"
           :en-GB "PACE6"}
   :page-size {:de-DE "Seitengröße"
               :en-GB "Page size"}
   :parameter {:de-DE "Parameter"
               :en-GB "Parameter"}
   :patent-init-center {:de-DE "Patentierter Algoritmus (SAP)"
                        :en-GB "Patent of selecting the init center"}
   :projects-protocol-action-change-annotation {:de-DE "Notizen ändern"
                                                :en-GB "Change annotations"}
   :export-displayed-datasources {:de-DE "Datenquellen:"
                                  :en-GB "Datasources:"}
   :pdf-footer-created {:de-DE "Erstellt von: "
                        :en-GB "Created by: "}
   :pdf-footer-date {:de-DE "Datum: "
                     :en-GB "Date: "}
   :pdf-footer-exported {:de-DE "Mit $app exportiert durch "
                         :en-GB "Exported with $app by "}
   :pdf-footer-page {:de-DE "Seite "
                     :en-GB "Page "}
   :config-export-show-date {:de-DE "Export Datum"
                             :en-GB "Export date"}
   :config-export-show-time {:de-DE "Export Zeit"
                             :en-GB "Export time"}
   :config-export-show-user {:de-DE "Nutzer, der den Export erstellt hat"
                             :en-GB "User who created the export"}
   :config-export-show-datasources {:de-DE "Datenquellen Zitierung"
                                    :en-GB "Datasource citation"}
   :config-export-custom-description {:de-DE "Benutzerdefinierter Text (%s)"
                                      :en-GB "Custom text (%s)"}
   :config-export-datasource-mapping {:de-DE "Datenquellen Zitierungen"
                                      :en-GB "Datasource citations"}
   :config-export-datasource-mapping-hint {:de-DE "In erstellten Exports werden sichtbare Datenquellen auf die unten angegebene Zitierung abgebildet"
                                           :en-GB "In the export displayed datasources will be mapped to the given citation below (if available)"}
   :config-export-intro {:de-DE "Beim Export (PNG & PDF) können automatisch Informationen in Form einer Annotation angehängt werden."
                         :en-GB "During export (PNG & PDF), it is possible to automatically attach information in an annotation."}
   :penalization-method {:de-DE "Benachteilungsmethode"
                         :en-GB "Penalization methods"}
   :penalization-weight {:de-DE "Benachteilungsgewichtung"
                         :en-GB "Penalization weights"}
   :periods {:de-DE "Perioden"
             :en-GB "Periods"}
   :pie-chart-label {:de-DE "Tortendiagramm"
                     :en-GB "Pie chart"}
   :placeholder {:de-DE "Rollenname"
                 :en-GB "Role name"}
   :png-export {:de-DE "Bild (PNG)"
                :en-GB "Image (PNG)"}
   :pnr {:de-DE "Polynomregression"
         :en-GB "Polynomial Regression"}
   :pnr-desc {:de-DE "Die Polynomregression ist ein Ansatz zur Modellierung der Beziehung zwischen einer skalaren Variable Y und einer mit X bezeichneten Variable. Bei der Polynomregression werden Daten mithilfe von Polynomfunktionen modelliert und unbekannte Modellparameter aus den Daten geschätzt."
              :en-GB "Polynomial regression is an approach to modeling the relationship between a scalar variable y and a variable denoted X. In polynomial regression, data is modeled using polynomial functions, and unknown model parameters are estimated from the data."}
   :pnr-not-enough-data {:de-DE "Nicht genug Daten - Dieser Algorithmus erfordert mehr Beobachtungen als derzeit bereitgestellt"
                         :en-GB "Not enough data - this algorithm requires more observations than is currently provided"}
   :policy-share {:de-DE "Teilen"
                  :en-GB "Share"}
   :policy-share-layout {:de-DE "Layout teilen"
                         :en-GB "Share layout"}
   :polynomial {:de-DE "Polynom"
                :en-GB "Polynomial"}
   :polynomial-num {:de-DE "Grad des Modells"
                    :en-GB "Degree of the model"}
   :positive {:de-DE "nur positive Koeffizienten"
              :en-GB "positiv coefficients only"}
   :post-proc-dialog-confirm {:de-DE "Ja, überschreiben"
                              :en-GB "Yes, overwrite"}
   :post-proc-dialog-explanations {:de-DE "Dein Projekt enthält Konfigurationen, die seitdem geändert wurden. Du kannst sie aktualisieren indem du diesen Dialog bestätigst. Alle Änderungen können im Protokoll rückgänig gemacht werden."
                                   :en-GB "Some configurations in your project have changed since. If you press yes the configurations will be updated in your project. With the protocol you can undo the overwrite."}
   :post-proc-dialog-headline {:de-DE "Möchtest du die Konfigurationen deines Projektes aktualisieren?"
                               :en-GB "Do you want to update the configuration from your project?"}
   :post-proc-dialog-title {:de-DE "Überschreiben?"
                            :en-GB "Overwrite?"}
   :predict-button {:de-DE "Prädiktion ausführen"
                    :en-GB "Run Prediction"}
   :prediction-done {:de-DE "Prädiktion abgeschlossen!"
                     :en-GB "Prediction done!"}
   :prediction-error {:de-DE "Fehler bei der Prädiktion!"
                      :en-GB "Prediction error!"}
   :prediction-error-label {:de-DE "Prädiktionsfehler"
                            :en-GB "Prediction error"}
   :prediction-error-section {:de-DE "Fehler"
                              :en-GB "Error"}
   :prediction-name-duplicate {:de-DE "Modellname existiert bereits"
                               :en-GB "Model name already exists"}
   :prediction-publish {:de-DE "Ergebnis speichern"
                        :en-GB "Save result"}
   :prediction-result-section {:de-DE "Ergebnisse"
                               :en-GB "Results"}
   :prediction-running {:de-DE "Prädiktion wird ausgeführt"
                        :en-GB "Prediction is running"}
   :prediction-save {:de-DE "Modell speichern:"
                     :en-GB "Save model:"}
   :prediction-warning {:de-DE "Prädiktion abgeschlossen mit Warnungen!"
                        :en-GB "Prediction done with warning!"}
   :prediction-warning-label {:de-DE "Prädiktionswarnungen"
                              :en-GB "Prediction warning"}
   :prediction-warning-section {:de-DE "Warnung"
                                :en-GB "Warning"}
   :presentation-add-slide {:de-DE "Neue Folie"
                            :en-GB "Add Slide"}
   :presentation-mode {:de-DE "Präsentationsmodus"
                       :en-GB "Presentation Mode"}
   :presentation-of {:de-DE "von"
                     :en-GB "of"}
   :presentation-play-button {:de-DE "Präsentation starten"
                              :en-GB "Present"}
   :presentation-remove-all-button {:de-DE "Alle Folien entfernen"
                                    :en-GB "Remove all slides"}
   :presentation-surround-button {:de-DE "Alle Fenster erfassen"
                                  :en-GB "Surround all windows"}
   :privacy-label {:de-DE "Datenschutz"
                   :en-GB "Privacy"}
   :probability {:de-DE "Konfidenz"
                 :en-GB "Confidence"}
   :problem-type {:de-DE "Problemtyp"
                  :en-GB "Problem Type"}
   :problem-type-select {:de-DE "Problemtyp"
                         :en-GB "Problem Type"}
   :proceed-warning-dialog {:de-DE "Trotzdem aufräumen"
                            :en-GB "Clean anyway"}
   :proceed-warning-project-dialog {:de-DE "Aufräumen"
                                    :en-GB "Clean"}
   :product-tour-back-button-label {:de-DE "Zurück"
                                    :en-GB "Back"}
   :product-tour-closed {:de-DE "Produkttour geschlossen"
                         :en-GB "Product tour closed."}
   :product-tour-desc-c-details {:de-DE "Schließe die Detailansicht. Ein Fenster kann mit einem Klick auf x geschlossen werden."
                                 :en-GB "You can close a window by clicking on x. Close the detail view."}
   :product-tour-desc-c-search {:de-DE "Ziehe die Suche über den Titel auf das Mosaik-Fenster. Wenn du loslässt, sind die beiden Fenster verbunden und die Daten werden in Mosaik geladen."
                                :en-GB "Drag and drop the search window onto the mosaic window. Afterwards, the windows are connected and the data will be loaded into mosaic"}
   :product-tour-desc-c-search-vis {:de-DE "Verbinde das Visualisierungsfenster mit der Suche.
                                          Ziehe dazu die Suche per Drag & Drop auf das Visualisierungsfenster."
                                    :en-GB "Connect the visualization window with the search.
                                          For that, drag and drop the search onto the other window."}
   :product-tour-desc-cl-mosaic-1 {:de-DE "In der Legende (rechte Seite) kannst du das selektierte Layout mit seinen Werten ansehen. Später kannst du das Layout wechseln oder anpassen. Schließe die Legende über einen Klick auf das Legendensymbol "
                                   :en-GB "In the legend (right side) you can see the selected layout with its value assignments. Later you can change the layout. Close the legend by clicking on the legend button "}
   :product-tour-desc-cl-mosaic-2 {:de-DE " ."
                                   :en-GB " ."}
   :product-tour-desc-d-search-1 {:de-DE "Wähle die freie Suche aus, klicke auf Thema/Datenquelle und wähle ein Thema oder eine Datenquelle aus, klicke auf Geografisch und wähle ein Land zum Starten. 
                                        Starte die Suche über den Suche-Button "
                                  :en-GB "Choose the free search, click on topic/datasource and choose a topic/datasource, click on geographic attributes and choose a country to start. 
                                        Start the search over the search button "}
   :product-tour-desc-d-search-2 {:de-DE "."
                                  :en-GB "."}
   :product-tour-desc-d-vis-1 {:de-DE "Öffne ein weiteres Explorationsfenster mithilfe der Werkzeug-Buttons. "
                               :en-GB "Open another exploration view with one of the buttons. "}
   :product-tour-desc-d-vis-2 {:de-DE " Diese öffnen ein neues Fenster, welches mit der Suche verbunden ist."
                               :en-GB " This will open a new window which is directly connected to the search."}
   :product-tour-desc-e-search {:de-DE "Klicke auf Suchen, um die Suche durchzuführen."
                                :en-GB "Click on search, to execute the defined query."}
   :product-tour-desc-m-mosaic-1 {:de-DE "Verschiebe Mosaik über den Titel mit Drag & Drop, sodass beide Fenster zu sehen sind. Die Arbeitsfläche kann nun mit Drag & Drop über die "
                                  :en-GB "Move the mosaic window arround with drag & drop so you can see both windows. In addition, you can pan (move on the work space) with the "}
   :product-tour-desc-m-mosaic-2 {:de-DE " ebenfalls bewegt werden."
                                  :en-GB "."}
   :product-tour-mouse-pref-left {:de-DE "linke Maustaste"
                                  :en-GB "left mouse button"}
   :product-tour-mouse-pref-middle {:de-DE "mittlere Maustaste"
                                    :en-GB "middle mouse button"}
   :product-tour-mouse-pref-right {:de-DE "rechte Maustaste"
                                   :en-GB "right mouse button"}
   :product-tour-desc-m-vis {:de-DE "Verschieb das neue Fenster, sodass du beide sehen kannst."
                             :en-GB "Move the new window so you can see the search and the new window."}
   :product-tour-desc-n-search {:de-DE "Hier kann man neue Fenster öffnen, zum Beispiel die Konfiguration.
                                      Aktuell gibt es keine Rechte für die Suche, weshalb die Visualisierungsfenster nicht sinnvoll genutzt werden können.
                                      Frag deinen Administrator für mehr Informationen."
                                :en-GB "Here you can open windows like settings.
                                      Currently you have no rights to use the search so the visualization windows can't be used properly.
                                      You can ask your admin for more infos."}
   :product-tour-desc-o-details {:de-DE " Zoome hinein bis du den Text auf einer Objektkarte lesen kannst und führe auf dieser einen Doppelklick aus. Dies öffnet eine Detailansicht mit allen Informationen über das Event."
                                 :en-GB "Double click on it to open the detail view, which contains the whole information of an event."}
   :product-tour-desc-o-mosaic-1 {:de-DE "Klick auf das Mosaik Symbol"
                                  :en-GB "Click on mosaic"}
   :product-tour-desc-o-mosaic-2 {:de-DE " um ein Explorationsfenster zu öffnen."
                                  :en-GB " to open an exploration window."}
   :product-tour-desc-o-search-1 {:de-DE "Öffne eine Suche "
                                  :en-GB "Open a search "}
   :product-tour-desc-o-search-2 {:de-DE ", um die Datenbasis deiner Analyse zu definieren."
                                  :en-GB " to define the data that you want to analyze."}
   :product-tour-desc-o-vis {:de-DE "Öffne ein Visualisierungsfenster, um die Ergebnisse der Suche darzustellen.
                                   Nutze beispielweise eine Tabelle."
                             :en-GB "Open a visualization window to visualize the search result, for example in a table."}
   :product-tour-desc-op-overviews-1 {:de-DE "Hier "
                                      :en-GB "Here "}
   :product-tour-desc-op-overviews-2 {:de-DE " werden alle sichtbaren Projekte angezeigt.
                                            Die Projekte können geladen, geteilt oder auch gelöscht werden."
                                      :en-GB " you can see all the available projects.
                                            In this overview you can load, share, and delete your projects."}
   :product-tour-desc-os-project-1 {:de-DE "Speichere "
                                    :en-GB "Save "}
   :product-tour-desc-os-project-2 {:de-DE " den aktuellen Stand deiner Arbeit als Projekt."
                                    :en-GB " your current state as a project so you can work on it later."}
   :product-tour-desc-pz-mosaic-1 {:de-DE "Exploriere die Arbeitsfläche mittels Panning und Zoomen. Nutze das Mausrad, um zu zoomen und halte zum Pannen die "
                                   :en-GB "Explore with pan and zoom! Use the mousewheel to zoom in on an event until you can read the text. Hold the "}
   :product-tour-desc-pz-mosaic-2 {:de-DE " gedrückt während du die Maus bewegst, damit veränderst du den sichtbaren Ausschnitt der Arbeitsfläche. "
                                   :en-GB " and move the mouse to pan to another event. "}
   :product-tour-desc-s-mosaic-1 {:de-DE "Nachdem die Daten geladen sind, siehst du viele einzelne Datenpunkte (die Eventsammlung).
                                       In der Werkzeugleiste (oberhalb des Fensters) findest du die wichtigsten Analysefunktionen. 
                                       Nutze das Sortieren "
                                  :en-GB "After the data is loaded you will see a lot of datapoints (known as event set). In the toolbar (above the window) you will find the important analyze functions.
                                       Use the sorting "}
   :product-tour-desc-s-mosaic-2 {:de-DE " um die Eventsammlung nach einem Attribut zu sortieren. Die Anfangssortierung ist immer nach Datum."
                                  :en-GB " to sort your events by an attribute. The default sorting is always by date."}
   :product-tour-desc-s-project {:de-DE "Vergib einen Projektnamen sowie optional eine Beschreibung, wenn du den Kontext des Projektes aufzeigen möchtest."
                                 :en-GB "Give your project a title and optionally a description."}
   :product-tour-done {:de-DE "Produkttour abgeschlossen 🎉"
                       :en-GB "Product tour done 🎉"}
   :product-tour-next-button-label {:de-DE "Weiter"
                                    :en-GB "Next"}
   :product-tour-popup-cancel {:de-DE "Später starten"
                               :en-GB "Start later"}
   :product-tour-popup-msg {:de-DE "Herzlich willkommen bei Explorama Lerne die Explorationssoftware in wenigen Schritten kennen. Wenn du die Produkttour später nochmal durchführen möchtest, findest du sie unten links auf der Willkommensseite."
                            :en-GB "Welcome to Explorama! Learn in a few steps how to use the exploration software. You can start the product tour on the bottom left of the welcome page later again."}
   :product-tour-popup-start {:de-DE "Starte Produkttour"
                              :en-GB "Start product tour"}
   :product-tour-popup-title {:de-DE "Willkommen"
                              :en-GB "Welcome"}
   :product-tour-step-done {:de-DE " Schritt erledigt"
                            :en-GB " Step completed"}
   :product-tour-steps-label {:de-DE "Schritte %s/%s"
                              :en-GB "Steps %s/%s"}
   :product-tour-title-c-details {:de-DE "Fenster schließen"
                                  :en-GB "Close the detail view"}
   :product-tour-title-c-search {:de-DE "Visualisiere die Daten"
                                 :en-GB "Visualize data in mosaic"}
   :product-tour-title-c-search-vis {:de-DE "Mit der Suche verbinden"
                                     :en-GB "Connect with the search"}
   :product-tour-title-cl-mosaic {:de-DE "Was bedeuten die Farben?"
                                  :en-GB "Meaning of different colors"}
   :product-tour-title-d-search {:de-DE "Definiere die Suche"
                                 :en-GB "Define search"}
   :product-tour-title-d-vis {:de-DE "Öffne ein anderes Explorationswerkzeug"
                              :en-GB "Open another visualization"}
   :product-tour-title-e-search {:de-DE "Definiere deine Datenbasis"
                                 :en-GB "Define your data basis"}
   :product-tour-title-m-mosaic {:de-DE "Verschiebe Mosaik"
                                 :en-GB "Move mosaic"}
   :product-tour-title-m-vis {:de-DE "Fenster verschieben"
                              :en-GB "Move the window"}
   :product-tour-title-n-search {:de-DE "Fenster öffnen"
                                 :en-GB "Open windows"}
   :product-tour-title-o-details {:de-DE "Detailansicht öffnen"
                                  :en-GB "Detail view"}
   :product-tour-title-o-mosaic {:de-DE "Mosaik öffnen"
                                 :en-GB "Open mosaic"}
   :product-tour-title-o-search {:de-DE "Suche öffnen"
                                 :en-GB "Open a search"}
   :product-tour-title-o-vis {:de-DE "Öffne eine Visualisierung"
                              :en-GB "Open visualization window"}
   :product-tour-title-op-overview {:de-DE "Projektübersicht"
                                    :en-GB "Project overview"}
   :product-tour-title-os-project {:de-DE "Speichere deine Arbeit"
                                   :en-GB "Save your work"}
   :product-tour-title-pz-mosaic {:de-DE "Pan und Zoom"
                                  :en-GB "Pan/Zoom"}
   :product-tour-title-s-mosaic {:de-DE "Sortieren nach"
                                 :en-GB "Sort by"}
   :product-tour-title-s-project {:de-DE "Speichern abschließen"
                                  :en-GB "Complete saving"}
   :project-desc-placeholder {:de-DE "Füge eine Beschreibung hinzu..."
                              :en-GB "Add a project description..."}
   :project-indicators-list-label {:de-DE "Projekt Indikatoren"
                                   :en-GB "Project Indicators"}
   :project-loading-message {:de-DE "Projekt wird geladen.."
                             :en-GB "Loading project.."}
   :project-must-be-loaded {:de-DE "Das Projekt muss geladen sein."
                            :en-GB "You need to load the project."}
   :project-without-steps {:de-DE "Projekt enthält keine Schritte."
                           :en-GB "Project contains no steps."}
   :property-required {:de-DE "Mind. eine Eigenschaft ist erforderlich"
                       :en-GB "At least one property is required"}
   :property-required-hint {:de-DE "Füge unten mindestens ein zusätzliches Attribut (nicht Kontext) hinzu."
                            :en-GB "Add at least one additional non-context attribute below."}
   :protocol-action-change-in-layout {:de-DE "Änderung am Layout"
                                      :en-GB "Change in Layout"}
   :protocol-already-open {:de-DE "Protokoll-Fenster schon geöffnet"
                           :en-GB "Protocol-Window already open"}
   :protocol-couldnt-open {:de-DE "Protokoll-Fenster nicht möglich"
                           :en-GB "Protocol-Window not possible"}
   :protocol-form-ok {:de-DE "OK"
                      :en-GB "OK"}
   :protocol-no-project-loaded {:de-DE "Es ist kein Projekt geladen. Protokolle können nur im Rahmen von Projekten genutzt werden. Bitte speicher deine Analyse zuerst als Projekt."
                                :en-GB "No project loaded. Protocols can only be used if a project is loaded. Please save your work as a project first."}
   :protocol-project-read-only {:de-DE "Das Projekt ist schreibgeschützt."
                                :en-GB "Project is read-only"}
   :protocol-window-title {:de-DE "Protokoll"
                           :en-GB "Protocol"}
   :public-hint-text {:de-DE "Diese Einstellung gibt jedem Nutzer Zugriffsrechte."
                      :en-GB "This setting will grant access to every user."}
   :public-label {:de-DE "Öffentlich?"
                  :en-GB "Public?"}
   :publish-dashboard-header-label {:de-DE "Dashboard teilen:"
                                    :en-GB "Share Dashboard:"}
   :publish-label {:de-DE "Veröffentlichen"
                   :en-GB "Publish"}
   :publish-report-header-label {:de-DE "Report teilen:"
                                 :en-GB "Share Report:"}
   :q {:de-DE "Q"
       :en-GB "Q"}
   :qr-decomposition {:de-DE "QR Decomposition"
                      :en-GB "QR Decomposition"}
   :quarter {:de-DE "Quartal"
             :en-GB "Quarter"}
   :r-attribute-label {:de-DE "Attribut Größe"
                       :en-GB "Size Attribute"}
   :radial-basis-function {:de-DE "Radiale Basisfunktion"
                           :en-GB "Radial Basis Function"}
   :random-with-replacement {:de-DE "Zufällig mit Ersatzwert"
                             :en-GB "Random with replacement"}
   :random-without-replacement {:de-DE "Zufällig ohne Ersatzwert"
                                :en-GB "Random without replacement"}
   :range {:de-DE "Zahlenbereich"
           :en-GB "Range"}
   :range-start {:de-DE "Zahlenbereich - Startwert"
                 :en-GB "Range - first value"}
   :range-end {:de-DE "Zahlenbereich - Endwert"
               :en-GB "Range - last value"}
   :read-only-projects {:de-DE "Schreibgeschützte Projekte"
                        :en-GB "Read-only projects"}
   :read-only {:de-DE "Schreibgeschützt"
               :en-GB "Read-only"}
   :read-only-share {:de-DE "Verwendung"
                     :en-GB "Usage only"}
   :read-only-share-groups {:de-DE "Verwendung (Gruppe)"
                            :en-GB "Use only (group)"}
   :read-only-share-user {:de-DE "Verwendung (Nutzer)"
                          :en-GB "Use only (user)"}
   :read-rights {:de-DE "Nutzungs- und Leserechte"
                 :en-GB "use and read-rights"}
   :redo-not-possible-layouts {:de-DE "Layouts"
                               :en-GB "Layouts"}
   :redo-not-possible-multi {:de-DE "Die folgenden Operationen können nicht erneut ausgeführt werden:\n"
                             :en-GB "The following operations cannot be performed again:\n"}
   :redo-not-possible-single {:de-DE "Die folgende Operation kann nicht erneut ausgeführt werden:\n"
                              :en-GB "The following operation cannot be performed again:\n"}
   :redo-no-data {:de-DE "Die Ergebnismenge ist leer."
                  :en-GB "Empty result set."}
   :reg-zeros {:de-DE "Die Trainingsdaten beinhalten '0'-Werte, der Algorithms liefert unter Umständen kein sinnvolles Ergebnis."
               :en-GB "The training data contains '0'-values, the algorithm may not return a useful result."}
   :regularization-parameter {:de-DE "Regularisierungsparameter"
                              :en-GB "Regularization parameter"}
   :related-to {:de-DE "Verknüpft"
                :en-GB "Related"}
   :relation-eq {:de-DE "gleich"
                 :en-GB "equal to"}
   :relation-ge {:de-DE "größer gleich"
                 :en-GB "greater than or equal to"}
   :relation-gt {:de-DE "größer als"
                 :en-GB "greater than"}
   :relation-le {:de-DE "kleiner gleich"
                 :en-GB "less than or equal to"}
   :relation-lt {:de-DE "kleiner als"
                 :en-GB "less than"}
   :remove-label {:de-DE "Entfernen"
                  :en-GB "Remove"}
   :timeline {:de-DE "Zeitleiste"
              :en-GB "Timeline"}
   :optimize {:de-DE "Optimieren"
              :en-GB "Optimize"}
   :scatter {:de-DE "Streu"
             :en-GB "Scatter"}
   :reorder-tooltip-text-horizontal {:de-DE "Horizontal anordnen (Zeitleiste)"
                                     :en-GB "Arrange horizontally (timeline)"}
   :reorder-tooltip-text-vertical {:de-DE "Breitenanpassung"
                                   :en-GB "Fit width"}
   :replace {:de-DE "Ersetzen"
             :en-GB "Replace"}
   :report-label {:de-DE "Report"
                  :en-GB "Report"}
   :report-placeholder-1-label {:de-DE "Klicke, um Text einzufügen,"
                                :en-GB "Click to add text"}
   :report-placeholder-2-label {:de-DE "oder"
                                :en-GB "or"}
   :report-placeholder-3-label {:de-DE "ziehe eine Visualisierung hierher."
                                :en-GB "drag a visualization here"}
   :reporting-placeholder-short-label {:de-DE "Loslassen, um Visualisierung einzufügen"
                                       :en-GB "Drop to insert visualisation"}
   :reporting-label {:de-DE "Berichte"
                     :en-GB "Reporting"}
   :reports-label {:de-DE "Reports"
                   :en-GB "Reports"}
   :report-name-exists {:de-DE "Ein Report mit diesem Namen existiert bereits."
                        :en-GB "A report with this name already exists."}
   :request-failed-details {:de-DE "Wenn keine Ursache erkennbar ist, könnte ein Problem mit der Serververbindung bestehen. Bitte wende dich an einen Administrator"
                            :en-GB "When there is no obviously reason, it might be an problem with the server connection. Please ask your administrator"}
   :request-failed-dialog-title {:de-DE "Anfrage fehlgeschlagen"
                                 :en-GB "Request failed"}
   :required-attributes-infotext-multi {:de-DE "Attribute <p> sind erforderlich"
                                        :en-GB "Attributes <p> are required"}
   :required-attributes-infotext-one {:de-DE "Attribut <p> ist erforderlich"
                                      :en-GB "Attribute <p> is required"}
   :required-attributes-infotext-one-add {:de-DE "Mind. eines der Attribute <p> wird zusätzlich benötigt."
                                          :en-GB "At least one of the attributes <p> is additionally required."}
   :required-attributes-infotext-one-needed {:de-DE "Mind. eines der Attribute <p> wird benötigt."
                                             :en-GB "At least one of the attributes <p> is required."}
   :required-attributes-label {:de-DE "Notwendige Attribute"
                               :en-GB "Required attributes"}
   :required-attributes-num-infotext-multi {:de-DE "Füge mind. <p> zusätzliche Attribute hinzu"
                                            :en-GB "Add at least <p> additional attributes"}
   :required-attributes-num-infotext-one {:de-DE "Füge mind. <p> zusätzliches Attribut hinzu"
                                          :en-GB "Add at least <p> additional attribute"}
   :requirements-div {:de-DE "Vorraussetzungen"
                      :en-GB "Requirements"}
   :requirements-section {:de-DE "Vorraussetzungen"
                          :en-GB "Requirements"}
   :reset-selection-label {:de-DE "Auswahl zurücksetzen"
                           :en-GB "Reset Selection"}
   :result-label {:de-DE "Attributname"
                  :en-GB "Attribute names"}
   :result-settings {:de-DE "Algorithmuseinstellungen"
                     :en-GB "Algorithm Settings"}
   :retrospective-forecast {:de-DE "Rückwirkende Vorhersage"
                            :en-GB "Retrospective Forecast"}
   :ric {:de-DE "RIC"
         :en-GB "RIC"}
   :ridge {:de-DE "Ridge"
           :en-GB "Ridge"}
   :right-label {:de-DE "Rechts"
                 :en-GB "Right"}
   :rights-roles-add-role-label {:de-DE "Rollenname"
                                 :en-GB "Role name"}
   :rights-roles-add-role-title {:de-DE "Neue Rolle"
                                 :en-GB "New role"}
   :rights-roles-management {:de-DE "Verwaltung von Rechten und Rollen"
                             :en-GB "Rights & roles management"}
   :rmse {:de-DE "RMSE"
          :en-GB "RMSE"}
   :role-name-empty {:de-DE "Rollenname muss definiert werden."
                     :en-GB "Role name needs to be defined."}
   :role-name-exist {:de-DE "Rollenname existiert schon."
                     :en-GB "Role with the same name already exists."}
   :roles-group {:de-DE "Rollen"
                 :en-GB "Roles"}
   :rows-added-message {:de-DE "Reihen werden hinzugefügt"
                        :en-GB "rows will be added"}
   :rows-removed-message {:de-DE "Reihen wurden entfernt"
                          :en-GB "rows were removed"}
   :rows-with-error {:de-DE "Reihen mit Fehlern"
                     :en-GB "Rows with errors"}
   :rows-with-warnings {:de-DE "Reihen mit Warnungen"
                        :en-GB "Rows with warnings"}
   :same-attribute-for-multiple-attribute-kinds-error {:de-DE "Die Benutzung eines Attributes als unabhängige und abhängige Variable ist nicht valide."
                                                       :en-GB "Using the same attribute as an independent and dependent variable is not valid."}
   :same-position-metric-label {:de-DE "Eventanzahl"
                                :en-GB "No. of events"}
   :sample-required {:de-DE "Beispiel ist erforderlich"
                     :en-GB "An example here is required"}
   :saturdays {:de-DE "Samstags"
               :en-GB "Saturdays"}
   :save-button-label {:de-DE "Speichern"
                       :en-GB "Save"}
   :save-cancel-button-label {:de-DE "Abbrechen"
                              :en-GB "Cancel"}
   :save-default-warning {:de-DE "Du kannst keine Standardlayouts überschreiben"
                          :en-GB "You cannot override default layout"}
   :save-existing-layouts {:de-DE "Vorhanden:"
                           :en-GB "Existing:"}
   :save-existing-layouts-no-rows {:de-DE "Keine Layouts vorhanden zum Überschreiben"
                                   :en-GB "No existing layouts to overwrite"}
   :save-existing-overlayers-no-rows {:de-DE "Keine Overlayer vorhanden zum Überschreiben"
                                      :en-GB "No existing overlayer to overwrite"}
   :save-failure {:de-DE "Es ist ein Fehler beim Speichern aufgetreten"
                  :en-GB "An error occured while saving"}
   :save-label {:de-DE "Speichern"
                :en-GB "Save"}
   :save-settings-button {:de-DE "Einstellungen speichern"
                          :en-GB "Apply"}
   :save-success-layout {:de-DE "Layout gespeichert"
                         :en-GB "Layout saved"}
   :save-success-overlayer {:de-DE "Overlayer gespeichert"
                            :en-GB "Overlayer saved"}
   :save-title {:de-DE "Speichern unter"
                :en-GB "Save as"}
   :saved-label {:de-DE "Gespeichert!"
                 :en-GB "Saved!"}
   :saving-label {:de-DE "Speichern.."
                  :en-GB "Saving.."}
   :scatter-chart-label {:de-DE "Streudiagramm"
                         :en-GB "Scatter plot"}
   :scatter-no-values-for-axis-part-1 {:de-DE "Ein ausgewähltes Achsenattribut führt zu einer leeren Eventsammlung und ist daher nicht nutzbar."
                                       :en-GB "A selected axis attribute results in an empty and unusable event set."}
   :scatter-no-values-for-axis-part-2 {:de-DE "Das Achsenattribut wird entfernt, bitte wähle ein anderes aus."
                                       :en-GB "The selection will be cleared, please select another one."}
   :scatter-no-values-for-axis-recommendation {:de-DE "OK"
                                               :en-GB "OK"}
   :scatter-no-data-title {:de-DE "Keine Ergebnismenge"
                           :en-GB "No Result Set"}
   :scatter-no-data-title-part-1 {:de-DE "Die durchgeführte Operation führt zu einer leeren Eventsammlung und ist daher nicht nutzbar."
                                  :en-GB "The performed operation leads to an empty and unusable event set."}
   :scatter-no-data-title-part-2 {:de-DE "Das Fenster zeigt, wenn vorhanden, die alte Datenmenge an, andernfalls keine Daten."
                                  :en-GB "The window shows if available the old data set or no events."}
   :scatter-no-data-title-recommendation {:de-DE "OK"
                                          :en-GB "OK"}
   :scatter-no-values-for-axis-title {:de-DE "Achsenattribut nicht valide"
                                      :en-GB "Axis attribute is not valid"}
   :scatter-plot-missing-values {:de-DE " Events werden ignoriert, da diese keine Werte für mindestens eins der gewählten Attribute besitzen"
                                 :en-GB " events are ignored due to nonexistent values for at least one of the selected attributes."}
   :scatter-plot-missing-values-short {:de-DE " ignorierte Events"
                                       :en-GB " ignored events"}
   :scatter-plot-settings-placeholder-x-axis {:de-DE "Y-Achse"
                                              :en-GB "Y-Axis"}
   :scatter-plot-settings-placeholder-y-axis {:de-DE "X-Achse"
                                              :en-GB "X-Axis"}
   :scatter-plot-settings-title {:de-DE "Streudiagramm Einstellungen"
                                 :en-GB "Scatter plot settings"}
   :scatter-plot-settings-x-axis {:de-DE "Attribut X-Achse"
                                  :en-GB "X-Axis attribute"}
   :scatter-plot-settings-y-axis {:de-DE "Attribut Y-Achse"
                                  :en-GB "Y-Axis attribute"}
   :search-ac-requesting {:de-DE "Daten werden geladen"
                          :en-GB "Loading Data"}
   :search-bar-label {:de-DE "Suchen:"
                      :en-GB "Search:"}
   :search-bar-placeholder {:de-DE "Gib mindestens drei Buchstaben ein"
                            :en-GB "Enter at least three characters"}
   :search-bar-result-attributes-label {:de-DE "Attribute (%d)"
                                        :en-GB "Attributes (%d)"}
   :search-bar-result-charateristic-label {:de-DE "Ausprägungen"
                                           :en-GB "Characteristics"}
   :search-bar-result-hint-constraint {:de-DE "Ergebnisse basieren auf deiner Sucheinschränkung."
                                       :en-GB "Results are based on search constraints."}
   :search-bar-result-hint-no-notes {:de-DE "Ausprägungen von Notes, Fulltext und numerischen Attributen werden nicht durchsucht."
                                     :en-GB "Characteristics of notes, fulltext and numeric attributes are not searched."}
   :search-bar-results-select-all {:de-DE "Alles auswählen"
                                   :en-GB "Select all"}
   :search-bar-warn-select-all {:de-DE "Zu viele Selektionen"
                                :en-GB "Too many selections"}
   :search-button-title {:de-DE "Suche"
                         :en-GB "Search"}
   :search-clicked-message {:de-DE "Dateninstanz wurde erzeugt. Verbinde das Fenster, um Daten zu visualisieren"
                            :en-GB "Datainstance created. Connect the window to visualize data"}
   :search-complete-hint {:de-DE "Ziehe die Suche auf ein Explorationsfenster um die Daten zu laden und zu visualisieren"
                          :en-GB "Drag the search to a exploration window to load and visualize the data"}
   :search-delimiter {:de-DE "bis"
                      :en-GB "to"}
   :search-label {:de-DE "Suche"
                  :en-GB "Search"}
   :search-name {:de-DE "Suche"
                 :en-GB "Search"}
   :search-placeholder {:de-DE "Im Datenatlas suchen"
                        :en-GB "Search in Data Atlas"}
   :search-projects {:de-DE "Projekte durchsuchen ..."
                     :en-GB "Search projects ..."}
   :search-query-estimate-result {:de-DE "ca. %d Ergebnisse"
                                  :en-GB "about %d results"}
   :search-query-filter-placeholder {:de-DE "finde suchen"
                                     :en-GB "Find ..."}
   :search-query-list-title {:de-DE "Letzte Suchen"
                             :en-GB "Latest Searches"}
   :search-query-save-label {:de-DE "Suche speichern:"
                             :en-GB "Save search:"}
   :search-query-title {:de-DE "Gespeicherte Suchen"
                        :en-GB "Saved Queries"}
   :search-query-title-duplicate {:de-DE "Titel existiert schon."
                                  :en-GB "Title already exist."}
   :search-query-title-empty {:de-DE "Title muss min. 3 zeichen lang sein."
                              :en-GB "Title must be at least 3 characters."}
   :load-label {:de-DE "Laden"
                :en-GB "Load"}
   :search-query-tooltip {:de-DE "Suchanfrage laden"
                          :en-GB "Load search query"}
   :search-query-tooltip-save {:de-DE "Suchanfrage speichern"
                               :en-GB "Save search query"}
   :<-1-minute {:de-DE "weniger als einer Minute"
                :en-GB "less than one minute"}
   :=-1-minute {:de-DE "einer Minute"
                :en-GB "one minute"}
   :x-minutes {:de-DE "$num Minuten"
               :en-GB "$num minutes"}
   :=-1-hour {:de-DE "einer Stunde"
              :en-GB "one hour"}
   :x-hours {:de-DE "$num Stunden"
             :en-GB "$num hours"}
   :=-1-day {:de-DE "einem Tag"
             :en-GB "one day"}
   :x-days {:de-DE "$num Tagen"
            :en-GB "$num days"}
   :=-1-month {:de-DE "einem Monat"
               :en-GB "one month"}
   :x-months {:de-DE "$num Monaten"
              :en-GB "$num months"}
   :search-query-used {:de-DE "zuletzt vor %s benutzt"
                       :en-GB "last used %s ago"}
   :search-strategy {:de-DE "Suchstrategie"
                     :en-GB "Search strategy"}
   :search-location-apply {:de-DE "Anwenden"
                           :en-GB "Apply"}
   :search-location-cancel {:de-DE "Abbrechen"
                            :en-GB "Cancel"}
   :search-location-select {:de-DE "Wähle einen Bereich"
                            :en-GB "Select location"}
   :search-location-hint {:de-DE "Ein Mausklick startet die Auswahl, ein erneuter Klick beendet sie."
                          :en-GB "Click once to start your selection, click again to finish it"}
   :search-select-location-tooltip {:de-DE "Bereich auswählen"
                                    :en-GB "Select area"}
   :search-reset-location-selection-tooltip {:de-DE "Auswahl zurücksetzen"
                                             :en-GB "Reset selection"}
   :search-updated-message {:de-DE "Suchparameter wurde erfolgreich aktualisiert"
                            :en-GB "Search parameters updated successfully"}
   :searchbutton-label {:de-DE "Suchen"
                        :en-GB "Search"}
   :searchbutton-tooltip {:de-DE "Erstellt/ersetzt initialen Informationsraum"
                          :en-GB "Creates/replaces initial information room"}
   :searchbutton-update-tooltip {:de-DE "Die Suche aktualisiert verbundene Fenster. Um Fenster zu verbinden und
                                       deine Daten zu visualisieren ziehe die Suche auf ein Explorationsfenster oder nutze die folgenden Buttons."
                                 :en-GB "Search updates connected windows. To connect and visualize your data
                                       drag the search on any exploration window or use the following buttons."}
   :seasonal-d {:de-DE "D saisonal"
                :en-GB "seasonal D"}
   :seasonal-d-info {:de-DE "Negativ: automatische Berechnung\nRest: Wert wird für Berechnung genutzt"
                     :en-GB "Negative: calculated automatic\nOther: use as seasonal-differencing order"}
   :seasonal-handle-method {:de-DE "Saisonalität (Methode)"
                            :en-GB "Seasonal handle method"}
   :seasonal-p {:de-DE "P saisonal"
                :en-GB "seasonal P"}
   :seasonal-period {:de-DE "saisonaler Zeitraum"
                     :en-GB "seasonal period"}
   :seasonal-period-info {:de-DE "Negativ: automatische Berechnung\n0 oder 1: Daten haben keine Saisons\nRest: Wert wird für Berechnung genutzt"
                          :en-GB "Negatic: calculated automatic\n0 or 1: non-seasonal\nOther: used as seasonal period"}
   :seasonal-q {:de-DE "Q saisonal"
                :en-GB "seasonal Q"}
   :seasonality {:de-DE "Saisonalität"
                 :en-GB "Seasonality"}
   :seasonality-criterion {:de-DE "Saisonalitäts Kriterium"
                           :en-GB "Seasonality criterion"}
   :seasonality-desc {:de-DE "Testet die Eingabedaten auf Saisonalität. Dabei werden werden zyklische Veränderungen beliebiger Länge in den Daten erkannt."
                      :en-GB "Tests whether the input data has seasonality or not. Cycles of varying length can be detected."}
   :seasonality-time-sampling-error {:de-DE "Datenpunkte müssen lückenlos und im gleichen Abstand zueinander sein."
                                     :en-GB "Datapoints must be equally spaced with no missing values."}
   :section-adv-handling-mapping-label {:de-DE "Erweiterte Operationen"
                                        :en-GB "Advanced Handling"}
   :section-adv-property-mapping-label {:de-DE "Fort. Attribute"
                                        :en-GB "Adv. Property Mappings"}
   :section-context-mapping-label {:de-DE "Kontext Mapping"
                                   :en-GB "Context Mapping"}
   :section-country-mapping-label {:de-DE "Länderzuordnung"
                                   :en-GB "Country Mapping"}
   :section-data-preview-label {:de-DE "Datenvorschau"
                                :en-GB "Data Preview"}
   :section-datasources-confirm-delete-message {:de-DE "Möchtest du die %s-Datenquelle wirklich löschen?"
                                                :en-GB "Do you want to delete the %s datasource?"}
   :section-datasources-confirm-delete-title {:de-DE "Datenquelle löschen"
                                              :en-GB "Delete datasource"}
   :section-datasources-delete-button {:de-DE "Löschen"
                                       :en-GB "Delete"}
   :section-datasources-delete-column {:en-GB ""}
   :section-datasources-free-space {:de-DE "Verfügbarer Speicher"
                                    :en-GB "Available space"}
   :section-datasources-label {:de-DE "Datenquellenverwaltung"
                               :en-GB "Datasource Management"}
   :section-filesettings-label {:de-DE "Dateieinstellungen"
                                :en-GB "File Settings"}
   :section-mosaic-features-label {:de-DE "Mosaik-Funktionen"
                                   :en-GB "Mosaic Features"}
   :section-import-new-label {:de-DE "Neue Datenquelle importieren"
                              :en-GB "Import New Datasource"}
   :section-mapping-label {:de-DE "Mapping"
                           :en-GB "Mapping"}
   :section-property-mapping-label {:de-DE "Eigenschafts Mapping"
                                    :en-GB "Property Mapping"}
   :section-sampledata-label {:de-DE "Gemappte Daten"
                              :en-GB "Mapped Data"}
   :section-std-mappings-label {:de-DE "Standard Mappings"
                                :en-GB "Standard Mappings"}
   :section-upload-label {:de-DE "Datei hochladen"
                          :en-GB "File Upload"}
   :select-all-group-label {:de-DE "Möglichkeiten"
                            :en-GB "Options"}
   :select-by-info {:de-DE "Wähle das Attribute nach welchem die Daten gruppiert werden"
                    :en-GB "Choose the attribute by which to group the data"}
   :select-country-ph {:de-DE "Land auswählen"
                       :en-GB "Choose Country"}
   :select-language-label {:de-DE "Sprache auswählen"
                           :en-GB "Select language"}
   :select-placeholder {:de-DE "Auswählen..."
                        :en-GB "Select..."}
   :select-placeholder-projects {:de-DE "Wähle Nutzer oder Rolle"
                                 :en-GB "Select user or role"}
   :select-placeholder-prefix {:de-DE "Auswählen: "
                               :en-GB "Select"}
   :selection {:de-DE "Auswahl"
               :en-GB "Selection"}
   :send-error-notification {:de-DE "Indikator indicator-name konnte nicht an user-name gesendet werden."
                             :en-GB "Indicator indicator-name could not be sent to user-name."}
   :send-label {:de-DE "Senden"
                :en-GB "Send"}
   :send-copy-label {:de-DE "Kopie senden"
                     :en-GB "Send copy"}
   :send-success-notification {:de-DE "Indikator indicator-name erfolgreich an user-name gesendet."
                               :en-GB "Indicator indicator-name successfully sent to user-name."}
   :send-to {:de-DE "Sende Kopie an:"
             :en-GB "Send copy"}
   :seperator-label {:de-DE "Separator"
                     :en-GB "Separator"}
   :server-ws-connection-lost-message {:de-DE "Serververbindung unterbrochen"
                                       :en-GB "Connection to server lost"}
   :set-frame-title {:de-DE "Setze Fenstertitel"
                     :en-GB "Set title"}
   :settings-attribute-for {:de-DE "Konfigurationen für"
                            :en-GB "Settings for"}
   :settings-section {:de-DE "Einstellungen"
                      :en-GB "Settings"}
   :settings-show-less {:de-DE "Erweiterte Optionen ausblenden"
                        :en-GB "Hide advanced options"}
   :settings-show-more {:de-DE "Erweiterte Optionen anzeigen"
                        :en-GB "Show advanced options"}
   :share-create-pdf-complete {:de-DE "Komplette Arbeitsfläche"
                               :en-GB "Complete desktop"}
   :share-create-pdf-create-button {:de-DE "Erstellen"
                                    :en-GB "Create"}
   :share-create-pdf-label {:de-DE "Exportieren als PDF"
                            :en-GB "Export PDF"}
   :share-create-pdf-separate {:de-DE "Separate Fenster"
                               :en-GB "Every window separately"}
   :share-create-pdf-textarea {:de-DE "Text zur PDF hinzufügen:"
                               :en-GB "Text to add in pdf:"}
   :share-create-pdf-visible {:de-DE "Nur sichtbarer Bereich"
                              :en-GB "Only visible area"}
   :share-dialog-checkbox-download {:de-DE "Download"
                                    :en-GB "Download"}
   :share-dialog-checkbox-e-mail {:de-DE "Link als E-Mail versenden"
                                  :en-GB "Send link via E-Mail"}
   :share-dialog-checkbox-link-e-mail {:de-DE "Projektlink als E-Mail versenden"
                                       :en-GB "Send project link via E-Mail"}
   :share-dialog-e-mail-options-placeholder {:de-DE "Auswahl der Empfänger"
                                             :en-GB "Choose recipients"}
   :share-dialog-e-mail-project-link-body {:de-DE "Hallo,NEWLINElade das Projekt: %s mit dem Link %sNEWLINEMit freundlichen Grüßen,NEWLINE%s"
                                           :en-GB "Hello,NEWLINEload the Project: %s with the link %sNEWLINEKind Regards,NEWLINE%s"}
   :share-dialog-e-mail-send-link {:de-DE "Link senden"
                                   :en-GB "Send link"}
   :share-dialog-export-as {:de-DE "Exportmodus"
                            :en-GB "Export mode"}
   :share-dialog-export-button {:de-DE "Exportieren"
                                :en-GB "Export"}
   :share-dialog-export-options-placeholder {:de-DE "Auswahl des Exportmodus"
                                             :en-GB "Choose export mode"}
   :share-dialog-no-permission {:de-DE "Keine Berechtigung"
                                :en-GB "No permission"}
   :share-dialog-not-exportable {:de-DE "Keine Berechtigung"
                                 :en-GB "No permission"}
   :share-dialog-title {:de-DE "Projekt teilen"
                        :en-GB "Share Project"}
   :share-entry-read-only-label {:de-DE "Schreibgeschützt"
                                 :en-GB "Read-only"}
   :share-label {:de-DE "Teilen"
                 :en-GB "Share"}
   :share-link-sublabel {:de-DE "Link für Nutzer mit Zugriffsrechten."
                         :en-GB "Link for users with access."}
   :link-label {:de-DE "Link"
                :en-GB "Link"}
   :share-public-exportable-label {:de-DE "Exportierbar"
                                   :en-GB "Exportable"}
   :share-public-read-only-label {:de-DE "Öffentlich (schreibgeschützt)"
                                  :en-GB "Public (read-only)"}
   :share-publish-label {:de-DE "Projekt veröffentlichen"
                         :en-GB "Publish project"}
   :share-publish-report-label {:de-DE "Report veröffentlichen"
                                :en-GB "Publish report"}
   :share-publish-dashboard-label {:de-DE "Dashboard veröffentlichen"
                                   :en-GB "Publish dashboard"}
   :share-publish-sublabel {:de-DE "Erteile allen Nutzern schreibgeschützen Zugriff."
                            :en-GB "Grant read-only access to all users."}
   :share-entries-sublabel {:de-DE "Erteile Zugriffsrechte an ausgewählte Gruppen und Nutzer."
                            :en-GB "Grant access to selected groups and users."}
   :share-save-button {:de-DE "Teilen"
                       :en-GB "Share"}
   :share-section-title {:de-DE "Teilen"
                         :en-GB "Share"}
   :share-settings-group {:de-DE "Teilen"
                          :en-GB "Share"}
   :share-with-headline {:de-DE "Teilen mit:"
                         :en-GB "Share with:"}
   :share-with-label {:de-DE "Geteilt mit"
                      :en-GB "Shared with"}
   :shared {:de-DE "Geteilte Konfguration"
            :en-GB "Shared"}
   :shared-dashboards-label {:de-DE "Geteilte Dashboards"
                             :en-GB "Shared Dashboards"}
   :shared-reports-label {:de-DE "Geteilte Reports"
                          :en-GB "Shared Reports"}
   :shared-with-me-label {:de-DE "Mit mir geteilt"
                          :en-GB "Shared with me"}
   :shared-project-label {:de-DE "Geteiltes Projekt"
                          :en-GB "Shared project"}
   :sheet-label {:de-DE "Tabellenblatt"
                 :en-GB "Sheet"}
   :sheet-required {:de-DE "Tabellenblatt ist erforderlich"
                    :en-GB "Sheet is required"}
   :sherlock-grp-rights-and-roles {:de-DE "Sherlock"
                                   :en-GB "Sherlock"}
   :sherlock-rights-and-roles {:de-DE "Rechte- und Rollenverwaltung"
                               :en-GB "Rights and roles management"}
   :show-dialog-option {:de-DE "Nicht mehr anzeigen"
                        :en-GB "Do not show again"}
   :show-info {:de-DE "Infos aufklappen"
               :en-GB "Show Info"}
   :show-legend-label {:de-DE "Legende anzeigen"
                       :en-GB "Show Legend"}
   :show-message-label {:de-DE "Text anzeigen"
                        :en-GB "Show message"}
   :single {:de-DE "Einzelnd"
            :en-GB "Single"}
   :single-label {:de-DE "Eine Spalte"
                  :en-GB "Single column"}
   :slide {:de-DE "Folie"
           :en-GB "Slide"}
   :smape {:de-DE "SMAPE"
           :en-GB "SMAPE"}
   :smooth-width {:de-DE "Glättung"
                  :en-GB "Smooth width"}
   :smooth-width-info {:de-DE "Anzahl der Werte, die für den gleitenden Mittelwert benutzt werden. Kann nicht die Hälfte der Datenlänge übersteigen."
                       :en-GB "Number of values used for moving average. Cannot be larger than half the data length."}
   :snapshot-all-tooltip {:de-DE "Alle Schritte anzeigen"
                          :en-GB "Show all steps"}
   :snapshot-edit-cancel {:de-DE "Abbrechen"
                          :en-GB "Cancel"}
   :snapshot-edit-save {:de-DE "Speichern"
                        :en-GB "Save"}
   :snapshot-only-tooltip {:de-DE "Nur Snapshots anzeigen"
                           :en-GB "Show only snapshots"}
   :snapshot-title-description {:de-DE "Beschreibung"
                                :en-GB "Description"}
   :snapshot-title-label {:de-DE "Titel"
                          :en-GB "Title"}
   :source-target-equal {:de-DE "Quelle und Ziel im Strömungslayer dürfen nicht gleich sein."
                         :en-GB "Source and target in movement overlayer can't be the same."}
   :split-columns-label {:de-DE "Aus Spalten bilden:"
                         :en-GB "Build from columns:"}
   :start-at-row-label {:de-DE "Starten ab Zeile"
                        :en-GB "Start at row"}
   :start-new-workspace {:de-DE "Fang mit einer neuen Arbeitsfläche an!"
                         :en-GB "Start with a new workspace."}
   :status-bar-project {:de-DE "Projekt"
                        :en-GB "Project"}
   :status-message-read-only-suffix {:de-DE "(schreibgeschützt)"
                                     :en-GB "(read-only)"}
   :step {:de-DE "Schrittlänge"
          :en-GB "Step length"}
   :step-as-snapshot-tooltip {:de-DE "Als Snapshot markieren"
                              :en-GB "Mark as a snapshot"}
   :step-info-action {:de-DE "Aktion:"
                      :en-GB "Action:"}
   :step-info-settings {:de-DE "Einstellung:"
                        :en-GB "Settings:"}
   :step-info-window {:de-DE "Fenster:"
                      :en-GB "Window:"}
   :step-not-snapshot-tooltip {:de-DE "Snapshot Markierung entfernen"
                               :en-GB "unmark snapshot"}
   :step-open-read-only-tooltip {:de-DE "Schreibgeschützt"
                                 :en-GB "Read-only"}
   :step-open-writeable-tooltip {:de-DE "Schritt öffnen"
                                 :en-GB "Open step"}
   :step-size {:de-DE "Schrittgröße"
               :en-GB "Step size"}
   :stepwise {:de-DE "schrittweise"
              :en-GB "stepwise"}
   :stop-view-invalid-selection-message-part-1 {:de-DE "Die folgende Auswahl wird nicht unterstützt\n $selection"
                                                :en-GB "The following selection is invalid\n $selection"}
   :stop-view-invalid-selection-message-part-2 {:de-DE "Bitte ändere deine Auswahl"
                                                :en-GB "Please select another one"}
   :stop-view-invalid-selection-recommendation {:de-DE "OK"
                                                :en-GB "OK"}
   :stop-view-invalid-selection-title {:de-DE "Nicht unterstützte Auswahl"
                                       :en-GB "Invalid selection"}
   :stop-view-invalid-time-selection-message-part-1 {:de-DE "Diese Kombination aus zeitlichen Attributen wird nicht unterstützt\n $selection"
                                                     :en-GB "The combination of temporal attributes is not supported\n $selection"}
   :stop-view-invalid-time-selection-message-part-2 {:de-DE "Bitte ändere deine Auswahl"
                                                     :en-GB "Please select another one"}
   :stop-view-invalid-time-selection-recommendation {:de-DE "OK"
                                                     :en-GB "OK"}
   :stop-view-invalid-time-selection-title {:de-DE "Nicht unterstützte Auswahl"
                                            :en-GB "Invalid selection"}
   :stop-view-unknown-part-1 {:de-DE "Die aktuelle Datenauswahl oder durchgeführte Operation führt zu einem Fehler."
                              :en-GB "The current operation or data set failed."}
   :stop-view-unknown-part-2 {:de-DE "Stelle sicher, dass deine Datenauswahl nicht leer ist."
                              :en-GB "Please ensure a not empty data selection."}
   :stop-view-unknown-recommendation {:de-DE "OK"
                                      :en-GB "OK"}
   :stop-view-unknown-title {:de-DE "Daten konnten nicht geladen werden"
                             :en-GB "Failed to load your data"}
   :stop-warning-dialog {:de-DE "Abbrechen"
                         :en-GB "Cancel"}
   :string {:de-DE "Kategorie"
            :en-GB "Category"}
   :subcat-alert-frequency {:de-DE "Benachrichtigungen"
                            :en-GB "Frequency"}
   :subcat-condition {:de-DE "Bedingungen"
                      :en-GB "Conditions"}
   :subcat-data {:de-DE "Daten"
                 :en-GB "Data"}
   :subcat-general {:de-DE "Allgemeines"
                    :en-GB "General"}
   :subset {:de-DE "Optimale Untergruppenselektion"
            :en-GB "Optimal subset selector"}
   :subtitle-label {:de-DE "Untertitel"
                    :en-GB "Subtitle"}
   :search-grp-rights-and-roles {:de-DE "Suche"
                                 :en-GB "Search"}
   :search-protocol-action-filter {:de-DE "Filtern"
                                   :en-GB "Filter"}
   :search-protocol-action-search {:de-DE "Suchen"
                                   :en-GB "Search"}
   :search-rights-and-roles {:de-DE "Rechte- und Rollenverwaltung"
                             :en-GB "Rights and roles management"}
   :sum {:de-DE "Summe"
         :en-GB "Sum"}
   :sum-by-label {:de-DE "Anzeigen nach"
                  :en-GB "Show by"}
   :sum-by-vals-label {:de-DE "Ausprägungen"
                       :en-GB "Characteristics"}
   :sum-label {:de-DE "Summe"
               :en-GB "Sum"}
   :sum-remaining-label {:de-DE "Sonstige zusammenfassen"
                         :en-GB "Summarize others"}
   :sundays {:de-DE "Sonntags"
             :en-GB "Sundays"}
   :svd {:de-DE "Singular value decomposition"
         :en-GB "Singular value decomposition"}
   :tab-just-saved {:de-DE "Deine Änderungen wurden gespeichert."
                    :en-GB "Your changes have been saved."}
   :table-column-actor {:de-DE "Akteur"
                        :en-GB "Actor"}
   :table-column-country {:de-DE "Land"
                          :en-GB "Country"}
   :table-column-source {:de-DE "Quelle"
                         :en-GB "Source"}
   :table-column-year {:de-DE "Jahr"
                       :en-GB "Year"}
   :table-protocol-action-current-page {:de-DE "Seite"
                                        :en-GB "Page"}
   :table-protocol-action-page-size {:de-DE "Seitengröße"
                                     :en-GB "Page size"}
   :table-protocol-action-set-page-size {:de-DE "Seitengröße"}
   :table-protocol-action-sort-data {:de-DE "Sortieren"
                                     :en-GB "Sorting"}
   :table-sort-asc {:de-DE "aufsteigend"
                    :en-GB "ascending"}
   :table-sort-desc {:de-DE "absteigend"
                     :en-GB "descending"}
   :table-tooltip-search {:de-DE "Visualisiere die Daten in der Tabelle"
                          :en-GB "Visualize the data in the table"}
   :temp-data-source-detail-label {:de-DE "Temporäre Datenquelle"
                                   :en-GB "Temporary Datasource"}
   :temp-data-source-list-label {:de-DE " (* temporär)"
                                 :en-GB " (* temporary)"}
   :template-selection-label {:de-DE "Layout"
                              :en-GB "Layout"}
   :temporary-layout-name {:de-DE "Temporäres Layout im Fenster"
                           :en-GB "Temporary frame layout"}
   :temporary-overlay-name {:de-DE "Temporäres Overlayer im Fenster"
                            :en-GB "Temporary frame overlayer"}
   :terms-of-use-label {:de-DE "Nutzungsbedingunen"
                        :en-GB "Terms of use"}
   :theme-dark {:de-DE "Dunkler Modus"
                :en-GB "Dark Mode"}
   :theme-light {:de-DE "Heller Modus"
                 :en-GB "Light Mode"}
   :theme {:de-DE "Theme"
           :en-GB "Theme"}
   :theme-system {:de-DE "Systemmodus"
                  :en-GB "System Mode"}
   :thousand-separator {:de-DE "."
                        :en-GB ","}
   :thread-ratio {:de-DE "Thread-Ratio"
                  :en-GB "Thread ratio"}
   :threshold {:de-DE "Schwellwert"
               :en-GB "Threshold"}
   :thursdays {:de-DE "Donnerstags"
               :en-GB "Thursdays"}
   :time-granularity-not-valid {:de-DE "Die ausgewählte zeitliche Granularität ist nicht mehr valide, wähle eine neue aus."
                                :en-GB "The time granularity is not valid anymore, change to something different."}
   :time-label {:de-DE "Zeitraum"
                :en-GB "Time"}
   :time-label-dropdown-placeholder {:de-DE "Jahr auswählen"
                                     :en-GB "Select year"}
   :time-tip-advanced {:de-DE "Gebe Jahre und/oder Jahreszeiträume an, die mit einem Komma separiert werden"
                       :en-GB "Enter years and/or year-ranges separated by comma."}
   :title {:de-DE "Sprachauswahl (Devmode)"
           :en-GB "Language Configuration (Devmode)"}
   :title-add {:de-DE "Externe Quelle"
               :en-GB "External Source"}
   :title-create-alert {:de-DE "Alarm bearbeiten"
                        :en-GB "Edit Alert"}
   :title-label {:de-DE "Titel"
                 :en-GB "Title"}
   :title-remove-all-slides {:de-DE "Folien entfernen"
                             :en-GB "Remove all Slides"}
   :to-column-label {:de-DE "Bis Spalte"
                     :en-GB "to column"}
   :toggle-desc-button {:de-DE "Beschreibung anzeigen"
                        :en-GB "Show Description"}
   :toggle-legend {:de-DE "Legende ein- oder ausblenden"
                   :en-GB "Show/hide legend"}
   :tolerance-abs {:de-DE "Absolute Toleranz"
                   :en-GB "Tolerance absolute"}
   :tolerance-rel {:de-DE "Relative Toleranz"
                   :en-GB "Tolerance relative"}
   :too-many-options-related {:de-DE "Zu viele Verknüpft optionen für das Attribut %s ausgewählt.\nEs werden die ersten %d optionen genutzt."
                              :en-GB "Too many related option for attribute %s selected.\nUsing only the first %d options."}
   :too-much-data-follow-recommendation {:de-DE "OK"
                                         :en-GB "OK"}
   :too-much-data-message-part-1 {:de-DE "Es wurden zu viele Daten ausgewählt."
                                  :en-GB "Too much data selected."}
   :too-much-data-message-part-1-min-max {:de-DE "Die ausgewählte Datenmenge ist zu groß\n(Aktuell: $data-count, Erlaubt bis: $max-data-amount)"
                                          :en-GB "Too much data selected\n(Current: $data-count, Allowed up to: $max-data-amount)"}
   :too-much-data-message-part-2 {:de-DE "Bitte eine Suche durchführen mit einer kleineren Datenauswahl."
                                  :en-GB "Please update the search with a smaller dataset."}
   :too-much-data-title {:de-DE "Stopp"
                         :en-GB "Stop"}
   :stop-view-stop-event-layer-part-1 {:de-DE "Die ausgewählte Datenmenge ist zu groß für Marker-Layouts und Strömung-Overlayer\nFlächen-Overlayer und Heatmaps sind weiterhin nutzbar.\n(Aktuell: $data-count, Erlaubt bis: $max-data-amount)"
                                       :en-GB "Too much data selected for marker layouts and movement overlayers.\nArea overlayers and heatmaps are still usable.\n(Current: $data-count, Allowed up to: $max-data-amount)"}
   :stop-view-stop-event-layer-part-2 {:de-DE "Für den vollen Funktionsumfang bitte eine Suche mit einer kleineren Datenauswahl durchführen. "
                                       :en-GB "For full possibilites please update the search with a smaller dataset."}
   :stop-view-stop-event-layer-title {:de-DE "Zu viele Daten"
                                      :en-GB "Too much data"}
   :stop-view-stop-event-layer-recommendation {:de-DE "OK"
                                               :en-GB "OK"}
   :top-ascending {:de-DE "Aufsteigend"
                   :en-GB "Ascending"}
   :top-cancel {:de-DE "Abbrechen"
                :en-GB "cancel"}
   :top-descending {:de-DE "Absteigend"
                    :en-GB "Descending"}
   :top-events-message {:de-DE "Wähle Parameter für Top-Events aus."
                        :en-GB "Select parameters for top events."}
   :top-events-sort-by {:de-DE "Sortieren nach"
                        :en-GB "Sort by"}
   :top-events-title {:de-DE "Top Events"
                      :en-GB "Top Events"}
   :top-events-top-label {:de-DE "Events"
                          :en-GB "Events"}
   :top-groups-aggregation {:de-DE "Aggregieren nach"
                            :en-GB "Aggregate by"}
   :top-groups-group-by {:de-DE "Gruppen nach"
                         :en-GB "Group by"}
   :top-groups-message {:de-DE "Wähle Parameter für Top-Gruppen aus."
                        :en-GB "Select parameters for top groups."}
   :top-groups-sort-groups {:de-DE "Sortieren nach"
                            :en-GB "Sort by"}
   :top-groups-title {:de-DE "Top Gruppen"
                      :en-GB "Top Groups"}
   :top-groups-top-label {:de-DE "Gruppen"
                          :en-GB "Groups"}
   :top-invalid-message {:de-DE "Auswahl nicht vollständig."
                         :en-GB "Some values are missing."}
   :top-number-of-events {:de-DE "Eventanzahl"
                          :en-GB "Number of Events"}
   :top-ok {:de-DE "Anwenden"
            :en-GB "apply"}
   :top-order {:de-DE "Sortierung"
               :en-GB "Order"}
   :traffic-light-criteria-or {:de-DE "oder"
                               :en-GB "or"}
   :traffic-light-data-tile-too-large-tooltip {:de-DE "Die Ergebnismenge ist zu groß, bitte selektiere zusätzliche Attribute."
                                               :en-GB "The result set for your search will be too large, please select additional attributes."}
   :traffic-light-empty-message {:de-DE "Keine Ergebnisse für diese Suche."
                                 :en-GB "No results can be found for this search."}
   :traffic-light-green {:de-DE "Das Bestimmtheitsmaß ist im optimalen Bereich."
                         :en-GB "The coefficient of determination is withing the optimal range."}
   :traffic-light-green-message {:de-DE "Moderate Ergebnismenge"
                                 :en-GB "Moderate result set"}
   :traffic-light-green-tooltip {:de-DE "Die Ergebnismenge ist klein, es gibt keine Beeinträchtigung."
                                 :en-GB "The result set for your search will be easy to handle for Explorama."}
   :traffic-light-none {:de-DE "Das Model besitzt kein Bestimmtheitsmaß, um es zu bewerten."
                        :en-GB "There is no coefficient of determination to grade the result with."}
   :traffic-light-none-message {:de-DE "Abschätzung der Ergebnismenge noch nicht verfügbar"
                                :en-GB "Result set estimations are not available"}
   :traffic-light-pending-message {:de-DE "Berechne Abschätzung der Ergebnismenge"
                                   :en-GB "Waiting for result set estimations"}
   :traffic-light-red {:de-DE "Das Bestimmtheitsmaß is entweder zu gering oder so hoch, dass der starke Verdacht auf Multikollinearität besteht."
                       :en-GB "The coefficient of determination is either below the threshold or above it, signaling multicollinearity."}
   :traffic-light-red-message {:de-DE "Sehr große Ergebnismenge"
                               :en-GB "Very large result set"}
   :traffic-light-red-tooltip {:de-DE "Die Ergebnismenge ist möglicherweise sehr groß, einige Vorgänge können sehr viel mehr Zeit in Anspruch nehmen."
                               :en-GB "The result set for your search\n might be very large\n and might slow Explorama down considerably."}
   :traffic-light-result-too-large {:de-DE "Die Ergebnismenge is zu groß"
                                    :en-GB "Result set is too large"}
   :traffic-light-result-too-large-tooltip {:de-DE "Die Ergebnismenge ist zu groß, die Suche kann nicht durchgeführt werden."
                                            :en-GB "The result set for your search will be too large."}
   :traffic-light-yellow {:de-DE "Das Bestimmtheitsmaß ist am unteren Ende des akzeptablen Bereich."
                          :en-GB "The coefficient of determination is within the lower end of the acceptable range."}
   :traffic-light-yellow-message {:de-DE "Große Ergebnismenge"
                                  :en-GB "Large result set"}
   :traffic-light-yellow-tooltip {:de-DE "Die Ergebnismenge ist möglicherweise groß, einige Vorgänge können mehr Zeit in Anspruch nehmen."
                                  :en-GB "The result set for your search\n might be large and some operations\n might take a bit of time."}
   :transform-indicator-invalid {:de-DE "Einige Felder sind nicht valide."
                                 :en-GB "Some fields are not valid."}
   :transpose-label {:de-DE "Transponieren"
                     :en-GB "Transpose"}
   :transpose-select-label {:de-DE "Transponier-Spalte"
                            :en-GB "Transpose-Column"}
   :transpose-settings-label {:de-DE "Transponierungseinstellungen Transpose-Settings"
                              :en-GB "Transpose-Settings"}
   :treemap-algo-squared {:de-DE "Quadratisch"
                          :en-GB "Squarified"}
   :treemap-algo-binary {:de-DE "Binärbaum"
                         :en-GB "Binary tree"}
   :treemap-algo-slice {:de-DE "Schneiden und Würfeln"
                        :en-GB "Slice and Dice"}
   :tooltip-treemap {:de-DE "Tree Map"
                     :en-GB "Treemap"}
   :tooltip-treemap-algorithm {:de-DE "Tree Map Typ"
                               :en-GB "Treemap type"}
   :trend {:de-DE "Trend"
           :en-GB "Trend"}
   :trend-desc {:de-DE "Testet die Eingabedaten auf einen Trend. Daten können entweder keinen Trend besitzen oder einen Aufwärts-/Abwärtstrend zeigen."
                :en-GB "Tests whether the input data has a trend or not. The trend can either be none at all or falling/rising."}
   :triggered-label {:de-DE "Ausgelöst: "
                     :en-GB "Triggered: "}
   :tuesdays {:de-DE "Dienstags"
              :en-GB "Tuesdays"}
   :two-columns-label {:de-DE "Zwei Spalten"
                       :en-GB "Two columns"}
   :type-label {:de-DE "Typ"
                :en-GB "Type"}
   :type-missmatch {:de-DE "Reihe <p> hat den falschen Typ. Erforderlich ist "
                    :en-GB "Row <p> has the wrong type. Required type"}
   :type-select-ph {:de-DE "Wähle Typ"
                    :en-GB "Select Type"}
   :undo-button-label {:de-DE "Rückgangig machen"
                       :en-GB "Undo"}
   :unit-label {:de-DE "Einheit"
                :en-GB "Unit"}
   :unknown-error {:de-DE "Unbekannter Fehler"
                   :en-GB "Unknown error"}
   :unknown-setting-label {:de-DE "Unbekannte Einstellung"
                           :en-GB "Unknown Setting"}
   :unsaved-workspace {:de-DE "Ungespeicherte Arbeitsfläche*"
                       :en-GB "Untitled workspace*"}
   :update-table-protocol-label {:de-DE "Tabelleneigenschaften geändert"
                                 :en-GB "Table properties changed"}
   :upload-drag-hint {:de-DE "Ziehe eine Datei hierher oder klicke zum Hochladen"
                      :en-GB "Drag a file here or click to upload"}
   :upload-mapping-button-label {:de-DE "Lade Zuweisungen"
                                 :en-GB "Load Mapping"}
   :use-label {:de-DE "Verwenden"
               :en-GB "Use"}
   :use-rights {:de-DE "Nutzungsrechte"
                :en-GB "use-rights only"}
   :use-zeros {:de-DE "Nullen werden genutzt"
               :en-GB "Use zeros"}
   :user-group {:de-DE "Benutzer"
                :en-GB "Users"}
   :user-input {:de-DE "Benutzereingabe"
                :en-GB "User input"}
   :user-invalid {:de-DE "Authentifikation der Zugriffrechte gescheitert. Bitte lade die Seite neu und versuche es erneut"
                  :en-GB "Authentification of access-rights failed. Please reload the Page and try again"}
   :user-settings-label {:de-DE "Benutzereinstellungen"
                         :en-GB "User Settings"}
   :users-label {:de-DE "Nutzer"
                 :en-GB "users"}
   :value-exceeds-max-length-message {:de-DE "Reihe <p> ist länger als 210 Zeichen"
                                      :en-GB "Row <p> exceeds 210 characters"}
   :value-label {:de-DE "Wert"
                 :en-GB "value"}
   :value-out-of-range {:de-DE "Reihe <p> Wert liegt außerhalb des gültigen Bereichs"
                        :en-GB "Row <p> value is out of range"}
   :variable-selection {:de-DE "Variableauswahl"
                        :en-GB "Variable selection"}

   :vertical-label-bat {:de-DE "Alarmierung/Benachrichtigungen"
                        :en-GB "Alerting/Notifications"}
   :vertical-label-reporting {:de-DE "Dashboards/Reports"
                              :en-GB "Dashboards/Reports"}
   :vertical-label-configuration {:de-DE "Einstellungen"
                                  :en-GB "Settings"}
   :vertical-label-map {:de-DE "Karte"
                        :en-GB "Map"}
   :vertical-label-mosaic {:de-DE "Mosaik"
                           :en-GB "Mosaic"}
   :vertical-label-table {:de-DE "Tabelle"
                          :en-GB "Table"}
   :vertical-label-data-provisioning {:de-DE "Temporärer Datenimport"
                                      :en-GB "Temporary Data Import"}
   :vertical-label-data-atlas {:de-DE "Datenatlas"
                               :en-GB "Data Atlas"}
   :vertical-label-indicator {:de-DE "Indikatorersteller"
                              :en-GB "Indicator Creator"}
   :view-label {:de-DE "Ansehen"
                :en-GB "View"}
   :visibile-label {:de-DE "sichtbar"
                    :en-GB "visible"}
   :visualization-grp-rights-and-roles {:de-DE "Visualization"
                                        :en-GB "Visualization"}
   :visualization-rights-and-roles {:de-DE "Rechte- und Rollenverwaltung"
                                    :en-GB "Rights and roles management"}
   :visualization-max-charts-exceeding {:de-DE "Maximale Anzahl an Graphen erreicht."
                                        :en-GB "Maximum number of charts reached."}
   :visualization-multi-charts-not-allowed {:de-DE "Es kann kein Graph hinzugefügt werden, mehrere Graphen sind mit Tortendiagramm und Wortwolke nicht erlaubt."
                                            :en-GB "You can't add another chart, not allowed for multiple charts are pie chart and wordcloud."}
   :visualization-max-number-chars-reached-new-charts {:de-DE "Maximale Anzahl an Characteristics sind %d bei Verwendung von mehreren Graphen."
                                                       :en-GB "Maximum number of characteristics is %d when using multiple charts."}
   :charts-protocol-action-update-chart {:de-DE "Diagramm-Konfigurationsveränderung"
                                         :en-GB "Chart config change"}
   :charts-protocol-action-chart-config {:de-DE "X-Achse:"
                                         :en-GB "X axis:"}
   :charts-protocol-action-chart-axis-config {:de-DE "Diagramm"
                                              :en-GB "Chart"}
   :charts-protocol-action-chart-config-y {:de-DE "Y-Achse:"
                                           :en-GB "Y axis"}
   :charts-protocol-action-chart-config-aggregated {:de-DE "Aggregation:"
                                                    :en-GB "Aggregation:"}
   :vr-and {:de-DE "und"
            :en-GB "and"}
   :vr-one {:de-DE "eine"
            :en-GB "one"}
   :vr-one-or-multiple {:de-DE "eine oder mehrere"
                        :en-GB "one or multiple"}
   :vr-plural {:de-DE "e"
               :en-GB "s"}
   :vr-require {:de-DE "Benötigt"
                :en-GB "Requires"}
   :vr-singular {:de-DE ""
                 :en-GB ""}
   :waiting-for-request-message {:de-DE "Warte auf die Bearbeitung der Anfrage"
                                 :en-GB "Waiting for processing of your request"}
   :waiting-for-request-message-tip {:de-DE "Eine andere Anfrage wird derzeit bearbeitet. Bitte warten..."
                                     :en-GB "Another request is currently being processed. Please wait..."}
   :warning-clean-message {:de-DE "Möchtest du die Arbeitsfläche aufräumen ohne deine Arbeit als Projekt zu speichern? Fortschritt geht verloren."
                           :en-GB "Do you want to clean up the workspace without saving your work as a project? You will lose your progress."}
   :warning-deletable-project-is-shared {:de-DE "Das Projekt ist derzeit mit folgenden Benutzern und Rollen geteilt: "
                                         :en-GB "The project is currently shared with following users and roles: "}
   :warning-header-title {:de-DE "Warnung"
                          :en-GB "Warning"}
   :warning-help-tooltip {:de-DE "Warnungen sind nicht kritisch, ein Import ist trotzdem möglich.\nWerden diese nicht behoben kann es jedoch dazu kommen, dass Reihen beim Importprozess ignoriert werden"
                          :en-GB "Warnings are not critical, an import is still possible.\nHowever, if these are not corrected, rows may be ignored during import"}
   :warning-open-project-message {:de-DE "Möchtest du die Arbeitsfläche aufräumen und das Projekt schließen? Deine Arbeit wird im Projekt gespeichert."
                                  :en-GB "Do you want to clean up the workspace and close the project? Your progress is saved in the project."}
   :warning-project-title-already-exists {:de-DE "Titel wird bereits in einem deiner Projekte verwendet"
                                          :en-GB "Title already in use in your projects"}
   :warning-read-only-project {:de-DE "Das Projekt ist schreibgeschützt.\nDu kannst die Beschreibung nicht ändern"
                               :en-GB "This is a read-only project.\nYou cannot change the project's details"}
   :warning-role-title-already-exists {:de-DE "Rolle existiert bereits"
                                       :en-GB "Role already exists "}
   :warning-section {:de-DE "Warnung"
                     :en-GB "Warning"}
   :warning-unauthorized-delete {:de-DE "Du darfst das Projekt nicht löschen"
                                 :en-GB "You are not authorized to delete this project"}
   :wednesdays {:de-DE "Mittwochs"
                :en-GB "Wednesdays"}
   :weekly {:de-DE "Wöchentlich"
            :en-GB "Weekly"}
   :welcome-card-data-atlas-desc {:de-DE "Schaffe dir einen Überblick über die verfügbaren Daten!"
                                  :en-GB "Get an overview of the available data."}
   :welcome-card-data-atlas-title {:de-DE "Datenatlas"
                                   :en-GB "Data Atlas"}
   :welcome-card-import-title {:de-DE "Import"
                               :en-GB "Import"}
   :welcome-card-import-desc {:de-DE "Nutze deine Daten für die Analy.se"
                              :en-GB "Use your data for analysis."}
   :welcome-card-empty-desc {:de-DE "Starte mit einer leeren Arbeitsfläche!"
                             :en-GB "Start with an empty workspace."}
   :welcome-card-empty-title {:de-DE "Neu"
                              :en-GB "New"}
   :welcome-card-search-desc {:de-DE "Starte deine Analyse mit der Definition einer Datenbasis!"
                              :en-GB "Start your analysis with the definition of a data set."}
   :welcome-card-search-title {:de-DE "Suche"
                               :en-GB "Search"}
   :welcome-close {:de-DE "Übersicht schließen"
                   :en-GB "Close overview"}
   :welcome-loading-message {:de-DE "Ladevorgang wird abgeschlossen"
                             :en-GB "Finishing loading"}
   :welcome-loading-tip {:de-DE "Einige Sekunden noch..."
                         :en-GB "Just a few seconds..."}
   :welcome-loading-tip-title {:de-DE "Fast geschafft"
                               :en-GB "Almost done"}
   :welcome-page-help {:de-DE "Hilfe"
                       :en-GB "Help"}
   :welcome-page-product-tour {:de-DE "Produkttour"
                               :en-GB "Product Tour"}
   :welcome-project-overview-text {:de-DE "Projektübersicht"
                                   :en-GB "Project overview"}
   :welcome-text-template {:de-DE "Willkommen bei %s!"
                           :en-GB "Welcome to %s!"}
   :welcome-tips-and-tricks-text-0 {:de-DE "Mit einem Doppelklick auf einen Datenpunkt in Mosaik, Karte oder Tabelle kannst du dir alle Informationen inklusive Volltext in der Detailansicht anzeigen lassen."
                                    :en-GB "Double click on a data point in mosaic, map or table to display all information including fulltext in the detail view."}
   :welcome-tips-and-tricks-text-1 {:de-DE "Öffne den Datenatlas, um einen Überblick über die vorhandenen Daten und Inhalte zu bekommen."
                                    :en-GB "Open the Data Atlas to get an overview about the available data and contents."}
   :welcome-tips-and-tricks-text-10 {:de-DE "Ziehe die Fenstergröße über die Seiten des Fensters so, wie es dir am besten passt."
                                     :en-GB "Drag at the window sides to set the optimal window size."}
   :welcome-tips-and-tricks-text-11 {:de-DE "Definiere deine eigenen Layouts und Karten-Overlayers über das Layout Management."
                                     :en-GB "Define your own layouts and map overlayers via the layout management."}
   :welcome-tips-and-tricks-text-12 {:de-DE "Mit einem Klick auf die Spaltenköpfe in der Tabelle kannst du diese auf- oder absteigend sortieren. Mit [Strg] + [Klick] auf einen weiteren Spaltenkopf, kannst du sekundär auch nach weiteren Attributen sortieren."
                                     :en-GB "With a click on the column header of table you can sort in ascending or descending order. Via [Ctrl] + [Click] on another column header you can add secondary sorting for more attributes."}
   :welcome-tips-and-tricks-text-13 {:de-DE "Möchtest du eine andere Ansicht auf die Objektkarten in Mosaik? Wechsle zum Streudiagramm über das Kontextmenü in der Kopfzeile und wähle dir passende Achsenattribute."
                                     :en-GB "Would you like another view on the object cards in mosaic? Switch to scatter plot via the context menu in the header and choose attributes for the axes."}
   :welcome-tips-and-tricks-text-14 {:de-DE "Möchtest du Änderungen an einem Fenster vornehmen, das bestehende Fenster aber beibehalten? Dann kopiere das Fenster ganz einfach über den Duplizieren-Button in der Kopfzeile."
                                     :en-GB "Would you like to make changes to a window but also keep it as it is? Simply copy the window via the duplicate button in the header."}
   :welcome-tips-and-tricks-text-15 {:de-DE "Für die Karte gibt es viele Overlayer, die dir mehr Informationen bieten können. Lege welche im Layout Management an und aktiviere sie über die Kartenlegende."
                                     :en-GB "There are many overlayers for the map which can provide you with more information. Define some in the layout management and activate them in the map legend."}
   :welcome-tips-and-tricks-text-16 {:de-DE "Möchtest du andere Informationen zu den Daten auf deinen Objektkarten sehen? Dann erstelle einfach dein eigenes Objektkartenlayout im Designer in den Einstellungen und wähle es im Mosaik-Fenster über den Einstellungen/Info-Button aus."
                                     :en-GB "Would you like to see other information on the object cards? Simply create your own object card layout in the designer in the settings and choose it in the mosaic window via the 'Settings & info' button."}
   :welcome-tips-and-tricks-text-17 {:de-DE "#zoom"
                                     :en-GB "#zoom"}
   :welcome-tips-and-tricks-text-2 {:de-DE "Mit [Strg] + [Linksklick] kannst du einen Datenpunkt in Mosaik, Karte oder Tabelle selektieren. Dieser wird dann in verbundenen Fenstern fokussiert."
                                    :en-GB "With [Ctrl] + [LeftClick] you can select a data point in mosaic, map or table. It will be focused in connected windows"}
   :welcome-tips-and-tricks-text-3 {:de-DE "Mit einem Rechtsklick auf eine Objektkarte und anschließend 'Kommentieren' kann ein Kommentar hinzugefügt werden."
                                    :en-GB "With a right click on an object card followed by 'Comment' you can add a comment."}
   :welcome-tips-and-tricks-text-4 {:de-DE "Ziehe zwei Explorationsfenster aufeinander, um zum Beispiel eine Schnittmenge oder eine Vereinigung zu erstellen."
                                    :en-GB "Drag two exploration windows onto each other to create, for example, an intersection or a union."}
   :welcome-tips-and-tricks-text-5 {:de-DE "Ziehe zwei Mosaik-Fenster aufeinander, um sie zu einer Zeitleiste miteinander zu koppeln."
                                    :en-GB "Drag two mosaic windows onto each other to couple them to a timeline."}
   :welcome-tips-and-tricks-text-6 {:de-DE "Du kannst Suchanfragen über den Speichern-Button unten rechts oder das Suchleisten-Symbol in der Kopfzeile speichern und abrufen."
                                    :en-GB "You can save and load search queries using the save button on the bottom right or the search query symbol in the header."}
   :welcome-tips-and-tricks-text-7 {:de-DE "Öffne diese Willkommensseite jederzeit wieder über das Explorama-Logo in der oberen linken Ecke."
                                    :en-GB "Open this welcome page any time by clicking the Explorama logo in the upper left corner."}
   :welcome-tips-and-tricks-text-8 {:de-DE "Mit einem Doppelklick auf die Kopfzeile eines Explorationsfensters kannst du den Titel bearbeiten."
                                    :en-GB "With a double click on the header of a exploration window you can edit the title."}
   :welcome-tips-and-tricks-text-9 {:de-DE "Über das Trichtersymbol in der Kopfzeile eines Explorationsfensters kannst du die Daten darin weiter filtern."
                                    :en-GB "Using the funnel symbol in the header of a exploration window you can further filter the data."}
   :welcome-tips-and-tricks-text-zoom-in-tip {:de-DE "Sie scheinen eine niedrige Zoomstufe zu verwenden. Mit [Strg] + [+] können Sie heranzoomen."
                                              :en-GB "You seem to be using a low zoom level. You can zoom in with [Ctrl] + [+]."}
   :welcome-tips-and-tricks-text-zoom-out-tip {:de-DE "Sie scheinen eine hohe Zoomstufe zu verwenden. Mit [Strg] + [-] können Sie herauszoomen."
                                               :en-GB "You seem to be using a high zoom level. You can zoom out with [Ctrl] + [-]."}
   :welcome-tips-and-tricks-text-zoom-tip {:de-DE " Mit [Strg] + [+] oder [Strg] + [-] können Sie die Zoomstufe Ihres Browsers ändern."
                                           :en-GB " With [Ctrl] + [+] or [Ctrl] + [-] you can change the zoom level of your browser."}
   :welcome-tips-and-tricks-title {:de-DE "Tipps und Tricks"
                                   :en-GB "Tips and Tricks"}
   :white-noise {:de-DE "Weißes Rauschen"
                 :en-GB "White Noise"}
   :white-noise-desc {:de-DE "Testet die Eingabedaten auf weißes Rauschen. Genutzt wird der Ljung-Box Test."
                      :en-GB "Tests whether the input data contains white noise ot not. The Ljung-Box test is used."}
   :white-noise-lag-error {:de-DE "Nicht genug Daten - Dieser Algorithmus erfordert mehr Beobachtungen als derzeit bereitgestellt."
                           :en-GB "Not enough data - this algorithm requires more observations than is currently provided."}
   :window-init-placement-label {:de-DE "Fensterpositionierung"
                                 :en-GB "Window Placement"}
   :window-init-placement-question {:de-DE "Wie sollen die Fenster aus der Toolbar heraus platziert werden?"
                                    :en-GB "How should windows from the toolbar be placed?"}
   :window-init-placement-manual-first {:de-DE "Manuelle"
                                        :en-GB "Manual"}
   :window-init-placement-auto-first {:de-DE "Automatische"
                                      :en-GB "Automatic"}
   :window-init-placement-manual-rest {:de-DE " Platzierung mit der Maus"
                                       :en-GB " placement via mouse"}
   :window-init-placement-auto-rest {:de-DE " Platzierung in der Ecke oben links"
                                     :en-GB " placement in the top left corner"}
   :window-search-placement-question {:de-DE "Wie sollen Fenster aus der Suche und kopierte Fenster platziert werden?"
                                      :en-GB "How should windows from search and copied windows be placed?"}
   :window-search-placement-manual-rest {:de-DE " Platzierung mit der Maus"
                                         :en-GB " placement via mouse"}
   :window-search-placement-manual-first {:de-DE "Manuelle"
                                          :en-GB "Manual"}
   :window-search-placement-auto-rest {:de-DE " Platzierung rechts neben der Quelle"
                                       :en-GB " placement next to the source"}
   :window-search-placement-auto-first {:de-DE "Automatische"
                                        :en-GB "Automatic"}
   :window-search-placement-left {:de-DE "Fenster öffnen sich von rechts nach links"
                                  :en-GB "Windows open from right to left"}
   :window-search-placement-right {:de-DE "Fenster öffnen sich von links nach rechts"
                                   :en-GB "Windows open from left to right"}
   :window-search-placement-left-tooltip {:de-DE "Aufeinanderfolgende Fenster werden von rechts nach links geöffnet. Das neuste Fenster ist immer links. Existierende Fenster werden nach rechts verschoben."
                                          :en-GB "Consecutive windows open from right to left. The newest window is always left. Existing windows will be moved to the right."}
   :window-search-placement-right-tooltip {:de-DE "Aufeinanderfolgende Fenster werden von links nach rechts geöffnet. Das neuste Fenster ist immer rechts. Existierende Fenster werden niemals verschoben."
                                           :en-GB "Consecutive windows open from left to right. The newest window is always right. Existing windows will not be moved."}
   :wmape {:de-DE "WMAPE"
           :en-GB "WMAPE"}
   :woco-protocol-action-close-frame {:de-DE "Fenster schließen"
                                      :en-GB "close window"}
   :woco-protocol-action-move-frame {:de-DE "Fenster verschieben"
                                     :en-GB "move window"}
   :woco-protocol-action-create-window {:de-DE "Öffne Fenster"
                                        :en-GB "Open window"}
   :woco-protocol-action-constraint-apply {:de-DE "Lokalen Filter anwenden"
                                           :en-GB "Apply local filter"}
   :woco-protocol-action-selection {:de-DE "Neue Selektion"
                                    :en-GB "New selection"}
   :woco-protocol-action-selection-current {:de-DE "Aktuell ausgewählt"
                                            :en-GB "Currently selected"}
   :woco-protocol-action-selection-current-event {:de-DE "Events"
                                                  :en-GB "events"}
   :woco-protocol-action-custom-title {:de-DE "Neuer Title"
                                       :en-GB "New Title"}
   :wordcloud-chart-label {:de-DE "Wortwolke"
                           :en-GB "Wordcloud"}
   :wordcloud-min-occurence-label {:de-DE "Min. Aufkommen"
                                   :en-GB "Min. occurrence"}
   :wordcloud-search-all-label {:de-DE "Attributsnamen & Ausprägungen"
                                :en-GB "Attribute names & characteristics"}
   :wordcloud-search-characteristics-label {:de-DE "Alle Ausprägungen"
                                            :en-GB "All characteristics"}
   :wordcloud-search-label {:de-DE "Durchsuchen"
                            :en-GB "Search"}
   :wordcloud-search-notes-only-label {:de-DE "Nur Notes"
                                       :en-GB "Only Notes"}
   :wordcloud-search-selected-attributes-label {:de-DE "Spezifische Attribute"
                                                :en-GB "Specific Attributes"}
   :wordcloud-select-attribute-label {:de-DE "Attribute"
                                      :en-GB "Attributes"}
   :wordcloud-stemming-attributes-label {:de-DE "Stemming:"
                                         :en-GB "Stemming:"}
   :wordcloud-stemming-info {:de-DE " Wörter werden anhand ihres Wortstammes zusammengefasst"
                             :en-GB "Words will be reduced to and combined based on their word stem"}
   :wordcloud-stopping-attributes-label {:de-DE "Stopping:"
                                         :en-GB "Stopping:"}
   :wordcloud-stopping-info {:de-DE "Stoppwörter, also häufig auftretende Wörter, die gewöhnlich keine inhaltliche Relevanz haben, werden nicht berücksichtigt"
                             :en-GB "“stop words” usually refers to the most common words in a language and which does not add much meaning to a sentence."}
   :workspace-connecting-edges {:de-DE "Fensterverbindungen anzeigen"
                                :en-GB "Show window connections"}
   :write-rights {:de-DE "Vollzugriff"
                  :en-GB "full access rights"}
   :x-axis-attribute-label {:de-DE "X-Achse"
                            :en-GB "X-Axis"}
   :xml-gen-abort-message {:de-DE "Generierung wird abgebrochen. Bitte warten..."
                           :en-GB "Generation is being canceled. Please wait..."}
   :xml-gen-failed-message {:de-DE "Es gab einen Fehler bei der XML Generierung, bitte überprüfe dein Datenmapping"
                            :en-GB "There was an error during XML generation, please check your data mapping"}
   :xml-generation-done-info {:de-DE "XML Generierung abgeschlossen"
                              :en-GB "XML generation done"}
   :xml-generation-done-warning {:de-DE "XML Generierung war erfolgreich, aber mit Warnungen."
                                 :en-GB "XML file was generated with warnings."}
   :xml-generation-progress-message {:de-DE "Erzeuge XML-Dateien. Bitte warten..."
                                     :en-GB "Generate XML Files. Please wait..."}
   :xml-generation-tip {:de-DE "Bitte, überprüfe das Log"
                        :en-GB "Please, check the log"}
   :xsd-message {:de-DE "Reihe <p> ist fehlerhaft. Fehlercode "
                 :en-GB "Row <p> has an error. Error code "}
   :y-axis-attribute-label {:de-DE "Y-Achse"
                            :en-GB "Y-Axis"}
   :y-axis-change-range-label {:de-DE "Y-Bereich ändern"
                               :en-GB "Change y-range"}
   :y-axis-change-range-default-label {:de-DE "Zurück zum Originalwert"
                                       :en-GB "Back to original"}
   :y-range {:de-DE "Y-Bereich"
             :en-GB "Y-Range"}
   :year {:de-DE "Jahr"
          :en-GB "Year"}
   :yes {:de-DE "Ja"
         :en-GB "Yes"}
   :zip-download-error {:de-DE "XML herunterladen gescheitert"
                        :en-GB "Download XML failed"}
   :zip-download-error-tip {:de-DE "Bitte kontaktiere einen Administrator, um sicherzustellen, dass die Anfrage erlaubt ist und genügend Rechenkapazitäten zur Verfügung stehen"
                            :en-GB "Please contact an admin to ensure that the request is allowed and that there is sufficient computing capacity available"}})
