# Description

App for getting video game news from different websites!

See release notes on master branch releases for features that cover last release. You could also download last .apk version from the playstore.

<a href='https://play.google.com/store/apps/details?id=com.eagskunst.emmanuel.gamingnews&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/></a>

See changelog and develop branch for not-official features.

**WARNING**: The app is in early stage. There's a lot to do and a lot of features to add. If you find/fix any bug please make an issue or a pull request.

# External libraries used:

-Picasso (http://square.github.io/picasso/)

-RSS Parser (https://github.com/prof18/RSS-Parser)

# Build

1. Open Android Studio.
2. Select File -> New -> Project from Version Control -> GitHub
3. Enter your github username and password.
4. Copy this repo's clone url.
5. Select the repository and hit clone.


# Changelog

**-9/10/2018:**
	Added Firebase libraries.
	Added Firebase Cloud Messaging instaces with NotificationMaker class
	NotificationMaker could recive from Firebase console and from server/http POST Request.
	Obviously, starting notifications implementation. 

**-3/9/2018:** Following good practices. Created BaseActivity.

**-1/9/2018:** 
	Created SettingsActivity and SettingsFragment to (obviously) handle Settings.
	Change app to night mode.
	Choose if you want to always load images.
	Fix FloatingAB position.

**-25/8/2018:** You can know save articles.

**-27/7/2018:** Added the posibility to search articles. Added custom navigation view icons. Erased one es/PS4 link.

**-19/7/2018:** Finished (my) proper navigation between fragments. Added also an int[] to check NavigationView actual item when onBackPressed().

**-15/7/2018:** Fixed Exception caused by refreshLayout.setRefreshing(false);
Implemented a better navigation between fragments but still is unfinished.


**-14/7/2018:** 
	Added a drawer layout with a navigation view.
	
  Changed MainActivity's all in one for a Fragment that can handle all categories of news.
	
  Changed ParserMaker's use of variable Activity in order to avoid a NullException. Now use an interface.
	
  Created Urls.json file for reading urls depending on language.
	
  Added GSON library for Json reading.
	
  Created Categories inside Models package.
	
  Created LoadUrls for handling the reading of the Json file.
	
  Created WebViewActivity.
	
  Deleted icon in toolbar.xml layout. Now is added with toolbar.setIcon()
	
  Changed harcoded strings and dimens.

**-11/7/2018:** Changed colors. Added app icon inside toolbar. Added a Floating Action Button for back to top compability.
**-2/7/2018:** Bugfixes for Locale and proper onBackPressed when paserMaker.isRunning = true. Change min. version for Android 4.1 Improvement of ParserMaker

**-27/6/2018:** First commit.
