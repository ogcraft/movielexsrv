
(def movies [
	{
		:shortname	"monsters", 
		:descriptions [
			{	
				:lang 	"en" 
				:title 	"Monsters University", 
				:desc 	"A look at the relationship between Mike and Sulley during their days at Monsters University -- when they weren't necessarily the best of friends.",
	 			:img "monsters.jpg",
	 			:year-released "2013"
			}
			{	
				:lang 	"ru" 
				:title 	"Университет монстров (2013)", 	
				:desc 	"Майк и Салли — самые опытные пугатели в Монстрополисе, но так было далеко не всегда. Когда они встретились впервые, эти монстры терпеть друг друга не могли. «Университет Монстров» — история о том, как наши старые знакомые прошли путь от взаимной неприязни к крепкой дружбе.",
	 			:img "monsters.jpg"
	 			:year-released "2013"
			}
		]
	  	:translations [
	  		{
	 			:lang "ru", 
	 			:title "Русский", 
	 			:desc "From original dvd", 
	 			:img nil 
	 		}
	  		{
	  			:lang "he", 
	  			:title "עברית", 
	  			:desc "From original dvd", 
	  			:img nil 
	  		}
	  	]
    }
    {
		:shortname "smurfs", 
		:descriptions [
			{	
				:lang 	"en"  
				:title 	"The Smurfs", 
				:desc 	"When the evil wizard Gargamel chases the tiny blue Smurfs out of their village, they tumble from their magical world into New York City.",
	 			:img 	"smurfs.jpg",
	 			:year-released "2011"
			}
		]
	  	:translations [
	  		{
	 			:lang "ar", 
	 			:title "Arabic", 
	 			:desc "From original dvd", 
	 			:img nil 
	 		}
	  		{
	  			:lang "he", 
	  			:title "עברית", 
	  			:desc "From original dvd", 
	  			:img nil 
	  		}
	  	]
   	}
   	{
		:shortname "despicableme2", 
		:descriptions [
			{	
				:lang 	"en"  
				:title 	"Despicable Me 2", 
				:desc 	"Gru is recruited by the Anti-Villain League to help deal with a powerful new super criminal.",
	 			:img 	"despicableme2.jpg",
	 			:year-released "2013"
			}
		]
	  	:translations [
	  		{
	 			:lang "ru", 
	 			:title "Русский", 
	 			:desc "From original dvd", 
	 			:img nil 
	 		}
	  		{
	  			:lang "he", 
	  			:title "עברית", 
	  			:desc "From original dvd", 
	  			:img nil 
	  		}
	  	]
   	}
   ])

;(clutch/with-db db (clutch/bulk-update movies))
;(clutch/all-documents db {:include_docs true})
