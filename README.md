[![CircleCI](https://circleci.com/gh/pranayairan/Coiny.svg?style=svg)](https://circleci.com/gh/pranayairan/Coiny)

# Coiny
Coiny is a beautiful cryptocurrency app, completely open source and 100% in kotlin. It supports following features

* Track prices of over 4000+ currencies over 100+ exchanges
* Get top coins, top pairs, top exchanges by volume. 
* Track latest news for all the coins and crypto community in general
* Completely secure, your data never leaves your device. 
* Choose your home currency and track prices in it. 
* Made with ❤️ and help from [opensource community](https://github.com/pranayairan/Coiny/blob/master/attribution.md)
* Open for contribution, please send a pull request. 

App available on Google Play: https://play.google.com/store/apps/details?id=com.binarybricks.coiny 

## Work in progress

* Ability to add transactions
* Ability to change exchanges
* Autorefresh of prices
* Candle charts

# App Architecture

Currently the app is using MVP with a repository. We have 2 data source, Room and in memory cache. Data flow is like this 

Fragments/Activities -> Presenter -> Repo -> Network/Cache (room/in memory)

In coming days I would like to remove inmemory cache and make everything come from Room. Network will keep the Room cache updated. This will give app some offline abilities. 

I am also using a ton of recycler view with [Adapter Delegate Pattern](http://hannesdorfmann.com/android/adapter-delegates). This enables me to plug and play the screens like Lego blocks. I am thinking to replace this with Epoxy in coming days. 


# Screenshots
<a href="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/0.jpg"><img src="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/0.jpg" height="480" width="240" ></a>
  <a href="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/1.jpg"><img src="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/1.jpg" height="480" width="240" ></a>
<a href="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/2.jpg"><img src="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/2.jpg" height="480" width="240" ></a>

<a href="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/3.jpg"><img src="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/3.jpg" height="480" width="240" ></a>
<a href="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/4.jpg"><img src="https://raw.githubusercontent.com/pranayairan/Coiny/master/screenshots/variant_2/4.jpg" height="480" width="240" ></a>

# Video
[![Coiny](https://img.youtube.com/vi/sOChpJlnE3k/0.jpg)](https://www.youtube.com/watch?v=sOChpJlnE3k)
