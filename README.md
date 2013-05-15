Android SideNavigation Library
==============================

Implementation of "Side Navigation" or "Fly-in app menu" pattern for Android (based on Google+ app).

It is shown as overlay on the actual application layout, in contrast to moving whole view aside when openning the menu.

Description
-----------

The Google+ app slides the navigation on top of the UI while the others move the UI to the side. 
Google+ also has the up caret icon and the action bar present when the menu is opened while other apps don't.

There was a interesting discussion about this pattern in the blog's Google+ page some time ago. 
You can find the post & discussion here: [Google+](https://plus.google.com/115177579026138386092/posts/AvXiTF7LqDK).


Compatibility
-------------

This library is compatible from API 7 (Android 1.6).

Installation
------------

The sample project requires:

* The library project

General information
-------------------

This version of the SideNavigation library is based on [johnkil's version](http://johnkil.github.com/SideNavigation).
It is mainly modified to allow injecting custom layout inside the drawer instead of using the menu.xml format to create ListView with items declared there.

Also main point of changes was adding possibility to drag the menu out instead of show/hide it with animation only.

Currently the animation is performed using [TranslateAnimation](http://developer.android.com/reference/android/view/animation/TranslateAnimation.html) applied directly to Canvas of customized LinearLayout instead of the startAnimation() method.
This was necessary, since we wanted to achieve behaviour, that you can partially drag the menu and the the animation will finish up openning it.


Usage
-----

To display the item you need the following code:

* Add SideNavigationView to the end of the layout. Example:

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent" >

    <ImageView
        android:id="@android:id/icon"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:src="@drawable/ic_android_logo" />

    <com.devspark.sidenavigation.SideNavigationView
        android:id="@+id/side_navigation_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</RelativeLayout>
```

* Create '.xml' layout of the content of menu.

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center_horizontal"
     >
    <TextView android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/white"
        android:text="Test"/>
    <TextView android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/white"
        android:text="Test"/>
    <TextView android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/white"
        android:text="Test"/>
    <TextView android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/white"
        android:text="Test"/>
    <TextView android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/white"
        android:text="Test"/>
    <TextView android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textColor="@android:color/white"
        android:text="Testaaaaaaaaa"/>
</LinearLayout>
```

* Set the custom layout as the view of the menu:

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
    // other code
    
    sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
    sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setContentView(R.layout.drawer_menu);
}
```

You have to handle all the events from items stored in the custom layout by yourself.

Contribution
------------

If you want to extend functionality of this library, please fork. In case you find any issues, please file a bug report.

Developed By
------------
* Evgeny Shishkin - <johnkil78@gmail.com>
* Damian Walczak - <damian.walczak@gmail.com>

License
-------

    Copyright (C) 2012 Evgeny Shishkin
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

