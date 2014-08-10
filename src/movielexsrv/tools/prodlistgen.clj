(ns movielexsrv.tools.prodlistgen
	(:require 
    	clojure.contrib.io 
    	clojure.java.io
    	[clojure.pprint :refer [pprint]]
    	[movielexsrv.models.db :as db]
  		[clj-time.core :as time]
		[clj-time.format :as time-format]))

; The CSV file uses commas (,) and semi-colons (;) to separate data values. Commas are used to separate primary data values, and semi-colons are used to separate subvalues. For example, the syntax for the CSV file is as follows:
; "product_id","publish_state","purchase_type","autotranslate ","locale; title; description","autofill","country; price"

; Descriptions and usage details are provided below.

; -- product_id
; This is equivalent to the In-app Product ID setting in the In-app Products UI. If you specify a product_id that already exists in a product list, and you choose to overwrite the product list while importing the CSV file, the data for the existing item is overwritten with the values specified in the CSV file. The overwrite feature does not delete items that are on a product list but not present in the CSV file.

; -- publish_state
; This is equivalent to the Publishing State setting in the In-app Products UI. Can be published or unpublished.

; -- purchase_type
; This is equivalent to the Product Type setting in the In-app Products UI. Can be managed_by_android, which is equivalent to Managed per user account in the In-app Products UI, or managed_by_publisher, which is equivalent to Unmanaged in the In-app Products UI.

; -- autotranslate
; This is equivalent to selecting the Fill fields with auto translation checkbox in the In-app Products UI. Can be true or false.

; -- locale
; This is equivalent to the Language setting in the In-app Products UI. You must have an entry for the default locale. The default locale must be the first entry in the list of locales, and it must include a title and description. If you want to provide translated versions of the title and description in addition to the default, you must use the following syntax rules:
; If autotranslate is true, you must specify the default locale, default title, default description, and other locales using the following format:
; "true,"default_locale; default_locale_title; default_locale_description; locale_2; locale_3, ..."
; If autotranslate is false, you must specify the default locale, default title, and default description as well as the translated titles and descriptions using the following format:
; "false,"default_locale; default_locale_title; default_locale_description; locale_2; locale_2_title; local_2_description; locale_3; locale_3_title; locale_3_description; ..."
; See table 1 for a list of the language codes you can use with the locale field.

; -- title
; This is equivalent to the Title setting in the In-app Products UI. If the title contains a semicolon, it must be escaped with a backslash (for example, "\;"). A backslash should also be escaped with a backslash (for example, "\\">.

; -- description
; This is equivalent to the Description in the In-app Products UI. If the description contains a semicolon, it must be escaped with a backslash (for example, "\;"). A backslash should also be escaped with a backslash (for example, "\\">.

; -- autofill
; This is equivalent to clicking Auto Fill in the In-app Products UI. Can be true or false. The syntax for specifying the country and price varies depending on which autofill setting you use.
; If autofill is set to true, you need to specify only the default price in your home currency and you must use this syntax:
; "true","default_price_in_home_currency"
; If autofill is set to false, you need to specify a country and a price for each currency and you must use the following syntax:
; "false", "home_country; default_price_in_home_currency; country_2; country_2_price; country_3; country_3_price; ..."
; -- country
; The country for which you are specifying a price. You can only list countries that your application is targeting. The country codes are two-letter uppercase ISO country codes (such as "US") as defined by ISO 3166-2.
; -- price
; This is equivalent to the Price in the In-app Products UI. The price must be specified in micro-units. To convert a currency value to micro-units, you multiply the real value by 1,000,000. For example, if you want to sell an in-app item for $1.99 you specify 1990000 in the price field.

(defn init-prodlistgen []
	(db/load-movies-data))

(def movie-price-microunits (* 10 1000000))

;test,published,managed_by_publisher,false,en_US;name;desc,true,1990000

(defn movie->csv [m]
	(let [
		desc_lang		(db/select-desc-by-lang "ru" (:descriptions m))
		product_id 		(:id m)
		publish_state 	"unpublished"
		purchase_type 	"managed_by_android"
		autotranslate 	"true"
		locale 			"ru_RU"
		title 			(str "\"" (:title desc_lang) "\"" )
		description     (str "\"" (:desc-short desc_lang)  "\"" )
		autofill 		"true"
		price 			(str movie-price-microunits)
 		]
		(str 
			product_id "," publish_state "," purchase_type "," autotranslate "," 
			locale ";" title ";" description "," 
			autofill "," price)))

(defn gen-product-records []
	(db/load-movies-data)
	(let [
			movies (filter #(.startsWith (:id %) "kp") (vals @db/movies))]
		(map movie->csv movies)))

(defn gen-product-list-csv [path]
	(let [
			fname (str path "/movies-product-list.csv")
			records (gen-product-records)]
		(println "Writing " (count records) " records to file: " fname)
		(with-open [w (clojure.java.io/writer fname :append false)]
  			(binding [*out* w]
    			(doseq [l records]
      			(println l))))))