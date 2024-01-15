(ns org.jakehoward.explore.try-hanami
  (:require [clojure.data.csv :as csv]
            [nextjournal.clerk :as clerk]
            [clojure.java.io :as io]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]))

(clerk/serve! {:watch-paths ["."] :browse? true})

(def mean-temp-data (-> "uk-mean-temp.csv"
                        io/resource
                        slurp
                        csv/read-csv))

(-> mean-temp-data
    clerk/use-headers
    clerk/table)

(clerk/vl {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
           :data {:url "https://vega.github.io/vega-lite/data/co2-concentration.csv"
                  :format {:parse {:Date "utc:'%Y-%m-%d'"}}}
           :width 800
           :height 500
           :transform [{:calculate "year(datum.Date)" :as "year"}
                       {:calculate "floor(datum.year / 10)" :as "decade"}
                       {:calculate "(datum.year % 10) + (month(datum.Date)/12)"
                        :as "scaled_date"}
                       {:calculate "datum.first_date === datum.scaled_date ? 'first' : datum.last_date === datum.scaled_date ? 'last' : null"
                        :as "end"}]
           :encoding {:x {:type "quantitative" :title "Year into Decade" :axis {:tickCount 11}}
                      :y {:title "CO2 concentration in ppm" :type "quantitative" :scale {:zero false}}
                      :color {:field "decade" :type "ordinal" :scale {:scheme "magma"} :legend nil}}
           :layer [{:mark "line" :encoding {:x {:field "scaled_date"} :y {:field "CO2"}}}
                   {:mark {:type "text" :baseline "top" :aria false}
                    :encoding {:x {:aggregate "min" :field "scaled_date"}
                               :y {:aggregate {:argmin "scaled_date"} :field "CO2"}
                               :text {:aggregate {:argmin "scaled_date"} :field "year"}}}
                   {:mark {:type "text" :aria false}
                    :encoding {:x {:aggregate "max" :field "scaled_date"}
                               :y {:aggregate {:argmax "scaled_date"} :field "CO2"}
                               :text {:aggregate {:argmax "scaled_date"} :field "year"}}}]
           :config {:text {:align "left" :dx 3 :dy 1}}})

(hc/get-default :BACKGROUND)

(-> (hc/xform ht/line-chart
              :FDATA (io/resource "uk-mean-temp.csv")
              :X "Year"
              :Y "Annual Mean Temperature")
    clerk/vl)
